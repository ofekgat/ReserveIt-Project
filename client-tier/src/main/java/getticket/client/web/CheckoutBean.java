package getticket.client.web;

import getticket.client.mock.InsufficientTicketsException;
import getticket.client.mock.MockData;
import getticket.client.mock.SeatUnavailableException;
import getticket.client.model.Booking;
import getticket.client.model.EventInstance;
import getticket.client.model.Seat;
import getticket.client.model.Venue;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for the shopping-cart / checkout flow: collect selected seats
 * (or, for general-admission venues, a ticket quantity) for one event
 * instance, show the running total, then hand off to MockData for a fake
 * "atomic" booking. Same shape as the server tier's real CheckoutBean, which
 * calls BookingService instead of MockData.
 */
@ManagedBean(name = "checkoutBean")
@SessionScoped
public class CheckoutBean implements Serializable {

    private static final long serialVersionUID = 1L;

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
        eventInstance = MockData.getInstanceById(instanceId);
        if (eventInstance == null) {
            addErrorMessage("This showtime no longer exists.");
            return null;
        }
        venue = MockData.getVenueById(eventInstance.getVid());
        availableSeats = venue != null && venue.isNumbered()
                ? MockData.getAvailableSeats(instanceId)
                : Collections.emptyList();
        selectedSeatIds.clear();
        ticketQuantity = 1;
        return "/seatSelection?faces-redirect=true";
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
                lastBooking = MockData.bookSeats(uid, eventInstance.getInstanceId(), selectedSeatIds);
                selectedSeatIds.clear();
            } else {
                lastBooking = MockData.bookGeneralAdmission(uid, eventInstance.getInstanceId(), ticketQuantity);
            }
            return "/confirmation?faces-redirect=true";
        } catch (SeatUnavailableException e) {
            selectedSeatIds.removeAll(e.getUnavailableSeatIds());
            refreshAvailableSeats();
            addErrorMessage("Some selected seats were just booked by someone else. Please choose again.");
            return null;
        } catch (InsufficientTicketsException e) {
            addErrorMessage("Only " + e.getAvailable() + " tickets are left for this showtime.");
            return null;
        }
    }

    private void refreshAvailableSeats() {
        availableSeats = MockData.getAvailableSeats(eventInstance.getInstanceId());
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
