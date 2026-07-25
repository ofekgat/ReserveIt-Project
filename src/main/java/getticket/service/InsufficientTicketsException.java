package getticket.service;

/** Thrown when a general-admission checkout requests more tickets than are still available. */
public class InsufficientTicketsException extends Exception {

    private final int requested;
    private final int available;

    public InsufficientTicketsException(int requested, int available) {
        super("Requested " + requested + " tickets but only " + available + " are available");
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
