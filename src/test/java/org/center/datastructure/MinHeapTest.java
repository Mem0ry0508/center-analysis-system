package org.center.datastructure;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinHeapTest {

    @Test
    void peekReturnsSmallestElement() {
        IHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        heap.insert(5);
        heap.insert(1);
        heap.insert(3);

        assertEquals(1, heap.peek());
    }

    @Test
    void removeReturnsElementsInAscendingOrder() {
        IHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        int[] values = {5, 1, 3, 9, 2, 8, 0, 7};
        for (int v : values) {
            heap.insert(v);
        }

        int previous = Integer.MIN_VALUE;
        int count = 0;
        while (!heap.isEmpty()) {
            int current = heap.remove();
            assertTrue(current >= previous);
            previous = current;
            count++;
        }
        assertEquals(values.length, count);
    }

    @Test
    void isEmptyAndSizeTrackHeapState() {
        IHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());

        heap.insert(10);
        heap.insert(20);
        assertEquals(2, heap.size());
        assertTrue(!heap.isEmpty());

        heap.remove();
        assertEquals(1, heap.size());
    }

    @Test
    void peekOnEmptyHeapThrows() {
        IHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        assertThrows(NoSuchElementException.class, heap::peek);
    }

    @Test
    void removeOnEmptyHeapThrows() {
        IHeap<Integer> heap = new MinHeap<Integer>(Comparator.naturalOrder());
        assertThrows(NoSuchElementException.class, heap::remove);
    }

    @Test
    void supportsCustomComparatorForNonComparablePriority() {
        record Alert(String name, int riskScore) {
        }
        IHeap<Alert> heap = new MinHeap<Alert>(Comparator.comparingInt(Alert::riskScore));
        heap.insert(new Alert("low", 10));
        heap.insert(new Alert("critical", 90));
        heap.insert(new Alert("medium", 50));

        assertEquals("low", heap.peek().name());
        assertEquals("low", heap.remove().name());
        assertEquals("medium", heap.remove().name());
        assertEquals("critical", heap.remove().name());
    }
}
