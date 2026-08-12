package getticket.web;

import getticket.dao.EventInstanceDao;
import getticket.dao.SeatDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.EventInstanceDaoImpl;
import getticket.dao.impl.SeatDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Booking;
import getticket.model.EventInstance;
import getticket.model.Seat;
import getticket.model.Venue;
import getticket.service.BookingService;
import getticket.service.InsufficientTicketsException;
import getticket.service.SeatUnavailableException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final EventInstanceDao eventInstanceDao = new EventInstanceDaoImpl();
    private final VenueDao venueDao = new VenueDaoImpl();
    private final BookingService bookingService = new BookingService();

    @ManagedProperty(value = "#{userSessionBean}")
    private UserSessionBean userSessionBean;

    private EventInstance eventInstance;
    private Venue venue;
    private List<Seat> availableSeats = Collections.emptyList();
    private final List<Integer> selectedSeatIds = new ArrayList<>();
    private int ticketQuantity = 1;

    private Booking lastBooking;

    /** Starts a fresh cart for the chosen event instance, discarding any previous selection. */
    public String selectInstance(int instanceId) {
        try {
            eventInstance = eventInstanceDao.getById(instanceId);
            if (eventInstance == null) {
                FacesMessages.addError("Error", "This showtime no longer exists.");
                return null;
            }
            venue = venueDao.getById(eventInstance.getVid());
            availableSeats = venue != null && venue.isNumbered()
                    ? seatDao.getAvailableSeats(instanceId)
                    : Collections.emptyList();
            selectedSeatIds.clear();
            ticketQuantity = 1;
            return "/seatSelection?faces-redirect=true";
        } catch (SQLException e) {
            FacesMessages.addError("Error", "Could not load seats, please try again.");
            return null;
        }
    }

    public void toggleSeat(int seatId) {
        if (!selectedSeatIds.remove(Integer.valueOf(seatId))) {
            selectedSeatIds.add(seatId);
        }
    }

    public boolean isSeatSelected(int seatId) {
        return selectedSeatIds.contains(seatId);
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
            FacesMessages.addError("Error", "Please log in before checking out.");
            return "/login?faces-redirect=true";
        }
        if (eventInstance == null) {
            FacesMessages.addError("Error", "Please select a showtime first.");
            return null;
        }

        int uid = userSessionBean.getCurrentUser().getUid();
        try {
            if (isNumberedVenue()) {
                if (selectedSeatIds.isEmpty()) {
                    FacesMessages.addError("Error", "Please select at least one seat.");
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
            refreshAvailableSeats();
            FacesMessages.addError("Error", "Some selected seats were just booked by someone else. Please choose again.");
            return null;
        } catch (InsufficientTicketsException e) {
            FacesMessages.addError("Error", "Only " + e.getAvailable() + " tickets are left for this showtime.");
            return null;
        } catch (SQLException e) {
            FacesMessages.addError("Error", "Booking failed, please try again.");
            return null;
        }
    }

    private void refreshAvailableSeats() {
        try {
            availableSeats = seatDao.getAvailableSeats(eventInstance.getInstanceId());
        } catch (SQLException e) {
            availableSeats = Collections.emptyList();
        }
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

    public List<Seat> getAvailableSeats() {
        return availableSeats;
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
}
