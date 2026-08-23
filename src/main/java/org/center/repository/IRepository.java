package org.center.repository;

import java.util.List;
import java.util.Optional;

/**
 * 共用 Repository 介面。所有 XxxRepository 實作此介面，
 * 內部一律使用 PreparedStatement，不得字串拼接 SQL。
 */
public interface IRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean update(T entity);
    boolean deleteById(ID id);
}
