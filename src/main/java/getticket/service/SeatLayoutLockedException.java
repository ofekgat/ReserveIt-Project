package getticket.service;

/**
 * Thrown when a venue's seating layout can no longer be changed because
 * tickets have already been sold for its seats — rebuilding the seat map
 * would orphan those tickets.
 */
public class SeatLayoutLockedException extends Exception {

    private final int soldTickets;

    public SeatLayoutLockedException(int soldTickets) {
        super("Seat layout is locked: " + soldTickets + " ticket(s) already sold for this venue");
        this.soldTickets = soldTickets;
    }

    public int getSoldTickets() {
        return soldTickets;
    }
}
