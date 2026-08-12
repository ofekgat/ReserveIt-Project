package getticket.dao.impl;

import getticket.dao.BookingDao;
import getticket.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDaoImpl extends BaseDao implements BookingDao {

    private static final String COLS = "Booking_id, Uid, Booking_time, Total_price, Status";

    @Override
    public int create(Booking booking) throws SQLException {
        return withConnection(conn -> create(booking, conn));
    }

    @Override
    public int create(Booking booking, Connection conn) throws SQLException {
        String sql = "INSERT INTO Bookings (Uid, Booking_time, Total_price, Status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getUid());
            ps.setTimestamp(2, booking.getBookingTime() != null
                    ? Timestamp.valueOf(booking.getBookingTime())
                    : new Timestamp(System.currentTimeMillis()));
            ps.setDouble(3, booking.getTotalPrice());
            ps.setString(4, booking.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    booking.setBookingId(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Booking getById(int bookingId) throws SQLException {
        return withConnection(conn -> getById(bookingId, conn));
    }

    @Override
    public Booking getById(int bookingId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Bookings WHERE Booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Booking> getBookingsByUser(int uid) throws SQLException {
        return withConnection(conn -> getBookingsByUser(uid, conn));
    }

    @Override
    public List<Booking> getBookingsByUser(int uid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Bookings WHERE Uid = ? ORDER BY Booking_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Booking> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<Booking> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Bookings ORDER BY Booking_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public boolean update(Booking booking) throws SQLException {
        return withConnection(conn -> update(booking, conn));
    }

    @Override
    public boolean update(Booking booking, Connection conn) throws SQLException {
        String sql = "UPDATE Bookings SET Total_price = ?, Status = ? WHERE Booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, booking.getTotalPrice());
            ps.setString(2, booking.getStatus());
            ps.setInt(3, booking.getBookingId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int bookingId) throws SQLException {
        return withConnection(conn -> delete(bookingId, conn));
    }

    @Override
    public boolean delete(int bookingId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Bookings WHERE Booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Booking> mapRows(ResultSet rs) throws SQLException {
        List<Booking> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("Booking_id"));
        booking.setUid(rs.getInt("Uid"));
        booking.setBookingTime(rs.getTimestamp("Booking_time").toLocalDateTime());
        booking.setTotalPrice(rs.getDouble("Total_price"));
        booking.setStatus(rs.getString("Status"));
        return booking;
    }
}
