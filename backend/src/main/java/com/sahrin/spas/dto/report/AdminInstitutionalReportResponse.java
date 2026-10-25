package com.sahrin.spas.dto.report;

import com.sahrin.spas.entity.ResultPublicationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminInstitutionalReportResponse {

    private long totalStudents;
    private long totalLecturers;
    private long totalDepartments;
    private long totalCourses;
    private long totalSubjects;
    private long totalEnrollments;
    private long publishedSubjectsCount;

    private Double averageScore;
    private Double overallPassRate;
    private Double overallAttendanceAverage;
    private long studentsBelowThresholdCount;
    private long totalActiveAlerts;

    private Map<String, Integer> gradeDistribution = new HashMap<>();

    private AttendanceBracketCounts attendanceBrackets = new AttendanceBracketCounts();
    private RiskAlertsSummaryStats riskAlertsSummary = new RiskAlertsSummaryStats();

    private List<DepartmentComparisonItem> departmentComparisons = new ArrayList<>();
    private List<CourseComparisonItem> courseComparisons = new ArrayList<>();
    private List<SubjectComparisonItem> subjectComparisons = new ArrayList<>();

    private String provisionalNotice;

    public AdminInstitutionalReportResponse() {
    }

    public static class AttendanceBracketCounts {
        private int excellentCount; // >= 90%
        private int goodCount;      // 80 - 89.9%
        private int warningCount;   // 70 - 79.9%
        private int criticalCount;  // < 70%

        public AttendanceBracketCounts() {
        }

        public AttendanceBracketCounts(int excellentCount, int goodCount, int warningCount, int criticalCount) {
            this.excellentCount = excellentCount;
            this.goodCount = goodCount;
            this.warningCount = warningCount;
            this.criticalCount = criticalCount;
        }

        public int getExcellentCount() {
            return excellentCount;
        }

        public void setExcellentCount(int excellentCount) {
            this.excellentCount = excellentCount;
        }

        public int getGoodCount() {
            return goodCount;
        }

        public void setGoodCount(int goodCount) {
            this.goodCount = goodCount;
        }

        public int getWarningCount() {
            return warningCount;
        }

        public void setWarningCount(int warningCount) {
            this.warningCount = warningCount;
        }

        public int getCriticalCount() {
            return criticalCount;
        }

        public void setCriticalCount(int criticalCount) {
            this.criticalCount = criticalCount;
        }
    }

    public static class RiskAlertsSummaryStats {
        private Map<String, Long> byRiskType = new HashMap<>();
        private Map<String, Long> bySeverity = new HashMap<>();
        private Map<String, Long> byStatus = new HashMap<>();

        public RiskAlertsSummaryStats() {
        }

        public Map<String, Long> getByRiskType() {
            return byRiskType;
        }

        public void setByRiskType(Map<String, Long> byRiskType) {
            this.byRiskType = byRiskType;
        }

        public Map<String, Long> getBySeverity() {
            return bySeverity;
        }

        public void setBySeverity(Map<String, Long> bySeverity) {
            this.bySeverity = bySeverity;
        }

        public Map<String, Long> getByStatus() {
            return byStatus;
        }

        public void setByStatus(Map<String, Long> byStatus) {
            this.byStatus = byStatus;
        }
    }

    public static class DepartmentComparisonItem {
        private Long departmentId;
        private String code;
        private String name;
        private int courseCount;
        private int subjectCount;
        private int enrollmentCount;
        private Double averageScore;
        private Double passRate;
        private Double averageAttendanceRate;
        private int activeAlertsCount;

        public DepartmentComparisonItem() {
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCourseCount() {
            return courseCount;
        }

        public void setCourseCount(int courseCount) {
            this.courseCount = courseCount;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public void setSubjectCount(int subjectCount) {
            this.subjectCount = subjectCount;
        }

        public int getEnrollmentCount() {
            return enrollmentCount;
        }

        public void setEnrollmentCount(int enrollmentCount) {
            this.enrollmentCount = enrollmentCount;
        }

        public Double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }

        public Double getPassRate() {
            return passRate;
        }

        public void setPassRate(Double passRate) {
            this.passRate = passRate;
        }

        public Double getAverageAttendanceRate() {
            return averageAttendanceRate;
        }

        public void setAverageAttendanceRate(Double averageAttendanceRate) {
            this.averageAttendanceRate = averageAttendanceRate;
        }

        public int getActiveAlertsCount() {
            return activeAlertsCount;
        }

        public void setActiveAlertsCount(int activeAlertsCount) {
            this.activeAlertsCount = activeAlertsCount;
        }
    }

    public static class CourseComparisonItem {
        private Long courseId;
        private String code;
        private String name;
        private String departmentName;
        private int subjectCount;
        private int enrollmentCount;
        private Double averageScore;
        private Double passRate;
        private Double averageAttendanceRate;
        private int activeAlertsCount;

        public CourseComparisonItem() {
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public int getSubjectCount() {
            return subjectCount;
        }

        public void setSubjectCount(int subjectCount) {
            this.subjectCount = subjectCount;
        }

        public int getEnrollmentCount() {
            return enrollmentCount;
        }

        public void setEnrollmentCount(int enrollmentCount) {
            this.enrollmentCount = enrollmentCount;
        }

        public Double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }

        public Double getPassRate() {
            return passRate;
        }

        public void setPassRate(Double passRate) {
            this.passRate = passRate;
        }

        public Double getAverageAttendanceRate() {
            return averageAttendanceRate;
        }

        public void setAverageAttendanceRate(Double averageAttendanceRate) {
            this.averageAttendanceRate = averageAttendanceRate;
        }

        public int getActiveAlertsCount() {
            return activeAlertsCount;
        }

        public void setActiveAlertsCount(int activeAlertsCount) {
            this.activeAlertsCount = activeAlertsCount;
        }
    }

    public static class SubjectComparisonItem {
        private Long subjectId;
        private String code;
        private String name;
        private String courseCode;
        private String departmentName;
        private int credits;
        private int enrollmentCount;
        private ResultPublicationStatus publicationStatus;
        private Double averageScore;
        private Double passRate;
        private Double averageAttendanceRate;
        private int atRiskCount;

        public SubjectComparisonItem() {
        }

        public Long getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Long subjectId) {
            this.subjectId = subjectId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public int getCredits() {
            return credits;
        }

        public void setCredits(int credits) {
            this.credits = credits;
        }

        public int getEnrollmentCount() {
            return enrollmentCount;
        }

        public void setEnrollmentCount(int enrollmentCount) {
            this.enrollmentCount = enrollmentCount;
        }

        public ResultPublicationStatus getPublicationStatus() {
            return publicationStatus;
        }

        public void setPublicationStatus(ResultPublicationStatus publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        public Double getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(Double averageScore) {
            this.averageScore = averageScore;
        }

        public Double getPassRate() {
            return passRate;
        }

        public void setPassRate(Double passRate) {
            this.passRate = passRate;
        }

        public Double getAverageAttendanceRate() {
            return averageAttendanceRate;
        }

        public void setAverageAttendanceRate(Double averageAttendanceRate) {
            this.averageAttendanceRate = averageAttendanceRate;
        }

        public int getAtRiskCount() {
            return atRiskCount;
        }

        public void setAtRiskCount(int atRiskCount) {
            this.atRiskCount = atRiskCount;
        }
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalLecturers() {
        return totalLecturers;
    }

    public void setTotalLecturers(long totalLecturers) {
        this.totalLecturers = totalLecturers;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public long getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(long totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public long getPublishedSubjectsCount() {
        return publishedSubjectsCount;
    }

    public void setPublishedSubjectsCount(long publishedSubjectsCount) {
        this.publishedSubjectsCount = publishedSubjectsCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getOverallPassRate() {
        return overallPassRate;
    }

    public void setOverallPassRate(Double overallPassRate) {
        this.overallPassRate = overallPassRate;
    }

    public Double getOverallAttendanceAverage() {
        return overallAttendanceAverage;
    }

    public void setOverallAttendanceAverage(Double overallAttendanceAverage) {
        this.overallAttendanceAverage = overallAttendanceAverage;
    }

    public long getStudentsBelowThresholdCount() {
        return studentsBelowThresholdCount;
    }

    public void setStudentsBelowThresholdCount(long studentsBelowThresholdCount) {
        this.studentsBelowThresholdCount = studentsBelowThresholdCount;
    }

    public long getTotalActiveAlerts() {
        return totalActiveAlerts;
    }

    public void setTotalActiveAlerts(long totalActiveAlerts) {
        this.totalActiveAlerts = totalActiveAlerts;
    }

    public Map<String, Integer> getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(Map<String, Integer> gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }

    public AttendanceBracketCounts getAttendanceBrackets() {
        return attendanceBrackets;
    }

    public void setAttendanceBrackets(AttendanceBracketCounts attendanceBrackets) {
        this.attendanceBrackets = attendanceBrackets;
    }

    public RiskAlertsSummaryStats getRiskAlertsSummary() {
        return riskAlertsSummary;
    }

    public void setRiskAlertsSummary(RiskAlertsSummaryStats riskAlertsSummary) {
        this.riskAlertsSummary = riskAlertsSummary;
    }

    public List<DepartmentComparisonItem> getDepartmentComparisons() {
        return departmentComparisons;
    }

    public void setDepartmentComparisons(List<DepartmentComparisonItem> departmentComparisons) {
        this.departmentComparisons = departmentComparisons;
    }

    public List<CourseComparisonItem> getCourseComparisons() {
        return courseComparisons;
    }

    public void setCourseComparisons(List<CourseComparisonItem> courseComparisons) {
        this.courseComparisons = courseComparisons;
    }

    public List<SubjectComparisonItem> getSubjectComparisons() {
        return subjectComparisons;
    }

    public void setSubjectComparisons(List<SubjectComparisonItem> subjectComparisons) {
        this.subjectComparisons = subjectComparisons;
    }

    public String getProvisionalNotice() {
        return provisionalNotice;
    }

    public void setProvisionalNotice(String provisionalNotice) {
        this.provisionalNotice = provisionalNotice;
    }
}
