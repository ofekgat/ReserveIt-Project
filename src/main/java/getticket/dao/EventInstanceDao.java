package getticket.dao;

import getticket.model.EventInstance;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface EventInstanceDao {

    int create(EventInstance instance) throws SQLException;
    int create(EventInstance instance, Connection conn) throws SQLException;

    EventInstance getById(int instanceId) throws SQLException;
    EventInstance getById(int instanceId, Connection conn) throws SQLException;

    /**
     * Reads the instance row with SELECT ... FOR UPDATE, locking it for
     * the duration of the caller's transaction. Any other transaction
     * that tries to lock the same row waits until this one commits or
     * rolls back — that serialization is what stops two concurrent
     * checkouts from both seeing the same seats as free.
     *
     * MUST be called inside a transaction (conn.setAutoCommit(false)),
     * otherwise the lock is released immediately and has no effect.
     */
    EventInstance getByIdForUpdate(int instanceId, Connection conn) throws SQLException;

    List<EventInstance> getAll() throws SQLException;
    List<EventInstance> getAll(Connection conn) throws SQLException;

    List<EventInstance> getInstancesByShow(int sid) throws SQLException;
    List<EventInstance> getInstancesByShow(int sid, Connection conn) throws SQLException;

    boolean update(EventInstance instance) throws SQLException;
    boolean update(EventInstance instance, Connection conn) throws SQLException;

    boolean delete(int instanceId) throws SQLException;
    boolean delete(int instanceId, Connection conn) throws SQLException;

    /**
     * Adjusts Available_tickets by `delta` (negative to sell, positive to
     * refund). Returns false — changing nothing — if a negative delta
     * would push the count below zero.
     *
     * The check and the write are one SQL statement, so two concurrent
     * callers can never both pass the check and oversell.
     */
    boolean updateAvailableTickets(int instanceId, int delta, Connection conn) throws SQLException;
}
