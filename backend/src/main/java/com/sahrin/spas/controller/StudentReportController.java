package com.sahrin.spas.controller;

import com.sahrin.spas.dto.report.StudentReportResponse;
import com.sahrin.spas.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academic/student/reports")
@PreAuthorize("hasRole('STUDENT')")
public class StudentReportController {

    private final ReportService reportService;

    public StudentReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/my-report")
    public ResponseEntity<StudentReportResponse> getMyAcademicReport(Authentication authentication) {
        String studentEmail = authentication.getName();
        StudentReportResponse response = reportService.getStudentReport(studentEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/my-report")
    public ResponseEntity<byte[]> exportMyAcademicReportCsv(Authentication authentication) {
        String studentEmail = authentication.getName();
        byte[] csv = reportService.exportStudentReportCsv(studentEmail);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_academic_transcript_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
