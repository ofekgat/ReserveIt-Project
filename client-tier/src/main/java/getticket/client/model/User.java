package getticket.client.model;

/**
 * Plain data holder for one demo user.
 *
 * The password field holds PLAINTEXT here — that is only acceptable because
 * this whole tier is a mock with no real security surface. The server tier's
 * User/PasswordUtil hash it properly; do not copy this class as-is once the
 * real DAO is wired in.
 */
public class User {

    private int uid;
    private String uname;
    private String password;
    private String email;
    private String role;

    public User() {
    }

    public User(int uid, String uname, String password, String email, String role) {
        this.uid = uid;
        this.uname = uname;
        this.password = password;
        this.email = email;
        this.role = role;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
