package com.sahrin.spas.repository;

import com.sahrin.spas.entity.StudentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {

    List<StudentMark> findByAssessmentId(Long assessmentId);

    List<StudentMark> findByEnrollmentId(Long enrollmentId);

    Optional<StudentMark> findByAssessmentIdAndEnrollmentId(Long assessmentId, Long enrollmentId);

    List<StudentMark> findByEnrollmentIdIn(List<Long> enrollmentIds);

    List<StudentMark> findByAssessmentIdIn(List<Long> assessmentIds);
}
