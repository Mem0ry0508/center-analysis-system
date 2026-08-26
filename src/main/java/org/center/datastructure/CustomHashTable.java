package org.center.datastructure;

/**
 * 自訂 Hash Table，separate chaining 處理碰撞，不包裝 java.util.HashMap。
 * 當 load factor 超過門檻時自動 rehash 到兩倍容量的桶陣列。
 */
public class CustomHashTable<K, V> implements IHashTable<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public CustomHashTable() {
        this.buckets = new Node[DEFAULT_CAPACITY];
        this.size = 0;
    }

    private int indexFor(K key, int capacity) {
        int h = (key == null) ? 0 : key.hashCode();
        h ^= (h >>> 16);
        return (h & 0x7fffffff) % capacity;
    }

    @Override
    public void put(K key, V value) {
        if (((double) (size + 1)) / buckets.length > LOAD_FACTOR_THRESHOLD) {
            resize(buckets.length * 2);
        }
        int index = indexFor(key, buckets.length);
        Node<K, V> current = buckets[index];
        while (current != null) {
            if (keysEqual(current.key, key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }
        buckets[index] = new Node<>(key, value, buckets[index]);
        size++;
    }

    @Override
    public V get(K key) {
        int index = indexFor(key, buckets.length);
        Node<K, V> current = buckets[index];
        while (current != null) {
            if (keysEqual(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    @Override
    public boolean remove(K key) {
        int index = indexFor(key, buckets.length);
        Node<K, V> current = buckets[index];
        Node<K, V> prev = null;
        while (current != null) {
            if (keysEqual(current.key, key)) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }

    @Override
    public boolean containsKey(K key) {
        int index = indexFor(key, buckets.length);
        Node<K, V> current = buckets[index];
        while (current != null) {
            if (keysEqual(current.key, key)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    private boolean keysEqual(K a, K b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Node<K, V>[] oldBuckets = buckets;
        Node<K, V>[] newBuckets = new Node[newCapacity];
        for (Node<K, V> head : oldBuckets) {
            Node<K, V> current = head;
            while (current != null) {
                Node<K, V> next = current.next;
                int index = indexFor(current.key, newCapacity);
                current.next = newBuckets[index];
                newBuckets[index] = current;
                current = next;
            }
        }
        buckets = newBuckets;
    }
}
