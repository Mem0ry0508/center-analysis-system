package org.center.analytics;

import org.center.model.Person;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonDirectoryTest {

    @Test
    void binaryAndLinearSearchAgreeForEveryId() {
        List<Person> people = shuffledPeople(50);
        PersonDirectory directory = new PersonDirectory(people);

        for (long id = 1; id <= 50; id++) {
            var viaBinary = directory.findById(id);
            var viaLinear = directory.findByIdLinear(id);
            assertEquals(viaLinear.map(Person::getPersonId), viaBinary.map(Person::getPersonId));
            assertTrue(viaBinary.isPresent());
            assertEquals(id, viaBinary.get().getPersonId());
        }
    }

    @Test
    void missingIdReturnsEmptyFromBothSearches() {
        PersonDirectory directory = new PersonDirectory(shuffledPeople(10));
        assertTrue(directory.findById(999).isEmpty());
        assertTrue(directory.findByIdLinear(999).isEmpty());
    }

    @Test
    void sortedByNameIsInAscendingOrder() {
        List<Person> people = new ArrayList<>();
        people.add(person(1, "王五"));
        people.add(person(2, "李四"));
        people.add(person(3, "張三"));
        List<String> names = new PersonDirectory(people).sortedByName().stream().map(Person::getName).toList();
        List<String> expected = new ArrayList<>(names);
        expected.sort(null);
        assertEquals(expected, names);
    }

    private List<Person> shuffledPeople(int n) {
        List<Person> people = new ArrayList<>();
        // interleave to guarantee input is unsorted without relying on Random
        for (int i = 0; i < n; i += 2) {
            people.add(person(i + 1, "P" + (i + 1)));
        }
        for (int i = 1; i < n; i += 2) {
            people.add(person(i + 1, "P" + (i + 1)));
        }
        return people;
    }

    private Person person(long id, String name) {
        Person p = new Person();
        p.setPersonId(id);
        p.setName(name);
        return p;
    }
}
