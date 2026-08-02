package getticket.client.model;

public class Venue {

    private int vid;
    private String vname;
    private boolean numbered;
    private int vcapacity;

    public Venue() {
    }

    public Venue(int vid, String vname, boolean numbered, int vcapacity) {
        this.vid = vid;
        this.vname = vname;
        this.numbered = numbered;
        this.vcapacity = vcapacity;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public String getVname() {
        return vname;
    }

    public void setVname(String vname) {
        this.vname = vname;
    }

    public boolean isNumbered() {
        return numbered;
    }

    public void setNumbered(boolean numbered) {
        this.numbered = numbered;
    }

    public int getVcapacity() {
        return vcapacity;
    }

    public void setVcapacity(int vcapacity) {
        this.vcapacity = vcapacity;
    }
}
