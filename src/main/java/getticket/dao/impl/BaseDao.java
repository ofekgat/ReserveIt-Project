package getticket.dao.impl;

import getticket.util.ConnectionPool;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Shared plumbing for the DAO implementations.
 *
 * Every DAO method exists in two forms: one that receives a Connection
 * (so it can take part in a caller's transaction) and one that doesn't.
 * The second form is written once here — borrow a connection, run the
 * first form, release it no matter what — instead of being repeated in
 * every method of every DAO.
 *
 * Implements Serializable (with no state to actually serialize) because
 * JSF backing beans hold DAO instances as fields, and those beans live in
 * the HTTP session — which Tomcat may try to persist to disk.
 */
abstract class BaseDao implements Serializable {

    private static final long serialVersionUID = 1L;

    /** A unit of DAO work that runs on a given connection and returns a value. */
    @FunctionalInterface
    protected interface DaoCall<T> {
        T run(Connection conn) throws SQLException;
    }

    /**
     * Borrows a connection from the pool, runs the given call on it, and
     * always releases it — including when the call throws.
     */
    protected <T> T withConnection(DaoCall<T> call) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            return call.run(conn);
        } finally {
            if (conn != null) {
                ConnectionPool.getInstance().releaseConnection(conn);
            }
        }
    }
}
