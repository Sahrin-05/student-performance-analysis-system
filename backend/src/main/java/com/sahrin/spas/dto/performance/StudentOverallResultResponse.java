package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;

public class StudentOverallResultResponse {

    private Long enrollmentId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int credits;
    private String academicYear;
    private String semester;
    private ResultPublicationStatus publicationStatus;
    private Double totalWeightedScore;
    private Grade grade;
    private Double gradePoint;

    public StudentOverallResultResponse() {
    }

    public StudentOverallResultResponse(Long enrollmentId, Long subjectId, String subjectCode, String subjectName,
                                        int credits, String academicYear, String semester,
                                        ResultPublicationStatus publicationStatus, Double totalWeightedScore,
                                        Grade grade, Double gradePoint) {
        this.enrollmentId = enrollmentId;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.credits = credits;
        this.academicYear = academicYear;
        this.semester = semester;
        this.publicationStatus = publicationStatus;
        this.totalWeightedScore = totalWeightedScore;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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

    public ResultPublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    public void setPublicationStatus(ResultPublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public Double getTotalWeightedScore() {
        return totalWeightedScore;
    }

    public void setTotalWeightedScore(Double totalWeightedScore) {
        this.totalWeightedScore = totalWeightedScore;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
    }
}
