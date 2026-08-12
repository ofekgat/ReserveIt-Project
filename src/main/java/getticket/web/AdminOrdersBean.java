package getticket.web;

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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for adminOrders.xhtml: order tracking across every customer.
 * A booking is always for exactly one Event_Instance (see BookingService.checkout,
 * which takes a single instanceId) so each order can be enriched with one
 * show/venue/time by following its first ticket to that instance.
 */
@ManagedBean(name = "adminOrdersBean")
@ViewScoped
public class AdminOrdersBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> STATUS_OPTIONS =
            Arrays.asList("PENDING", "CONFIRMED", "PAID", "CANCELLED");

    private final BookingDao bookingDao = new BookingDaoImpl();
    private final TicketDao ticketDao = new TicketDaoImpl();
    private final UserDao userDao = new UserDaoImpl();
    private final EventInstanceDao eventInstanceDao = new EventInstanceDaoImpl();
    private final ShowDao showDao = new ShowDaoImpl();
    private final VenueDao venueDao = new VenueDaoImpl();

    private List<OrderRow> orders = Collections.emptyList();

    public void loadAll() {
        try {
            Map<Integer, String> usernameCache = new HashMap<>();
            Map<Integer, EventInstance> instanceCache = new HashMap<>();
            Map<Integer, String> showNameCache = new HashMap<>();
            Map<Integer, String> venueNameCache = new HashMap<>();

            List<Booking> bookings = bookingDao.getAll();
            List<OrderRow> rows = new ArrayList<>();
            for (Booking booking : bookings) {
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
            orders = rows;
        } catch (SQLException e) {
            orders = new ArrayList<>();
            addErrorMessage("Could not load orders, please try again.");
        }
    }

    public void updateStatus(OrderRow row) {
        try {
            bookingDao.update(row.getBooking());
            addInfoMessage("Order #" + row.getBooking().getBookingId() + " updated to " + row.getBooking().getStatus() + ".");
        } catch (SQLException e) {
            addErrorMessage("Could not update the order, please try again.");
        }
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
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

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    public List<OrderRow> getOrders() {
        return orders;
    }

    public List<String> getStatusOptions() {
        return STATUS_OPTIONS;
    }

    /** Display-only view combining one Booking with the show/venue/time it's for. */
    public static class OrderRow implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Booking booking;
        private final String username;
        private final String showName;
        private final String venueName;
        private final LocalDateTime showTime;
        private final int ticketCount;

        OrderRow(Booking booking, String username, String showName, String venueName,
                 LocalDateTime showTime, int ticketCount) {
            this.booking = booking;
            this.username = username;
            this.showName = showName;
            this.venueName = venueName;
            this.showTime = showTime;
            this.ticketCount = ticketCount;
        }

        public Booking getBooking() {
            return booking;
        }

        public String getUsername() {
            return username;
        }

        public String getShowName() {
            return showName;
        }

        public String getVenueName() {
            return venueName;
        }

        public LocalDateTime getShowTime() {
            return showTime;
        }

        public int getTicketCount() {
            return ticketCount;
        }
    }
}
