package getticket.dao;

import getticket.model.Seat;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SeatDao {

    int create(Seat seat) throws SQLException;
    int create(Seat seat, Connection conn) throws SQLException;

    /** Inserts many seats in one round trip — used when a venue's seat map is created. */
    void createBatch(List<Seat> seats, Connection conn) throws SQLException;

    Seat getById(int seatId) throws SQLException;
    Seat getById(int seatId, Connection conn) throws SQLException;

    List<Seat> getSeatsByVenue(int vid) throws SQLException;
    List<Seat> getSeatsByVenue(int vid, Connection conn) throws SQLException;

    /** Seats in this instance's venue that have no ticket yet for this instance. */
    List<Seat> getAvailableSeats(int instanceId) throws SQLException;
    List<Seat> getAvailableSeats(int instanceId, Connection conn) throws SQLException;

    /**
     * Of the given seat ids, returns those already ticketed for this
     * instance. An empty result means every requested seat is still free.
     * Called inside the checkout transaction, after the instance row is locked.
     */
    List<Integer> getBookedSeatIds(List<Integer> seatIds, int instanceId, Connection conn) throws SQLException;

    boolean update(Seat seat) throws SQLException;
    boolean update(Seat seat, Connection conn) throws SQLException;

    boolean delete(int seatId) throws SQLException;
    boolean delete(int seatId, Connection conn) throws SQLException;

    /** Drops a venue's whole seat map. Used when its seating layout is rebuilt. */
    int deleteByVenue(int vid, Connection conn) throws SQLException;
}
