package getticket.dao;

import getticket.model.Venue;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface VenueDao {

    int create(Venue venue) throws SQLException;
    int create(Venue venue, Connection conn) throws SQLException;

    Venue getById(int vid) throws SQLException;
    Venue getById(int vid, Connection conn) throws SQLException;

    List<Venue> getAll() throws SQLException;
    List<Venue> getAll(Connection conn) throws SQLException;

    List<Venue> getVenuesByLocation(int locationId) throws SQLException;
    List<Venue> getVenuesByLocation(int locationId, Connection conn) throws SQLException;

    boolean update(Venue venue) throws SQLException;
    boolean update(Venue venue, Connection conn) throws SQLException;

    boolean delete(int vid) throws SQLException;
    boolean delete(int vid, Connection conn) throws SQLException;
}
