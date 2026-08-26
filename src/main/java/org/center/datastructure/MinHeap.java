package org.center.datastructure;

import java.util.Comparator;

/**
 * 自訂 Min Heap，array-based binary heap，peek/remove 回傳依 comparator 排序最小的元素。
 * 用於依到期日、風險值或嚴重度排序警示（Alert）：comparator 值越小代表優先度越高。
 */
public class MinHeap<T> extends BinaryHeap<T> {

    public MinHeap(Comparator<T> comparator) {
        super(comparator);
    }
}
