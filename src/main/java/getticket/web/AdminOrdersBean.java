package getticket.web;

import getticket.dao.BookingDao;
import getticket.dao.impl.BookingDaoImpl;
import getticket.service.OrderReportService;
import getticket.service.OrderRow;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for adminOrders.xhtml: order tracking across every customer.
 * The rows themselves come from OrderReportService, which the reports screen
 * shares; this bean only adds the per-order status editing on top.
 */
@ManagedBean(name = "adminOrdersBean")
@ViewScoped
public class AdminOrdersBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> STATUS_OPTIONS =
            Arrays.asList("PENDING", "CONFIRMED", "PAID", "CANCELLED");

    private final BookingDao bookingDao = new BookingDaoImpl();
    private final OrderReportService reportService = new OrderReportService();

    private List<OrderRow> orders = Collections.emptyList();

    public void loadAll() {
        try {
            orders = reportService.loadOrders();
        } catch (SQLException e) {
            orders = new ArrayList<>();
            addErrorMessage("Could not load orders, please try again.");
        }
    }

    public void updateStatus(OrderRow row) {
        try {
            bookingDao.update(row.getBooking());
            addInfoMessage("Order #" + row.getBooking().getBookingId() + " updated to " + row.getBooking().getStatus() + ".");
        } catch (SQLException e) {
            addErrorMessage("Could not update the order, please try again.");
        }
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DISPLAY_FORMAT);
    }

    private void addErrorMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    private void addInfoMessage(String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", detail));
    }

    public List<OrderRow> getOrders() {
        return orders;
    }

    public List<String> getStatusOptions() {
        return STATUS_OPTIONS;
    }
}
