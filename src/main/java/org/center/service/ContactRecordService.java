package org.center.service;

import org.center.model.ContactRecord;
import org.center.repository.ContactRecordRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ContactRecordService {

    private final ContactRecordRepository contactRecordRepository;

    public ContactRecordService() {
        this(new ContactRecordRepository());
    }

    public ContactRecordService(ContactRecordRepository contactRecordRepository) {
        this.contactRecordRepository = contactRecordRepository;
    }

    public List<ContactRecord> findAll() {
        return contactRecordRepository.findAll();
    }

    public Optional<ContactRecord> findById(Long id) {
        return contactRecordRepository.findById(id);
    }

    public List<ContactRecord> findOverdueFollowUps() {
        return contactRecordRepository.findOverdueFollowUps(LocalDate.now());
    }

    public ContactRecord save(ContactRecord contactRecord) {
        if (contactRecord.getContactId() == null) {
            return contactRecordRepository.save(contactRecord);
        }
        contactRecordRepository.update(contactRecord);
        return contactRecord;
    }

    public boolean delete(Long id) {
        return contactRecordRepository.deleteById(id);
    }
}
