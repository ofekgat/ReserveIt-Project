package getticket.dao;

import getticket.model.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    int create(User user) throws SQLException;
    int create(User user, Connection conn) throws SQLException;

    User getById(int uid) throws SQLException;
    User getById(int uid, Connection conn) throws SQLException;

    /** Used for login. Returns null if no such username exists. */
    User getByUsername(String uname) throws SQLException;
    User getByUsername(String uname, Connection conn) throws SQLException;

    List<User> getAll() throws SQLException;
    List<User> getAll(Connection conn) throws SQLException;

    boolean update(User user) throws SQLException;
    boolean update(User user, Connection conn) throws SQLException;

    boolean delete(int uid) throws SQLException;
    boolean delete(int uid, Connection conn) throws SQLException;
}
