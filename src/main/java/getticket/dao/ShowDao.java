package getticket.dao;

import getticket.model.Show;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ShowDao extends GenericDao<Show> {

    /** Returns shows in the given category, for the "browse by category" search flow. */
    List<Show> getShowsByCategory(String category) throws SQLException;

    /** Free-text search on the show name, for the "search by name" flow. */
    List<Show> searchByName(String keyword) throws SQLException;

    /** Shows that have at least one Event_Instance scheduled on the given date. */
    List<Show> getShowsByDate(LocalDate date) throws SQLException;
}
