package getticket.dao;

import getticket.model.Ticket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TicketDao {

    /**
     * Inserts one ticket.
     *
     * For a numbered seat already sold for this instance, MySQL rejects
     * the write with SQLIntegrityConstraintViolationException, because
     * of the uq_tickets_instance_seat UNIQUE constraint in the schema.
     * That exception is deliberately not caught here: the service layer
     * decides how to present it to the user.
     */
    int create(Ticket ticket) throws SQLException;
    int create(Ticket ticket, Connection conn) throws SQLException;

    Ticket getById(int ticketId) throws SQLException;
    Ticket getById(int ticketId, Connection conn) throws SQLException;

    List<Ticket> getTicketsByBooking(int bookingId) throws SQLException;
    List<Ticket> getTicketsByBooking(int bookingId, Connection conn) throws SQLException;

    List<Ticket> getTicketsByInstance(int instanceId) throws SQLException;
    List<Ticket> getTicketsByInstance(int instanceId, Connection conn) throws SQLException;

    int countByInstance(int instanceId) throws SQLException;
    int countByInstance(int instanceId, Connection conn) throws SQLException;

    boolean delete(int ticketId) throws SQLException;
    boolean delete(int ticketId, Connection conn) throws SQLException;
}
