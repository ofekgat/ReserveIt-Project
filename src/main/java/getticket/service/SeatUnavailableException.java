package getticket.service;

import java.util.Collections;
import java.util.List;

/** Thrown when one or more requested seats are already booked for the event instance. */
public class SeatUnavailableException extends Exception {

    private final List<Integer> unavailableSeatIds;

    public SeatUnavailableException(List<Integer> unavailableSeatIds) {
        super("Seats already booked: " + unavailableSeatIds);
        this.unavailableSeatIds = Collections.unmodifiableList(unavailableSeatIds);
    }

    public List<Integer> getUnavailableSeatIds() {
        return unavailableSeatIds;
    }
}
