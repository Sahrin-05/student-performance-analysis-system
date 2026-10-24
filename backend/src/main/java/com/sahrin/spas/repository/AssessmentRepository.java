package com.sahrin.spas.repository;

import com.sahrin.spas.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(Long subjectId, String academicYear, String semester);

    List<Assessment> findBySubjectIdOrderByCreatedAtAsc(Long subjectId);

    Optional<Assessment> findByIdAndSubjectId(Long id, Long subjectId);
}
