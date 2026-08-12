package getticket.service;

import getticket.dao.SeatDao;
import getticket.dao.TicketDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.SeatDaoImpl;
import getticket.dao.impl.TicketDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Seat;
import getticket.model.Venue;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for managing venues (halls).
 *
 * A numbered venue is only half-created by inserting its Venues row: seat
 * selection reads the Seats table, so a venue with IsNumbered = TRUE and no
 * Seats rows renders an empty, unbookable seat map. This service keeps the
 * two in step, writing the venue and its seat grid in one transaction.
 */
public class VenueService {

    /** Seats per row when laying out a new seat grid; the last row may be shorter. */
    public static final int DEFAULT_SEATS_PER_ROW = 10;

    private final VenueDao venueDao;
    private final SeatDao seatDao;
    private final TicketDao ticketDao;

    public VenueService() {
        this(new VenueDaoImpl(), new SeatDaoImpl(), new TicketDaoImpl());
    }

    public VenueService(VenueDao venueDao, SeatDao seatDao, TicketDao ticketDao) {
        this.venueDao = venueDao;
        this.seatDao = seatDao;
        this.ticketDao = ticketDao;
    }

    /**
     * Creates a venue and, when it uses numbered seating, its full seat grid —
     * Vcapacity seats laid out seatsPerRow to a row. Either both land or neither does.
     */
    public int createVenue(Venue venue, int seatsPerRow) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);

            int vid = venueDao.create(venue, conn);
            if (venue.isNumbered()) {
                seatDao.createBatch(buildSeatGrid(vid, venue.getVcapacity(), seatsPerRow, 0), conn);
            }

            conn.commit();
            return vid;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            restoreAutoCommitAndRelease(conn);
        }
    }

    /**
     * Updates a venue and brings its seat map back in line with its capacity.
     *
     * The seat map is compared against the venue's own Vcapacity rather than
     * against the previous values, so a venue whose seats drifted out of sync
     * (for instance one created before seat generation existed) is repaired on
     * the next save even when nothing else changed.
     *
     * Growing a seat map only appends rows, which leaves existing seats — and
     * the tickets pointing at them — untouched. Shrinking it, or dropping it
     * entirely by switching to general admission, has to delete seats, so it is
     * refused with {@link SeatLayoutLockedException} once any ticket has been
     * sold for this venue; nothing at all is written in that case.
     */
    public boolean updateVenue(Venue venue, int seatsPerRow) throws SQLException, SeatLayoutLockedException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false);

            Venue existing = venueDao.getById(venue.getVid(), conn);
            if (existing == null) {
                return false;
            }

            List<Seat> currentSeats = seatDao.getSeatsByVenue(venue.getVid(), conn);
            int currentSeatCount = currentSeats.size();
            int targetSeatCount = venue.isNumbered() ? venue.getVcapacity() : 0;

            boolean destructive = targetSeatCount < currentSeatCount;
            if (destructive) {
                int soldTickets = ticketDao.countByVenue(venue.getVid(), conn);
                if (soldTickets > 0) {
                    throw new SeatLayoutLockedException(soldTickets);
                }
            }

            boolean updated = venueDao.update(venue, conn);

            if (targetSeatCount < currentSeatCount) {
                // Safe to rebuild from scratch: the check above proved no tickets exist.
                seatDao.deleteByVenue(venue.getVid(), conn);
                if (targetSeatCount > 0) {
                    seatDao.createBatch(buildSeatGrid(venue.getVid(), targetSeatCount, seatsPerRow, 0), conn);
                }
            } else if (targetSeatCount > currentSeatCount) {
                // Append the shortfall in fresh rows after the last existing one, so the
                // UNIQUE (Vid, Row_num, Seat_num) constraint can't collide with what's there.
                int startRow = maxRow(currentSeats);
                seatDao.createBatch(
                        buildSeatGrid(venue.getVid(), targetSeatCount - currentSeatCount, seatsPerRow, startRow),
                        conn);
            }

            conn.commit();
            return updated;
        } catch (SQLException | SeatLayoutLockedException e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            restoreAutoCommitAndRelease(conn);
        }
    }

    private int maxRow(List<Seat> seats) {
        int max = 0;
        for (Seat seat : seats) {
            max = Math.max(max, seat.getRowNum());
        }
        return max;
    }

    /**
     * Lays out `count` seats, `seatsPerRow` to a row, in the rows following
     * `rowOffset` (0 to start at row 1).
     */
    private List<Seat> buildSeatGrid(int vid, int count, int seatsPerRow, int rowOffset) {
        int perRow = seatsPerRow > 0 ? seatsPerRow : DEFAULT_SEATS_PER_ROW;
        List<Seat> seats = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            seats.add(new Seat(vid, rowOffset + (i / perRow) + 1, (i % perRow) + 1));
        }
        return seats;
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
