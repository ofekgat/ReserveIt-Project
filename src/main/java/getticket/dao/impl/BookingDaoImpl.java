package getticket.dao.impl;

import getticket.dao.BookingDao;
import getticket.model.Booking;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookingDaoImpl implements BookingDao {

    private static final String INSERT_SQL =
            "INSERT INTO Bookings (Uid, Booking_time, Total_price, Status) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Bookings WHERE Booking_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Bookings";
    private static final String SELECT_BY_USER_SQL =
            "SELECT * FROM Bookings WHERE Uid = ? ORDER BY Booking_time DESC";
    private static final String UPDATE_SQL =
            "UPDATE Bookings SET Uid = ?, Booking_time = ?, Total_price = ?, Status = ? WHERE Booking_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Bookings WHERE Booking_id = ?";

    @Override
    public int create(Booking booking) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            return create(booking, conn);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public int create(Booking booking, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getUid());
            ps.setTimestamp(2, Timestamp.valueOf(booking.getBookingTime()));
            ps.setDouble(3, booking.getTotalPrice());
            ps.setString(4, booking.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    booking.setBookingId(id);
                    return id;
                }
            }
            throw new SQLException("Creating booking failed, no generated key obtained.");
        }
    }

    @Override
    public Booking getById(int id) throws SQLException {
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
    public List<Booking> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
                return bookings;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Booking> getBookingsByUser(int uid) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_USER_SQL)) {
                ps.setInt(1, uid);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Booking> bookings = new ArrayList<>();
                    while (rs.next()) {
                        bookings.add(mapRow(rs));
                    }
                    return bookings;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Booking booking) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, booking.getUid());
                ps.setTimestamp(2, Timestamp.valueOf(booking.getBookingTime()));
                ps.setDouble(3, booking.getTotalPrice());
                ps.setString(4, booking.getStatus());
                ps.setInt(5, booking.getBookingId());
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

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("Booking_id"),
                rs.getInt("Uid"),
                rs.getTimestamp("Booking_time").toLocalDateTime(),
                rs.getDouble("Total_price"),
                rs.getString("Status")
        );
    }
}
