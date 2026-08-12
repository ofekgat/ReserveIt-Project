package getticket.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    private int bookingId;
    private int uid;
    private LocalDateTime bookingTime;
    private double totalPrice;
    private String status;

    public Booking() {
    }

    public Booking(int uid, LocalDateTime bookingTime, double totalPrice, String status) {
        this.uid = uid;
        this.bookingTime = bookingTime;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Booking{bookingId=" + bookingId + ", uid=" + uid + ", totalPrice=" + totalPrice + ", status=" + status + "}";
    }
}
