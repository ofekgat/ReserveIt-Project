package getticket.dao.impl;

import getticket.dao.LocationDao;
import getticket.model.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDaoImpl extends BaseDao implements LocationDao {

    private static final String COLS = "Location_id, City, Address";

    @Override
    public int create(Location location) throws SQLException {
        return withConnection(conn -> create(location, conn));
    }

    @Override
    public int create(Location location, Connection conn) throws SQLException {
        String sql = "INSERT INTO Locations (City, Address) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, location.getCity());
            ps.setString(2, location.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    location.setLocationId(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Location getById(int locationId) throws SQLException {
        return withConnection(conn -> getById(locationId, conn));
    }

    @Override
    public Location getById(int locationId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Locations WHERE Location_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Location> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<Location> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Locations ORDER BY City";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public boolean update(Location location) throws SQLException {
        return withConnection(conn -> update(location, conn));
    }

    @Override
    public boolean update(Location location, Connection conn) throws SQLException {
        String sql = "UPDATE Locations SET City = ?, Address = ? WHERE Location_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location.getCity());
            ps.setString(2, location.getAddress());
            ps.setInt(3, location.getLocationId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int locationId) throws SQLException {
        return withConnection(conn -> delete(locationId, conn));
    }

    @Override
    public boolean delete(int locationId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Locations WHERE Location_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Location> mapRows(ResultSet rs) throws SQLException {
        List<Location> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Location mapRow(ResultSet rs) throws SQLException {
        Location location = new Location();
        location.setLocationId(rs.getInt("Location_id"));
        location.setCity(rs.getString("City"));
        location.setAddress(rs.getString("Address"));
        return location;
    }
}
