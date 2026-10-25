package com.sahrin.spas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.report.AdminInstitutionalReportResponse;
import com.sahrin.spas.dto.report.LecturerSubjectReportResponse;
import com.sahrin.spas.dto.report.StudentReportResponse;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.JwtService;
import com.sahrin.spas.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminReportController.class,
        LecturerReportController.class,
        StudentReportController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private void mockAuth(Long userId, String email, Role role) {
        String token = "valid." + role.name().toLowerCase() + ".token";
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.extractSubject(token)).thenReturn(email);

        User user = new User("User", "ID-" + userId, email, "$2a$10$hash", role, UserStatus.ACTIVE);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    @Test
    @DisplayName("ADMIN can access institutional report via GET /api/admin/reports/institutional")
    void testAdminGetInstitutionalReport() throws Exception {
        mockAuth(1L, "admin@spas.edu", Role.ADMIN);

        AdminInstitutionalReportResponse resp = new AdminInstitutionalReportResponse();
        resp.setTotalStudents(50L);
        resp.setTotalLecturers(10L);
        resp.setAverageScore(72.5);

        when(reportService.getAdminInstitutionalReport(any(), any(), any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/admin/reports/institutional")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").value(50))
                .andExpect(jsonPath("$.totalLecturers").value(10))
                .andExpect(jsonPath("$.averageScore").value(72.5));
    }

    @Test
    @DisplayName("STUDENT receives 403 Forbidden when requesting admin institutional report")
    void testStudentForbiddenOnAdminReport() throws Exception {
        mockAuth(2L, "student@spas.edu", Role.STUDENT);

        mockMvc.perform(get("/api/admin/reports/institutional")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can export summary CSV via GET /api/admin/reports/export/summary")
    void testAdminExportSummaryCsv() throws Exception {
        mockAuth(1L, "admin@spas.edu", Role.ADMIN);

        byte[] fakeCsv = "Metric,Value\r\nTotal Students,50\r\n".getBytes(StandardCharsets.UTF_8);
        when(reportService.exportAdminSummaryCsv(any(), any(), any(), any(), any())).thenReturn(fakeCsv);

        mockMvc.perform(get("/api/admin/reports/export/summary")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"institutional_summary_report.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().bytes(fakeCsv));
    }

    // ==========================================
    // LECTURER ENDPOINTS
    // ==========================================

    @Test
    @DisplayName("LECTURER can access subject report via GET /api/academic/lecturer/reports/subject/{subjectId}")
    void testLecturerGetSubjectReport() throws Exception {
        mockAuth(3L, "lecturer@spas.edu", Role.LECTURER);

        LecturerSubjectReportResponse resp = new LecturerSubjectReportResponse();
        resp.setSubjectId(100L);
        resp.setSubjectCode("CS101");
        resp.setEnrolledStudentsCount(30);
        resp.setAverageScore(68.4);

        when(reportService.getLecturerSubjectReport(eq(100L), any(), any(), eq("lecturer@spas.edu"))).thenReturn(resp);

        mockMvc.perform(get("/api/academic/lecturer/reports/subject/100")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectId").value(100))
                .andExpect(jsonPath("$.subjectCode").value("CS101"))
                .andExpect(jsonPath("$.enrolledStudentsCount").value(30))
                .andExpect(jsonPath("$.averageScore").value(68.4));
    }

    @Test
    @DisplayName("STUDENT receives 403 Forbidden when accessing lecturer subject report")
    void testStudentForbiddenOnLecturerReport() throws Exception {
        mockAuth(2L, "student@spas.edu", Role.STUDENT);

        mockMvc.perform(get("/api/academic/lecturer/reports/subject/100")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("LECTURER can export subject performance CSV via GET /api/academic/lecturer/reports/export/subject/{subjectId}")
    void testLecturerExportPerformanceCsv() throws Exception {
        mockAuth(3L, "lecturer@spas.edu", Role.LECTURER);

        byte[] fakeCsv = "Student ID,Score\r\nSTU-001,85\r\n".getBytes(StandardCharsets.UTF_8);
        when(reportService.exportLecturerSubjectPerformanceCsv(eq(100L), any(), any(), eq("lecturer@spas.edu"))).thenReturn(fakeCsv);

        mockMvc.perform(get("/api/academic/lecturer/reports/export/subject/100")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"subject_100_performance_report.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().bytes(fakeCsv));
    }

    // ==========================================
    // STUDENT ENDPOINTS
    // ==========================================

    @Test
    @DisplayName("STUDENT can access own academic report via GET /api/academic/student/reports/my-report")
    void testStudentGetMyReport() throws Exception {
        mockAuth(2L, "student@spas.edu", Role.STUDENT);

        StudentReportResponse resp = new StudentReportResponse();
        resp.setStudentIdentifier("STU-001");
        resp.setGpa(3.75);
        resp.setTotalCreditsEarned(15);

        when(reportService.getStudentReport("student@spas.edu")).thenReturn(resp);

        mockMvc.perform(get("/api/academic/student/reports/my-report")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentIdentifier").value("STU-001"))
                .andExpect(jsonPath("$.gpa").value(3.75))
                .andExpect(jsonPath("$.totalCreditsEarned").value(15));
    }

    @Test
    @DisplayName("LECTURER receives 403 Forbidden when accessing student personal report")
    void testLecturerForbiddenOnStudentReport() throws Exception {
        mockAuth(3L, "lecturer@spas.edu", Role.LECTURER);

        mockMvc.perform(get("/api/academic/student/reports/my-report")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("STUDENT can export own report CSV via GET /api/academic/student/reports/export/my-report")
    void testStudentExportReportCsv() throws Exception {
        mockAuth(2L, "student@spas.edu", Role.STUDENT);

        byte[] fakeCsv = "Subject,Grade\r\nCS101,A\r\n".getBytes(StandardCharsets.UTF_8);
        when(reportService.exportStudentReportCsv("student@spas.edu")).thenReturn(fakeCsv);

        mockMvc.perform(get("/api/academic/student/reports/export/my-report")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_academic_transcript_report.csv\""))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().bytes(fakeCsv));
    }
}
