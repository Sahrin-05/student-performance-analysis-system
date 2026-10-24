package com.sahrin.spas.repository;

import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionId(Long sessionId);

    List<AttendanceRecord> findByEnrollmentId(Long enrollmentId);

    Optional<AttendanceRecord> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId);

    List<AttendanceRecord> findBySessionIdIn(List<Long> sessionIds);

    List<AttendanceRecord> findByEnrollmentIdIn(List<Long> enrollmentIds);

    long countByEnrollmentIdAndStatus(Long enrollmentId, AttendanceStatus status);
}
