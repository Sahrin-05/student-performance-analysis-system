package com.sahrin.spas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sahrin.spas.config.SecurityConfig;
import com.sahrin.spas.dto.academic.CourseRequest;
import com.sahrin.spas.dto.academic.CourseResponse;
import com.sahrin.spas.dto.academic.DepartmentRequest;
import com.sahrin.spas.dto.academic.DepartmentResponse;
import com.sahrin.spas.dto.academic.EnrollmentRequest;
import com.sahrin.spas.dto.academic.EnrollmentResponse;
import com.sahrin.spas.dto.academic.LecturerAssignmentRequest;
import com.sahrin.spas.dto.academic.LecturerAssignmentResponse;
import com.sahrin.spas.dto.academic.SubjectRequest;
import com.sahrin.spas.dto.academic.SubjectResponse;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.entity.User.UserStatus;
import com.sahrin.spas.exception.DuplicateResourceException;
import com.sahrin.spas.exception.GlobalExceptionHandler;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AcademicService;
import com.sahrin.spas.service.JwtService;
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

@WebMvcTest(controllers = AdminAcademicController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminAcademicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    // ==========================================
    // DEPARTMENTS
    // ==========================================

    @Test
    @DisplayName("GET /api/admin/academic/departments with ADMIN token returns 200 OK")
    void getDepartments_admin_returns200() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        DepartmentResponse dept = new DepartmentResponse(1L, "CS", "Computer Science", "Desc", true, LocalDateTime.now(), LocalDateTime.now());
        when(academicService.getAllDepartments(null)).thenReturn(List.of(dept));

        mockMvc.perform(get("/api/admin/academic/departments")
                        .header("Authorization", "Bearer valid.admin.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("CS"))
                .andExpect(jsonPath("$[0].name").value("Computer Science"));
    }

    @Test
    @DisplayName("GET /api/admin/academic/departments without token returns 403 Forbidden")
    void getDepartments_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/academic/departments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/academic/departments with STUDENT token returns 403 Forbidden")
    void getDepartments_student_returnsForbidden() throws Exception {
        mockAuth(2L, "student@university.edu", Role.STUDENT);

        mockMvc.perform(get("/api/admin/academic/departments")
                        .header("Authorization", "Bearer valid.student.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/academic/departments with valid body returns 201 Created")
    void createDepartment_valid_returns201() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        DepartmentRequest req = new DepartmentRequest("ENG", "Engineering", "Engineering Dept");
        DepartmentResponse res = new DepartmentResponse(2L, "ENG", "Engineering", "Engineering Dept", true, LocalDateTime.now(), LocalDateTime.now());

        when(academicService.createDepartment(any(DepartmentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/admin/academic/departments")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.code").value("ENG"));
    }

    @Test
    @DisplayName("POST /api/admin/academic/departments with blank fields returns 400 Bad Request")
    void createDepartment_invalid_returns400() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        DepartmentRequest req = new DepartmentRequest("", "", "");

        mockMvc.perform(post("/api/admin/academic/departments")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /api/admin/academic/departments with duplicate code returns 409 Conflict")
    void createDepartment_duplicate_returns409() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        DepartmentRequest req = new DepartmentRequest("CS", "Computer Science", "Desc");
        when(academicService.createDepartment(any(DepartmentRequest.class)))
                .thenThrow(new DuplicateResourceException("Department with code 'CS' already exists."));

        mockMvc.perform(post("/api/admin/academic/departments")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Department with code 'CS' already exists."));
    }

    // ==========================================
    // COURSES & SUBJECTS
    // ==========================================

    @Test
    @DisplayName("POST /api/admin/academic/courses returns 201 Created")
    void createCourse_valid_returns201() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        CourseRequest req = new CourseRequest("BCS", "Computer Science Degree", "Desc", 1L);
        CourseResponse res = new CourseResponse(10L, "BCS", "Computer Science Degree", "Desc", 1L, "CS", "Computer Science", true, LocalDateTime.now(), LocalDateTime.now());

        when(academicService.createCourse(any(CourseRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/admin/academic/courses")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("BCS"));
    }

    @Test
    @DisplayName("POST /api/admin/academic/subjects returns 201 Created")
    void createSubject_valid_returns201() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        SubjectRequest req = new SubjectRequest("CS101", "Programming", "Intro", 3, 1, 10L);
        SubjectResponse res = new SubjectResponse(100L, "CS101", "Programming", "Intro", 3, 1, 10L, "BCS", "Computer Science Degree", 1L, "CS", "Computer Science", true, LocalDateTime.now(), LocalDateTime.now());

        when(academicService.createSubject(any(SubjectRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/admin/academic/subjects")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.code").value("CS101"));
    }

    // ==========================================
    // ASSIGNMENTS & ENROLMENTS
    // ==========================================

    @Test
    @DisplayName("POST /api/admin/academic/lecturer-assignments returns 201 Created")
    void assignLecturer_valid_returns201() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        LecturerAssignmentRequest req = new LecturerAssignmentRequest(100L, 50L, "2025/2026", "Semester 1");
        LecturerAssignmentResponse res = new LecturerAssignmentResponse(1L, 100L, "CS101", "Programming", "BCS", 50L, "Dr. Alice", "alice@university.edu", "LEC-01", "2025/2026", "Semester 1", true, LocalDateTime.now());

        when(academicService.assignLecturer(any(LecturerAssignmentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/admin/academic/lecturer-assignments")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lecturerName").value("Dr. Alice"));
    }

    @Test
    @DisplayName("POST /api/admin/academic/enrolments returns 201 Created")
    void enrolStudent_valid_returns201() throws Exception {
        mockAuth(1L, "admin@university.edu", Role.ADMIN);

        EnrollmentRequest req = new EnrollmentRequest(60L, 10L, 100L, "2025/2026", "Semester 1");
        EnrollmentResponse res = new EnrollmentResponse(1L, 60L, "Bob Smith", "bob@student.university.edu", "STU-01", 10L, "BCS", "Bachelor of CS", 100L, "CS101", "Programming", 3, "2025/2026", "Semester 1", EnrollmentStatus.ACTIVE, LocalDateTime.now());

        when(academicService.enrolStudent(any(EnrollmentRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/admin/academic/enrolments")
                        .header("Authorization", "Bearer valid.admin.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentName").value("Bob Smith"));
    }
}
