package getticket.client.mock;

/**
 * Thrown by MockData.bookGeneralAdmission() when fewer tickets are left
 * than requested. Mirrors the shape the real BookingService throws.
 */
public class InsufficientTicketsException extends Exception {

    private final int requested;
    private final int available;

    public InsufficientTicketsException(int requested, int available) {
        super("Requested " + requested + " tickets, only " + available + " available");
        this.requested = requested;
        this.available = available;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
