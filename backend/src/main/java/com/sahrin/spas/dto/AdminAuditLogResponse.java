package com.sahrin.spas.dto;

import com.sahrin.spas.entity.AdminAuditLog;

import java.time.LocalDateTime;

public class AdminAuditLogResponse {

    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityIdentifier;
    private String details;
    private Long performedById;
    private String performedByName;
    private String performedByEmail;
    private LocalDateTime performedAt;

    public AdminAuditLogResponse() {
    }

    public AdminAuditLogResponse(Long id, String action, String entityType, Long entityId,
                                 String entityIdentifier, String details, Long performedById,
                                 String performedByName, String performedByEmail, LocalDateTime performedAt) {
        this.id = id;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityIdentifier = entityIdentifier;
        this.details = details;
        this.performedById = performedById;
        this.performedByName = performedByName;
        this.performedByEmail = performedByEmail;
        this.performedAt = performedAt;
    }

    public static AdminAuditLogResponse from(AdminAuditLog log) {
        Long performedById = log.getPerformedBy() != null ? log.getPerformedBy().getId() : null;
        String performedByName = log.getPerformedBy() != null ? log.getPerformedBy().getFullName() : null;

        return new AdminAuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getEntityIdentifier(),
                log.getDetails(),
                performedById,
                performedByName,
                log.getPerformedByEmail(),
                log.getPerformedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    public void setEntityIdentifier(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getPerformedById() {
        return performedById;
    }

    public void setPerformedById(Long performedById) {
        this.performedById = performedById;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getPerformedByEmail() {
        return performedByEmail;
    }

    public void setPerformedByEmail(String performedByEmail) {
        this.performedByEmail = performedByEmail;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }
}
