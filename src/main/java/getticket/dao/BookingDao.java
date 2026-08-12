package getticket.dao;

import getticket.model.Booking;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface BookingDao {

    int create(Booking booking) throws SQLException;
    int create(Booking booking, Connection conn) throws SQLException;

    Booking getById(int bookingId) throws SQLException;
    Booking getById(int bookingId, Connection conn) throws SQLException;

    List<Booking> getBookingsByUser(int uid) throws SQLException;
    List<Booking> getBookingsByUser(int uid, Connection conn) throws SQLException;

    /** All bookings in the system, newest first. For admin order tracking. */
    List<Booking> getAll() throws SQLException;
    List<Booking> getAll(Connection conn) throws SQLException;

    boolean update(Booking booking) throws SQLException;
    boolean update(Booking booking, Connection conn) throws SQLException;

    boolean delete(int bookingId) throws SQLException;
    boolean delete(int bookingId, Connection conn) throws SQLException;
}
