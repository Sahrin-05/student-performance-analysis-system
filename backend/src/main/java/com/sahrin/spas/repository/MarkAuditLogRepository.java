package com.sahrin.spas.repository;

import com.sahrin.spas.entity.MarkAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarkAuditLogRepository extends JpaRepository<MarkAuditLog, Long> {

    List<MarkAuditLog> findByMarkIdOrderByPerformedAtDesc(Long markId);

    List<MarkAuditLog> findByEnrollmentIdOrderByPerformedAtDesc(Long enrollmentId);

    List<MarkAuditLog> findByAssessmentIdOrderByPerformedAtDesc(Long assessmentId);
}
