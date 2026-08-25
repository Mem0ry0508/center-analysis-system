package org.center.algorithm;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergeSortTest {

    @Test
    void sortsRandomIntegersAscending() {
        Random random = new Random(42);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            list.add(random.nextInt(1000));
        }

        MergeSort.sort(list, Comparator.naturalOrder());

        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1) <= list.get(i));
        }
    }

    @Test
    void sortsWithCustomComparatorDescending() {
        List<Integer> list = new ArrayList<>(List.of(5, 1, 9, 3, 7));

        MergeSort.sort(list, Comparator.<Integer>naturalOrder().reversed());

        assertEquals(List.of(9, 7, 5, 3, 1), list);
    }

    @Test
    void handlesEmptyAndSingleElementLists() {
        List<Integer> empty = new ArrayList<>();
        MergeSort.sort(empty, Comparator.naturalOrder());
        assertEquals(List.of(), empty);

        List<Integer> single = new ArrayList<>(List.of(42));
        MergeSort.sort(single, Comparator.naturalOrder());
        assertEquals(List.of(42), single);
    }

    @Test
    void handlesAlreadySortedAndReverseSortedInput() {
        List<Integer> sorted = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        MergeSort.sort(sorted, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 3, 4, 5), sorted);

        List<Integer> reversed = new ArrayList<>(List.of(5, 4, 3, 2, 1));
        MergeSort.sort(reversed, Comparator.naturalOrder());
        assertEquals(List.of(1, 2, 3, 4, 5), reversed);
    }

    @Test
    void isStableForEqualKeys() {
        record Entry(int key, int originalIndex) {
        }
        List<Entry> list = new ArrayList<>(List.of(
                new Entry(1, 0),
                new Entry(2, 1),
                new Entry(1, 2),
                new Entry(2, 3),
                new Entry(1, 4)
        ));

        MergeSort.sort(list, Comparator.comparingInt(Entry::key));

        List<Integer> keyOneOriginalOrder = list.stream()
                .filter(e -> e.key() == 1)
                .map(Entry::originalIndex)
                .toList();
        assertEquals(List.of(0, 2, 4), keyOneOriginalOrder);

        List<Integer> keyTwoOriginalOrder = list.stream()
                .filter(e -> e.key() == 2)
                .map(Entry::originalIndex)
                .toList();
        assertEquals(List.of(1, 3), keyTwoOriginalOrder);
    }
}
