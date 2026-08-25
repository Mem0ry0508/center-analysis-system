package org.center.algorithm;

import java.util.Comparator;
import java.util.List;

/**
 * Linear Search，O(n)，不要求資料已排序。
 * 作為 {@link BinarySearch} 效能對照的基準線（見個人報告 100/1000/10000 筆效能比較）。
 */
public final class LinearSearch {

    private LinearSearch() {
    }

    public static <T> int search(List<T> list, T target, Comparator<T> comparator) {
        for (int i = 0; i < list.size(); i++) {
            if (comparator.compare(list.get(i), target) == 0) {
                return i;
            }
        }
        return -1;
    }
}
