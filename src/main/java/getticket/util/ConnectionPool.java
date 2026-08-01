package getticket.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A growable JDBC connection pool.
 *
 * Opens `db.pool.initialSize` real connections at startup. If every
 * connection is busy and the pool is below `db.pool.maxSize`, a new
 * real connection is created on demand. Once maxSize is reached,
 * getConnection() waits briefly for one to be returned, then times out.
 *
 * Callers borrow a connection with getConnection() and give it back
 * by calling connection.close() — this does NOT close the real
 * connection, it returns it to the pool (via a dynamic proxy that
 * intercepts close()).
 *
 * Thread-safe: BlockingQueue and AtomicInteger handle concurrent use.
 */
public class ConnectionPool {

    private static final String PROPERTIES_FILE = "db.properties";
    private static ConnectionPool instance;

    private final BlockingQueue<Connection> availableConnections;
    private final CopyOnWriteArrayList<Connection> allRealConnections;
    private final AtomicInteger totalConnections;
    private final int maxPoolSize;
    private final long borrowTimeoutSeconds = 5;

    private final String url;
    private final String user;
    private final String password;

    private ConnectionPool(int initialSize, int maxSize, String url, String user, String password) throws SQLException {
        this.availableConnections = new LinkedBlockingQueue<>(maxSize);
        this.allRealConnections = new CopyOnWriteArrayList<>();
        this.totalConnections = new AtomicInteger(0);
        this.maxPoolSize = maxSize;
        this.url = url;
        this.user = user;
        this.password = password;

        for (int i = 0; i < initialSize; i++) {
            availableConnections.add(wrap(createRealConnection()));
        }
    }

    /** Returns the single shared pool, creating it from db.properties on first call. */
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            try {
                instance = createFromPropertiesFile();
            } catch (IOException | SQLException e) {
                throw new RuntimeException("Failed to initialize ConnectionPool", e);
            }
        }
        return instance;
    }

    private static ConnectionPool createFromPropertiesFile() throws IOException, SQLException {
        Properties props = new Properties();
        try (InputStream in = ConnectionPool.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IOException(
                    PROPERTIES_FILE + " not found on classpath. " +
                    "Copy the shared template and fill in your local MySQL credentials.");
            }
            props.load(in);
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
        String driverClassName = props.getProperty("db.driver");
        int initialSize = Integer.parseInt(props.getProperty("db.pool.initialSize", "5"));
        int maxSize = Integer.parseInt(props.getProperty("db.pool.maxSize", "15"));

        // Modern JDBC drivers usually auto-register themselves once the
        // jar is on the classpath. Loading the class explicitly is a
        // harmless safety net for environments where auto-registration
        // doesn't kick in.
        if (driverClassName != null && !driverClassName.trim().isEmpty()) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC driver class not found on classpath: " + driverClassName, e);
            }
        }

        return new ConnectionPool(initialSize, maxSize, url, user, password);
    }

    /**
     * Borrows a connection from the pool.
     * - If one is free, returns it immediately.
     * - Else, if the pool hasn't reached maxSize yet, opens a new one.
     * - Else, waits briefly for one to be returned, then gives up.
     *
     * The caller MUST call connection.close() when done — normally
     * via try-with-resources. That returns the connection to the
     * pool; it does not close it.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = availableConnections.poll();
        if (conn != null) {
            return conn;
        }

        if (totalConnections.get() < maxPoolSize) {
            synchronized (this) {
                if (totalConnections.get() < maxPoolSize) {
                    return wrap(createRealConnection());
                }
            }
        }

        try {
            conn = availableConnections.poll(borrowTimeoutSeconds, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timed out waiting for a free database connection. " +
                        "All " + maxPoolSize + " pool connections (the configured maximum) are in use.");
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", e);
        }
    }

    private Connection createRealConnection() throws SQLException {
        Connection real = DriverManager.getConnection(url, user, password);
        allRealConnections.add(real);
        totalConnections.incrementAndGet();
        return real;
    }

    /** Wraps a real connection so that close() returns it to the pool instead of closing it. */
    private Connection wrap(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(real));
    }

    private class PooledConnectionHandler implements InvocationHandler {
        private final Connection real;

        PooledConnectionHandler(Connection real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("close".equals(method.getName())) {
                availableConnections.offer((Connection) proxy);
                return null;
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    /**
     * Returns a borrowed connection to the pool.
     *
     * Equivalent to calling conn.close() on a pooled connection — both
     * routes end up back in the queue. This named method exists because
     * "release" states the intent plainly, where close() on a pooled
     * connection reads as if it destroys it.
     *
     * Safe to call with null, and safe to call twice.
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error releasing connection to pool: " + e.getMessage());
        }
    }

    /** Closes every real connection. Call once, when the application shuts down. */
    public synchronized void shutdown() {
        for (Connection real : allRealConnections) {
            try {
                real.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection during shutdown: " + e.getMessage());
            }
        }
        availableConnections.clear();
    }
}
