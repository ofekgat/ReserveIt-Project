package getticket.dao.impl;

import getticket.dao.VenueDao;
import getticket.model.Venue;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VenueDaoImpl implements VenueDao {

    private static final String INSERT_SQL =
            "INSERT INTO Venues (Location_id, Vname, IsNumbered, Vcapacity) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Venues WHERE Vid = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Venues";
    private static final String UPDATE_SQL =
            "UPDATE Venues SET Location_id = ?, Vname = ?, IsNumbered = ?, Vcapacity = ? WHERE Vid = ?";
    private static final String DELETE_SQL = "DELETE FROM Venues WHERE Vid = ?";

    @Override
    public int create(Venue venue) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, venue.getLocationId());
                ps.setString(2, venue.getVname());
                ps.setBoolean(3, venue.isNumbered());
                ps.setInt(4, venue.getVcapacity());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        venue.setVid(id);
                        return id;
                    }
                }
                throw new SQLException("Creating venue failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Venue getById(int id) throws SQLException {
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
    public List<Venue> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Venue> venues = new ArrayList<>();
                while (rs.next()) {
                    venues.add(mapRow(rs));
                }
                return venues;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Venue venue) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, venue.getLocationId());
                ps.setString(2, venue.getVname());
                ps.setBoolean(3, venue.isNumbered());
                ps.setInt(4, venue.getVcapacity());
                ps.setInt(5, venue.getVid());
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

    private Venue mapRow(ResultSet rs) throws SQLException {
        return new Venue(
                rs.getInt("Vid"),
                rs.getInt("Location_id"),
                rs.getString("Vname"),
                rs.getBoolean("IsNumbered"),
                rs.getInt("Vcapacity")
        );
    }
}
