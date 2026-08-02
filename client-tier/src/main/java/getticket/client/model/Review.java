package getticket.client.model;

public class Review {

    private int reviewId;
    private int sid;
    private int uid;
    private String uname; // denormalized for display; the real DAO would join Users instead
    private int rating; // 1-5
    private String comment;

    public Review() {
    }

    public Review(int reviewId, int sid, int uid, String uname, int rating, String comment) {
        this.reviewId = reviewId;
        this.sid = sid;
        this.uid = uid;
        this.uname = uname;
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

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
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
