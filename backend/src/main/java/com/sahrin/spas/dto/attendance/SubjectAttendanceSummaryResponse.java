package com.sahrin.spas.dto.attendance;

import java.util.List;

public class SubjectAttendanceSummaryResponse {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private String semester;
    private int totalSessions;
    private double overallAttendanceRate;
    private List<StudentSubjectAttendanceRate> studentSummaries;

    public SubjectAttendanceSummaryResponse() {
    }

    public static class StudentSubjectAttendanceRate {
        private Long enrollmentId;
        private Long studentId;
        private String studentIdentifier;
        private String studentName;
        private int totalSessions;
        private int excusedSessions;
        private int eligibleSessions;
        private int attendedSessions;
        private int absentSessions;
        private double attendancePercentage;
        private boolean belowWarningThreshold;

        public StudentSubjectAttendanceRate() {
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

    public int getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(int totalSessions) {
        this.totalSessions = totalSessions;
    }

    public double getOverallAttendanceRate() {
        return overallAttendanceRate;
    }

    public void setOverallAttendanceRate(double overallAttendanceRate) {
        this.overallAttendanceRate = overallAttendanceRate;
    }

    public List<StudentSubjectAttendanceRate> getStudentSummaries() {
        return studentSummaries;
    }

    public void setStudentSummaries(List<StudentSubjectAttendanceRate> studentSummaries) {
        this.studentSummaries = studentSummaries;
    }
}
