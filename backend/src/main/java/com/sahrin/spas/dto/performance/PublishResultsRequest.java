package com.sahrin.spas.dto.performance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PublishResultsRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Semester is required")
    private String semester;

    public PublishResultsRequest() {
    }

    public PublishResultsRequest(Long subjectId, String academicYear, String semester) {
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.semester = semester;
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
