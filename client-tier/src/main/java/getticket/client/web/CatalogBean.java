package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.EventInstance;
import getticket.client.model.Show;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

    // JSF 2.2's built-in converters predate java.time, so dates are parsed/formatted
    // by hand here instead of via <f:convertDateTime type="localDate/localDateTime">.
    private static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("EEE dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Search form fields (catalog.xhtml). The two dropdowns hold "" for "any".
    private String categoryFilter;
    private String nameFilter;
    private String dateFilterText;
    private List<Show> shows = Collections.emptyList();

    // Dropdown options, read from what is actually in the catalog.
    private List<String> categories = Collections.emptyList();
    private List<LocalDate> scheduledDates = Collections.emptyList();

    // Show-details view parameter and the data it loads (showDetails.xhtml).
    private int sid;
    private Show selectedShow;
    private List<EventInstance> instancesForSelectedShow = Collections.emptyList();

    /** Bound to catalog.xhtml's f:viewAction: fills the dropdowns and shows the whole catalog. */
    public void loadAllShows() {
        categories = MockData.getAllCategories();
        scheduledDates = MockData.getScheduledDates();
        applyFilters();
    }

    /**
     * Runs the catalog search with all three filters applied together, so
     * narrowing by category and by date compose instead of replacing each other.
     */
    public void applyFilters() {
        shows = MockData.searchShows(nameFilter, categoryFilter, parseDateFilter());
    }

    /** Resets every filter and shows the full catalog again. */
    public void clearFilters() {
        nameFilter = null;
        categoryFilter = null;
        dateFilterText = null;
        applyFilters();
    }

    /** The date dropdown submits "yyyy-MM-dd", or "" for "any date". */
    private LocalDate parseDateFilter() {
        if (dateFilterText == null || dateFilterText.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateFilterText, DATE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** True when at least one filter is narrowing the catalog — drives the "Clear" button. */
    public boolean isFiltered() {
        return (nameFilter != null && !nameFilter.isBlank())
                || (categoryFilter != null && !categoryFilter.isBlank())
                || (dateFilterText != null && !dateFilterText.isBlank());
    }

    public String dateOptionValue(LocalDate date) {
        return date.format(DATE_INPUT_FORMAT);
    }

    public String dateOptionLabel(LocalDate date) {
        return date.format(DATE_DISPLAY_FORMAT);
    }

    /** Formats an EventInstance's start time for display; used since JSF 2.2 has no java.time converter. */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
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

    public String getDateFilterText() {
        return dateFilterText;
    }

    public void setDateFilterText(String dateFilterText) {
        this.dateFilterText = dateFilterText;
    }

    public List<Show> getShows() {
        return shows;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<LocalDate> getScheduledDates() {
        return scheduledDates;
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
