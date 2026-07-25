package getticket.web;

import getticket.dao.UserDao;
import getticket.dao.impl.UserDaoImpl;
import getticket.model.User;
import getticket.util.PasswordUtil;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Backing bean for login/registration screens. Holds the signed-in User for
 * the lifetime of the HTTP session, so other beans can read who is logged in.
 */
@ManagedBean(name = "userSessionBean")
@SessionScoped
public class UserSessionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_ROLE = "CUSTOMER";

    /** Raw HttpSession attribute AuthFilter checks, since it runs before JSF would lazily create this bean. */
    static final String SESSION_UID_ATTRIBUTE = "uid";

    private final UserDao userDao = new UserDaoImpl();

    // Login/registration form fields.
    private String uname;
    private String password;
    private String email;

    private User currentUser;

    public String login() {
        try {
            User user = userDao.getByUsername(uname);
            if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Invalid username or password.");
                return null;
            }
            this.currentUser = user;
            markLoggedIn();
            clearForm();
            return "/catalog?faces-redirect=true";
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Login failed", "Unexpected error, please try again.");
            return null;
        }
    }

    public String register() {
        try {
            if (userDao.getByUsername(uname) != null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", "Username is already taken.");
                return null;
            }
            User user = new User(uname, PasswordUtil.hash(password), email, DEFAULT_ROLE);
            userDao.create(user);
            this.currentUser = user;
            markLoggedIn();
            clearForm();
            return "/catalog?faces-redirect=true";
        } catch (SQLException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", "Unexpected error, please try again.");
            return null;
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
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
