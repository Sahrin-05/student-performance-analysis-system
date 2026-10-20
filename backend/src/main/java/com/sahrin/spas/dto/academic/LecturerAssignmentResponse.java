package com.sahrin.spas.dto.academic;

import java.time.LocalDateTime;

public class LecturerAssignmentResponse {

    private Long id;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    private String lecturerIdentifier;
    private String academicYear;
    private String semester;
    private boolean active;
    private LocalDateTime assignedAt;

    public LecturerAssignmentResponse() {
    }

    public LecturerAssignmentResponse(Long id, Long subjectId, String subjectCode, String subjectName, String courseName, Long lecturerId, String lecturerName, String lecturerEmail, String lecturerIdentifier, String academicYear, String semester, boolean active, LocalDateTime assignedAt) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.courseName = courseName;
        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;
        this.lecturerEmail = lecturerEmail;
        this.lecturerIdentifier = lecturerIdentifier;
        this.academicYear = academicYear;
        this.semester = semester;
        this.active = active;
        this.assignedAt = assignedAt;
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getLecturerEmail() {
        return lecturerEmail;
    }

    public void setLecturerEmail(String lecturerEmail) {
        this.lecturerEmail = lecturerEmail;
    }

    public String getLecturerIdentifier() {
        return lecturerIdentifier;
    }

    public void setLecturerIdentifier(String lecturerIdentifier) {
        this.lecturerIdentifier = lecturerIdentifier;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
