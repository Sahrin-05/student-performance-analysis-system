package com.sahrin.spas.controller;

import com.sahrin.spas.dto.report.AdminInstitutionalReportResponse;
import com.sahrin.spas.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/institutional")
    public ResponseEntity<AdminInstitutionalReportResponse> getInstitutionalReport(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester
    ) {
        AdminInstitutionalReportResponse response = reportService.getAdminInstitutionalReport(
                departmentId, courseId, subjectId, academicYear, semester);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/summary")
    public ResponseEntity<byte[]> exportSummaryCsv(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester
    ) {
        byte[] csv = reportService.exportAdminSummaryCsv(departmentId, courseId, subjectId, academicYear, semester);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"institutional_summary_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/departments")
    public ResponseEntity<byte[]> exportDepartmentsCsv(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester
    ) {
        byte[] csv = reportService.exportAdminDepartmentComparisonCsv(departmentId, courseId, subjectId, academicYear, semester);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"departments_performance_comparison.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/subjects")
    public ResponseEntity<byte[]> exportSubjectsCsv(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester
    ) {
        byte[] csv = reportService.exportAdminSubjectComparisonCsv(departmentId, courseId, subjectId, academicYear, semester);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subjects_performance_comparison.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/risk")
    public ResponseEntity<byte[]> exportRiskCsv(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            @RequestParam(value = "semester", required = false) String semester
    ) {
        byte[] csv = reportService.exportAdminRiskAlertsCsv(departmentId, courseId, subjectId, academicYear, semester);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"institutional_risk_alerts_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
