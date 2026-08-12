package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.BookingSummary;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // JSF 2.2's built-in converters predate java.time, so dates are formatted
    // by hand here instead of via <f:convertDateTime type="localDateTime">.
    private static final DateTimeFormatter DATETIME_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_DISPLAY_FORMAT);
    }

    public String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_DISPLAY_FORMAT);
    }
}
