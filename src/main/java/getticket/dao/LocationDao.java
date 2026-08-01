package getticket.dao;

import getticket.model.Location;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface LocationDao {

    int create(Location location) throws SQLException;
    int create(Location location, Connection conn) throws SQLException;

    Location getById(int locationId) throws SQLException;
    Location getById(int locationId, Connection conn) throws SQLException;

    List<Location> getAll() throws SQLException;
    List<Location> getAll(Connection conn) throws SQLException;

    boolean update(Location location) throws SQLException;
    boolean update(Location location, Connection conn) throws SQLException;

    boolean delete(int locationId) throws SQLException;
    boolean delete(int locationId, Connection conn) throws SQLException;
}
