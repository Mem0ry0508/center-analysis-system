package org.center.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContactRecord {
    private Long contactId;
    private Long personId;
    private LocalDateTime contactDate;
    private String method;
    private String content;
    private Integer moodRating;
    private String result;
    private String followUpAction;
    private LocalDate nextContactDate;
    private Long createdBy;

    public ContactRecord() {
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public LocalDateTime getContactDate() {
        return contactDate;
    }

    public void setContactDate(LocalDateTime contactDate) {
        this.contactDate = contactDate;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMoodRating() {
        return moodRating;
    }

    public void setMoodRating(Integer moodRating) {
        this.moodRating = moodRating;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFollowUpAction() {
        return followUpAction;
    }

    public void setFollowUpAction(String followUpAction) {
        this.followUpAction = followUpAction;
    }

    public LocalDate getNextContactDate() {
        return nextContactDate;
    }

    public void setNextContactDate(LocalDate nextContactDate) {
        this.nextContactDate = nextContactDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
