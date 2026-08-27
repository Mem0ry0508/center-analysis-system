package org.center.analytics;

import org.center.algorithm.BinarySearch;
import org.center.algorithm.LinearSearch;
import org.center.algorithm.MergeSort;
import org.center.model.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 人員索引：分析模組共用的姓名／編號查找入口，把自訂排序與搜尋演算法接進真實操作路徑。
 *
 * <ul>
 *   <li>建構時用自訂 {@link MergeSort} 各排出一份「依 personId」與「依姓名」的清單（stable, O(n log n)）。</li>
 *   <li>{@link #findById(long)} 在已排序清單上用自訂 {@link BinarySearch}（O(log n) 精確比對）。</li>
 *   <li>{@link #findByIdLinear(long)} 用自訂 {@link LinearSearch}（O(n)）作為效能對照基準（報告 100/1000/10000 筆比較）。</li>
 * </ul>
 */
public class PersonDirectory {

    private static final Comparator<Person> BY_ID =
            Comparator.comparingLong(p -> p.getPersonId() == null ? Long.MIN_VALUE : p.getPersonId());
    private static final Comparator<Person> BY_NAME =
            Comparator.comparing(p -> p.getName() == null ? "" : p.getName());

    private final List<Person> byId;
    private final List<Person> byName;

    public PersonDirectory(List<Person> people) {
        this.byId = new ArrayList<>(people);
        MergeSort.sort(byId, BY_ID);
        this.byName = new ArrayList<>(people);
        MergeSort.sort(byName, BY_NAME);
    }

    /** 用自訂 BinarySearch 在依 ID 排序的清單上精確查找。 */
    public Optional<Person> findById(long id) {
        int index = BinarySearch.search(byId, probe(id), BY_ID);
        return index < 0 ? Optional.empty() : Optional.of(byId.get(index));
    }

    /** 用自訂 LinearSearch 查找，作為 BinarySearch 的效能對照基準。 */
    public Optional<Person> findByIdLinear(long id) {
        int index = LinearSearch.search(byId, probe(id), BY_ID);
        return index < 0 ? Optional.empty() : Optional.of(byId.get(index));
    }

    /** 分析模組拿 personId 換姓名時走 BinarySearch；查無此人時回退顯示編號。 */
    public String nameOf(long id) {
        return findById(id).map(Person::getName).orElse("#" + id);
    }

    public List<Person> sortedById() {
        return Collections.unmodifiableList(byId);
    }

    public List<Person> sortedByName() {
        return Collections.unmodifiableList(byName);
    }

    public int size() {
        return byId.size();
    }

    private static Person probe(long id) {
        Person probe = new Person();
        probe.setPersonId(id);
        return probe;
    }
}
