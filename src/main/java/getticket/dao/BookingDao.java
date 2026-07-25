package getticket.dao;

import getticket.model.Booking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface BookingDao extends GenericDao<Booking> {

    /** A user's booking history, for the "my bookings" personal area. */
    List<Booking> getBookingsByUser(int uid) throws SQLException;

    /** Same as {@link #create(Booking)} but runs on the caller's transaction. */
    int create(Booking booking, Connection conn) throws SQLException;
}
