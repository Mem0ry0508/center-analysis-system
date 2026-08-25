package org.center.datastructure;

import java.util.Comparator;

/**
 * 自訂 Max Heap，array-based binary heap，peek/remove 回傳依 comparator 排序最大的元素。
 * 內部以反轉 comparator 重用 BinaryHeap 的 sift 邏輯。
 */
public class MaxHeap<T> extends BinaryHeap<T> {

    public MaxHeap(Comparator<T> comparator) {
        super(comparator.reversed());
    }
}
