package getticket.service;

import getticket.dao.BookingDao;
import getticket.dao.EventInstanceDao;
import getticket.dao.SeatDao;
import getticket.dao.TicketDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.BookingDaoImpl;
import getticket.dao.impl.EventInstanceDaoImpl;
import getticket.dao.impl.SeatDaoImpl;
import getticket.dao.impl.TicketDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Booking;
import getticket.model.EventInstance;
import getticket.model.Ticket;
import getticket.model.Venue;
import getticket.util.ConnectionPool;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic for the ticket checkout flow (design doc section 7/8).
 * checkout() runs as one atomic JDBC transaction: locking the target
 * Event_Instance row up front (SELECT ... FOR UPDATE) serializes any other
 * checkout attempt for the same instance, which is what prevents two users
 * from being sold the same seat (double booking) under concurrent load.
 *
 * Implements Serializable (no real state to serialize) because CheckoutBean
 * holds this as a field and lives in the HTTP session.
 */
public class BookingService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";

    private final BookingDao bookingDao;
    private final TicketDao ticketDao;
    private final EventInstanceDao eventInstanceDao;
    private final SeatDao seatDao;
    private final VenueDao venueDao;

    public BookingService() {
        this(new BookingDaoImpl(), new TicketDaoImpl(), new EventInstanceDaoImpl(),
                new SeatDaoImpl(), new VenueDaoImpl());
    }

    public BookingService(BookingDao bookingDao, TicketDao ticketDao, EventInstanceDao eventInstanceDao,
                           SeatDao seatDao, VenueDao venueDao) {
        this.bookingDao = bookingDao;
        this.ticketDao = ticketDao;
        this.eventInstanceDao = eventInstanceDao;
        this.seatDao = seatDao;
        this.venueDao = venueDao;
    }

    /**
     * Books the given seats of the given event instance for uid, in a single transaction:
     * verify the seats are still free, create the Booking with its total price, create one
     * Ticket per seat, and decrement Available_tickets. Either all of it commits, or none of it does.
     */
    public Booking checkout(int uid, int instanceId, List<Integer> seatIds)
            throws SQLException, SeatUnavailableException {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);

            EventInstance instance = eventInstanceDao.getByIdForUpdate(instanceId, conn);
            if (instance == null) {
                throw new SQLException("Event instance " + instanceId + " does not exist");
            }

            // 1. Verify the selected seats are still free.
            List<Integer> alreadyBooked = seatDao.getBookedSeatIds(seatIds, instanceId, conn);
            if (!alreadyBooked.isEmpty()) {
                throw new SeatUnavailableException(alreadyBooked);
            }

            // 2. Create the booking, with the total price for every ticket in it.
            double totalPrice = instance.getTicketPrice() * seatIds.size();
            Booking booking = new Booking(uid, LocalDateTime.now(), totalPrice, BOOKING_STATUS_CONFIRMED);
            int bookingId = bookingDao.create(booking, conn);

            // 3. Create one ticket per selected seat, linked to the booking.
            for (int seatId : seatIds) {
                ticketDao.create(new Ticket(bookingId, instanceId, seatId), conn);
            }

            // 4. Update the instance's remaining available ticket count.
            boolean decremented = eventInstanceDao.updateAvailableTickets(instanceId, -seatIds.size(), conn);
            if (!decremented) {
                throw new SQLException("Not enough available tickets for instance " + instanceId);
            }

            conn.commit();
            return booking;
        } catch (SQLException | SeatUnavailableException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            restoreAutoCommitAndRelease(conn);
        }
    }

    /**
     * Books ticketQuantity general-admission tickets (no specific seat) of the given
     * event instance for uid. Only valid for venues where IsNumbered is false; the
     * decrement's atomic "don't go negative" guard is what prevents overselling here,
     * since there are no Seats rows to check individually.
     */
    public Booking checkoutGeneralAdmission(int uid, int instanceId, int ticketQuantity)
            throws SQLException, InsufficientTicketsException {
        if (ticketQuantity <= 0) {
            throw new IllegalArgumentException("Ticket quantity must be positive");
        }

        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);

            EventInstance instance = eventInstanceDao.getByIdForUpdate(instanceId, conn);
            if (instance == null) {
                throw new SQLException("Event instance " + instanceId + " does not exist");
            }
            Venue venue = venueDao.getById(instance.getVid());
            if (venue == null) {
                throw new SQLException("Venue " + instance.getVid() + " does not exist");
            }
            if (venue.isNumbered()) {
                throw new IllegalStateException("Venue " + venue.getVid()
                        + " has numbered seating; use checkout(uid, instanceId, seatIds) instead");
            }

            double totalPrice = instance.getTicketPrice() * ticketQuantity;
            Booking booking = new Booking(uid, LocalDateTime.now(), totalPrice, BOOKING_STATUS_CONFIRMED);
            int bookingId = bookingDao.create(booking, conn);

            for (int i = 0; i < ticketQuantity; i++) {
                ticketDao.create(new Ticket(bookingId, instanceId, null), conn);
            }

            boolean decremented = eventInstanceDao.updateAvailableTickets(instanceId, -ticketQuantity, conn);
            if (!decremented) {
                throw new InsufficientTicketsException(ticketQuantity, instance.getAvailableTickets());
            }

            conn.commit();
            return booking;
        } catch (SQLException | InsufficientTicketsException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            restoreAutoCommitAndRelease(conn);
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // the connection is being released either way
        }
    }

    private void restoreAutoCommitAndRelease(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
            // best-effort; the pool will hand out a connection regardless
        }
        ConnectionPool.getInstance().releaseConnection(conn);
    }
}
