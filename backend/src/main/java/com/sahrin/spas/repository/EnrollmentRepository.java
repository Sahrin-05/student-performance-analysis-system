package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findAllByOrderByEnrolledAtDesc();

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByStudentEmail(String email);

    List<Enrollment> findByStudentEmailAndStatus(String email, EnrollmentStatus status);

    List<Enrollment> findBySubjectId(Long subjectId);

    List<Enrollment> findBySubjectIdAndStatus(Long subjectId, EnrollmentStatus status);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndSubjectIdAndAcademicYearAndSemester(
            Long studentId,
            Long subjectId,
            String academicYear,
            String semester
    );

    boolean existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndStatus(
            Long studentId,
            Long subjectId,
            String academicYear,
            String semester,
            EnrollmentStatus status
    );

    boolean existsBySubjectId(Long subjectId);

    boolean existsByCourseId(Long courseId);

    long countBySubjectIdAndStatus(Long subjectId, EnrollmentStatus status);
}
