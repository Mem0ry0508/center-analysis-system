package org.center.util;

import org.center.algorithm.BinarySearch;
import org.center.algorithm.LinearSearch;
import org.center.algorithm.MergeSort;
import org.center.datastructure.CustomHashTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * 資料結構／演算法效能測試（報告要求的 100 / 1000 / 10000 筆比較）。
 *
 * <p>執行：{@code mvn -q compile exec:java -Dexec.mainClass=org.center.util.PerformanceBenchmark}
 *
 * <p>比較項目：
 * <ul>
 *   <li>自訂 MergeSort（O(n log n)）在三種資料量下的耗時成長</li>
 *   <li>自訂 BinarySearch（O(log n)）vs LinearSearch（O(n)）在已排序陣列上的查找</li>
 *   <li>自訂 CustomHashTable（平均 O(1)）vs List 線性掃描 的查找</li>
 * </ul>
 */
public final class PerformanceBenchmark {

    private static final int[] SIZES = {100, 1_000, 10_000};
    private static final int LOOKUP_QUERIES = 5_000;
    private static final long SEED = 42L;
    private static final Comparator<Integer> NATURAL = Comparator.naturalOrder();

    private PerformanceBenchmark() {
    }

    public static void main(String[] args) {
        System.out.println("== 資料結構／演算法效能測試（單位：毫秒，取多次平均）==\n");
        benchMergeSort();
        benchSearch();
        benchHashTableLookup();
        System.out.println("\n備註：每項皆先 warm-up，再取多輪平均；資料以固定亂數種子產生，可重現。");
    }

    private static void benchMergeSort() {
        System.out.println("[1] 自訂 MergeSort 排序耗時");
        System.out.printf("%-10s %-15s %-15s%n", "n", "MergeSort", "每筆平均(µs)");
        for (int n : SIZES) {
            List<Integer> base = randomList(n);
            long nanos = timeAveraged(20, () -> {
                List<Integer> copy = new ArrayList<>(base);
                MergeSort.sort(copy, NATURAL);
            });
            System.out.printf("%-10d %-15.3f %-15.3f%n", n, nanos / 1_000_000.0, nanos / 1_000.0 / n);
        }
        System.out.println();
    }

    private static void benchSearch() {
        System.out.println("[2] 已排序陣列查找：BinarySearch vs LinearSearch（" + LOOKUP_QUERIES + " 次查詢）");
        System.out.printf("%-10s %-18s %-18s %-10s%n", "n", "BinarySearch(ms)", "LinearSearch(ms)", "倍數");
        Random random = new Random(SEED);
        for (int n : SIZES) {
            List<Integer> sorted = sortedList(n);
            int[] targets = new int[LOOKUP_QUERIES];
            for (int i = 0; i < LOOKUP_QUERIES; i++) {
                targets[i] = random.nextInt(n * 2);
            }
            long binary = timeAveraged(10, () -> {
                for (int t : targets) {
                    BinarySearch.search(sorted, t, NATURAL);
                }
            });
            long linear = timeAveraged(10, () -> {
                for (int t : targets) {
                    LinearSearch.search(sorted, t, NATURAL);
                }
            });
            System.out.printf("%-10d %-18.3f %-18.3f %-10.1f%n",
                    n, binary / 1_000_000.0, linear / 1_000_000.0,
                    binary == 0 ? 0 : (double) linear / binary);
        }
        System.out.println();
    }

    private static void benchHashTableLookup() {
        System.out.println("[3] 依鍵查找：CustomHashTable vs List 線性掃描（" + LOOKUP_QUERIES + " 次查詢）");
        System.out.printf("%-10s %-20s %-20s %-10s%n", "n", "CustomHashTable(ms)", "List 掃描(ms)", "倍數");
        Random random = new Random(SEED);
        for (int n : SIZES) {
            List<Integer> keys = sortedList(n);
            CustomHashTable<Integer, Integer> table = new CustomHashTable<>();
            for (int k : keys) {
                table.put(k, k);
            }
            int[] queries = new int[LOOKUP_QUERIES];
            for (int i = 0; i < LOOKUP_QUERIES; i++) {
                queries[i] = random.nextInt(n * 2);
            }
            long hash = timeAveraged(10, () -> {
                for (int q : queries) {
                    table.get(q);
                }
            });
            long scan = timeAveraged(10, () -> {
                for (int q : queries) {
                    keys.contains(q);
                }
            });
            System.out.printf("%-10d %-20.3f %-20.3f %-10.1f%n",
                    n, hash / 1_000_000.0, scan / 1_000_000.0,
                    hash == 0 ? 0 : (double) scan / hash);
        }
    }

    /** warm-up 5 輪後，取 rounds 輪平均的單輪耗時（奈秒）。 */
    private static long timeAveraged(int rounds, Runnable task) {
        for (int i = 0; i < 5; i++) {
            task.run();
        }
        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) {
            task.run();
        }
        return (System.nanoTime() - start) / rounds;
    }

    private static List<Integer> randomList(int n) {
        Random random = new Random(SEED);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(random.nextInt(n * 10));
        }
        return list;
    }

    private static List<Integer> sortedList(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i * 2);
        }
        return list;
    }
}
