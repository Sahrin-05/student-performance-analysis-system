package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.StudentMark;

import java.time.LocalDateTime;

public class StudentMarkResponse {

    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private Long enrollmentId;
    private String studentIdentifier;
    private String studentName;
    private double score;
    private double maxMarks;
    private double weightPercentage;
    private double weightedContribution;
    private String comments;
    private String markedByName;
    private LocalDateTime markedAt;
    private LocalDateTime updatedAt;

    public StudentMarkResponse() {
    }

    public StudentMarkResponse(StudentMark m) {
        this.id = m.getId();
        if (m.getAssessment() != null) {
            this.assessmentId = m.getAssessment().getId();
            this.assessmentTitle = m.getAssessment().getTitle();
            this.maxMarks = m.getAssessment().getMaxMarks();
            this.weightPercentage = m.getAssessment().getWeightPercentage();
            if (m.getAssessment().getMaxMarks() > 0) {
                this.weightedContribution = (m.getScore() / m.getAssessment().getMaxMarks()) * m.getAssessment().getWeightPercentage();
                this.weightedContribution = Math.round(this.weightedContribution * 100.0) / 100.0;
            }
        }
        if (m.getEnrollment() != null && m.getEnrollment().getStudent() != null) {
            this.enrollmentId = m.getEnrollment().getId();
            this.studentIdentifier = m.getEnrollment().getStudent().getIdentifier();
            this.studentName = m.getEnrollment().getStudent().getFullName();
        }
        this.score = m.getScore();
        this.comments = m.getComments();
        if (m.getMarkedBy() != null) {
            this.markedByName = m.getMarkedBy().getFullName();
        }
        this.markedAt = m.getCreatedAt();
        this.updatedAt = m.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
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

    public double getWeightedContribution() {
        return weightedContribution;
    }

    public void setWeightedContribution(double weightedContribution) {
        this.weightedContribution = weightedContribution;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMarkedByName() {
        return markedByName;
    }

    public void setMarkedByName(String markedByName) {
        this.markedByName = markedByName;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
