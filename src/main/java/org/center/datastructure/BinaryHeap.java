package org.center.datastructure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Array-based binary heap 共用邏輯，供 MinHeap/MaxHeap 依 comparator 決定的優先順序繼承使用。
 * comparator.compare(a, b) &lt; 0 表示 a 優先於 b（先被 remove/peek 出來）。
 */
abstract class BinaryHeap<T> implements IHeap<T> {

    private final List<T> data = new ArrayList<>();
    private final Comparator<T> comparator;

    protected BinaryHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void insert(T item) {
        data.add(item);
        siftUp(data.size() - 1);
    }

    @Override
    public T peek() {
        if (data.isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        return data.get(0);
    }

    @Override
    public T remove() {
        if (data.isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        T root = data.get(0);
        T last = data.remove(data.size() - 1);
        if (!data.isEmpty()) {
            data.set(0, last);
            siftDown(0);
        }
        return root;
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int size() {
        return data.size();
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(data.get(index), data.get(parent)) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int index) {
        int n = data.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int highestPriority = index;

            if (left < n && comparator.compare(data.get(left), data.get(highestPriority)) < 0) {
                highestPriority = left;
            }
            if (right < n && comparator.compare(data.get(right), data.get(highestPriority)) < 0) {
                highestPriority = right;
            }
            if (highestPriority == index) {
                break;
            }
            swap(index, highestPriority);
            index = highestPriority;
        }
    }

    private void swap(int i, int j) {
        T tmp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, tmp);
    }
}
