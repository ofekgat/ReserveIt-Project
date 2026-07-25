package getticket.model;

import java.io.Serializable;

public class Show implements Serializable {

    private static final long serialVersionUID = 1L;

    private int sid;
    private String sname;
    private String description;
    private String category;
    private String imageUrl;

    public Show() {
    }

    public Show(String sname, String description, String category, String imageUrl) {
        this.sname = sname;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public Show(int sid, String sname, String description, String category, String imageUrl) {
        this.sid = sid;
        this.sname = sname;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Show{" +
                "sid=" + sid +
                ", sname='" + sname + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
