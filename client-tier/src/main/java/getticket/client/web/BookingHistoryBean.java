package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.BookingSummary;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for myBookings.xhtml ("My Tickets") — a screen that doesn't
 * exist yet on the server tier. Lists every booking the signed-in user has
 * made, newest first, with the show/venue/time it's for.
 */
@ManagedBean(name = "bookingHistoryBean")
@ViewScoped
public class BookingHistoryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{userSessionBean}")
    private UserSessionBean userSessionBean;

    private List<BookingSummary> bookings = Collections.emptyList();

    public void loadBookings() {
        if (userSessionBean != null && userSessionBean.isLoggedIn()) {
            bookings = MockData.getBookingSummariesByUser(userSessionBean.getCurrentUser().getUid());
        } else {
            bookings = Collections.emptyList();
        }
    }

    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    public List<BookingSummary> getBookings() {
        return bookings;
    }
}
