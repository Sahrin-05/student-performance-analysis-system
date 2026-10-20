package com.sahrin.spas.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LecturerAssignmentRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Lecturer ID is required")
    private Long lecturerId;

    @NotBlank(message = "Academic year is required (e.g. 2025/2026)")
    @Size(max = 20, message = "Academic year cannot exceed 20 characters")
    private String academicYear;

    @NotBlank(message = "Semester is required (e.g. Semester 1)")
    @Size(max = 20, message = "Semester cannot exceed 20 characters")
    private String semester;

    public LecturerAssignmentRequest() {
    }

    public LecturerAssignmentRequest(Long subjectId, Long lecturerId, String academicYear, String semester) {
        this.subjectId = subjectId;
        this.lecturerId = lecturerId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
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
}
