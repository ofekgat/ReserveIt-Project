package getticket.model;

import java.io.Serializable;

public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private int reviewId;
    private int sid;
    private int uid;
    private int rating; // 1-5, enforced by chk_reviews_rating in the schema
    private String comment;

    public Review() {
    }

    public Review(int sid, int uid, int rating, String comment) {
        this.sid = sid;
        this.uid = uid;
        this.rating = rating;
        this.comment = comment;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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

    @Override
    public String toString() {
        return "Review{reviewId=" + reviewId + ", sid=" + sid + ", uid=" + uid + ", rating=" + rating + "}";
    }
}
