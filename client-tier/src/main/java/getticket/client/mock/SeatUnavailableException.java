package getticket.client.mock;

import java.util.List;

/**
 * Thrown by MockData.bookSeats() when one or more requested seats were
 * already taken. Mirrors the shape the real BookingService throws, so
 * CheckoutBean's error handling is a straight port once the server tier
 * is wired in.
 */
public class SeatUnavailableException extends Exception {

    private final List<Integer> unavailableSeatIds;

    public SeatUnavailableException(List<Integer> unavailableSeatIds) {
        super("Seats already booked: " + unavailableSeatIds);
        this.unavailableSeatIds = unavailableSeatIds;
    }

    public List<Integer> getUnavailableSeatIds() {
        return unavailableSeatIds;
    }
}
