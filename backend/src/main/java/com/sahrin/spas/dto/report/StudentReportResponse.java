package com.sahrin.spas.dto.report;

import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentReportResponse {

    private Long studentId;
    private String studentIdentifier;
    private String studentName;
    private String studentEmail;
    private Double gpa;
    private int totalCreditsEarned;
    private int totalEnrolledCredits;
    private int totalEnrolledSubjects;
    private int publishedSubjectsCount;
    private int passedSubjectsCount;
    private int failedSubjectsCount;
    private Double overallAttendancePercentage;
    private String provisionalNotice;
    private List<StudentSubjectReportItem> subjects = new ArrayList<>();

    public StudentReportResponse() {
    }

    public static class StudentSubjectReportItem {
        private Long enrollmentId;
        private Long subjectId;
        private String subjectCode;
        private String subjectName;
        private int credits;
        private String academicYear;
        private String semester;
        private ResultPublicationStatus publicationStatus;
        private LocalDateTime publishedAt;
        private Double totalWeightedScore;
        private Grade grade;
        private Double gradePoint;
        private List<StudentAssessmentScoreItem> assessmentScores = new ArrayList<>();
        private int totalSessions;
        private int attendedSessions;
        private int absentSessions;
        private int excusedSessions;
        private int eligibleSessions;
        private double attendancePercentage;
        private boolean belowAttendanceThreshold;
        private List<StudentRiskAlertItem> activeAlerts = new ArrayList<>();

        public StudentSubjectReportItem() {
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

        public List<StudentAssessmentScoreItem> getAssessmentScores() {
            return assessmentScores;
        }

        public void setAssessmentScores(List<StudentAssessmentScoreItem> assessmentScores) {
            this.assessmentScores = assessmentScores;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(int totalSessions) {
            this.totalSessions = totalSessions;
        }

        public int getAttendedSessions() {
            return attendedSessions;
        }

        public void setAttendedSessions(int attendedSessions) {
            this.attendedSessions = attendedSessions;
        }

        public int getAbsentSessions() {
            return absentSessions;
        }

        public void setAbsentSessions(int absentSessions) {
            this.absentSessions = absentSessions;
        }

        public int getExcusedSessions() {
            return excusedSessions;
        }

        public void setExcusedSessions(int excusedSessions) {
            this.excusedSessions = excusedSessions;
        }

        public int getEligibleSessions() {
            return eligibleSessions;
        }

        public void setEligibleSessions(int eligibleSessions) {
            this.eligibleSessions = eligibleSessions;
        }

        public double getAttendancePercentage() {
            return attendancePercentage;
        }

        public void setAttendancePercentage(double attendancePercentage) {
            this.attendancePercentage = attendancePercentage;
        }

        public boolean isBelowAttendanceThreshold() {
            return belowAttendanceThreshold;
        }

        public void setBelowAttendanceThreshold(boolean belowAttendanceThreshold) {
            this.belowAttendanceThreshold = belowAttendanceThreshold;
        }

        public List<StudentRiskAlertItem> getActiveAlerts() {
            return activeAlerts;
        }

        public void setActiveAlerts(List<StudentRiskAlertItem> activeAlerts) {
            this.activeAlerts = activeAlerts;
        }
    }

    public static class StudentAssessmentScoreItem {
        private Long assessmentId;
        private String assessmentTitle;
        private AssessmentType assessmentType;
        private double maxMarks;
        private Double score;
        private double weightPercentage;
        private Double weightedContribution;

        public StudentAssessmentScoreItem() {
        }

        public StudentAssessmentScoreItem(Long assessmentId, String assessmentTitle, AssessmentType assessmentType,
                                          double maxMarks, Double score, double weightPercentage, Double weightedContribution) {
            this.assessmentId = assessmentId;
            this.assessmentTitle = assessmentTitle;
            this.assessmentType = assessmentType;
            this.maxMarks = maxMarks;
            this.score = score;
            this.weightPercentage = weightPercentage;
            this.weightedContribution = weightedContribution;
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

        public AssessmentType getAssessmentType() {
            return assessmentType;
        }

        public void setAssessmentType(AssessmentType assessmentType) {
            this.assessmentType = assessmentType;
        }

        public double getMaxMarks() {
            return maxMarks;
        }

        public void setMaxMarks(double maxMarks) {
            this.maxMarks = maxMarks;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
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
    }

    public static class StudentRiskAlertItem {
        private Long alertId;
        private AlertRiskType riskType;
        private AlertSeverity severity;
        private AlertStatus status;
        private String triggerReason;
        private String advisoryGuidance;
        private LocalDateTime detectedAt;

        public StudentRiskAlertItem() {
        }

        public StudentRiskAlertItem(Long alertId, AlertRiskType riskType, AlertSeverity severity,
                                    AlertStatus status, String triggerReason, String advisoryGuidance, LocalDateTime detectedAt) {
            this.alertId = alertId;
            this.riskType = riskType;
            this.severity = severity;
            this.status = status;
            this.triggerReason = triggerReason;
            this.advisoryGuidance = advisoryGuidance;
            this.detectedAt = detectedAt;
        }

        public Long getAlertId() {
            return alertId;
        }

        public void setAlertId(Long alertId) {
            this.alertId = alertId;
        }

        public AlertRiskType getRiskType() {
            return riskType;
        }

        public void setRiskType(AlertRiskType riskType) {
            this.riskType = riskType;
        }

        public AlertSeverity getSeverity() {
            return severity;
        }

        public void setSeverity(AlertSeverity severity) {
            this.severity = severity;
        }

        public AlertStatus getStatus() {
            return status;
        }

        public void setStatus(AlertStatus status) {
            this.status = status;
        }

        public String getTriggerReason() {
            return triggerReason;
        }

        public void setTriggerReason(String triggerReason) {
            this.triggerReason = triggerReason;
        }

        public String getAdvisoryGuidance() {
            return advisoryGuidance;
        }

        public void setAdvisoryGuidance(String advisoryGuidance) {
            this.advisoryGuidance = advisoryGuidance;
        }

        public LocalDateTime getDetectedAt() {
            return detectedAt;
        }

        public void setDetectedAt(LocalDateTime detectedAt) {
            this.detectedAt = detectedAt;
        }
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

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public int getTotalCreditsEarned() {
        return totalCreditsEarned;
    }

    public void setTotalCreditsEarned(int totalCreditsEarned) {
        this.totalCreditsEarned = totalCreditsEarned;
    }

    public int getTotalEnrolledCredits() {
        return totalEnrolledCredits;
    }

    public void setTotalEnrolledCredits(int totalEnrolledCredits) {
        this.totalEnrolledCredits = totalEnrolledCredits;
    }

    public int getTotalEnrolledSubjects() {
        return totalEnrolledSubjects;
    }

    public void setTotalEnrolledSubjects(int totalEnrolledSubjects) {
        this.totalEnrolledSubjects = totalEnrolledSubjects;
    }

    public int getPublishedSubjectsCount() {
        return publishedSubjectsCount;
    }

    public void setPublishedSubjectsCount(int publishedSubjectsCount) {
        this.publishedSubjectsCount = publishedSubjectsCount;
    }

    public int getPassedSubjectsCount() {
        return passedSubjectsCount;
    }

    public void setPassedSubjectsCount(int passedSubjectsCount) {
        this.passedSubjectsCount = passedSubjectsCount;
    }

    public int getFailedSubjectsCount() {
        return failedSubjectsCount;
    }

    public void setFailedSubjectsCount(int failedSubjectsCount) {
        this.failedSubjectsCount = failedSubjectsCount;
    }

    public Double getOverallAttendancePercentage() {
        return overallAttendancePercentage;
    }

    public void setOverallAttendancePercentage(Double overallAttendancePercentage) {
        this.overallAttendancePercentage = overallAttendancePercentage;
    }

    public String getProvisionalNotice() {
        return provisionalNotice;
    }

    public void setProvisionalNotice(String provisionalNotice) {
        this.provisionalNotice = provisionalNotice;
    }

    public List<StudentSubjectReportItem> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<StudentSubjectReportItem> subjects) {
        this.subjects = subjects;
    }
}
