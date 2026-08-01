package getticket.dao.impl;

import getticket.dao.UserDao;
import getticket.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl extends BaseDao implements UserDao {

    private static final String COLS = "Uid, Uname, Password, Email, Role";

    @Override
    public int create(User user) throws SQLException {
        return withConnection(conn -> create(user, conn));
    }

    @Override
    public int create(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO Users (Uname, Password, Email, Role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUname());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    user.setUid(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public User getById(int uid) throws SQLException {
        return withConnection(conn -> getById(uid, conn));
    }

    @Override
    public User getById(int uid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Users WHERE Uid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public User getByUsername(String uname) throws SQLException {
        return withConnection(conn -> getByUsername(uname, conn));
    }

    @Override
    public User getByUsername(String uname, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Users WHERE Uname = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uname);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<User> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Users ORDER BY Uid";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public boolean update(User user) throws SQLException {
        return withConnection(conn -> update(user, conn));
    }

    @Override
    public boolean update(User user, Connection conn) throws SQLException {
        // Password is excluded on purpose — changing it should go through
        // a dedicated method so it can't be overwritten by accident.
        String sql = "UPDATE Users SET Uname = ?, Email = ?, Role = ? WHERE Uid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUname());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getUid());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int uid) throws SQLException {
        return withConnection(conn -> delete(uid, conn));
    }

    @Override
    public boolean delete(int uid, Connection conn) throws SQLException {
        String sql = "DELETE FROM Users WHERE Uid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            return ps.executeUpdate() == 1;
        }
    }

    private List<User> mapRows(ResultSet rs) throws SQLException {
        List<User> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUid(rs.getInt("Uid"));
        user.setUname(rs.getString("Uname"));
        user.setPassword(rs.getString("Password"));
        user.setEmail(rs.getString("Email"));
        user.setRole(rs.getString("Role"));
        return user;
    }
}
