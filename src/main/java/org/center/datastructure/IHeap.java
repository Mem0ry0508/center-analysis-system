package org.center.datastructure;

/**
 * 自訂 Min/Max Heap 介面。
 * 用於依到期日、風險值或嚴重度排序警示（Alert）。
 */
public interface IHeap<T> {
    void insert(T item);
    T peek();
    T remove();
    boolean isEmpty();
    int size();
}
