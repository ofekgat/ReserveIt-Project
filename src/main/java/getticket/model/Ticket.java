package getticket.model;

public class Ticket {

    private int ticketId;
    private int bookingId;
    private int instanceId;
    private Integer seatId; // null for general-admission tickets

    public Ticket() {
    }

    public Ticket(int bookingId, int instanceId, Integer seatId) {
        this.bookingId = bookingId;
        this.instanceId = instanceId;
        this.seatId = seatId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    @Override
    public String toString() {
        return "Ticket{ticketId=" + ticketId + ", bookingId=" + bookingId
                + ", instanceId=" + instanceId + ", seatId=" + seatId + "}";
    }
}
