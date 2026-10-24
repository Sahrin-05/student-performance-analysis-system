package com.sahrin.spas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertSummaryStatsResponse;
import com.sahrin.spas.dto.risk.EvaluateRiskRequest;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.dto.risk.StudentRiskAlertResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.JwtService;
import com.sahrin.spas.service.RiskAlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdminRiskAlertController.class,
        LecturerRiskAlertController.class,
        StudentRiskAlertController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RiskAlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RiskAlertService riskAlertService;

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

    @Test
    @DisplayName("ADMIN can retrieve institutional alerts summary via GET /api/admin/risk-alerts/summary")
    void testAdminGetSummaryStats() throws Exception {
        mockAuth(1L, "admin@spas.edu", Role.ADMIN);

        AlertSummaryStatsResponse stats = new AlertSummaryStatsResponse();
        stats.setTotalAlerts(12);
        stats.setActiveAlerts(5);
        stats.setCriticalAlerts(2);
        stats.setAttendanceWarnings(3);

        when(riskAlertService.getAdminSummaryStats()).thenReturn(stats);

        mockMvc.perform(get("/api/admin/risk-alerts/summary")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAlerts").value(12))
                .andExpect(jsonPath("$.activeAlerts").value(5))
                .andExpect(jsonPath("$.criticalAlerts").value(2));
    }

    @Test
    @DisplayName("ADMIN can trigger risk evaluation via POST /api/admin/risk-alerts/evaluate")
    void testAdminTriggerEvaluation() throws Exception {
        mockAuth(1L, "admin@spas.edu", Role.ADMIN);

        EvaluateRiskRequest req = new EvaluateRiskRequest(100L, "2025/2026", "SEMESTER_1");
        RiskAlertResponse resp = new RiskAlertResponse();
        resp.setId(1L);
        resp.setRiskType(AlertRiskType.ATTENDANCE_WARNING);
        resp.setSeverity(AlertSeverity.HIGH);

        when(riskAlertService.evaluateSubjectRisk(eq(100L), eq("2025/2026"), eq("SEMESTER_1"), eq("admin@spas.edu")))
                .thenReturn(List.of(resp));

        mockMvc.perform(post("/api/admin/risk-alerts/evaluate")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].riskType").value("ATTENDANCE_WARNING"));
    }

    @Test
    @DisplayName("LECTURER can record action notes via PATCH /api/academic/lecturer/risk-alerts/{id}/action")
    void testLecturerRecordAction() throws Exception {
        mockAuth(10L, "lecturer@spas.edu", Role.LECTURER);

        AlertActionRequest req = new AlertActionRequest("Met with student for academic advisement.");
        RiskAlertResponse resp = new RiskAlertResponse();
        resp.setId(1L);
        resp.setStatus(AlertStatus.ACTION_RECORDED);
        resp.setActionNotes("Met with student for academic advisement.");

        when(riskAlertService.recordAction(eq(1L), any(AlertActionRequest.class), eq("lecturer@spas.edu")))
                .thenReturn(resp);

        mockMvc.perform(patch("/api/academic/lecturer/risk-alerts/1/action")
                        .header("Authorization", "Bearer valid.lecturer.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTION_RECORDED"))
                .andExpect(jsonPath("$.actionNotes").value("Met with student for academic advisement."));
    }

    @Test
    @DisplayName("STUDENT can view own sanitized alerts via GET /api/academic/student/risk-alerts/my-alerts")
    void testStudentGetMyAlerts() throws Exception {
        mockAuth(20L, "student@spas.edu", Role.STUDENT);

        StudentRiskAlertResponse alert = new StudentRiskAlertResponse();
        alert.setId(1L);
        alert.setSubjectCode("CS101");
        alert.setSubjectName("Programming 101");
        alert.setRiskType(AlertRiskType.ATTENDANCE_WARNING);
        alert.setSeverity(AlertSeverity.HIGH);
        alert.setStatus(AlertStatus.DETECTED);
        alert.setAdvisoryGuidance("Please attend upcoming lectures.");

        when(riskAlertService.getMyAlerts("student@spas.edu")).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/academic/student/risk-alerts/my-alerts")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].subjectCode").value("CS101"))
                .andExpect(jsonPath("$[0].advisoryGuidance").value("Please attend upcoming lectures."));
    }

    @Test
    @DisplayName("STUDENT cannot access Admin risk alerts (403 Forbidden)")
    void testStudentCannotAccessAdminRiskAlerts() throws Exception {
        mockAuth(20L, "student@spas.edu", Role.STUDENT);

        mockMvc.perform(get("/api/admin/risk-alerts/summary")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("LECTURER cannot access Admin risk alerts (403 Forbidden)")
    void testLecturerCannotAccessAdminRiskAlerts() throws Exception {
        mockAuth(10L, "lecturer@spas.edu", Role.LECTURER);

        mockMvc.perform(get("/api/admin/risk-alerts/summary")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden());
    }
}
