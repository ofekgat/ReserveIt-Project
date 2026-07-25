package getticket.dao.impl;

import getticket.dao.EventInstanceDao;
import getticket.model.EventInstance;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventInstanceDaoImpl implements EventInstanceDao {

    private static final String INSERT_SQL =
            "INSERT INTO Event_Instances (Sid, Vid, Start_time, Ticket_price, Available_tickets, Event_Status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Event_Instances WHERE Instance_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Event_Instances";
    private static final String SELECT_BY_SHOW_SQL =
            "SELECT * FROM Event_Instances WHERE Sid = ? ORDER BY Start_time";
    private static final String UPDATE_SQL =
            "UPDATE Event_Instances SET Sid = ?, Vid = ?, Start_time = ?, Ticket_price = ?, " +
            "Available_tickets = ?, Event_Status = ? WHERE Instance_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Event_Instances WHERE Instance_id = ?";
    private static final String UPDATE_AVAILABLE_TICKETS_SQL =
            "UPDATE Event_Instances SET Available_tickets = Available_tickets + ? " +
            "WHERE Instance_id = ? AND Available_tickets + ? >= 0";
    private static final String SELECT_BY_ID_FOR_UPDATE_SQL =
            "SELECT * FROM Event_Instances WHERE Instance_id = ? FOR UPDATE";

    @Override
    public int create(EventInstance instance) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
                }
                throw new SQLException("Creating event instance failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public EventInstance getById(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapRow(rs) : null;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<EventInstance> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<EventInstance> instances = new ArrayList<>();
                while (rs.next()) {
                    instances.add(mapRow(rs));
                }
                return instances;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<EventInstance> getInstancesByShow(int sid) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_SHOW_SQL)) {
                ps.setInt(1, sid);
                try (ResultSet rs = ps.executeQuery()) {
                    List<EventInstance> instances = new ArrayList<>();
                    while (rs.next()) {
                        instances.add(mapRow(rs));
                    }
                    return instances;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean updateAvailableTickets(int instanceId, int delta) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            return updateAvailableTickets(instanceId, delta, conn);
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean updateAvailableTickets(int instanceId, int delta, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_AVAILABLE_TICKETS_SQL)) {
            ps.setInt(1, delta);
            ps.setInt(2, instanceId);
            ps.setInt(3, delta);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public EventInstance getByIdForUpdate(int instanceId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_FOR_UPDATE_SQL)) {
            ps.setInt(1, instanceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public boolean update(EventInstance instance) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, instance.getSid());
                ps.setInt(2, instance.getVid());
                ps.setTimestamp(3, Timestamp.valueOf(instance.getStartTime()));
                ps.setDouble(4, instance.getTicketPrice());
                ps.setInt(5, instance.getAvailableTickets());
                ps.setString(6, instance.getEventStatus());
                ps.setInt(7, instance.getInstanceId());
                return ps.executeUpdate() > 0;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    private EventInstance mapRow(ResultSet rs) throws SQLException {
        return new EventInstance(
                rs.getInt("Instance_id"),
                rs.getInt("Sid"),
                rs.getInt("Vid"),
                rs.getTimestamp("Start_time").toLocalDateTime(),
                rs.getDouble("Ticket_price"),
                rs.getInt("Available_tickets"),
                rs.getString("Event_Status")
        );
    }
}
