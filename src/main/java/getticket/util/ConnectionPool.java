package getticket.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe pool of JDBC connections to the MySQL database.
 * Connection details are never hard-coded: they are read from environment
 * variables first, falling back to db.properties on the classpath.
 */
public final class ConnectionPool {

    private static final String CONFIG_FILE = "db.properties";
    private static final long BORROW_TIMEOUT_SECONDS = 10;

    private static volatile ConnectionPool instance;

    private final String url;
    private final String user;
    private final String password;
    private final BlockingQueue<Connection> pool;
    private final int maxSize;

    private ConnectionPool() {
        Properties props = loadProperties();

        String driver = getConfig("DB_DRIVER", "db.driver", props);
        this.url = getConfig("DB_URL", "db.url", props);
        this.user = getConfig("DB_USER", "db.user", props);
        this.password = getConfig("DB_PASSWORD", "db.password", props);
        int initialSize = Integer.parseInt(getConfig("DB_POOL_INITIAL_SIZE", "db.pool.initialSize", props));
        this.maxSize = Integer.parseInt(getConfig("DB_POOL_MAX_SIZE", "db.pool.maxSize", props));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver not found on classpath: " + driver, e);
        }

        this.pool = new ArrayBlockingQueue<>(maxSize);
        try {
            for (int i = 0; i < initialSize; i++) {
                pool.offer(createConnection());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize the connection pool", e);
        }
    }

    public static ConnectionPool getInstance() {
        ConnectionPool result = instance;
        if (result == null) {
            synchronized (ConnectionPool.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ConnectionPool();
                }
            }
        }
        return result;
    }

    /**
     * Borrows a connection from the pool, growing the pool up to maxSize on demand.
     * Blocks up to BORROW_TIMEOUT_SECONDS while waiting for one to free up.
     */
    public Connection getConnection() throws SQLException {
        synchronized (pool) {
            if (pool.isEmpty() && currentPoolCapacityUsed() < maxSize) {
                pool.offer(createConnection());
            }
        }
        try {
            Connection connection = pool.poll(BORROW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (connection == null) {
                throw new SQLException("Timed out waiting for a free connection from the pool");
            }
            if (connection.isClosed()) {
                connection = createConnection();
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a connection", e);
        }
    }

    /** Returns a connection to the pool so it can be reused by another caller. */
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        pool.offer(connection);
    }

    /** Closes every pooled connection; call on application shutdown. */
    public void shutdown() {
        Connection connection;
        while ((connection = pool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // best-effort cleanup
            }
        }
    }

    private int currentPoolCapacityUsed() {
        return pool.size();
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = ConnectionPool.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        return props;
    }

    private static String getConfig(String envKey, String propKey, Properties props) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String propValue = props.getProperty(propKey);
        if (propValue == null) {
            throw new IllegalStateException("Missing DB configuration: set env var " + envKey
                    + " or property " + propKey + " in " + CONFIG_FILE);
        }
        return propValue;
    }
}
