package getticket.dao.impl;

import getticket.dao.ShowDao;
import getticket.model.Show;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShowDaoImpl implements ShowDao {

    private static final String INSERT_SQL =
            "INSERT INTO Shows (Sname, Description, Category, ImageUrl) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Shows WHERE Sid = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Shows";
    private static final String SELECT_BY_CATEGORY_SQL = "SELECT * FROM Shows WHERE Category = ?";
    private static final String SEARCH_BY_NAME_SQL = "SELECT * FROM Shows WHERE Sname LIKE ?";
    private static final String SELECT_BY_DATE_SQL =
            "SELECT DISTINCT s.* FROM Shows s " +
            "JOIN Event_Instances ei ON ei.Sid = s.Sid " +
            "WHERE DATE(ei.Start_time) = ?";
    private static final String UPDATE_SQL =
            "UPDATE Shows SET Sname = ?, Description = ?, Category = ?, ImageUrl = ? WHERE Sid = ?";
    private static final String DELETE_SQL = "DELETE FROM Shows WHERE Sid = ?";

    @Override
    public int create(Show show) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
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
                }
                throw new SQLException("Creating show failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Show getById(int id) throws SQLException {
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
    public List<Show> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Show> shows = new ArrayList<>();
                while (rs.next()) {
                    shows.add(mapRow(rs));
                }
                return shows;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Show> getShowsByCategory(String category) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CATEGORY_SQL)) {
                ps.setString(1, category);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Show> shows = new ArrayList<>();
                    while (rs.next()) {
                        shows.add(mapRow(rs));
                    }
                    return shows;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Show> searchByName(String keyword) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SEARCH_BY_NAME_SQL)) {
                ps.setString(1, "%" + keyword + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    List<Show> shows = new ArrayList<>();
                    while (rs.next()) {
                        shows.add(mapRow(rs));
                    }
                    return shows;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Show> getShowsByDate(LocalDate date) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_DATE_SQL)) {
                ps.setDate(1, Date.valueOf(date));
                try (ResultSet rs = ps.executeQuery()) {
                    List<Show> shows = new ArrayList<>();
                    while (rs.next()) {
                        shows.add(mapRow(rs));
                    }
                    return shows;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Show show) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setString(1, show.getSname());
                ps.setString(2, show.getDescription());
                ps.setString(3, show.getCategory());
                ps.setString(4, show.getImageUrl());
                ps.setInt(5, show.getSid());
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

    private Show mapRow(ResultSet rs) throws SQLException {
        return new Show(
                rs.getInt("Sid"),
                rs.getString("Sname"),
                rs.getString("Description"),
                rs.getString("Category"),
                rs.getString("ImageUrl")
        );
    }
}
