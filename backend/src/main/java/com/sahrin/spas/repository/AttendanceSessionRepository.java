package com.sahrin.spas.repository;

import com.sahrin.spas.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(Long subjectId, String academicYear, String semester);

    List<AttendanceSession> findBySubjectIdOrderBySessionDateDesc(Long subjectId);
}
