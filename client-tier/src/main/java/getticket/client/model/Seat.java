package getticket.client.model;

public class Seat {

    private int seatId;
    private int vid;
    private int rowNum;
    private int seatNum;

    public Seat() {
    }

    public Seat(int seatId, int vid, int rowNum, int seatNum) {
        this.seatId = seatId;
        this.vid = vid;
        this.rowNum = rowNum;
        this.seatNum = seatNum;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }
}
