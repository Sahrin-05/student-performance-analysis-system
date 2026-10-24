package com.sahrin.spas.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AttendanceSessionRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotBlank(message = "Session topic is required")
    @Size(max = 200, message = "Topic cannot exceed 200 characters")
    private String sessionTopic;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Semester is required")
    private String semester;

    public AttendanceSessionRequest() {
    }

    public AttendanceSessionRequest(Long subjectId, LocalDate sessionDate, String sessionTopic, String academicYear, String semester) {
        this.subjectId = subjectId;
        this.sessionDate = sessionDate;
        this.sessionTopic = sessionTopic;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
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
}
