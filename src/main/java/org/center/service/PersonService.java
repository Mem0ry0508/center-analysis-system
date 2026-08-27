package org.center.service;

import org.center.model.Person;
import org.center.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

public class PersonService {

    private final PersonRepository personRepository;
    AuditService auditService = new AuditService();

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
        boolean isNew = person.getPersonId() == null;
        Person result;
        if (isNew) {
            result = personRepository.save(person);
        } else {
            personRepository.update(person);
            result = person;
        }
        auditService.record(isNew ? "CREATE" : "UPDATE", "people", result.getPersonId(),
                isNew ? "新增人員 " + result.getName() : "修改人員 " + result.getName());
        return result;
    }

    public boolean deactivate(Long id) {
        boolean ok = personRepository.deleteById(id);
        if (ok) {
            auditService.record("VOID", "people", id, "停用人員");
        }
        return ok;
    }
}
