package dk.ek.persistence.daos;

import java.util.Set;

/**
 * Purpose: This is an interface for making a DAO (Data Access Object) that can be used to perform CRUD operations on any entity.
 * Author: Thomas Hartmann
 * @param <T>
 */
interface IDAO<T> {
    T findById(Long id);
    Set<T> getAll();
    T create(T t);
    T update(T t);
    void delete(Long id);
}