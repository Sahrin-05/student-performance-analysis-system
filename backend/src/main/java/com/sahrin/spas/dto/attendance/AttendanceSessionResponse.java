package com.sahrin.spas.dto.attendance;

import com.sahrin.spas.entity.AttendanceSession;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceSessionResponse {

    private Long id;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private LocalDate sessionDate;
    private String sessionTopic;
    private String academicYear;
    private String semester;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AttendanceSessionResponse() {
    }

    public AttendanceSessionResponse(AttendanceSession s) {
        this.id = s.getId();
        if (s.getSubject() != null) {
            this.subjectId = s.getSubject().getId();
            this.subjectCode = s.getSubject().getCode();
            this.subjectName = s.getSubject().getName();
        }
        this.sessionDate = s.getSessionDate();
        this.sessionTopic = s.getSessionTopic();
        this.academicYear = s.getAcademicYear();
        this.semester = s.getSemester();
        if (s.getRecordedBy() != null) {
            this.recordedByName = s.getRecordedBy().getFullName();
        }
        this.createdAt = s.getCreatedAt();
        this.updatedAt = s.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRecordedByName() {
        return recordedByName;
    }

    public void setRecordedByName(String recordedByName) {
        this.recordedByName = recordedByName;
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
