package org.center.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 自訂 Merge Sort（stable, O(n log n) worst/average/best case，額外 O(n) 空間）。
 * 不使用 Collections.sort/Arrays.sort。
 */
public final class MergeSort {

    private MergeSort() {
    }

    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list.size() < 2) {
            return;
        }
        List<T> sorted = mergeSort(list, comparator);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, sorted.get(i));
        }
    }

    private static <T> List<T> mergeSort(List<T> list, Comparator<T> comparator) {
        if (list.size() < 2) {
            return list;
        }
        int mid = list.size() / 2;
        List<T> left = mergeSort(new ArrayList<>(list.subList(0, mid)), comparator);
        List<T> right = mergeSort(new ArrayList<>(list.subList(mid, list.size())), comparator);
        return merge(left, right, comparator);
    }

    private static <T> List<T> merge(List<T> left, List<T> right, Comparator<T> comparator) {
        List<T> result = new ArrayList<>(left.size() + right.size());
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            if (comparator.compare(left.get(i), right.get(j)) <= 0) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        while (i < left.size()) {
            result.add(left.get(i++));
        }
        while (j < right.size()) {
            result.add(right.get(j++));
        }
        return result;
    }
}
