package com.sahrin.spas.dto.attendance;

import java.util.List;

public class StudentAttendanceSummaryResponse {

    private Long enrollmentId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private int credits;
    private String academicYear;
    private String semester;
    private int totalSessions;
    private int excusedSessions;
    private int eligibleSessions;
    private int attendedSessions;
    private int absentSessions;
    private double attendancePercentage;
    private boolean belowWarningThreshold;
    private double warningThreshold = 80.0;
    private String warningMessage;
    private List<AttendanceRecordResponse> records;

    public StudentAttendanceSummaryResponse() {
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

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
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

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public boolean isBelowWarningThreshold() {
        return belowWarningThreshold;
    }

    public void setBelowWarningThreshold(boolean belowWarningThreshold) {
        this.belowWarningThreshold = belowWarningThreshold;
    }

    public double getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public List<AttendanceRecordResponse> getRecords() {
        return records;
    }

    public void setRecords(List<AttendanceRecordResponse> records) {
        this.records = records;
    }
}
