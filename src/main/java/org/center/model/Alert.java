package org.center.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Alert {
    private Long alertId;
    private String alertType;
    private int severity;
    private LocalDate dueDate;
    private String priorityTier;
    private String sourceTable;
    private Long sourceId;
    private Long relatedPersonId;
    private String triggerReason;
    private String message;
    private String status;
    private Long assignedTo;
    private LocalDateTime readAt;
    private LocalDateTime snoozedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public Alert() {
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPriorityTier() {
        return priorityTier;
    }

    public void setPriorityTier(String priorityTier) {
        this.priorityTier = priorityTier;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getRelatedPersonId() {
        return relatedPersonId;
    }

    public void setRelatedPersonId(Long relatedPersonId) {
        this.relatedPersonId = relatedPersonId;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(LocalDateTime snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
