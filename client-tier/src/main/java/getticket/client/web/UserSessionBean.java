package getticket.client.web;

import getticket.client.mock.MockData;
import getticket.client.model.User;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Backing bean for login/registration screens. Holds the signed-in User for
 * the lifetime of the HTTP session, so other beans can read who is logged in.
 *
 * Talks to MockData instead of a UserDao — a plaintext password check, not
 * PasswordUtil. Same public method names/signatures as the server tier's
 * real UserSessionBean so login.xhtml and template.xhtml don't need to change.
 */
@ManagedBean(name = "userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    static final String SESSION_UID_ATTRIBUTE = "uid";

    // Login/registration form fields.
    private String uname;
    private String password;
    private String email;

    private User currentUser;

    public String login() {
        User user = MockData.getUserByUsername(uname);
        if (user == null || !user.getPassword().equals(password)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Invalid username or password.");
            return null;
        }
        this.currentUser = user;
        markLoggedIn();
        clearForm();
        return "/catalog?faces-redirect=true";
    }

    public String register() {
        User user = MockData.registerUser(uname, password, email);
        if (user == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", "Username is already taken.");
            return null;
        }
        this.currentUser = user;
        markLoggedIn();
        clearForm();
        return "/catalog?faces-redirect=true";
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    private void markLoggedIn() {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put(SESSION_UID_ATTRIBUTE, currentUser.getUid());
    }

    private void clearForm() {
        password = null;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
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

    public User getCurrentUser() {
        return currentUser;
    }
}
