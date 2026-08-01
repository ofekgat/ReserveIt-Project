package getticket.dao.impl;

import getticket.dao.TicketDao;
import getticket.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDaoImpl extends BaseDao implements TicketDao {

    private static final String COLS = "Ticket_id, Booking_id, Instance_id, Seat_id";

    @Override
    public int create(Ticket ticket) throws SQLException {
        return withConnection(conn -> create(ticket, conn));
    }

    @Override
    public int create(Ticket ticket, Connection conn) throws SQLException {
        String sql = "INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ticket.getBookingId());
            ps.setInt(2, ticket.getInstanceId());

            // Seat_id is null for general admission — setInt() would
            // throw, so the null case needs setNull() explicitly.
            if (ticket.getSeatId() != null) {
                ps.setInt(3, ticket.getSeatId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            // Throws SQLIntegrityConstraintViolationException if this
            // seat is already ticketed for this instance. Left to
            // propagate: the service layer decides what the user sees.
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ticket.setTicketId(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Ticket getById(int ticketId) throws SQLException {
        return withConnection(conn -> getById(ticketId, conn));
    }

    @Override
    public Ticket getById(int ticketId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Tickets WHERE Ticket_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Ticket> getTicketsByBooking(int bookingId) throws SQLException {
        return withConnection(conn -> getTicketsByBooking(bookingId, conn));
    }

    @Override
    public List<Ticket> getTicketsByBooking(int bookingId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Tickets WHERE Booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Ticket> getTicketsByInstance(int instanceId) throws SQLException {
        return withConnection(conn -> getTicketsByInstance(instanceId, conn));
    }

    @Override
    public List<Ticket> getTicketsByInstance(int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Tickets WHERE Instance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public int countByInstance(int instanceId) throws SQLException {
        return withConnection(conn -> countByInstance(instanceId, conn));
    }

    @Override
    public int countByInstance(int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tickets WHERE Instance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public boolean delete(int ticketId) throws SQLException {
        return withConnection(conn -> delete(ticketId, conn));
    }

    @Override
    public boolean delete(int ticketId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Tickets WHERE Ticket_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Ticket> mapRows(ResultSet rs) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("Ticket_id"));
        ticket.setBookingId(rs.getInt("Booking_id"));
        ticket.setInstanceId(rs.getInt("Instance_id"));

        // getInt() returns 0 for SQL NULL — wasNull() distinguishes
        // "really zero" from "was NULL" on a nullable numeric column.
        int seatId = rs.getInt("Seat_id");
        ticket.setSeatId(rs.wasNull() ? null : seatId);
        return ticket;
    }
}
