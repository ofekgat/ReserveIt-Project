package getticket.service;

import getticket.model.Booking;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * One booking together with the customer, show, venue and showtime it belongs to.
 * Read-only: the admin order screen and the reports screen both display these,
 * which is why the enrichment lives in OrderReportService rather than in a bean.
 */
public class OrderRow implements Serializable {

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
