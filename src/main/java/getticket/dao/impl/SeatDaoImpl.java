package getticket.dao.impl;

import getticket.dao.SeatDao;
import getticket.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDaoImpl extends BaseDao implements SeatDao {

    private static final String COLS = "Seat_id, Vid, Row_num, Seat_num";

    @Override
    public int create(Seat seat) throws SQLException {
        return withConnection(conn -> create(seat, conn));
    }

    @Override
    public int create(Seat seat, Connection conn) throws SQLException {
        String sql = "INSERT INTO Seats (Vid, Row_num, Seat_num) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public void createBatch(List<Seat> seats, Connection conn) throws SQLException {
        String sql = "INSERT INTO Seats (Vid, Row_num, Seat_num) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Seat seat : seats) {
                ps.setInt(1, seat.getVid());
                ps.setInt(2, seat.getRowNum());
                ps.setInt(3, seat.getSeatNum());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Seat getById(int seatId) throws SQLException {
        return withConnection(conn -> getById(seatId, conn));
    }

    @Override
    public Seat getById(int seatId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Seats WHERE Seat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Seat> getSeatsByVenue(int vid) throws SQLException {
        return withConnection(conn -> getSeatsByVenue(vid, conn));
    }

    @Override
    public List<Seat> getSeatsByVenue(int vid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Seats WHERE Vid = ? ORDER BY Row_num, Seat_num";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vid);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Seat> getAvailableSeats(int instanceId) throws SQLException {
        return withConnection(conn -> getAvailableSeats(instanceId, conn));
    }

    @Override
    public List<Seat> getAvailableSeats(int instanceId, Connection conn) throws SQLException {
        // LEFT JOIN + IS NULL = "seats with no matching ticket for this instance".
        String sql =
            "SELECT s.Seat_id, s.Vid, s.Row_num, s.Seat_num " +
            "FROM Seats s " +
            "JOIN Event_Instances ei ON ei.Vid = s.Vid " +
            "LEFT JOIN Tickets t ON t.Seat_id = s.Seat_id AND t.Instance_id = ei.Instance_id " +
            "WHERE ei.Instance_id = ? AND t.Ticket_id IS NULL " +
            "ORDER BY s.Row_num, s.Seat_num";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Integer> getBookedSeatIds(List<Integer> seatIds, int instanceId, Connection conn) throws SQLException {
        List<Integer> booked = new ArrayList<>();
        if (seatIds == null || seatIds.isEmpty()) {
            return booked;
        }

        // IN (?) needs one placeholder per id, built to match the list
        // size. The ids themselves are still bound as parameters, so
        // this stays injection-safe.
        StringBuilder sql = new StringBuilder(
                "SELECT Seat_id FROM Tickets WHERE Instance_id = ? AND Seat_id IN (");
        for (int i = 0; i < seatIds.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, instanceId);
            for (int i = 0; i < seatIds.size(); i++) {
                ps.setInt(i + 2, seatIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    booked.add(rs.getInt("Seat_id"));
                }
            }
        }
        return booked;
    }

    @Override
    public boolean update(Seat seat) throws SQLException {
        return withConnection(conn -> update(seat, conn));
    }

    @Override
    public boolean update(Seat seat, Connection conn) throws SQLException {
        String sql = "UPDATE Seats SET Row_num = ?, Seat_num = ? WHERE Seat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seat.getRowNum());
            ps.setInt(2, seat.getSeatNum());
            ps.setInt(3, seat.getSeatId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int seatId) throws SQLException {
        return withConnection(conn -> delete(seatId, conn));
    }

    @Override
    public boolean delete(int seatId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Seats WHERE Seat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int deleteByVenue(int vid, Connection conn) throws SQLException {
        String sql = "DELETE FROM Seats WHERE Vid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vid);
            return ps.executeUpdate();
        }
    }

    private List<Seat> mapRows(ResultSet rs) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        while (rs.next()) {
            seats.add(mapRow(rs));
        }
        return seats;
    }

    private Seat mapRow(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("Seat_id"));
        seat.setVid(rs.getInt("Vid"));
        seat.setRowNum(rs.getInt("Row_num"));
        seat.setSeatNum(rs.getInt("Seat_num"));
        return seat;
    }
}
