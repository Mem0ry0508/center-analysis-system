package org.center.algorithm;

import java.util.Comparator;
import java.util.List;

/**
 * 對已排序 List 做 Binary Search，O(log n)。
 * 前提：list 已依同一個 comparator 遞增排序（例如先用 {@link MergeSort} 排序過）。
 * 用於跟 {@link LinearSearch} 做效能對照（見個人報告 100/1000/10000 筆效能比較）。
 */
public final class BinarySearch {

    private BinarySearch() {
    }

    public static <T> int search(List<T> sortedList, T target, Comparator<T> comparator) {
        int low = 0;
        int high = sortedList.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = comparator.compare(sortedList.get(mid), target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }
}
