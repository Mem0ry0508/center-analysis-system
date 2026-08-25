package org.center.datastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomHashTableTest {

    @Test
    void putAndGetReturnsStoredValue() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        table.put("alice", 20);
        table.put("bob", 25);

        assertEquals(20, table.get("alice"));
        assertEquals(25, table.get("bob"));
    }

    @Test
    void putWithExistingKeyOverwritesValue() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        table.put("alice", 20);
        table.put("alice", 21);

        assertEquals(21, table.get("alice"));
        assertEquals(1, table.size());
    }

    @Test
    void getMissingKeyReturnsNull() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        assertNull(table.get("missing"));
    }

    @Test
    void containsKeyReflectsPresence() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        table.put("alice", 20);

        assertTrue(table.containsKey("alice"));
        assertFalse(table.containsKey("bob"));
    }

    @Test
    void removeDeletesEntryAndReturnsTrue() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        table.put("alice", 20);

        assertTrue(table.remove("alice"));
        assertFalse(table.containsKey("alice"));
        assertEquals(0, table.size());
    }

    @Test
    void removeMissingKeyReturnsFalse() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        assertFalse(table.remove("missing"));
    }

    @Test
    void sizeTracksNumberOfEntries() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        assertEquals(0, table.size());

        table.put("a", 1);
        table.put("b", 2);
        table.put("c", 3);
        assertEquals(3, table.size());

        table.remove("b");
        assertEquals(2, table.size());
    }

    @Test
    void handlesCollisionsAndResizeWithManyEntries() {
        IHashTable<Integer, String> table = new CustomHashTable<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            table.put(i, "value-" + i);
        }

        assertEquals(n, table.size());
        for (int i = 0; i < n; i++) {
            assertEquals("value-" + i, table.get(i));
        }
    }

    @Test
    void supportsNullKey() {
        IHashTable<String, Integer> table = new CustomHashTable<>();
        table.put(null, 99);

        assertEquals(99, table.get(null));
        assertTrue(table.containsKey(null));
    }
}
