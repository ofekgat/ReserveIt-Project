package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.Review;
import getticket.client.model.Show;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Backing bean for reviews.xhtml — a screen that doesn't exist yet on the
 * server tier. Shows every review left for one show (sid view param) plus
 * its average rating, and lets a signed-in user add their own.
 *
 * One review per user per show, same as the Data tier's uq_reviews_user_show
 * constraint (enforced here in memory, since there is no DB to enforce it).
 */
@ManagedBean(name = "reviewBean")
@ViewScoped
public class ReviewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManagedProperty(value = "#{userSessionBean}")
    private UserSessionBean userSessionBean;

    private int sid;
    private Show show;
    private List<Review> reviews = Collections.emptyList();

    // New-review form fields.
    private int rating = 5;
    private String comment;

    public String loadReviews() {
        show = MockData.getShowById(sid);
        if (show == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Show not found.");
            return "/catalog?faces-redirect=true";
        }
        reviews = MockData.getReviewsByShow(sid);
        return null;
    }

    public String submitReview() {
        if (userSessionBean == null || !userSessionBean.isLoggedIn()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Please log in to leave a review.");
            return null;
        }
        int uid = userSessionBean.getCurrentUser().getUid();
        boolean alreadyReviewed = reviews.stream().anyMatch(r -> r.getUid() == uid);
        if (alreadyReviewed) {
            addMessage(FacesMessage.SEVERITY_ERROR, "You already reviewed this show.");
            return null;
        }
        MockData.addReview(sid, uid, userSessionBean.getCurrentUser().getUname(), rating, comment);
        reviews = MockData.getReviewsByShow(sid);
        comment = null;
        rating = 5;
        return null;
    }

    public double getAverageRating() {
        return MockData.averageRating(sid);
    }

    /** "★★★☆☆" style rendering for a 1-5 rating; used by both the review list and the form preview. */
    public String starsFor(int value) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            stars.append(i <= value ? '★' : '☆');
        }
        return stars.toString();
    }

    private void addMessage(FacesMessage.Severity severity, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, "Reviews", detail));
    }

    public void setUserSessionBean(UserSessionBean userSessionBean) {
        this.userSessionBean = userSessionBean;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public Show getShow() {
        return show;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
