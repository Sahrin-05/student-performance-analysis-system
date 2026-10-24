package com.sahrin.spas.repository;

import com.sahrin.spas.entity.SubjectResultPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectResultPublicationRepository extends JpaRepository<SubjectResultPublication, Long> {

    Optional<SubjectResultPublication> findBySubjectIdAndAcademicYearAndSemester(Long subjectId, String academicYear, String semester);
}
