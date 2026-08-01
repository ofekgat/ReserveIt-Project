package getticket.dao.impl;

import getticket.dao.VenueDao;
import getticket.model.Venue;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueDaoImpl extends BaseDao implements VenueDao {

    private static final String COLS = "Vid, Location_id, Vname, IsNumbered, Vcapacity";

    @Override
    public int create(Venue venue) throws SQLException {
        return withConnection(conn -> create(venue, conn));
    }

    @Override
    public int create(Venue venue, Connection conn) throws SQLException {
        String sql = "INSERT INTO Venues (Location_id, Vname, IsNumbered, Vcapacity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Venue getById(int vid) throws SQLException {
        return withConnection(conn -> getById(vid, conn));
    }

    @Override
    public Venue getById(int vid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Venues WHERE Vid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Venue> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<Venue> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Venues ORDER BY Vname";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public List<Venue> getVenuesByLocation(int locationId) throws SQLException {
        return withConnection(conn -> getVenuesByLocation(locationId, conn));
    }

    @Override
    public List<Venue> getVenuesByLocation(int locationId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Venues WHERE Location_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public boolean update(Venue venue) throws SQLException {
        return withConnection(conn -> update(venue, conn));
    }

    @Override
    public boolean update(Venue venue, Connection conn) throws SQLException {
        String sql = "UPDATE Venues SET Location_id = ?, Vname = ?, IsNumbered = ?, Vcapacity = ? WHERE Vid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venue.getLocationId());
            ps.setString(2, venue.getVname());
            ps.setBoolean(3, venue.isNumbered());
            ps.setInt(4, venue.getVcapacity());
            ps.setInt(5, venue.getVid());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int vid) throws SQLException {
        return withConnection(conn -> delete(vid, conn));
    }

    @Override
    public boolean delete(int vid, Connection conn) throws SQLException {
        String sql = "DELETE FROM Venues WHERE Vid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vid);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Venue> mapRows(ResultSet rs) throws SQLException {
        List<Venue> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Venue mapRow(ResultSet rs) throws SQLException {
        Venue venue = new Venue();
        venue.setVid(rs.getInt("Vid"));
        venue.setLocationId(rs.getInt("Location_id"));
        venue.setVname(rs.getString("Vname"));
        venue.setNumbered(rs.getBoolean("IsNumbered"));
        venue.setVcapacity(rs.getInt("Vcapacity"));
        return venue;
    }
}
