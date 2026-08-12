package getticket.web;

import getticket.dao.EventInstanceDao;
import getticket.dao.SeatDao;
import getticket.dao.TicketDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.EventInstanceDaoImpl;
import getticket.dao.impl.SeatDaoImpl;
import getticket.dao.impl.TicketDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Booking;
import getticket.model.EventInstance;
import getticket.model.Seat;
import getticket.model.Ticket;
import getticket.model.Venue;
import getticket.service.BookingService;
import getticket.service.InsufficientTicketsException;
import getticket.service.SeatUnavailableException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backing bean for the shopping-cart / checkout flow: collect selected seats
 * (or, for general-admission venues, a ticket quantity) for one event
 * instance, show the running total, then hand off to BookingService for the
 * atomic booking transaction.
 */
@ManagedBean(name = "checkoutBean")
@SessionScoped
public class CheckoutBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SeatDao seatDao = new SeatDaoImpl();
    private final TicketDao ticketDao = new TicketDaoImpl();
    private final EventInstanceDao eventInstanceDao = new EventInstanceDaoImpl();
    private final VenueDao venueDao = new VenueDaoImpl();
    private final BookingService bookingService = new BookingService();

    @ManagedProperty(value = "#{userSessionBean}")
    private UserSessionBean userSessionBean;

    private EventInstance eventInstance;
    private Venue venue;

    // The whole hall, not just what's free: a taken seat has to stay on the map
    // (greyed out) so the customer can see where the gaps are.
    private List<SeatRow> seatRows = Collections.emptyList();
    private Set<Integer> takenSeatIds = Collections.emptySet();

    private final List<Integer> selectedSeatIds = new ArrayList<>();
    private int ticketQuantity = 1;

    private Booking lastBooking;

    /** Starts a fresh cart for the chosen event instance, discarding any previous selection. */
    public String selectInstance(int instanceId) {
        try {
            eventInstance = eventInstanceDao.getById(instanceId);
            if (eventInstance == null) {
                addErrorMessage("This showtime no longer exists.");
                return null;
            }
            venue = venueDao.getById(eventInstance.getVid());
            selectedSeatIds.clear();
            ticketQuantity = 1;
            loadSeatMap();
            return "/seatSelection?faces-redirect=true";
        } catch (SQLException e) {
            addErrorMessage("Could not load seats, please try again.");
            return null;
        }
    }

    /**
     * Loads the venue's full seat map plus the set of seats already ticketed for
     * this instance, then groups the seats into rows for display.
     */
    private void loadSeatMap() throws SQLException {
        if (venue == null || !venue.isNumbered()) {
            seatRows = Collections.emptyList();
            takenSeatIds = Collections.emptySet();
            return;
        }

        Set<Integer> taken = new HashSet<>();
        for (Ticket ticket : ticketDao.getTicketsByInstance(eventInstance.getInstanceId())) {
            if (ticket.getSeatId() != null) {
                taken.add(ticket.getSeatId());
            }
        }
        takenSeatIds = taken;
        seatRows = groupIntoRows(seatDao.getSeatsByVenue(venue.getVid()));
    }

    /** Groups seats into one SeatRow per Row_num; the DAO already orders by row then seat. */
    private List<SeatRow> groupIntoRows(List<Seat> seats) {
        Map<Integer, List<Seat>> byRow = new LinkedHashMap<>();
        for (Seat seat : seats) {
            byRow.computeIfAbsent(seat.getRowNum(), r -> new ArrayList<>()).add(seat);
        }
        List<SeatRow> rows = new ArrayList<>(byRow.size());
        for (Map.Entry<Integer, List<Seat>> entry : byRow.entrySet()) {
            rows.add(new SeatRow(entry.getKey(), entry.getValue()));
        }
        return rows;
    }

    /** True when this seat is already ticketed for the current showtime. */
    public boolean isSeatTaken(int seatId) {
        return takenSeatIds.contains(seatId);
    }

    public void toggleSeat(int seatId) {
        if (!selectedSeatIds.remove(Integer.valueOf(seatId))) {
            selectedSeatIds.add(seatId);
        }
    }

    public boolean isSeatSelected(int seatId) {
        return selectedSeatIds.contains(seatId);
    }

    /**
     * Human-readable "Row R, seat S" labels for the current selection, in the
     * order the seats appear in the hall — the summary would otherwise show
     * raw database ids, which mean nothing to a customer.
     */
    public List<String> getSelectedSeatLabels() {
        List<String> labels = new ArrayList<>();
        for (SeatRow row : seatRows) {
            for (Seat seat : row.getSeats()) {
                if (selectedSeatIds.contains(seat.getSeatId())) {
                    labels.add("Row " + seat.getRowNum() + ", seat " + seat.getSeatNum());
                }
            }
        }
        return labels;
    }

    public double getTotalPrice() {
        if (eventInstance == null) {
            return 0;
        }
        int count = isNumberedVenue() ? selectedSeatIds.size() : ticketQuantity;
        return eventInstance.getTicketPrice() * count;
    }

    public boolean isNumberedVenue() {
        return venue != null && venue.isNumbered();
    }

    public String checkout() {
        if (userSessionBean == null || !userSessionBean.isLoggedIn()) {
            addErrorMessage("Please log in before checking out.");
            return "/login?faces-redirect=true";
        }
        if (eventInstance == null) {
            addErrorMessage("Please select a showtime first.");
            return null;
        }

        int uid = userSessionBean.getCurrentUser().getUid();
        try {
            if (isNumberedVenue()) {
                if (selectedSeatIds.isEmpty()) {
                    addErrorMessage("Please select at least one seat.");
                    return null;
                }
                lastBooking = bookingService.checkout(uid, eventInstance.getInstanceId(), selectedSeatIds);
                selectedSeatIds.clear();
            } else {
                lastBooking = bookingService.checkoutGeneralAdmission(
                        uid, eventInstance.getInstanceId(), ticketQuantity);
            }
            return "/confirmation?faces-redirect=true";
        } catch (SeatUnavailableException e) {
            selectedSeatIds.removeAll(e.getUnavailableSeatIds());
            refreshSeatMap();
            addErrorMessage("Some selected seats were just booked by someone else. Please choose again.");
            return null;
        } catch (InsufficientTicketsException e) {
            addErrorMessage("Only " + e.getAvailable() + " tickets are left for this showtime.");
            return null;
        } catch (SQLException e) {
            addErrorMessage("Booking failed, please try again.");
            return null;
        }
    }

    private void refreshSeatMap() {
        try {
            loadSeatMap();
        } catch (SQLException e) {
            seatRows = Collections.emptyList();
            takenSeatIds = Collections.emptySet();
        }
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    public EventInstance getEventInstance() {
        return eventInstance;
    }

    public Venue getVenue() {
        return venue;
    }

    public List<SeatRow> getSeatRows() {
        return seatRows;
    }

    public List<Integer> getSelectedSeatIds() {
        return selectedSeatIds;
    }

    public int getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public Booking getLastBooking() {
        return lastBooking;
    }

    /** One row of the hall, so the seat map can be rendered a row per line like a cinema. */
    public static class SeatRow implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int rowNum;
        private final List<Seat> seats;

        SeatRow(int rowNum, List<Seat> seats) {
            this.rowNum = rowNum;
            this.seats = seats;
        }

        public int getRowNum() {
            return rowNum;
        }

        public List<Seat> getSeats() {
            return seats;
        }
    }
}
