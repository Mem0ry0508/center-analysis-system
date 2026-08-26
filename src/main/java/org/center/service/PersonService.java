package org.center.service;

import org.center.model.Person;
import org.center.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

public class PersonService {

    private final PersonRepository personRepository;

    public PersonService() {
        this(new PersonRepository());
    }

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    public Person save(Person person) {
        if (person.getStatus() == null) {
            person.setStatus("active");
        }
        if (person.getPersonId() == null) {
            return personRepository.save(person);
        }
        personRepository.update(person);
        return person;
    }

    public boolean deactivate(Long id) {
        return personRepository.deleteById(id);
    }
}
