package getticket.dao.impl;

import getticket.dao.TicketDao;
import getticket.model.Ticket;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TicketDaoImpl implements TicketDao {

    private static final String INSERT_SQL =
            "INSERT INTO Tickets (Booking_id, Instance_id, Seat_id) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Tickets WHERE Ticket_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Tickets";
    private static final String SELECT_BY_BOOKING_SQL = "SELECT * FROM Tickets WHERE Booking_id = ?";
    private static final String SELECT_BY_INSTANCE_SQL = "SELECT * FROM Tickets WHERE Instance_id = ?";
    private static final String UPDATE_SQL =
            "UPDATE Tickets SET Booking_id = ?, Instance_id = ?, Seat_id = ? WHERE Ticket_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Tickets WHERE Ticket_id = ?";

    @Override
    public int create(Ticket ticket) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            return create(ticket, conn);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public int create(Ticket ticket, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ticket.getBookingId());
            ps.setInt(2, ticket.getInstanceId());
            setNullableSeatId(ps, 3, ticket.getSeatId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ticket.setTicketId(id);
                    return id;
                }
            }
            throw new SQLException("Creating ticket failed, no generated key obtained.");
        }
    }

    @Override
    public Ticket getById(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapRow(rs) : null;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Ticket> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Ticket> tickets = new ArrayList<>();
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
                return tickets;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Ticket> getTicketsByBooking(int bookingId) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_BOOKING_SQL)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Ticket> tickets = new ArrayList<>();
                    while (rs.next()) {
                        tickets.add(mapRow(rs));
                    }
                    return tickets;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Ticket> getTicketsByInstance(int instanceId) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_INSTANCE_SQL)) {
                ps.setInt(1, instanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Ticket> tickets = new ArrayList<>();
                    while (rs.next()) {
                        tickets.add(mapRow(rs));
                    }
                    return tickets;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Ticket ticket) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, ticket.getBookingId());
                ps.setInt(2, ticket.getInstanceId());
                setNullableSeatId(ps, 3, ticket.getSeatId());
                ps.setInt(4, ticket.getTicketId());
                return ps.executeUpdate() > 0;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    private void setNullableSeatId(PreparedStatement ps, int index, Integer seatId) throws SQLException {
        if (seatId == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, seatId);
        }
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        int seatId = rs.getInt("Seat_id");
        Integer seatIdOrNull = rs.wasNull() ? null : seatId;
        return new Ticket(
                rs.getInt("Ticket_id"),
                rs.getInt("Booking_id"),
                rs.getInt("Instance_id"),
                seatIdOrNull
        );
    }
}
