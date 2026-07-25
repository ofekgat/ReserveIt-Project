package getticket.dao.impl;

import getticket.dao.SeatDao;
import getticket.model.Seat;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeatDaoImpl implements SeatDao {

    private static final String INSERT_SQL =
            "INSERT INTO Seats (Vid, Row_num, Seat_num) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Seats WHERE Seat_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Seats";
    private static final String SELECT_BY_VENUE_SQL =
            "SELECT * FROM Seats WHERE Vid = ? ORDER BY Row_num, Seat_num";
    private static final String SELECT_AVAILABLE_SQL =
            "SELECT s.* FROM Seats s " +
            "JOIN Event_Instances ei ON ei.Vid = s.Vid " +
            "WHERE ei.Instance_id = ? " +
            "AND s.Seat_id NOT IN (" +
            "    SELECT t.Seat_id FROM Tickets t WHERE t.Instance_id = ? AND t.Seat_id IS NOT NULL" +
            ") ORDER BY s.Row_num, s.Seat_num";
    private static final String UPDATE_SQL =
            "UPDATE Seats SET Vid = ?, Row_num = ?, Seat_num = ? WHERE Seat_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Seats WHERE Seat_id = ?";

    @Override
    public int create(Seat seat) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, seat.getVid());
                ps.setInt(2, seat.getRowNum());
                ps.setInt(3, seat.getSeatNum());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        seat.setSeatId(id);
                        return id;
                    }
                }
                throw new SQLException("Creating seat failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Seat getById(int id) throws SQLException {
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
    public List<Seat> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Seat> seats = new ArrayList<>();
                while (rs.next()) {
                    seats.add(mapRow(rs));
                }
                return seats;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Seat> getSeatsByVenue(int vid) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_VENUE_SQL)) {
                ps.setInt(1, vid);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Seat> seats = new ArrayList<>();
                    while (rs.next()) {
                        seats.add(mapRow(rs));
                    }
                    return seats;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Seat> getAvailableSeats(int instanceId) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_AVAILABLE_SQL)) {
                ps.setInt(1, instanceId);
                ps.setInt(2, instanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Seat> seats = new ArrayList<>();
                    while (rs.next()) {
                        seats.add(mapRow(rs));
                    }
                    return seats;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Integer> getBookedSeatIds(List<Integer> seatIds, int instanceId, Connection conn) throws SQLException {
        if (seatIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = String.join(",", Collections.nCopies(seatIds.size(), "?"));
        String sql = "SELECT Seat_id FROM Tickets WHERE Instance_id = ? AND Seat_id IN (" + placeholders + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            for (int i = 0; i < seatIds.size(); i++) {
                ps.setInt(i + 2, seatIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> booked = new ArrayList<>();
                while (rs.next()) {
                    booked.add(rs.getInt("Seat_id"));
                }
                return booked;
            }
        }
    }

    @Override
    public boolean update(Seat seat) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, seat.getVid());
                ps.setInt(2, seat.getRowNum());
                ps.setInt(3, seat.getSeatNum());
                ps.setInt(4, seat.getSeatId());
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

    private Seat mapRow(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("Seat_id"),
                rs.getInt("Vid"),
                rs.getInt("Row_num"),
                rs.getInt("Seat_num")
        );
    }
}
