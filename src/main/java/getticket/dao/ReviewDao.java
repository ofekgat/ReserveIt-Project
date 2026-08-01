package getticket.dao;

import getticket.model.Review;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ReviewDao {

    /** Rejected by uq_reviews_user_show if this user already reviewed this show. */
    int create(Review review) throws SQLException;
    int create(Review review, Connection conn) throws SQLException;

    Review getById(int reviewId) throws SQLException;
    Review getById(int reviewId, Connection conn) throws SQLException;

    List<Review> getReviewsByShow(int sid) throws SQLException;
    List<Review> getReviewsByShow(int sid, Connection conn) throws SQLException;

    List<Review> getReviewsByUser(int uid) throws SQLException;
    List<Review> getReviewsByUser(int uid, Connection conn) throws SQLException;

    boolean update(Review review) throws SQLException;
    boolean update(Review review, Connection conn) throws SQLException;

    boolean delete(int reviewId) throws SQLException;
    boolean delete(int reviewId, Connection conn) throws SQLException;
}
