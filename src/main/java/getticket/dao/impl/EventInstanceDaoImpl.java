package getticket.dao.impl;

import getticket.dao.EventInstanceDao;
import getticket.model.EventInstance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventInstanceDaoImpl extends BaseDao implements EventInstanceDao {

    private static final String COLS =
            "Instance_id, Sid, Vid, Start_time, Ticket_price, Available_tickets, Event_Status";

    @Override
    public int create(EventInstance instance) throws SQLException {
        return withConnection(conn -> create(instance, conn));
    }

    @Override
    public int create(EventInstance instance, Connection conn) throws SQLException {
        String sql = "INSERT INTO Event_Instances " +
                "(Sid, Vid, Start_time, Ticket_price, Available_tickets, Event_Status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, instance.getSid());
            ps.setInt(2, instance.getVid());
            ps.setTimestamp(3, Timestamp.valueOf(instance.getStartTime()));
            ps.setDouble(4, instance.getTicketPrice());
            ps.setInt(5, instance.getAvailableTickets());
            ps.setString(6, instance.getEventStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    instance.setInstanceId(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public EventInstance getById(int instanceId) throws SQLException {
        return withConnection(conn -> getById(instanceId, conn));
    }

    @Override
    public EventInstance getById(int instanceId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Event_Instances WHERE Instance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public EventInstance getByIdForUpdate(int instanceId, Connection conn) throws SQLException {
        // FOR UPDATE takes an exclusive lock on this row until the
        // caller's transaction ends. A second checkout for the same
        // instance blocks here instead of reading stale availability.
        String sql = "SELECT " + COLS + " FROM Event_Instances WHERE Instance_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<EventInstance> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<EventInstance> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Event_Instances ORDER BY Start_time";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public List<EventInstance> getInstancesByShow(int sid) throws SQLException {
        return withConnection(conn -> getInstancesByShow(sid, conn));
    }

    @Override
    public List<EventInstance> getInstancesByShow(int sid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Event_Instances WHERE Sid = ? ORDER BY Start_time";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sid);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<java.time.LocalDate> getScheduledDates() throws SQLException {
        return withConnection(this::getScheduledDates);
    }

    @Override
    public List<java.time.LocalDate> getScheduledDates(Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT DATE(Start_time) AS Show_date FROM Event_Instances " +
                "WHERE Event_Status <> 'CANCELLED' ORDER BY Show_date";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<java.time.LocalDate> dates = new ArrayList<>();
            while (rs.next()) {
                dates.add(rs.getDate("Show_date").toLocalDate());
            }
            return dates;
        }
    }

    @Override
    public boolean update(EventInstance instance) throws SQLException {
        return withConnection(conn -> update(instance, conn));
    }

    @Override
    public boolean update(EventInstance instance, Connection conn) throws SQLException {
        String sql = "UPDATE Event_Instances SET Sid = ?, Vid = ?, Start_time = ?, " +
                "Ticket_price = ?, Available_tickets = ?, Event_Status = ? WHERE Instance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instance.getSid());
            ps.setInt(2, instance.getVid());
            ps.setTimestamp(3, Timestamp.valueOf(instance.getStartTime()));
            ps.setDouble(4, instance.getTicketPrice());
            ps.setInt(5, instance.getAvailableTickets());
            ps.setString(6, instance.getEventStatus());
            ps.setInt(7, instance.getInstanceId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int instanceId) throws SQLException {
        return withConnection(conn -> delete(instanceId, conn));
    }

    @Override
    public boolean delete(int instanceId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Event_Instances WHERE Instance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instanceId);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean updateAvailableTickets(int instanceId, int delta, Connection conn) throws SQLException {
        // "Available_tickets + ? >= 0" guards against overselling. For a
        // positive delta (a refund) it is always true. The check and the
        // write are one statement, so it holds under concurrency.
        String sql = "UPDATE Event_Instances SET Available_tickets = Available_tickets + ? " +
                "WHERE Instance_id = ? AND Available_tickets + ? >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, instanceId);
            ps.setInt(3, delta);
            return ps.executeUpdate() == 1;
        }
    }

    private List<EventInstance> mapRows(ResultSet rs) throws SQLException {
        List<EventInstance> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private EventInstance mapRow(ResultSet rs) throws SQLException {
        EventInstance instance = new EventInstance();
        instance.setInstanceId(rs.getInt("Instance_id"));
        instance.setSid(rs.getInt("Sid"));
        instance.setVid(rs.getInt("Vid"));
        instance.setStartTime(rs.getTimestamp("Start_time").toLocalDateTime());
        instance.setTicketPrice(rs.getDouble("Ticket_price"));
        instance.setAvailableTickets(rs.getInt("Available_tickets"));
        instance.setEventStatus(rs.getString("Event_Status"));
        return instance;
    }
}
