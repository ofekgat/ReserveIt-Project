package getticket.dao;

import getticket.model.EventInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface EventInstanceDao extends GenericDao<EventInstance> {

    /** All scheduled instances (dates/venues) of a given show. */
    List<EventInstance> getInstancesByShow(int sid) throws SQLException;

    /**
     * Adjusts available_tickets by delta (negative when booking, positive on cancellation).
     * Must run inside the same transaction as the Booking/Ticket inserts to prevent double booking.
     */
    boolean updateAvailableTickets(int instanceId, int delta) throws SQLException;

    /**
     * Reads and row-locks (SELECT ... FOR UPDATE) the instance within an existing transaction on conn.
     * Holding this lock for the transaction's duration serializes concurrent checkouts for the
     * same instance, which is what prevents double booking.
     */
    EventInstance getByIdForUpdate(int instanceId, Connection conn) throws SQLException;

    /** Same as {@link #updateAvailableTickets(int, int)} but runs on the caller's transaction. */
    boolean updateAvailableTickets(int instanceId, int delta, Connection conn) throws SQLException;
}
