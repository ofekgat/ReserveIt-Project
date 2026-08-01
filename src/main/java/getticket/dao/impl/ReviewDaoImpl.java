package getticket.dao.impl;

import getticket.dao.ReviewDao;
import getticket.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDaoImpl extends BaseDao implements ReviewDao {

    private static final String COLS = "Review_id, Sid, Uid, Rating, Comment";

    @Override
    public int create(Review review) throws SQLException {
        return withConnection(conn -> create(review, conn));
    }

    @Override
    public int create(Review review, Connection conn) throws SQLException {
        String sql = "INSERT INTO Reviews (Sid, Uid, Rating, Comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                throw new SQLException("Insert succeeded but no generated key was returned.");
            }
        }
    }

    @Override
    public Review getById(int reviewId) throws SQLException {
        return withConnection(conn -> getById(reviewId, conn));
    }

    @Override
    public Review getById(int reviewId, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Reviews WHERE Review_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Review> getReviewsByShow(int sid) throws SQLException {
        return withConnection(conn -> getReviewsByShow(sid, conn));
    }

    @Override
    public List<Review> getReviewsByShow(int sid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Reviews WHERE Sid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sid);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public List<Review> getReviewsByUser(int uid) throws SQLException {
        return withConnection(conn -> getReviewsByUser(uid, conn));
    }

    @Override
    public List<Review> getReviewsByUser(int uid, Connection conn) throws SQLException {
        String sql = "SELECT " + COLS + " FROM Reviews WHERE Uid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        }
    }

    @Override
    public boolean update(Review review) throws SQLException {
        return withConnection(conn -> update(review, conn));
    }

    @Override
    public boolean update(Review review, Connection conn) throws SQLException {
        String sql = "UPDATE Reviews SET Rating = ?, Comment = ? WHERE Review_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, review.getRating());
            ps.setString(2, review.getComment());
            ps.setInt(3, review.getReviewId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int reviewId) throws SQLException {
        return withConnection(conn -> delete(reviewId, conn));
    }

    @Override
    public boolean delete(int reviewId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Reviews WHERE Review_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Review> mapRows(ResultSet rs) throws SQLException {
        List<Review> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("Review_id"));
        review.setSid(rs.getInt("Sid"));
        review.setUid(rs.getInt("Uid"));
        review.setRating(rs.getInt("Rating"));
        review.setComment(rs.getString("Comment"));
        return review;
    }
}
