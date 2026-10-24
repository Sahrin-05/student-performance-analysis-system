package com.sahrin.spas.dto.attendance;

import java.util.List;

public class SessionRosterAttendanceResponse {

    private AttendanceSessionResponse session;
    private List<StudentAttendanceItem> rosterRecords;

    public SessionRosterAttendanceResponse() {
    }

    public SessionRosterAttendanceResponse(AttendanceSessionResponse session, List<StudentAttendanceItem> rosterRecords) {
        this.session = session;
        this.rosterRecords = rosterRecords;
    }

    public static class StudentAttendanceItem {
        private Long enrollmentId;
        private Long studentId;
        private String studentIdentifier;
        private String studentName;
        private String studentEmail;
        private String status; // PRESENT, ABSENT, LATE, EXCUSED, or UNRECORDED
        private String remarks;

        public StudentAttendanceItem() {
        }

        public StudentAttendanceItem(Long enrollmentId, Long studentId, String studentIdentifier,
                                     String studentName, String studentEmail, String status, String remarks) {
            this.enrollmentId = enrollmentId;
            this.studentId = studentId;
            this.studentIdentifier = studentIdentifier;
            this.studentName = studentName;
            this.studentEmail = studentEmail;
            this.status = status;
            this.remarks = remarks;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }

    public AttendanceSessionResponse getSession() {
        return session;
    }

    public void setSession(AttendanceSessionResponse session) {
        this.session = session;
    }

    public List<StudentAttendanceItem> getRosterRecords() {
        return rosterRecords;
    }

    public void setRosterRecords(List<StudentAttendanceItem> rosterRecords) {
        this.rosterRecords = rosterRecords;
    }
}
