package getticket.web;

import getticket.service.OrderReport;
import getticket.service.OrderReportService;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Backing bean for adminReports.xhtml: sales reports over the orders.
 *
 * The date filters are kept as Strings and parsed by hand rather than bound
 * through f:convertDateTime — JSF 2.2 has no java.time converters, so binding a
 * LocalDate directly fails at render time.
 */
@ManagedBean(name = "adminReportsBean")
@ViewScoped
public class AdminReportsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** What an <input type="date"> posts back. */
    private static final DateTimeFormatter DATE_INPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> STATUS_OPTIONS =
            Arrays.asList("PENDING", "CONFIRMED", "PAID", "CANCELLED");

    private final OrderReportService reportService = new OrderReportService();

    private String fromDateText;
    private String toDateText;
    private String statusFilter = "";

    private OrderReport report;

    /** Opens the screen on an all-time report, so there is something to look at straight away. */
    public void loadDefault() {
        generate();
    }

    public void generate() {
        LocalDate from = parseDate(fromDateText);
        if (isMalformed(fromDateText, from)) {
            FacesMessages.addError("Error", "The 'from' date is not a valid date.");
            return;
        }
        LocalDate to = parseDate(toDateText);
        if (isMalformed(toDateText, to)) {
            FacesMessages.addError("Error", "The 'to' date is not a valid date.");
            return;
        }
        if (from != null && to != null && to.isBefore(from)) {
            FacesMessages.addError("Error", "The 'to' date is before the 'from' date.");
            return;
        }

        try {
            report = reportService.buildReport(from, to, statusFilter);
        } catch (SQLException e) {
            report = null;
            FacesMessages.addError("Error", "Could not build the report, please try again.");
        }
    }

    /** Clears the filters and falls back to the all-time report. */
    public void reset() {
        fromDateText = null;
        toDateText = null;
        statusFilter = "";
        generate();
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DATE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** True when something was typed but it didn't parse — an empty box is fine, garbage isn't. */
    private boolean isMalformed(String text, LocalDate parsed) {
        return text != null && !text.trim().isEmpty() && parsed == null;
    }

    public OrderReport getReport() {
        return report;
    }

    public List<String> getStatusOptions() {
        return STATUS_OPTIONS;
    }

    public String getFromDateText() {
        return fromDateText;
    }

    public void setFromDateText(String fromDateText) {
        this.fromDateText = fromDateText;
    }

    public String getToDateText() {
        return toDateText;
    }

    public void setToDateText(String toDateText) {
        this.toDateText = toDateText;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }
}
