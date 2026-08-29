package getticket.service;

import getticket.dao.BookingDao;
import getticket.dao.EventInstanceDao;
import getticket.dao.ShowDao;
import getticket.dao.TicketDao;
import getticket.dao.UserDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.BookingDaoImpl;
import getticket.dao.impl.EventInstanceDaoImpl;
import getticket.dao.impl.ShowDaoImpl;
import getticket.dao.impl.TicketDaoImpl;
import getticket.dao.impl.UserDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Booking;
import getticket.model.EventInstance;
import getticket.model.Ticket;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Read-side reporting over the Bookings table, for the admin order and report
 * screens.
 *
 * A booking is always for exactly one Event_Instance (see BookingService.checkout,
 * which takes a single instanceId), so each order can be enriched with one
 * show/venue/time by following its first ticket to that instance.
 */
public class OrderReportService {

    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BookingDao bookingDao;
    private final TicketDao ticketDao;
    private final UserDao userDao;
    private final EventInstanceDao eventInstanceDao;
    private final ShowDao showDao;
    private final VenueDao venueDao;

    public OrderReportService() {
        this(new BookingDaoImpl(), new TicketDaoImpl(), new UserDaoImpl(),
                new EventInstanceDaoImpl(), new ShowDaoImpl(), new VenueDaoImpl());
    }

    public OrderReportService(BookingDao bookingDao, TicketDao ticketDao, UserDao userDao,
                              EventInstanceDao eventInstanceDao, ShowDao showDao, VenueDao venueDao) {
        this.bookingDao = bookingDao;
        this.ticketDao = ticketDao;
        this.userDao = userDao;
        this.eventInstanceDao = eventInstanceDao;
        this.showDao = showDao;
        this.venueDao = venueDao;
    }

    /** Every booking in the system, enriched with its customer, show, venue and showtime. */
    public List<OrderRow> loadOrders() throws SQLException {
        Map<Integer, String> usernameCache = new HashMap<>();
        Map<Integer, EventInstance> instanceCache = new HashMap<>();
        Map<Integer, String> showNameCache = new HashMap<>();
        Map<Integer, String> venueNameCache = new HashMap<>();

        List<OrderRow> rows = new ArrayList<>();
        for (Booking booking : bookingDao.getAll()) {
            String username = usernameCache.computeIfAbsent(booking.getUid(), this::lookupUsername);

            List<Ticket> tickets = ticketDao.getTicketsByBooking(booking.getBookingId());
            String showName = "—";
            String venueName = "—";
            LocalDateTime showTime = null;
            if (!tickets.isEmpty()) {
                int instanceId = tickets.get(0).getInstanceId();
                EventInstance instance = instanceCache.computeIfAbsent(instanceId, this::lookupInstance);
                if (instance != null) {
                    showTime = instance.getStartTime();
                    showName = showNameCache.computeIfAbsent(instance.getSid(), this::lookupShowName);
                    venueName = venueNameCache.computeIfAbsent(instance.getVid(), this::lookupVenueName);
                }
            }

            rows.add(new OrderRow(booking, username, showName, venueName, showTime, tickets.size()));
        }
        return rows;
    }

    /**
     * Orders placed within the given range (both bounds inclusive, null meaning
     * open-ended) and optionally narrowed to one status, with their totals and
     * per-show / per-venue / per-status breakdowns.
     */
    public OrderReport buildReport(LocalDate from, LocalDate to, String status) throws SQLException {
        List<OrderRow> lines = new ArrayList<>();
        for (OrderRow row : loadOrders()) {
            if (matches(row, from, to, status)) {
                lines.add(row);
            }
        }
        lines.sort(Comparator.comparing(
                (OrderRow row) -> row.getBooking().getBookingTime(),
                Comparator.nullsLast(Comparator.reverseOrder())));

        return new OrderReport(lines,
                groupBy(lines, OrderRow::getShowName, Comparator.comparingDouble(OrderReport.Group::getRevenue).reversed()),
                groupBy(lines, OrderRow::getVenueName, Comparator.comparingDouble(OrderReport.Group::getRevenue).reversed()),
                groupBy(lines, row -> row.getBooking().getStatus(), Comparator.comparing(OrderReport.Group::getLabel)),
                describe(from, to, status));
    }

    static boolean isCancelled(OrderRow row) {
        return CANCELLED_STATUS.equalsIgnoreCase(row.getBooking().getStatus());
    }

    private boolean matches(OrderRow row, LocalDate from, LocalDate to, String status) {
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(row.getBooking().getStatus())) {
            return false;
        }
        if (from == null && to == null) {
            return true;
        }
        LocalDateTime placed = row.getBooking().getBookingTime();
        if (placed == null) {
            // No timestamp to compare against, so it can't be placed inside a date range.
            return false;
        }
        LocalDate placedOn = placed.toLocalDate();
        return (from == null || !placedOn.isBefore(from)) && (to == null || !placedOn.isAfter(to));
    }

    private List<OrderReport.Group> groupBy(List<OrderRow> lines, Function<OrderRow, String> key,
                                            Comparator<OrderReport.Group> order) {
        Map<String, OrderReport.Group> groups = new LinkedHashMap<>();
        for (OrderRow row : lines) {
            String label = key.apply(row);
            groups.computeIfAbsent(label == null || label.isEmpty() ? "—" : label, OrderReport.Group::new).add(row);
        }
        List<OrderReport.Group> result = new ArrayList<>(groups.values());
        result.sort(order);
        return result;
    }

    private String describe(LocalDate from, LocalDate to, String status) {
        StringBuilder label = new StringBuilder();
        if (from != null && to != null) {
            label.append(from.format(LABEL_FORMAT)).append(" – ").append(to.format(LABEL_FORMAT));
        } else if (from != null) {
            label.append("From ").append(from.format(LABEL_FORMAT));
        } else if (to != null) {
            label.append("Up to ").append(to.format(LABEL_FORMAT));
        } else {
            label.append("All time");
        }
        if (status != null && !status.isEmpty()) {
            label.append(" · ").append(status);
        }
        return label.toString();
    }

    private String lookupUsername(int uid) {
        try {
            var user = userDao.getById(uid);
            return user != null ? user.getUname() : ("User #" + uid);
        } catch (SQLException e) {
            return "User #" + uid;
        }
    }

    private EventInstance lookupInstance(int instanceId) {
        try {
            return eventInstanceDao.getById(instanceId);
        } catch (SQLException e) {
            return null;
        }
    }

    private String lookupShowName(int sid) {
        try {
            var show = showDao.getById(sid);
            return show != null ? show.getSname() : ("Show #" + sid);
        } catch (SQLException e) {
            return "Show #" + sid;
        }
    }

    private String lookupVenueName(int vid) {
        try {
            var venue = venueDao.getById(vid);
            return venue != null ? venue.getVname() : ("Venue #" + vid);
        } catch (SQLException e) {
            return "Venue #" + vid;
        }
    }
}
