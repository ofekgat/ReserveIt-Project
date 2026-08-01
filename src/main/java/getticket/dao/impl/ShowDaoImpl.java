package getticket.dao.impl;

import getticket.dao.ShowDao;
import getticket.model.Show;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShowDaoImpl extends BaseDao implements ShowDao {

    private static final String COLS = "Sid, Sname, Description, Category, ImageUrl";

    @Override
    public int create(Show show) throws SQLException {
        return withConnection(conn -> create(show, conn));
    }

    @Override
    public int create(Show show, Connection conn) throws SQLException {
        String sql = "INSERT INTO Shows (Sname, Description, Category, ImageUrl) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, show.getSname());
            ps.setString(2, show.getDescription());
            ps.setString(3, show.getCategory());
            ps.setString(4, show.getImageUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    show.setSid(id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Show getById(int sid) throws SQLException {
        return withConnection(conn -> getById(sid, conn));
    }

    @Override
    public Show getById(int sid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Shows WHERE Sid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Show> getAll() throws SQLException {
        return withConnection(this::getAll);
    }

    @Override
    public List<Show> getAll(Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Shows ORDER BY Sname";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapRows(rs);
        }
    }

    @Override
    public List<Show> getShowsByCategory(String category) throws SQLException {
        return withConnection(conn -> getShowsByCategory(category, conn));
    }

    @Override
    public List<Show> getShowsByCategory(String category, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Shows WHERE Category = ? ORDER BY Sname";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Show> searchByName(String nameFragment) throws SQLException {
        return withConnection(conn -> searchByName(nameFragment, conn));
    }

    @Override
    public List<Show> searchByName(String nameFragment, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Shows WHERE Sname LIKE ? ORDER BY Sname";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // The wildcards go in the VALUE, not the SQL string — the
            // user's text stays a parameter and can't alter the query.
            ps.setString(1, "%" + (nameFragment == null ? "" : nameFragment) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Show> getShowsByDate(LocalDate date) throws SQLException {
        return withConnection(conn -> getShowsByDate(date, conn));
    }

    @Override
    public List<Show> getShowsByDate(LocalDate date, Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT s.Sid, s.Sname, s.Description, s.Category, s.ImageUrl " +
                "FROM Shows s JOIN Event_Instances ei ON ei.Sid = s.Sid " +
                "WHERE DATE(ei.Start_time) = ? ORDER BY s.Sname";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public boolean update(Show show) throws SQLException {
        return withConnection(conn -> update(show, conn));
    }

    @Override
    public boolean update(Show show, Connection conn) throws SQLException {
        String sql = "UPDATE Shows SET Sname = ?, Description = ?, Category = ?, ImageUrl = ? WHERE Sid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, show.getSname());
            ps.setString(2, show.getDescription());
            ps.setString(3, show.getCategory());
            ps.setString(4, show.getImageUrl());
            ps.setInt(5, show.getSid());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int sid) throws SQLException {
        return withConnection(conn -> delete(sid, conn));
    }

    @Override
    public boolean delete(int sid, Connection conn) throws SQLException {
        String sql = "DELETE FROM Shows WHERE Sid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sid);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Show> mapRows(ResultSet rs) throws SQLException {
        List<Show> shows = new ArrayList<>();
        while (rs.next()) {
            shows.add(mapRow(rs));
        }
        return shows;
    }

    private Show mapRow(ResultSet rs) throws SQLException {
        Show show = new Show();
        show.setSid(rs.getInt("Sid"));
        show.setSname(rs.getString("Sname"));
        show.setDescription(rs.getString("Description"));
        show.setCategory(rs.getString("Category"));
        show.setImageUrl(rs.getString("ImageUrl"));
        return show;
    }
}
