package com.sahrin.spas.repository;

import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    // Student-scoped queries
    List<RiskAlert> findByStudentIdOrderByDetectedAtDesc(Long studentId);

    // Subject-scoped queries
    List<RiskAlert> findBySubjectIdOrderByDetectedAtDesc(Long subjectId);

    List<RiskAlert> findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(
            Long subjectId, String academicYear, String semester);

    // Lecturer-scoped across multiple assigned subjects
    List<RiskAlert> findBySubjectIdInOrderByDetectedAtDesc(Collection<Long> subjectIds);

    List<RiskAlert> findBySubjectIdInAndAcademicYearAndSemesterOrderByDetectedAtDesc(
            Collection<Long> subjectIds, String academicYear, String semester);

    // System-wide queries
    List<RiskAlert> findAllByOrderByDetectedAtDesc();

    // Deduplication check: check if an active (non-RESOLVED) alert already exists
    boolean existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
            Long studentId, Long subjectId, String academicYear, String semester,
            AlertRiskType riskType, AlertStatus excludedStatus);

    Optional<RiskAlert> findFirstByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
            Long studentId, Long subjectId, String academicYear, String semester,
            AlertRiskType riskType, AlertStatus excludedStatus);

    // Statistical counts for Admin & Lecturer Dashboards
    long countByStatusNot(AlertStatus status);

    long countBySeverityAndStatusNot(AlertSeverity severity, AlertStatus status);

    long countByRiskTypeAndStatusNot(AlertRiskType riskType, AlertStatus status);

    long countByStatus(AlertStatus status);

    @Query("SELECT COUNT(ra) FROM RiskAlert ra WHERE ra.subject.id IN :subjectIds AND ra.status <> :excludedStatus")
    long countBySubjectIdInAndStatusNot(@Param("subjectIds") Collection<Long> subjectIds,
                                        @Param("excludedStatus") AlertStatus excludedStatus);

    @Query("SELECT COUNT(ra) FROM RiskAlert ra WHERE ra.subject.id IN :subjectIds AND ra.severity = :severity AND ra.status <> :excludedStatus")
    long countBySubjectIdInAndSeverityAndStatusNot(@Param("subjectIds") Collection<Long> subjectIds,
                                                  @Param("severity") AlertSeverity severity,
                                                  @Param("excludedStatus") AlertStatus excludedStatus);
}
