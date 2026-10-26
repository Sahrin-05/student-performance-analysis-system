package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.AdminAuditLogResponse;
import com.sahrin.spas.dto.PageResponse;
import com.sahrin.spas.entity.AdminAuditLog;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.repository.AdminAuditLogRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AdminAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AdminAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AdminAuditServiceImpl(AdminAuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void logAction(String action, String entityType, Long entityId, String entityIdentifier, String details, String performedByEmail) {
        String email = (performedByEmail != null && !performedByEmail.trim().isEmpty())
                ? performedByEmail.trim()
                : "system";

        User performedBy = userRepository.findByEmail(email.toLowerCase()).orElse(null);

        AdminAuditLog log = new AdminAuditLog(
                action,
                entityType,
                entityId,
                entityIdentifier,
                details,
                performedBy,
                email
        );

        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAuditLogResponse> getAuditLogsPaged(String action, String entityType, String search, int page, int size) {
        String act = (action != null && !action.trim().isEmpty()) ? action.trim() : null;
        String type = (entityType != null && !entityType.trim().isEmpty()) ? entityType.trim() : null;
        String query = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        int pageNum = Math.max(page, 0);
        int pageSize = (size > 0) ? Math.min(size, 100) : 20;

        Page<AdminAuditLog> pageResult = auditLogRepository.searchLogs(act, type, query, PageRequest.of(pageNum, pageSize));
        return PageResponse.from(pageResult, AdminAuditLogResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAuditLogResponse> getAuditLogs(String action, String entityType, String search) {
        String act = (action != null && !action.trim().isEmpty()) ? action.trim() : null;
        String type = (entityType != null && !entityType.trim().isEmpty()) ? entityType.trim() : null;
        String query = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<AdminAuditLog> list = auditLogRepository.searchLogsList(act, type, query);
        return list.stream().map(AdminAuditLogResponse::from).toList();
    }
}
