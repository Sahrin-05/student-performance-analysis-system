package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SubjectRosterMarksResponse {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private String semester;
    private ResultPublicationStatus publicationStatus;
    private LocalDateTime publishedAt;
    private String publishedByName;
    private double totalAssessmentWeight;
    private boolean canPublish;
    private List<AssessmentResponse> assessments;
    private List<StudentRosterRow> studentRows;

    public SubjectRosterMarksResponse() {
    }

    public static class StudentRosterRow {
        private Long enrollmentId;
        private Long studentId;
        private String studentIdentifier;
        private String studentName;
        private String studentEmail;
        private String enrollmentStatus;
        // assessmentId -> StudentMarkResponse
        private Map<Long, StudentMarkResponse> marks;
        private double totalWeightedScore;
        private Grade projectedGrade;
        private Double projectedGradePoint;

        public StudentRosterRow() {
        }

        public Long getEnrollmentId() {
            return enrollmentId;
        }

        public void setEnrollmentId(Long enrollmentId) {
            this.enrollmentId = enrollmentId;
        }

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
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

        public String getStudentEmail() {
            return studentEmail;
        }

        public void setStudentEmail(String studentEmail) {
            this.studentEmail = studentEmail;
        }

        public String getEnrollmentStatus() {
            return enrollmentStatus;
        }

        public void setEnrollmentStatus(String enrollmentStatus) {
            this.enrollmentStatus = enrollmentStatus;
        }

        public Map<Long, StudentMarkResponse> getMarks() {
            return marks;
        }

        public void setMarks(Map<Long, StudentMarkResponse> marks) {
            this.marks = marks;
        }

        public double getTotalWeightedScore() {
            return totalWeightedScore;
        }

        public void setTotalWeightedScore(double totalWeightedScore) {
            this.totalWeightedScore = totalWeightedScore;
        }

        public Grade getProjectedGrade() {
            return projectedGrade;
        }

        public void setProjectedGrade(Grade projectedGrade) {
            this.projectedGrade = projectedGrade;
        }

        public Double getProjectedGradePoint() {
            return projectedGradePoint;
        }

        public void setProjectedGradePoint(Double projectedGradePoint) {
            this.projectedGradePoint = projectedGradePoint;
        }
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

    public ResultPublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    public void setPublicationStatus(ResultPublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getPublishedByName() {
        return publishedByName;
    }

    public void setPublishedByName(String publishedByName) {
        this.publishedByName = publishedByName;
    }

    public double getTotalAssessmentWeight() {
        return totalAssessmentWeight;
    }

    public void setTotalAssessmentWeight(double totalAssessmentWeight) {
        this.totalAssessmentWeight = totalAssessmentWeight;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public void setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
    }

    public List<AssessmentResponse> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<AssessmentResponse> assessments) {
        this.assessments = assessments;
    }

    public List<StudentRosterRow> getStudentRows() {
        return studentRows;
    }

    public void setStudentRows(List<StudentRosterRow> studentRows) {
        this.studentRows = studentRows;
    }
}
