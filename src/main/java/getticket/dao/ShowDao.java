package getticket.dao;

import getticket.model.Show;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data-access contract for the Shows table.
 *
 * Each method comes in two forms:
 *  - a convenience form that borrows and releases its own connection,
 *    for simple standalone reads (catalog browsing, lookups);
 *  - a form taking an explicit Connection, so several calls can share
 *    one transaction. The caller owns that connection.
 */
public interface ShowDao {

    int create(Show show) throws SQLException;
    int create(Show show, Connection conn) throws SQLException;

    Show getById(int sid) throws SQLException;
    Show getById(int sid, Connection conn) throws SQLException;

    List<Show> getAll() throws SQLException;
    List<Show> getAll(Connection conn) throws SQLException;

    List<Show> getShowsByCategory(String category) throws SQLException;
    List<Show> getShowsByCategory(String category, Connection conn) throws SQLException;

    /** Partial, case-insensitive match on Sname. */
    List<Show> searchByName(String nameFragment) throws SQLException;
    List<Show> searchByName(String nameFragment, Connection conn) throws SQLException;

    /** Shows that have at least one Event_Instance starting on the given date. */
    List<Show> getShowsByDate(LocalDate date) throws SQLException;
    List<Show> getShowsByDate(LocalDate date, Connection conn) throws SQLException;

    boolean update(Show show) throws SQLException;
    boolean update(Show show, Connection conn) throws SQLException;

    boolean delete(int sid) throws SQLException;
    boolean delete(int sid, Connection conn) throws SQLException;
}
