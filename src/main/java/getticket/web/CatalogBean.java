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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backing bean for the show catalog: search/browse shows by category, date or
 * name (catalog.xhtml), then show a single show's scheduled instances
 * (showDetails.xhtml). Since this bean is view-scoped, showDetails.xhtml loads
 * its own data from a "sid" view parameter rather than relying on state left
 * behind by catalog.xhtml (a redirect to a different view gets a fresh instance).
 */
@ManagedBean(name = "catalogBean")
@ViewScoped
public class CatalogBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ShowDao showDao = new ShowDaoImpl();
    private final EventInstanceDao eventInstanceDao = new EventInstanceDaoImpl();
    private final VenueDao venueDao = new VenueDaoImpl();
    private final Map<Integer, String> venueNameCache = new HashMap<>();

    // Search form fields (catalog.xhtml).
    private String categoryFilter;
    private String nameFilter;
    private LocalDate dateFilter;
    private List<Show> shows = Collections.emptyList();

    // Show-details view parameter and the data it loads (showDetails.xhtml).
    private int sid;
    private Show selectedShow;
    private List<EventInstance> instancesForSelectedShow = Collections.emptyList();

    public void loadAllShows() {
        try {
            shows = showDao.getAll();
        } catch (SQLException e) {
            shows = new ArrayList<>();
            addErrorMessage("Could not load shows, please try again.");
        }
    }

    public void searchByCategory() {
        try {
            shows = showDao.getShowsByCategory(categoryFilter);
        } catch (SQLException e) {
            shows = new ArrayList<>();
            addErrorMessage("Search failed, please try again.");
        }
    }

    public void searchByName() {
        try {
            shows = showDao.searchByName(nameFilter);
        } catch (SQLException e) {
            shows = new ArrayList<>();
            addErrorMessage("Search failed, please try again.");
        }
    }

    public void searchByDate() {
        try {
            shows = showDao.getShowsByDate(dateFilter);
        } catch (SQLException e) {
            shows = new ArrayList<>();
            addErrorMessage("Search failed, please try again.");
        }
    }

    /** Bound to showDetails.xhtml's f:viewAction; loads the show named by the sid view param. */
    public String loadShowDetails() {
        try {
            selectedShow = showDao.getById(sid);
            if (selectedShow == null) {
                addErrorMessage("Show not found.");
                return "/catalog?faces-redirect=true";
            }
            instancesForSelectedShow = eventInstanceDao.getInstancesByShow(sid);
            return null;
        } catch (SQLException e) {
            addErrorMessage("Could not load showtimes, please try again.");
            return "/catalog?faces-redirect=true";
        }
    }

    /** Venue name for display next to an EventInstance; falls back to a placeholder on error. */
    public String venueNameFor(int vid) {
        return venueNameCache.computeIfAbsent(vid, this::lookupVenueName);
    }

    private String lookupVenueName(int vid) {
        try {
            Venue venue = venueDao.getById(vid);
            return venue != null ? venue.getVname() : ("Venue #" + vid);
        } catch (SQLException e) {
            return "Venue #" + vid;
        }
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    public String getCategoryFilter() {
        return categoryFilter;
    }

    public void setCategoryFilter(String categoryFilter) {
        this.categoryFilter = categoryFilter;
    }

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    public LocalDate getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(LocalDate dateFilter) {
        this.dateFilter = dateFilter;
    }

    public List<Show> getShows() {
        return shows;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public Show getSelectedShow() {
        return selectedShow;
    }

    public List<EventInstance> getInstancesForSelectedShow() {
        return instancesForSelectedShow;
    }
}
