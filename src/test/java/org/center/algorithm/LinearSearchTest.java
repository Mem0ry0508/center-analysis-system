package org.center.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinearSearchTest {

    private final List<Integer> unsorted = List.of(9, 3, 7, 1, 5, 3);

    @Test
    void findsElementRegardlessOfOrder() {
        assertEquals(0, LinearSearch.search(unsorted, 9, Comparator.naturalOrder()));
        assertEquals(2, LinearSearch.search(unsorted, 7, Comparator.naturalOrder()));
        assertEquals(4, LinearSearch.search(unsorted, 5, Comparator.naturalOrder()));
    }

    @Test
    void returnsMinusOneWhenNotFound() {
        assertEquals(-1, LinearSearch.search(unsorted, 100, Comparator.naturalOrder()));
    }

    @Test
    void returnsMinusOneOnEmptyList() {
        assertEquals(-1, LinearSearch.search(List.of(), 1, Comparator.naturalOrder()));
    }

    @Test
    void returnsFirstOccurrenceWhenDuplicatesExist() {
        assertEquals(1, LinearSearch.search(unsorted, 3, Comparator.naturalOrder()));
    }
}
