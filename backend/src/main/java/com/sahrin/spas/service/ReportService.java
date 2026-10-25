package com.sahrin.spas.service;

import com.sahrin.spas.dto.report.AdminInstitutionalReportResponse;
import com.sahrin.spas.dto.report.LecturerSubjectReportResponse;
import com.sahrin.spas.dto.report.StudentReportResponse;

public interface ReportService {

    // ==========================================
    // STUDENT REPORTS & EXPORTS
    // ==========================================

    StudentReportResponse getStudentReport(String studentEmail);

    byte[] exportStudentReportCsv(String studentEmail);

    // ==========================================
    // LECTURER REPORTS & EXPORTS
    // ==========================================

    LecturerSubjectReportResponse getLecturerSubjectReport(Long subjectId, String academicYear, String semester, String lecturerEmail);

    byte[] exportLecturerSubjectPerformanceCsv(Long subjectId, String academicYear, String semester, String lecturerEmail);

    byte[] exportLecturerSubjectAttendanceCsv(Long subjectId, String academicYear, String semester, String lecturerEmail);

    byte[] exportLecturerSubjectAtRiskCsv(Long subjectId, String academicYear, String semester, String lecturerEmail);

    // ==========================================
    // ADMIN REPORTS & EXPORTS
    // ==========================================

    AdminInstitutionalReportResponse getAdminInstitutionalReport(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester);

    byte[] exportAdminSummaryCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester);

    byte[] exportAdminDepartmentComparisonCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester);

    byte[] exportAdminSubjectComparisonCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester);

    byte[] exportAdminRiskAlertsCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester);
}
