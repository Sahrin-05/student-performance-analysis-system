package com.sahrin.spas.controller;

import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.academic.EnrollmentResponse;
import com.sahrin.spas.dto.academic.SubjectResponse;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AcademicService;
import com.sahrin.spas.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AcademicController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AcademicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcademicService academicService;

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
    @DisplayName("GET /api/academic/lecturer/my-subjects with LECTURER token returns 200 OK")
    void getMyAssignedSubjects_lecturer_returns200() throws Exception {
        mockAuth(50L, "alice@university.edu", Role.LECTURER);

        SubjectResponse s = new SubjectResponse(100L, "CS101", "Programming", "Intro", 3, 1, 10L, "BCS", "Computer Science", 1L, "CS", "Computer Science", true, LocalDateTime.now(), LocalDateTime.now());
        when(academicService.getLecturerAssignedSubjects("alice@university.edu")).thenReturn(List.of(s));

        mockMvc.perform(get("/api/academic/lecturer/my-subjects")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("CS101"));
    }

    @Test
    @DisplayName("GET /api/academic/lecturer/my-subjects with STUDENT token returns 403 Forbidden")
    void getMyAssignedSubjects_student_returnsForbidden() throws Exception {
        mockAuth(60L, "bob@student.university.edu", Role.STUDENT);

        mockMvc.perform(get("/api/academic/lecturer/my-subjects")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/academic/lecturer/subjects/{id}/students when unassigned returns 403 Forbidden")
    void getMySubjectStudents_unassigned_returns403() throws Exception {
        mockAuth(50L, "alice@university.edu", Role.LECTURER);

        when(academicService.getLecturerSubjectStudents(100L, "alice@university.edu"))
                .thenThrow(new AccessDeniedException("Access denied: You are not actively assigned to teach this subject."));

        mockMvc.perform(get("/api/academic/lecturer/subjects/100/students")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to access this resource."));
    }

    @Test
    @DisplayName("GET /api/academic/student/my-enrolments with STUDENT token returns 200 OK")
    void getMyEnrolments_student_returns200() throws Exception {
        mockAuth(60L, "bob@student.university.edu", Role.STUDENT);

        EnrollmentResponse e = new EnrollmentResponse(1L, 60L, "Bob Smith", "bob@student.university.edu", "STU-01", 10L, "BCS", "Bachelor of CS", 100L, "CS101", "Programming", 3, "2025/2026", "Semester 1", EnrollmentStatus.ACTIVE, LocalDateTime.now());
        when(academicService.getStudentEnrolments("bob@student.university.edu")).thenReturn(List.of(e));

        mockMvc.perform(get("/api/academic/student/my-enrolments")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subjectCode").value("CS101"));
    }

    @Test
    @DisplayName("GET /api/academic/student/my-enrolments with LECTURER token returns 403 Forbidden")
    void getMyEnrolments_lecturer_returnsForbidden() throws Exception {
        mockAuth(50L, "alice@university.edu", Role.LECTURER);

        mockMvc.perform(get("/api/academic/student/my-enrolments")
                        .header("Authorization", "Bearer valid.lecturer.token"))
                .andExpect(status().isForbidden());
    }
}
