package getticket.dao.impl;

import getticket.dao.LocationDao;
import getticket.model.Location;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LocationDaoImpl implements LocationDao {

    private static final String INSERT_SQL = "INSERT INTO Locations (City, Address) VALUES (?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Locations WHERE Location_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Locations";
    private static final String UPDATE_SQL = "UPDATE Locations SET City = ?, Address = ? WHERE Location_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Locations WHERE Location_id = ?";

    @Override
    public int create(Location location) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, location.getCity());
                ps.setString(2, location.getAddress());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        location.setLocationId(id);
                        return id;
                    }
                }
                throw new SQLException("Creating location failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Location getById(int id) throws SQLException {
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
    public List<Location> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Location> locations = new ArrayList<>();
                while (rs.next()) {
                    locations.add(mapRow(rs));
                }
                return locations;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Location location) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, location.getCity());
                ps.setString(2, location.getAddress());
                ps.setInt(3, location.getLocationId());
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

    private Location mapRow(ResultSet rs) throws SQLException {
        return new Location(
                rs.getInt("Location_id"),
                rs.getString("City"),
                rs.getString("Address")
        );
    }
}
