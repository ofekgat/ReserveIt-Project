package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.EventInstance;
import getticket.client.model.Show;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for the show catalog: search/browse shows by category, date or
 * name (catalog.xhtml), then show a single show's scheduled instances
 * (showDetails.xhtml). Same shape as the server tier's real CatalogBean, but
 * every lookup goes through MockData instead of ShowDao/EventInstanceDao —
 * no SQLException to catch, since there is no database.
 */
@ManagedBean(name = "catalogBean")
@ViewScoped
public class CatalogBean implements Serializable {

    private static final long serialVersionUID = 1L;

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
        shows = MockData.getAllShows();
    }

    public void searchByCategory() {
        shows = MockData.getShowsByCategory(categoryFilter);
    }

    public void searchByName() {
        shows = MockData.searchShowsByName(nameFilter);
    }

    public void searchByDate() {
        shows = MockData.getShowsByDate(dateFilter);
    }

    /** Bound to showDetails.xhtml's f:viewAction; loads the show named by the sid view param. */
    public String loadShowDetails() {
        selectedShow = MockData.getShowById(sid);
        if (selectedShow == null) {
            return "/catalog?faces-redirect=true";
        }
        instancesForSelectedShow = MockData.getInstancesByShow(sid);
        return null;
    }

    /** Venue name for display next to an EventInstance. */
    public String venueNameFor(int vid) {
        return MockData.venueNameFor(vid);
    }

    /** Average rating (0-5) for the currently selected show, for showDetails.xhtml. */
    public double getAverageRating() {
        return selectedShow != null ? MockData.averageRating(selectedShow.getSid()) : 0;
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
