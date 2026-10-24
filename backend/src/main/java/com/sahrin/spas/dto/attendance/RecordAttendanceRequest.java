package com.sahrin.spas.dto.attendance;

import com.sahrin.spas.entity.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RecordAttendanceRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotEmpty(message = "Attendance records list cannot be empty")
    @Valid
    private List<StudentAttendanceItem> records;

    public RecordAttendanceRequest() {
    }

    public RecordAttendanceRequest(Long sessionId, List<StudentAttendanceItem> records) {
        this.sessionId = sessionId;
        this.records = records;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public List<StudentAttendanceItem> getRecords() {
        return records;
    }

    public void setRecords(List<StudentAttendanceItem> records) {
        this.records = records;
    }

    public static class StudentAttendanceItem {
        @NotNull(message = "Enrollment ID is required")
        private Long enrollmentId;

        @NotNull(message = "Attendance status is required")
        private AttendanceStatus status;

        @Size(max = 255, message = "Remarks cannot exceed 255 characters")
        private String remarks;

        public StudentAttendanceItem() {
        }

        public StudentAttendanceItem(Long enrollmentId, AttendanceStatus status, String remarks) {
            this.enrollmentId = enrollmentId;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getEnrollmentId() {
            return enrollmentId;
        }

        public void setEnrollmentId(Long enrollmentId) {
            this.enrollmentId = enrollmentId;
        }

        public AttendanceStatus getStatus() {
            return status;
        }

        public void setStatus(AttendanceStatus status) {
            this.status = status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
