package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AssessmentType;

import java.time.LocalDateTime;

public class AssessmentResponse {

    private Long id;
    private String title;
    private AssessmentType type;
    private double maxMarks;
    private double weightPercentage;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private String semester;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AssessmentResponse() {
    }

    public AssessmentResponse(Assessment a) {
        this.id = a.getId();
        this.title = a.getTitle();
        this.type = a.getType();
        this.maxMarks = a.getMaxMarks();
        this.weightPercentage = a.getWeightPercentage();
        if (a.getSubject() != null) {
            this.subjectId = a.getSubject().getId();
            this.subjectCode = a.getSubject().getCode();
            this.subjectName = a.getSubject().getName();
        }
        this.academicYear = a.getAcademicYear();
        this.semester = a.getSemester();
        if (a.getCreator() != null) {
            this.creatorName = a.getCreator().getFullName();
        }
        this.createdAt = a.getCreatedAt();
        this.updatedAt = a.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(double weightPercentage) {
        this.weightPercentage = weightPercentage;
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

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
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
