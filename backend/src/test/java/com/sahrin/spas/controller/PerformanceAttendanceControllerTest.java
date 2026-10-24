package com.sahrin.spas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.attendance.StudentAttendanceSummaryResponse;
import com.sahrin.spas.dto.performance.AssessmentRequest;
import com.sahrin.spas.dto.performance.AssessmentResponse;
import com.sahrin.spas.dto.performance.StudentPublishedResultsResponse;
import com.sahrin.spas.dto.performance.SubjectResultPublicationResponse;
import com.sahrin.spas.dto.performance.UnlockRequest;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AssessmentType;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AttendanceService;
import com.sahrin.spas.service.JwtService;
import com.sahrin.spas.service.PerformanceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        LecturerPerformanceController.class,
        StudentPerformanceController.class,
        LecturerAttendanceController.class,
        StudentAttendanceController.class,
        AdminPerformanceController.class
})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PerformanceAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PerformanceService performanceService;

    @MockitoBean
    private AttendanceService attendanceService;

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
    @DisplayName("Lecturer can create assessment via POST /api/academic/lecturer/performance/assessments")
    void testLecturerCreateAssessment() throws Exception {
        mockAuth(10L, "lecturer@spas.edu", Role.LECTURER);

        AssessmentRequest req = new AssessmentRequest(
                "Quiz 1", AssessmentType.QUIZ, 50.0, 20.0, 100L, "2025/2026", "SEMESTER_1");
        AssessmentResponse resp = new AssessmentResponse();
        resp.setId(1L);
        resp.setTitle("Quiz 1");
        resp.setType(AssessmentType.QUIZ);
        resp.setMaxMarks(50.0);
        resp.setWeightPercentage(20.0);

        when(performanceService.createAssessment(any(AssessmentRequest.class), eq("lecturer@spas.edu"))).thenReturn(resp);

        mockMvc.perform(post("/api/academic/lecturer/performance/assessments")
                        .header("Authorization", "Bearer valid.lecturer.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Quiz 1"));
    }

    @Test
    @DisplayName("Student cannot access lecturer performance endpoints (403 Forbidden)")
    void testStudentForbiddenFromLecturerPerformance() throws Exception {
        mockAuth(20L, "student@spas.edu", Role.STUDENT);

        AssessmentRequest req = new AssessmentRequest(
                "Quiz 1", AssessmentType.QUIZ, 50.0, 20.0, 100L, "2025/2026", "SEMESTER_1");

        mockMvc.perform(post("/api/academic/lecturer/performance/assessments")
                        .header("Authorization", "Bearer valid.student.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student can view published results via GET /api/academic/student/performance/enrolments/{id}/results")
    void testStudentGetResults() throws Exception {
        mockAuth(20L, "student@spas.edu", Role.STUDENT);

        StudentPublishedResultsResponse resp = new StudentPublishedResultsResponse();
        resp.setSubjectId(100L);
        resp.setSubjectCode("CS101");
        resp.setPublicationStatus(ResultPublicationStatus.PUBLISHED);
        resp.setTotalWeightedScore(85.0);
        resp.setGrade(Grade.A);
        resp.setAssessmentResults(List.of());

        when(performanceService.getMySubjectResults(eq(500L), eq("student@spas.edu"))).thenReturn(resp);

        mockMvc.perform(get("/api/academic/student/performance/enrolments/500/results")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.totalWeightedScore").value(85.0));
    }

    @Test
    @DisplayName("Student can get attendance summary via GET /api/academic/student/attendance/enrolments/{id}")
    void testStudentGetAttendanceSummary() throws Exception {
        mockAuth(20L, "student@spas.edu", Role.STUDENT);

        StudentAttendanceSummaryResponse resp = new StudentAttendanceSummaryResponse();
        resp.setEnrollmentId(500L);
        resp.setSubjectId(100L);
        resp.setSubjectCode("CS101");
        resp.setTotalSessions(10);
        resp.setEligibleSessions(8);
        resp.setAttendedSessions(7);
        resp.setAttendancePercentage(87.5);
        resp.setBelowWarningThreshold(false);
        resp.setRecords(List.of());

        when(attendanceService.getMySubjectAttendance(eq(500L), eq("student@spas.edu"))).thenReturn(resp);

        mockMvc.perform(get("/api/academic/student/attendance/enrolments/500")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendancePercentage").value(87.5))
                .andExpect(jsonPath("$.belowWarningThreshold").value(false));
    }

    @Test
    @DisplayName("Admin can unlock subject results via POST /api/admin/performance/subjects/{id}/unlock")
    void testAdminUnlockResults() throws Exception {
        mockAuth(1L, "admin@spas.edu", Role.ADMIN);

        UnlockRequest req = new UnlockRequest("Score recount approved by Dean");
        SubjectResultPublicationResponse resp = new SubjectResultPublicationResponse();
        resp.setId(1L);
        resp.setSubjectId(100L);
        resp.setStatus(ResultPublicationStatus.DRAFT);

        when(performanceService.unlockSubjectResults(eq(100L), eq("2025/2026"), eq("SEMESTER_1"), any(UnlockRequest.class), eq("admin@spas.edu")))
                .thenReturn(resp);

        mockMvc.perform(post("/api/admin/performance/subjects/100/unlock")
                        .header("Authorization", "Bearer valid.admin.token")
                        .param("academicYear", "2025/2026")
                        .param("semester", "SEMESTER_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}
