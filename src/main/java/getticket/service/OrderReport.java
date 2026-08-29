package getticket.service;

import java.io.Serializable;
import java.util.List;

/**
 * The result of one report run: the orders that matched the filters, the
 * headline totals for them, and the same totals broken down by show, by venue
 * and by status.
 *
 * Tickets and money skip CANCELLED orders — a cancelled seat was never sold, so
 * counting it would overstate both revenue and attendance. Order counts still
 * include them, and the cancelled tally is reported on its own.
 */
public class OrderReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<OrderRow> lines;
    private final List<Group> byShow;
    private final List<Group> byVenue;
    private final List<Group> byStatus;
    private final String rangeLabel;

    private final int totalOrders;
    private final int cancelledOrders;
    private final int ticketsSold;
    private final double revenue;

    OrderReport(List<OrderRow> lines, List<Group> byShow, List<Group> byVenue, List<Group> byStatus,
                String rangeLabel) {
        this.lines = lines;
        this.byShow = byShow;
        this.byVenue = byVenue;
        this.byStatus = byStatus;
        this.rangeLabel = rangeLabel;

        int cancelled = 0;
        int tickets = 0;
        double money = 0;
        for (OrderRow row : lines) {
            if (OrderReportService.isCancelled(row)) {
                cancelled++;
            } else {
                tickets += row.getTicketCount();
                money += row.getBooking().getTotalPrice();
            }
        }
        this.totalOrders = lines.size();
        this.cancelledOrders = cancelled;
        this.ticketsSold = tickets;
        this.revenue = money;
    }

    public List<OrderRow> getLines() {
        return lines;
    }

    public List<Group> getByShow() {
        return byShow;
    }

    public List<Group> getByVenue() {
        return byVenue;
    }

    public List<Group> getByStatus() {
        return byStatus;
    }

    /** Human-readable description of the filters this report was run with. */
    public String getRangeLabel() {
        return rangeLabel;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    /** Orders that still count as sales, i.e. everything that wasn't cancelled. */
    public int getSoldOrders() {
        return totalOrders - cancelledOrders;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getAverageOrderValue() {
        return getSoldOrders() == 0 ? 0 : revenue / getSoldOrders();
    }

    /** Totals for one show, venue or status within the report. */
    public static class Group implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String label;
        private int orders;
        private int cancelledOrders;
        private int tickets;
        private double revenue;

        Group(String label) {
            this.label = label;
        }

        void add(OrderRow row) {
            orders++;
            if (OrderReportService.isCancelled(row)) {
                cancelledOrders++;
            } else {
                tickets += row.getTicketCount();
                revenue += row.getBooking().getTotalPrice();
            }
        }

        public String getLabel() {
            return label;
        }

        public int getOrders() {
            return orders;
        }

        public int getCancelledOrders() {
            return cancelledOrders;
        }

        public int getTickets() {
            return tickets;
        }

        public double getRevenue() {
            return revenue;
        }
    }
}
