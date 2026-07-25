package getticket.dao;

import getticket.model.User;

import java.sql.SQLException;

public interface UserDao extends GenericDao<User> {

    /** Looks up a user by login name; used to authenticate on sign-in. Returns null if not found. */
    User getByUsername(String uname) throws SQLException;
}
