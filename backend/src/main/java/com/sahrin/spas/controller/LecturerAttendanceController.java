package com.sahrin.spas.controller;

import com.sahrin.spas.dto.attendance.AttendanceRecordResponse;
import com.sahrin.spas.dto.attendance.AttendanceSessionRequest;
import com.sahrin.spas.dto.attendance.AttendanceSessionResponse;
import com.sahrin.spas.dto.attendance.RecordAttendanceRequest;
import com.sahrin.spas.dto.attendance.SessionRosterAttendanceResponse;
import com.sahrin.spas.dto.attendance.SubjectAttendanceSummaryResponse;
import com.sahrin.spas.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic/lecturer/attendance")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerAttendanceController {

    private final AttendanceService attendanceService;

    public LecturerAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionResponse> createSession(
            @Valid @RequestBody AttendanceSessionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AttendanceSessionResponse response = attendanceService.createSession(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<AttendanceSessionResponse> updateSession(
            @PathVariable("id") Long id,
            @Valid @RequestBody AttendanceSessionRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AttendanceSessionResponse response = attendanceService.updateSession(id, request, email);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        attendanceService.deleteSession(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subjects/{subjectId}/sessions")
    public ResponseEntity<List<AttendanceSessionResponse>> getSubjectSessions(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<AttendanceSessionResponse> list = attendanceService.getSubjectSessions(subjectId, academicYear, semester, email);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/sessions/{sessionId}/roster")
    public ResponseEntity<SessionRosterAttendanceResponse> getSessionRoster(
            @PathVariable("sessionId") Long sessionId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SessionRosterAttendanceResponse response = attendanceService.getSessionRoster(sessionId, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/records")
    public ResponseEntity<List<AttendanceRecordResponse>> recordAttendance(
            @Valid @RequestBody RecordAttendanceRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<AttendanceRecordResponse> list = attendanceService.recordSessionAttendance(request, email);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/subjects/{subjectId}/summary")
    public ResponseEntity<SubjectAttendanceSummaryResponse> getSubjectAttendanceSummary(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam("academicYear") String academicYear,
            @RequestParam("semester") String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        SubjectAttendanceSummaryResponse response = attendanceService.getSubjectAttendanceSummary(
                subjectId, academicYear, semester, email);
        return ResponseEntity.ok(response);
    }
}
