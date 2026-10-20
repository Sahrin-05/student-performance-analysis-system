package com.sahrin.spas.service;

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

import java.util.List;

public interface AcademicService {

    // Department operations
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    DepartmentResponse setDepartmentStatus(Long id, boolean active);
    List<DepartmentResponse> getAllDepartments(Boolean activeOnly);
    DepartmentResponse getDepartmentById(Long id);

    // Course operations
    CourseResponse createCourse(CourseRequest request);
    CourseResponse updateCourse(Long id, CourseRequest request);
    CourseResponse setCourseStatus(Long id, boolean active);
    List<CourseResponse> getAllCourses(Long departmentId, Boolean activeOnly);
    CourseResponse getCourseById(Long id);

    // Subject operations
    SubjectResponse createSubject(SubjectRequest request);
    SubjectResponse updateSubject(Long id, SubjectRequest request);
    SubjectResponse setSubjectStatus(Long id, boolean active);
    List<SubjectResponse> getAllSubjects(Long courseId, Long departmentId, Boolean activeOnly);
    SubjectResponse getSubjectById(Long id);

    // Lecturer assignment operations
    LecturerAssignmentResponse assignLecturer(LecturerAssignmentRequest request);
    LecturerAssignmentResponse updateAssignment(Long id, LecturerAssignmentRequest request);
    LecturerAssignmentResponse setAssignmentStatus(Long id, boolean active);
    List<LecturerAssignmentResponse> getAllAssignments(Long subjectId, Long lecturerId);

    // Student enrolment operations
    EnrollmentResponse enrolStudent(EnrollmentRequest request);
    EnrollmentResponse updateEnrollmentStatus(Long id, EnrollmentStatus status);
    List<EnrollmentResponse> getAllEnrollments(Long courseId, Long subjectId, Long studentId);

    // Lecturer portal operations
    List<SubjectResponse> getLecturerAssignedSubjects(String lecturerEmail);
    List<EnrollmentResponse> getLecturerSubjectStudents(Long subjectId, String lecturerEmail);

    // Student portal operations
    List<EnrollmentResponse> getStudentEnrolments(String studentEmail);
}
