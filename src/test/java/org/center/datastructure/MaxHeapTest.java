package org.center.datastructure;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxHeapTest {

    @Test
    void peekReturnsLargestElement() {
        IHeap<Integer> heap = new MaxHeap<Integer>(Comparator.naturalOrder());
        heap.insert(5);
        heap.insert(1);
        heap.insert(9);

        assertEquals(9, heap.peek());
    }

    @Test
    void removeReturnsElementsInDescendingOrder() {
        IHeap<Integer> heap = new MaxHeap<Integer>(Comparator.naturalOrder());
        int[] values = {5, 1, 3, 9, 2, 8, 0, 7};
        for (int v : values) {
            heap.insert(v);
        }

        int previous = Integer.MAX_VALUE;
        int count = 0;
        while (!heap.isEmpty()) {
            int current = heap.remove();
            assertTrue(current <= previous);
            previous = current;
            count++;
        }
        assertEquals(values.length, count);
    }

    @Test
    void peekOnEmptyHeapThrows() {
        IHeap<Integer> heap = new MaxHeap<Integer>(Comparator.naturalOrder());
        assertThrows(NoSuchElementException.class, heap::peek);
    }

    @Test
    void removeOnEmptyHeapThrows() {
        IHeap<Integer> heap = new MaxHeap<Integer>(Comparator.naturalOrder());
        assertThrows(NoSuchElementException.class, heap::remove);
    }

    @Test
    void supportsCustomComparatorForAlertPriorityByRiskScore() {
        record Alert(String name, int riskScore) {
        }
        IHeap<Alert> heap = new MaxHeap<Alert>(Comparator.comparingInt(Alert::riskScore));
        heap.insert(new Alert("low", 10));
        heap.insert(new Alert("critical", 90));
        heap.insert(new Alert("medium", 50));

        assertEquals("critical", heap.peek().name());
        assertEquals("critical", heap.remove().name());
        assertEquals("medium", heap.remove().name());
        assertEquals("low", heap.remove().name());
    }
}
