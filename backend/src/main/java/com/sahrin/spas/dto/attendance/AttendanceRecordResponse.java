package com.sahrin.spas.dto.attendance;

import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceRecordResponse {

    private Long id;
    private Long sessionId;
    private LocalDate sessionDate;
    private String sessionTopic;
    private Long enrollmentId;
    private String studentIdentifier;
    private String studentName;
    private AttendanceStatus status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AttendanceRecordResponse() {
    }

    public AttendanceRecordResponse(AttendanceRecord r) {
        this.id = r.getId();
        if (r.getSession() != null) {
            this.sessionId = r.getSession().getId();
            this.sessionDate = r.getSession().getSessionDate();
            this.sessionTopic = r.getSession().getSessionTopic();
        }
        if (r.getEnrollment() != null && r.getEnrollment().getStudent() != null) {
            this.enrollmentId = r.getEnrollment().getId();
            this.studentIdentifier = r.getEnrollment().getStudent().getIdentifier();
            this.studentName = r.getEnrollment().getStudent().getFullName();
        }
        this.status = r.getStatus();
        this.remarks = r.getRemarks();
        this.createdAt = r.getCreatedAt();
        this.updatedAt = r.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getSessionTopic() {
        return sessionTopic;
    }

    public void setSessionTopic(String sessionTopic) {
        this.sessionTopic = sessionTopic;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
