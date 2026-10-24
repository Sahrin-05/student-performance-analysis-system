package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.AssessmentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AssessmentRequest {

    @NotBlank(message = "Assessment title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotNull(message = "Maximum marks is required")
    @Positive(message = "Maximum marks must be strictly greater than 0")
    @DecimalMax(value = "1000.0", message = "Maximum marks cannot exceed 1000")
    private Double maxMarks;

    @NotNull(message = "Weight percentage is required")
    @DecimalMin(value = "0.01", message = "Weight percentage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Weight percentage cannot exceed 100")
    private Double weightPercentage;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotBlank(message = "Semester is required")
    private String semester;

    public AssessmentRequest() {
    }

    public AssessmentRequest(String title, AssessmentType type, Double maxMarks, Double weightPercentage,
                             Long subjectId, String academicYear, String semester) {
        this.title = title;
        this.type = type;
        this.maxMarks = maxMarks;
        this.weightPercentage = weightPercentage;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AssessmentType getType() {
        return type;
    }

    public void setType(AssessmentType type) {
        this.type = type;
    }

    public Double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Double getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(Double weightPercentage) {
        this.weightPercentage = weightPercentage;
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
