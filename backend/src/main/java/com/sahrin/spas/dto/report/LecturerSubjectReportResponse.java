package com.sahrin.spas.dto.report;

import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LecturerSubjectReportResponse {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int credits;
    private String academicYear;
    private String semester;
    private ResultPublicationStatus publicationStatus;
    private LocalDateTime publishedAt;

    private int enrolledStudentsCount;
    private int assessedStudentsCount;
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private int passCount;
    private int failCount;
    private Double passRate;

    private Map<String, Integer> gradeDistribution = new HashMap<>();

    private int totalSessionsConducted;
    private Double averageAttendancePercentage;
    private int studentsBelowThresholdCount;
    private int activeAlertsCount;

    private List<AssessmentSummaryItem> assessments = new ArrayList<>();
    private List<LecturerStudentReportRow> students = new ArrayList<>();
    private List<LecturerAtRiskStudentItem> atRiskStudents = new ArrayList<>();

    private String provisionalNotice;

    public LecturerSubjectReportResponse() {
    }

    public static class AssessmentSummaryItem {
        private Long id;
        private String title;
        private AssessmentType type;
        private double maxMarks;
        private double weightPercentage;

        public AssessmentSummaryItem() {
        }

        public AssessmentSummaryItem(Long id, String title, AssessmentType type, double maxMarks, double weightPercentage) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.maxMarks = maxMarks;
            this.weightPercentage = weightPercentage;
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
    }

    public static class LecturerStudentReportRow {
        private Long enrollmentId;
        private Long studentId;
        private String studentIdentifier;
        private String studentName;
        private String studentEmail;
        private String enrollmentStatus;
        private Map<Long, Double> assessmentScores = new HashMap<>();
        private Double totalWeightedScore;
        private Grade grade;
        private Double gradePoint;
        private int attendedSessions;
        private int absentSessions;
        private int excusedSessions;
        private int eligibleSessions;
        private double attendancePercentage;
        private boolean belowAttendanceThreshold;
        private int activeAlertCount;
        private AlertSeverity highestAlertSeverity;

        public LecturerStudentReportRow() {
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

        public Map<Long, Double> getAssessmentScores() {
            return assessmentScores;
        }

        public void setAssessmentScores(Map<Long, Double> assessmentScores) {
            this.assessmentScores = assessmentScores;
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

        public int getActiveAlertCount() {
            return activeAlertCount;
        }

        public void setActiveAlertCount(int activeAlertCount) {
            this.activeAlertCount = activeAlertCount;
        }

        public AlertSeverity getHighestAlertSeverity() {
            return highestAlertSeverity;
        }

        public void setHighestAlertSeverity(AlertSeverity highestAlertSeverity) {
            this.highestAlertSeverity = highestAlertSeverity;
        }
    }

    public static class LecturerAtRiskStudentItem {
        private Long alertId;
        private Long studentId;
        private String studentIdentifier;
        private String studentName;
        private AlertRiskType riskType;
        private AlertSeverity severity;
        private AlertStatus status;
        private String triggerReason;
        private LocalDateTime detectedAt;
        private String actionTaken;
        private String reviewedByName;

        public LecturerAtRiskStudentItem() {
        }

        public Long getAlertId() {
            return alertId;
        }

        public void setAlertId(Long alertId) {
            this.alertId = alertId;
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

        public LocalDateTime getDetectedAt() {
            return detectedAt;
        }

        public void setDetectedAt(LocalDateTime detectedAt) {
            this.detectedAt = detectedAt;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public void setActionTaken(String actionTaken) {
            this.actionTaken = actionTaken;
        }

        public String getReviewedByName() {
            return reviewedByName;
        }

        public void setReviewedByName(String reviewedByName) {
            this.reviewedByName = reviewedByName;
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

    public int getEnrolledStudentsCount() {
        return enrolledStudentsCount;
    }

    public void setEnrolledStudentsCount(int enrolledStudentsCount) {
        this.enrolledStudentsCount = enrolledStudentsCount;
    }

    public int getAssessedStudentsCount() {
        return assessedStudentsCount;
    }

    public void setAssessedStudentsCount(int assessedStudentsCount) {
        this.assessedStudentsCount = assessedStudentsCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Double highestScore) {
        this.highestScore = highestScore;
    }

    public Double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(Double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public Map<String, Integer> getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(Map<String, Integer> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }

    public int getTotalSessionsConducted() {
        return totalSessionsConducted;
    }

    public void setTotalSessionsConducted(int totalSessionsConducted) {
        this.totalSessionsConducted = totalSessionsConducted;
    }

    public Double getAverageAttendancePercentage() {
        return averageAttendancePercentage;
    }

    public void setAverageAttendancePercentage(Double averageAttendancePercentage) {
        this.averageAttendancePercentage = averageAttendancePercentage;
    }

    public int getStudentsBelowThresholdCount() {
        return studentsBelowThresholdCount;
    }

    public void setStudentsBelowThresholdCount(int studentsBelowThresholdCount) {
        this.studentsBelowThresholdCount = studentsBelowThresholdCount;
    }

    public int getActiveAlertsCount() {
        return activeAlertsCount;
    }

    public void setActiveAlertsCount(int activeAlertsCount) {
        this.activeAlertsCount = activeAlertsCount;
    }

    public List<AssessmentSummaryItem> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<AssessmentSummaryItem> assessments) {
        this.assessments = assessments;
    }

    public List<LecturerStudentReportRow> getStudents() {
        return students;
    }

    public void setStudents(List<LecturerStudentReportRow> students) {
        this.students = students;
    }

    public List<LecturerAtRiskStudentItem> getAtRiskStudents() {
        return atRiskStudents;
    }

    public void setAtRiskStudents(List<LecturerAtRiskStudentItem> atRiskStudents) {
        this.atRiskStudents = atRiskStudents;
    }

    public String getProvisionalNotice() {
        return provisionalNotice;
    }

    public void setProvisionalNotice(String provisionalNotice) {
        this.provisionalNotice = provisionalNotice;
    }
}
