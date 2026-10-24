package com.sahrin.spas.service;

import com.sahrin.spas.dto.attendance.AttendanceRecordResponse;
import com.sahrin.spas.dto.attendance.AttendanceSessionRequest;
import com.sahrin.spas.dto.attendance.AttendanceSessionResponse;
import com.sahrin.spas.dto.attendance.RecordAttendanceRequest;
import com.sahrin.spas.dto.attendance.SessionRosterAttendanceResponse;
import com.sahrin.spas.dto.attendance.StudentAttendanceSummaryResponse;
import com.sahrin.spas.dto.attendance.SubjectAttendanceSummaryResponse;

import java.util.List;

public interface AttendanceService {

    // Lecturer session management
    AttendanceSessionResponse createSession(AttendanceSessionRequest request, String lecturerEmail);
    AttendanceSessionResponse updateSession(Long sessionId, AttendanceSessionRequest request, String lecturerEmail);
    void deleteSession(Long sessionId, String lecturerEmail);
    List<AttendanceSessionResponse> getSubjectSessions(Long subjectId, String academicYear, String semester, String lecturerEmail);

    // Lecturer attendance marking
    SessionRosterAttendanceResponse getSessionRoster(Long sessionId, String lecturerEmail);
    List<AttendanceRecordResponse> recordSessionAttendance(RecordAttendanceRequest request, String lecturerEmail);
    SubjectAttendanceSummaryResponse getSubjectAttendanceSummary(Long subjectId, String academicYear, String semester, String lecturerEmail);

    // Student attendance tracking
    StudentAttendanceSummaryResponse getMySubjectAttendance(Long enrollmentId, String studentEmail);
    List<StudentAttendanceSummaryResponse> getMyAllAttendance(String studentEmail);
}
