package getticket.web;

import getticket.dao.EventInstanceDao;
import getticket.dao.ShowDao;
import getticket.dao.VenueDao;
import getticket.dao.impl.EventInstanceDaoImpl;
import getticket.dao.impl.ShowDaoImpl;
import getticket.dao.impl.VenueDaoImpl;
import getticket.model.EventInstance;
import getticket.model.Show;
import getticket.model.Venue;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for adminShows.xhtml: lets an admin enter new shows and, for
 * a selected show, schedule/edit/remove its Event_Instances (the actual
 * showtimes customers book — a show, at a venue, at a specific time/price).
 * The instances panel for a show is toggled with f:ajax so it opens without
 * a full page reload.
 */
@ManagedBean(name = "adminShowBean")
@ViewScoped
public class AdminShowBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATETIME_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ShowDao showDao = new ShowDaoImpl();
    private final EventInstanceDao eventInstanceDao = new EventInstanceDaoImpl();
    private final VenueDao venueDao = new VenueDaoImpl();

    private List<Show> shows = Collections.emptyList();
    private List<Venue> venues = Collections.emptyList();

    // Show create/edit form. editingSid == 0 means "creating a new show".
    private int editingSid;
    private String sname;
    private String description;
    private String category;
    private String imageUrl;

    // Which show's instances panel is expanded, and its instances.
    private int expandedSid;
    private List<EventInstance> instancesForExpandedShow = Collections.emptyList();

    // Instance create/edit form. editingInstanceId == 0 means "creating a new instance".
    private int editingInstanceId;
    private Integer instanceVid;
    private String startTimeText;
    private double ticketPrice;
    private int availableTickets;
    private String eventStatus = "SCHEDULED";

    public void loadAll() {
        try {
            shows = showDao.getAll();
            venues = venueDao.getAll();
        } catch (SQLException e) {
            shows = new ArrayList<>();
            venues = new ArrayList<>();
            addErrorMessage("Could not load shows, please try again.");
        }
    }

    // ---- Shows ----

    public void startCreateShow() {
        editingSid = 0;
        sname = null;
        description = null;
        category = null;
        imageUrl = null;
    }

    public void startEditShow(Show show) {
        editingSid = show.getSid();
        sname = show.getSname();
        description = show.getDescription();
        category = show.getCategory();
        imageUrl = show.getImageUrl();
    }

    public void saveShow() {
        try {
            Show show = new Show(sname, description, category, imageUrl);
            if (editingSid == 0) {
                showDao.create(show);
                addInfoMessage("Show \"" + sname + "\" created.");
            } else {
                show.setSid(editingSid);
                showDao.update(show);
                addInfoMessage("Show \"" + sname + "\" updated.");
            }
            loadAll();
            startCreateShow();
        } catch (SQLException e) {
            addErrorMessage("Could not save the show, please try again.");
        }
    }

    public void deleteShow(int sid) {
        try {
            showDao.delete(sid);
            addInfoMessage("Show deleted.");
            if (expandedSid == sid) {
                expandedSid = 0;
                instancesForExpandedShow = Collections.emptyList();
            }
            loadAll();
        } catch (SQLException e) {
            addErrorMessage("Could not delete the show — it may still have bookings or showtimes.");
        }
    }

    // ---- Event instances (showtimes) ----

    /** Bound to an f:ajax command: expands/collapses the instances panel for a show without a full page reload. */
    public void toggleInstances(int sid) {
        if (expandedSid == sid) {
            expandedSid = 0;
            instancesForExpandedShow = Collections.emptyList();
            return;
        }
        expandedSid = sid;
        startCreateInstance();
        loadInstancesForExpandedShow();
    }

    private void loadInstancesForExpandedShow() {
        try {
            instancesForExpandedShow = eventInstanceDao.getInstancesByShow(expandedSid);
        } catch (SQLException e) {
            instancesForExpandedShow = new ArrayList<>();
            addErrorMessage("Could not load showtimes, please try again.");
        }
    }

    public void startCreateInstance() {
        editingInstanceId = 0;
        instanceVid = venues.isEmpty() ? null : venues.get(0).getVid();
        startTimeText = null;
        ticketPrice = 0;
        // Seeded from the venue's capacity, never 0: an instance with zero available
        // tickets can't be booked at all, since checkout refuses to push the count negative.
        availableTickets = capacityOf(instanceVid);
        eventStatus = "SCHEDULED";
    }

    /** Capacity of the given venue, or 0 when it is unknown. */
    private int capacityOf(Integer vid) {
        if (vid == null) {
            return 0;
        }
        for (Venue venue : venues) {
            if (venue.getVid() == vid) {
                return venue.getVcapacity();
            }
        }
        return 0;
    }

    /**
     * Bound to the venue dropdown's f:ajax: re-seeds the available-ticket count
     * from the newly picked venue's capacity, so the admin isn't left with a
     * figure belonging to a different hall.
     */
    public void venueChanged() {
        if (editingInstanceId == 0) {
            availableTickets = capacityOf(instanceVid);
        }
    }

    public void startEditInstance(EventInstance instance) {
        editingInstanceId = instance.getInstanceId();
        instanceVid = instance.getVid();
        startTimeText = instance.getStartTime().format(DATETIME_INPUT_FORMAT);
        ticketPrice = instance.getTicketPrice();
        availableTickets = instance.getAvailableTickets();
        eventStatus = instance.getEventStatus();
    }

    public void saveInstance() {
        LocalDateTime startTime;
        try {
            startTime = LocalDateTime.parse(startTimeText, DATETIME_INPUT_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            addErrorMessage("Enter a valid showtime date and time.");
            return;
        }
        if (instanceVid == null) {
            addErrorMessage("Choose a venue for this showtime.");
            return;
        }
        if (ticketPrice < 0) {
            addErrorMessage("Ticket price cannot be negative.");
            return;
        }
        if (availableTickets <= 0) {
            addErrorMessage("Available tickets must be at least 1, otherwise nobody can book this showtime.");
            return;
        }
        int capacity = capacityOf(instanceVid);
        if (capacity > 0 && availableTickets > capacity) {
            addErrorMessage("Available tickets (" + availableTickets
                    + ") exceed the venue's capacity of " + capacity + ".");
            return;
        }

        try {
            EventInstance instance = new EventInstance(
                    expandedSid, instanceVid, startTime, ticketPrice, availableTickets, eventStatus);
            if (editingInstanceId == 0) {
                eventInstanceDao.create(instance);
                addInfoMessage("Showtime added.");
            } else {
                instance.setInstanceId(editingInstanceId);
                eventInstanceDao.update(instance);
                addInfoMessage("Showtime updated.");
            }
            loadInstancesForExpandedShow();
            startCreateInstance();
        } catch (SQLException e) {
            addErrorMessage("Could not save the showtime, please try again.");
        }
    }

    public void deleteInstance(int instanceId) {
        try {
            eventInstanceDao.delete(instanceId);
            addInfoMessage("Showtime deleted.");
            loadInstancesForExpandedShow();
        } catch (SQLException e) {
            addErrorMessage("Could not delete the showtime — it may already have bookings.");
        }
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
    }

    public String venueNameFor(int vid) {
        for (Venue venue : venues) {
            if (venue.getVid() == vid) {
                return venue.getVname();
            }
        }
        return "Venue #" + vid;
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public boolean isEditingShow() {
        return editingSid != 0;
    }

    public int getEditingSid() {
        return editingSid;
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

    public int getExpandedSid() {
        return expandedSid;
    }

    public List<EventInstance> getInstancesForExpandedShow() {
        return instancesForExpandedShow;
    }

    public boolean isEditingInstance() {
        return editingInstanceId != 0;
    }

    public Integer getInstanceVid() {
        return instanceVid;
    }

    public void setInstanceVid(Integer instanceVid) {
        this.instanceVid = instanceVid;
    }

    public String getStartTimeText() {
        return startTimeText;
    }

    public void setStartTimeText(String startTimeText) {
        this.startTimeText = startTimeText;
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
}
