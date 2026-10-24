package com.sahrin.spas.repository;

import com.sahrin.spas.entity.LecturerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerAssignmentRepository extends JpaRepository<LecturerAssignment, Long> {

    List<LecturerAssignment> findAllByOrderByAssignedAtDesc();

    List<LecturerAssignment> findByLecturerIdAndActiveTrue(Long lecturerId);

    List<LecturerAssignment> findByLecturerEmailAndActiveTrue(String email);

    List<LecturerAssignment> findBySubjectIdAndActiveTrue(Long subjectId);

    Optional<LecturerAssignment> findBySubjectIdAndLecturerIdAndAcademicYearAndSemester(
            Long subjectId,
            Long lecturerId,
            String academicYear,
            String semester
    );

    boolean existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
            Long subjectId,
            Long lecturerId,
            String academicYear,
            String semester
    );

    boolean existsBySubjectIdAndLecturerIdAndActiveTrue(Long subjectId, Long lecturerId);

    boolean existsBySubjectId(Long subjectId);
}
