package com.sahrin.spas.repository;

import com.sahrin.spas.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    @Query("""
        SELECT a FROM AdminAuditLog a
        WHERE (:action IS NULL OR a.action = :action)
          AND (:entityType IS NULL OR a.entityType = :entityType)
          AND (:search IS NULL OR
               LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.entityIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.performedByEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.performedAt DESC
    """)
    Page<AdminAuditLog> searchLogs(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM AdminAuditLog a
        WHERE (:action IS NULL OR a.action = :action)
          AND (:entityType IS NULL OR a.entityType = :entityType)
          AND (:search IS NULL OR
               LOWER(a.details) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.entityIdentifier) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.performedByEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY a.performedAt DESC
    """)
    List<AdminAuditLog> searchLogsList(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("search") String search
    );
}
