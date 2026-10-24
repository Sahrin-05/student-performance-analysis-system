package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public class StudentPublishedResultsResponse {

    private Long enrollmentId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int credits;
    private String academicYear;
    private String semester;
    private ResultPublicationStatus publicationStatus;
    private LocalDateTime publishedAt;
    private String statusMessage;
    private Double totalWeightedScore;
    private Grade grade;
    private Double gradePoint;
    private String gradingNotice;
    private List<StudentAssessmentResultItem> assessmentResults;

    public StudentPublishedResultsResponse() {
    }

    public static class StudentAssessmentResultItem {
        private Long assessmentId;
        private String title;
        private AssessmentType type;
        private double maxMarks;
        private Double studentScore;
        private double weightPercentage;
        private Double weightedContribution;
        private String comments;

        public StudentAssessmentResultItem() {
        }

        public StudentAssessmentResultItem(Long assessmentId, String title, AssessmentType type, double maxMarks,
                                           Double studentScore, double weightPercentage, Double weightedContribution, String comments) {
            this.assessmentId = assessmentId;
            this.title = title;
            this.type = type;
            this.maxMarks = maxMarks;
            this.studentScore = studentScore;
            this.weightPercentage = weightPercentage;
            this.weightedContribution = weightedContribution;
            this.comments = comments;
        }

        public Long getAssessmentId() {
            return assessmentId;
        }

        public void setAssessmentId(Long assessmentId) {
            this.assessmentId = assessmentId;
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

        public Double getStudentScore() {
            return studentScore;
        }

        public void setStudentScore(Double studentScore) {
            this.studentScore = studentScore;
        }

        public double getWeightPercentage() {
            return weightPercentage;
        }

        public void setWeightPercentage(double weightPercentage) {
            this.weightPercentage = weightPercentage;
        }

        public Double getWeightedContribution() {
            return weightedContribution;
        }

        public void setWeightedContribution(Double weightedContribution) {
            this.weightedContribution = weightedContribution;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
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

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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

    public String getGradingNotice() {
        return gradingNotice;
    }

    public void setGradingNotice(String gradingNotice) {
        this.gradingNotice = gradingNotice;
    }

    public List<StudentAssessmentResultItem> getAssessmentResults() {
        return assessmentResults;
    }

    public void setAssessmentResults(List<StudentAssessmentResultItem> assessmentResults) {
        this.assessmentResults = assessmentResults;
    }
}
