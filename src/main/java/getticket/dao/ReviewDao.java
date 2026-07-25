package getticket.dao;

import getticket.model.Review;

import java.sql.SQLException;
import java.util.List;

public interface ReviewDao extends GenericDao<Review> {

    /** Reviews written for a given show, for display on the show's page. */
    List<Review> getReviewsByShow(int sid) throws SQLException;
}
