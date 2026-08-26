package org.center.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinarySearchTest {

    private final List<Integer> sorted = List.of(1, 3, 5, 7, 9, 11, 13, 15);

    @Test
    void findsElementAtStartMiddleAndEnd() {
        assertEquals(0, BinarySearch.search(sorted, 1, Comparator.naturalOrder()));
        assertEquals(3, BinarySearch.search(sorted, 7, Comparator.naturalOrder()));
        assertEquals(7, BinarySearch.search(sorted, 15, Comparator.naturalOrder()));
    }

    @Test
    void returnsMinusOneWhenNotFound() {
        assertEquals(-1, BinarySearch.search(sorted, 4, Comparator.naturalOrder()));
        assertEquals(-1, BinarySearch.search(sorted, 0, Comparator.naturalOrder()));
        assertEquals(-1, BinarySearch.search(sorted, 100, Comparator.naturalOrder()));
    }

    @Test
    void returnsMinusOneOnEmptyList() {
        assertEquals(-1, BinarySearch.search(List.of(), 1, Comparator.naturalOrder()));
    }

    @Test
    void handlesSingleElementList() {
        assertEquals(0, BinarySearch.search(List.of(42), 42, Comparator.naturalOrder()));
        assertEquals(-1, BinarySearch.search(List.of(42), 1, Comparator.naturalOrder()));
    }

    @Test
    void findsAValidIndexWhenDuplicatesExist() {
        List<Integer> withDuplicates = List.of(1, 2, 2, 2, 3, 4);
        int index = BinarySearch.search(withDuplicates, 2, Comparator.naturalOrder());
        assertTrue(index >= 1 && index <= 3);
    }
}
