package getticket.web;

import getticket.dao.LocationDao;
import getticket.dao.SeatDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.LocationDaoImpl;
import getticket.dao.impl.SeatDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.Location;
import getticket.model.Venue;
import getticket.service.SeatLayoutLockedException;
import getticket.service.VenueService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for adminVenues.xhtml: lets an admin enter new halls (venues)
 * and edit/remove existing ones. A venue always belongs to a Location
 * (city + address); the admin either picks an existing one or types a new
 * city/address inline, in which case a Location row is created first.
 *
 * Writes go through VenueService rather than VenueDao directly, because a
 * numbered venue also needs its Seats rows laid out — a Venues row on its own
 * would render an empty, unbookable seat map.
 */
@ManagedBean(name = "adminVenueBean")
@ViewScoped
public class AdminVenueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final VenueDao venueDao = new VenueDaoImpl();
    private final LocationDao locationDao = new LocationDaoImpl();
    private final SeatDao seatDao = new SeatDaoImpl();
    private final VenueService venueService = new VenueService();

    private List<Venue> venues = Collections.emptyList();
    private List<Location> locations = Collections.emptyList();
    private final Map<Integer, String> locationNameCache = new HashMap<>();
    private final Map<Integer, Integer> seatCountCache = new HashMap<>();

    // Create/edit form fields. editingVid == 0 means "creating a new venue".
    private int editingVid;
    private String vname;
    private boolean numbered = true;
    private int vcapacity;
    private int seatsPerRow = VenueService.DEFAULT_SEATS_PER_ROW;
    private Integer locationId;
    private String newCity;
    private String newAddress;

    public void loadAll() {
        try {
            venues = venueDao.getAll();
            locations = locationDao.getAll();
            locationNameCache.clear();
            for (Location location : locations) {
                locationNameCache.put(location.getLocationId(), describeLocation(location));
            }
            seatCountCache.clear();
            for (Venue venue : venues) {
                seatCountCache.put(venue.getVid(), seatDao.getSeatsByVenue(venue.getVid()).size());
            }
        } catch (SQLException e) {
            venues = new ArrayList<>();
            locations = new ArrayList<>();
            addErrorMessage("Could not load venues, please try again.");
        }
    }

    public String locationNameFor(int locationId) {
        return locationNameCache.getOrDefault(locationId, "Location #" + locationId);
    }

    /** How many Seats rows a venue actually has — 0 for general admission. */
    public int seatCountFor(int vid) {
        return seatCountCache.getOrDefault(vid, 0);
    }

    public void startCreate() {
        editingVid = 0;
        vname = null;
        numbered = true;
        vcapacity = 0;
        seatsPerRow = VenueService.DEFAULT_SEATS_PER_ROW;
        locationId = locations.isEmpty() ? null : locations.get(0).getLocationId();
        newCity = null;
        newAddress = null;
    }

    public void startEdit(Venue venue) {
        editingVid = venue.getVid();
        vname = venue.getVname();
        numbered = venue.isNumbered();
        vcapacity = venue.getVcapacity();
        seatsPerRow = VenueService.DEFAULT_SEATS_PER_ROW;
        locationId = venue.getLocationId();
        newCity = null;
        newAddress = null;
    }

    public void save() {
        if (vcapacity <= 0) {
            addErrorMessage("Capacity must be at least 1.");
            return;
        }

        try {
            int resolvedLocationId = resolveLocationId();
            if (resolvedLocationId == 0) {
                addErrorMessage("Choose an existing location or enter a new city and address.");
                return;
            }

            Venue venue = new Venue(resolvedLocationId, vname, numbered, vcapacity);
            if (editingVid == 0) {
                venueService.createVenue(venue, seatsPerRow);
                addInfoMessage(numbered
                        ? "Venue \"" + vname + "\" created with " + vcapacity + " numbered seats."
                        : "Venue \"" + vname + "\" created.");
            } else {
                venue.setVid(editingVid);
                venueService.updateVenue(venue, seatsPerRow);
                addInfoMessage("Venue \"" + vname + "\" updated.");
            }
            loadAll();
            startCreate();
        } catch (SeatLayoutLockedException e) {
            addErrorMessage("Cannot change this venue's seating: " + e.getSoldTickets()
                    + " ticket(s) have already been sold for its seats.");
        } catch (SQLException e) {
            addErrorMessage("Could not save the venue, please try again.");
        }
    }

    public void delete(int vid) {
        try {
            venueDao.delete(vid);
            addInfoMessage("Venue deleted.");
            loadAll();
        } catch (SQLException e) {
            addErrorMessage("Could not delete the venue — it may still have scheduled showtimes.");
        }
    }

    /** Creates a new Location from newCity/newAddress if provided, otherwise returns the selected locationId. */
    private int resolveLocationId() throws SQLException {
        if (newCity != null && !newCity.isBlank() && newAddress != null && !newAddress.isBlank()) {
            Location location = new Location(newCity.trim(), newAddress.trim());
            return locationDao.create(location);
        }
        return locationId != null ? locationId : 0;
    }

    private String describeLocation(Location location) {
        return location.getCity() + " — " + location.getAddress();
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public boolean isEditing() {
        return editingVid != 0;
    }

    public int getEditingVid() {
        return editingVid;
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

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getNewCity() {
        return newCity;
    }

    public void setNewCity(String newCity) {
        this.newCity = newCity;
    }

    public String getNewAddress() {
        return newAddress;
    }

    public void setNewAddress(String newAddress) {
        this.newAddress = newAddress;
    }
}
