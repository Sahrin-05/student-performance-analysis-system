package com.sahrin.spas.controller;

import com.sahrin.spas.dto.attendance.StudentAttendanceSummaryResponse;
import com.sahrin.spas.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic/student/attendance")
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/enrolments/{enrollmentId}")
    public ResponseEntity<StudentAttendanceSummaryResponse> getMySubjectAttendance(
            @PathVariable("enrollmentId") Long enrollmentId,
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        StudentAttendanceSummaryResponse response = attendanceService.getMySubjectAttendance(enrollmentId, studentEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-attendance")
    public ResponseEntity<List<StudentAttendanceSummaryResponse>> getMyAllAttendance(
            Authentication authentication
    ) {
        String studentEmail = authentication.getName();
        List<StudentAttendanceSummaryResponse> list = attendanceService.getMyAllAttendance(studentEmail);
        return ResponseEntity.ok(list);
    }
}
