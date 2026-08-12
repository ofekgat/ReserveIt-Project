package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.BookingSummary;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for adminOrders.xhtml: order tracking across every customer,
 * backed by MockData instead of BookingDao. Same public shape as the server
 * tier's real AdminOrdersBean, reusing the existing BookingSummary view type
 * (already built for myBookings.xhtml) instead of a bespoke row class.
 */
@ManagedBean(name = "adminOrdersBean")
@ViewScoped
public class AdminOrdersBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<String> STATUS_OPTIONS =
            Arrays.asList("PENDING", "CONFIRMED", "PAID", "CANCELLED");
    // JSF 2.2's built-in converters predate java.time, so dates are formatted
    // by hand here instead of via <f:convertDateTime type="localDateTime">.
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private List<BookingSummary> orders = Collections.emptyList();

    public void loadAll() {
        orders = MockData.getAllBookingSummaries();
    }

    public void updateStatus(BookingSummary order) {
        MockData.updateBookingStatus(order.getBooking().getBookingId(), order.getBooking().getStatus());
        addInfoMessage("Order #" + order.getBooking().getBookingId() + " updated to " + order.getBooking().getStatus() + ".");
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
    }

    public String usernameFor(int uid) {
        var user = MockData.getUserById(uid);
        return user != null ? user.getUname() : ("User #" + uid);
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    public List<BookingSummary> getOrders() {
        return orders;
    }

    public List<String> getStatusOptions() {
        return STATUS_OPTIONS;
    }
}
