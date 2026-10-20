package com.sahrin.spas.dto.academic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Academic year is required (e.g. 2025/2026)")
    @Size(max = 20, message = "Academic year cannot exceed 20 characters")
    private String academicYear;

    @NotBlank(message = "Semester is required (e.g. Semester 1)")
    @Size(max = 20, message = "Semester cannot exceed 20 characters")
    private String semester;

    public EnrollmentRequest() {
    }

    public EnrollmentRequest(Long studentId, Long courseId, Long subjectId, String academicYear, String semester) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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
