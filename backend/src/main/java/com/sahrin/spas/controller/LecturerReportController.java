package com.sahrin.spas.controller;

import com.sahrin.spas.dto.report.LecturerSubjectReportResponse;
import com.sahrin.spas.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academic/lecturer/reports")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerReportController {

    private final ReportService reportService;

    public LecturerReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<LecturerSubjectReportResponse> getSubjectReport(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        LecturerSubjectReportResponse response = reportService.getLecturerSubjectReport(
                subjectId, academicYear, semester, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/subject/{subjectId}")
    public ResponseEntity<byte[]> exportSubjectPerformanceCsv(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        byte[] csv = reportService.exportLecturerSubjectPerformanceCsv(subjectId, academicYear, semester, email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"subject_%d_performance_report.csv\"", subjectId))
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/subject/{subjectId}/attendance")
    public ResponseEntity<byte[]> exportSubjectAttendanceCsv(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        byte[] csv = reportService.exportLecturerSubjectAttendanceCsv(subjectId, academicYear, semester, email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"subject_%d_attendance_report.csv\"", subjectId))
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/subject/{subjectId}/at-risk")
    public ResponseEntity<byte[]> exportSubjectAtRiskCsv(
            @PathVariable("subjectId") Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester,
            Authentication authentication
    ) {
        String email = authentication.getName();
        byte[] csv = reportService.exportLecturerSubjectAtRiskCsv(subjectId, academicYear, semester, email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"subject_%d_at_risk_students.csv\"", subjectId))
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
