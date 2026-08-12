package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.Venue;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for adminVenues.xhtml: lets an admin enter new halls (venues)
 * and edit/remove existing ones, backed by MockData instead of VenueDao.
 * Same public shape as the server tier's real AdminVenueBean, minus the
 * Location concept — the client Venue model has no city/address (see
 * client/model/Venue.java), an existing simplification of this tier.
 */
@ManagedBean(name = "adminVenueBean")
@ViewScoped
public class AdminVenueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Venue> venues = Collections.emptyList();

    // Create/edit form fields. editingVid == 0 means "creating a new venue".
    private int editingVid;
    private String vname;
    private boolean numbered = true;
    private int vcapacity;
    private int seatsPerRow = MockData.DEFAULT_SEATS_PER_ROW;

    public void loadAll() {
        venues = MockData.getAllVenues();
    }

    /** How many seats a venue actually has — 0 for general admission. */
    public int seatCountFor(int vid) {
        return MockData.getSeatsByVenue(vid).size();
    }

    public void startCreate() {
        editingVid = 0;
        vname = null;
        numbered = true;
        vcapacity = 0;
        seatsPerRow = MockData.DEFAULT_SEATS_PER_ROW;
    }

    public void startEdit(Venue venue) {
        editingVid = venue.getVid();
        vname = venue.getVname();
        numbered = venue.isNumbered();
        vcapacity = venue.getVcapacity();
        seatsPerRow = MockData.DEFAULT_SEATS_PER_ROW;
    }

    public void save() {
        if (vcapacity <= 0) {
            addErrorMessage("Capacity must be at least 1.");
            return;
        }

        if (editingVid == 0) {
            MockData.createVenue(vname, numbered, vcapacity, seatsPerRow);
            addInfoMessage(numbered
                    ? "Venue \"" + vname + "\" created with " + vcapacity + " numbered seats."
                    : "Venue \"" + vname + "\" created.");
        } else {
            MockData.updateVenue(editingVid, vname, numbered, vcapacity, seatsPerRow);
            addInfoMessage("Venue \"" + vname + "\" updated.");
        }
        loadAll();
        startCreate();
    }

    public void delete(int vid) {
        MockData.deleteVenue(vid);
        addInfoMessage("Venue deleted.");
        loadAll();
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public boolean isEditing() {
        return editingVid != 0;
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
}
