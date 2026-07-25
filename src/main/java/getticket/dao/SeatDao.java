package getticket.dao;

import getticket.model.Seat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SeatDao extends GenericDao<Seat> {

    /** All physical seats belonging to a venue; used to render the seat map. */
    List<Seat> getSeatsByVenue(int vid) throws SQLException;

    /** Seats in the event instance's venue that are not yet linked to a ticket for that instance. */
    List<Seat> getAvailableSeats(int instanceId) throws SQLException;

    /**
     * Of the given seatIds, returns the ones that already have a ticket for this instance.
     * Runs on the caller's transaction so the check is consistent with a prior FOR UPDATE lock.
     */
    List<Integer> getBookedSeatIds(List<Integer> seatIds, int instanceId, Connection conn) throws SQLException;
}
