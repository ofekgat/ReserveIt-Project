package getticket.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Common CRUD contract shared by every entity-specific DAO.
 * All primary keys in this schema are ints, so no generic ID type is needed.
 */
public interface GenericDao<T> {

    int create(T entity) throws SQLException;

    T getById(int id) throws SQLException;

    List<T> getAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(int id) throws SQLException;
}
