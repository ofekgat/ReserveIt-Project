package getticket.dao.impl;

import getticket.dao.ReviewDao;
import getticket.model.Review;
import getticket.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReviewDaoImpl implements ReviewDao {

    private static final String INSERT_SQL =
            "INSERT INTO Reviews (Sid, Uid, Rating, Comment) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM Reviews WHERE Review_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM Reviews";
    private static final String SELECT_BY_SHOW_SQL = "SELECT * FROM Reviews WHERE Sid = ?";
    private static final String UPDATE_SQL =
            "UPDATE Reviews SET Sid = ?, Uid = ?, Rating = ?, Comment = ? WHERE Review_id = ?";
    private static final String DELETE_SQL = "DELETE FROM Reviews WHERE Review_id = ?";

    @Override
    public int create(Review review) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, review.getSid());
                ps.setInt(2, review.getUid());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        review.setReviewId(id);
                        return id;
                    }
                }
                throw new SQLException("Creating review failed, no generated key obtained.");
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Review getById(int id) throws SQLException {
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
    public List<Review> getAll() throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
                 ResultSet rs = ps.executeQuery()) {
                List<Review> reviews = new ArrayList<>();
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
                return reviews;
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<Review> getReviewsByShow(int sid) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_SHOW_SQL)) {
                ps.setInt(1, sid);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Review> reviews = new ArrayList<>();
                    while (rs.next()) {
                        reviews.add(mapRow(rs));
                    }
                    return reviews;
                }
            }
        } finally {
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public boolean update(Review review) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionPool.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setInt(1, review.getSid());
                ps.setInt(2, review.getUid());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                ps.setInt(5, review.getReviewId());
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

    private Review mapRow(ResultSet rs) throws SQLException {
        return new Review(
                rs.getInt("Review_id"),
                rs.getInt("Sid"),
                rs.getInt("Uid"),
                rs.getInt("Rating"),
                rs.getString("Comment")
        );
    }
}
