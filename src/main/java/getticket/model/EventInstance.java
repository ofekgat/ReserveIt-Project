package getticket.model;

import java.time.LocalDateTime;

public class EventInstance {

    private int instanceId;
    private int sid;
    private int vid;
    private LocalDateTime startTime;
    private double ticketPrice;
    private int availableTickets;
    private String eventStatus;

    public EventInstance() {
    }

    public EventInstance(int sid, int vid, LocalDateTime startTime,
                          double ticketPrice, int availableTickets, String eventStatus) {
        this.sid = sid;
        this.vid = vid;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
        this.availableTickets = availableTickets;
        this.eventStatus = eventStatus;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    @Override
    public String toString() {
        return "EventInstance{instanceId=" + instanceId + ", sid=" + sid + ", vid=" + vid
                + ", startTime=" + startTime + ", availableTickets=" + availableTickets
                + ", status=" + eventStatus + "}";
    }
}
