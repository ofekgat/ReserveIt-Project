package getticket.model;

import java.io.Serializable;

public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    private int seatId;
    private int vid;
    private int rowNum;
    private int seatNum;

    public Seat() {
    }

    public Seat(int vid, int rowNum, int seatNum) {
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

    @Override
    public String toString() {
        return "Seat{seatId=" + seatId + ", row=" + rowNum + ", seat=" + seatNum + "}";
    }
}
