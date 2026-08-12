package getticket.model;

import java.io.Serializable;

/**
 * Plain data holder for one row in the Users table.
 * No logic here — mapping rows to objects and back happens in UserDAOImpl.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;
    private String uname;
    private String password; // stores a HASH, never plaintext
    private String email;
    private String role;

    public User() {
    }

    public User(String uname, String password, String email, String role) {
        this.uname = uname;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{uid=" + uid + ", uname='" + uname + "', email='" + email + "', role=" + role + "}";
    }
}
