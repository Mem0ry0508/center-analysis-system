package org.center.datastructure;

/**
 * 自訂 Hash Table 介面。
 * 實作類別（例如 CustomHashTable）需自行處理節點、碰撞（chaining 或 open addressing）、
 * 查詢及刪除，不得直接包裝 java.util.HashMap。
 */
public interface IHashTable<K, V> {
    void put(K key, V value);
    V get(K key);
    boolean remove(K key);
    boolean containsKey(K key);
    int size();
}
