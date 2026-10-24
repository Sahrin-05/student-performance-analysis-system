package com.sahrin.spas.dto.risk;

import jakarta.validation.constraints.NotBlank;

public class EvaluateRiskRequest {

    private Long subjectId;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Semester is required")
    private String semester;

    public EvaluateRiskRequest() {
    }

    public EvaluateRiskRequest(Long subjectId, String academicYear, String semester) {
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
