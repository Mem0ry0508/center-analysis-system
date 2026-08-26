package org.center.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Course {
    private Long courseId;
    private String name;
    private Long instructorId;
    private int capacity;
    private String paymentType;
    private String classTimeSlot;
    private String classroom;
    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDate firstClassDate;
    private String status;
    private String suspendReason;
    private LocalDateTime createdAt;

    public Course() {
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getClassTimeSlot() {
        return classTimeSlot;
    }

    public void setClassTimeSlot(String classTimeSlot) {
        this.classTimeSlot = classTimeSlot;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public LocalDate getFirstClassDate() {
        return firstClassDate;
    }

    public void setFirstClassDate(LocalDate firstClassDate) {
        this.firstClassDate = firstClassDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuspendReason() {
        return suspendReason;
    }

    public void setSuspendReason(String suspendReason) {
        this.suspendReason = suspendReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
