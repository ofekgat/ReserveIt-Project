package getticket.model;

import java.io.Serializable;

public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    private int locationId;
    private String city;
    private String address;

    public Location() {
    }

    public Location(String city, String address) {
        this.city = city;
        this.address = address;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Location{locationId=" + locationId + ", city='" + city + "', address='" + address + "'}";
    }
}
