package getticket.model;

import java.io.Serializable;

public class Venue implements Serializable {

    private static final long serialVersionUID = 1L;

    private int vid;
    private int locationId;
    private String vname;
    private boolean isNumbered;
    private int vcapacity;

    public Venue() {
    }

    public Venue(int locationId, String vname, boolean isNumbered, int vcapacity) {
        this.locationId = locationId;
        this.vname = vname;
        this.isNumbered = isNumbered;
        this.vcapacity = vcapacity;
    }

    public int getVid() { return vid; }
    public void setVid(int vid) { this.vid = vid; }

    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }

    public String getVname() { return vname; }
    public void setVname(String vname) { this.vname = vname; }

    public boolean isNumbered() { return isNumbered; }
    public void setNumbered(boolean numbered) { isNumbered = numbered; }

    public int getVcapacity() { return vcapacity; }
    public void setVcapacity(int vcapacity) { this.vcapacity = vcapacity; }

    @Override
    public String toString() {
        return "Venue{vid=" + vid + ", vname='" + vname + "', isNumbered=" + isNumbered + ", vcapacity=" + vcapacity + "}";
    }
}
