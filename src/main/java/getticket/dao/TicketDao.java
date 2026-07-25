package getticket.dao;

import getticket.model.Ticket;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TicketDao extends GenericDao<Ticket> {

    /** All tickets issued under a single booking. */
    List<Ticket> getTicketsByBooking(int bookingId) throws SQLException;

    /** All tickets already issued for an event instance; used to compute booked/available seats. */
    List<Ticket> getTicketsByInstance(int instanceId) throws SQLException;

    /** Same as {@link #create(Ticket)} but runs on the caller's transaction. */
    int create(Ticket ticket, Connection conn) throws SQLException;
}
