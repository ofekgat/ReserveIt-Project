package getticket.client.model;

import java.time.LocalDateTime;

/**
 * Display-only view combining one Booking with the show/venue/time it's for
 * and how many tickets it contains. The real Data/Application tiers would
 * build this with a join or a couple of extra queries; here MockData just
 * assembles it directly since everything already lives in memory.
 */
public class BookingSummary {

    private final Booking booking;
    private final String showName;
    private final String venueName;
    private final LocalDateTime showTime;
    private final int ticketCount;

    public BookingSummary(Booking booking, String showName, String venueName,
                           LocalDateTime showTime, int ticketCount) {
        this.booking = booking;
        this.showName = showName;
        this.venueName = venueName;
        this.showTime = showTime;
        this.ticketCount = ticketCount;
    }

    public Booking getBooking() {
        return booking;
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
