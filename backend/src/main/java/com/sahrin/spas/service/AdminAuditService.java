package com.sahrin.spas.service;

import com.sahrin.spas.dto.AdminAuditLogResponse;
import com.sahrin.spas.dto.PageResponse;

import java.util.List;

public interface AdminAuditService {

    void logAction(String action, String entityType, Long entityId, String entityIdentifier, String details, String performedByEmail);

    PageResponse<AdminAuditLogResponse> getAuditLogsPaged(String action, String entityType, String search, int page, int size);

    List<AdminAuditLogResponse> getAuditLogs(String action, String entityType, String search);
}
