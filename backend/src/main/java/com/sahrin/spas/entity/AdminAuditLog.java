package com.sahrin.spas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "admin_audit_logs",
    indexes = {
        @Index(name = "idx_admin_audit_action", columnList = "action"),
        @Index(name = "idx_admin_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_admin_audit_performed_at", columnList = "performed_at")
    }
)
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_identifier", length = 100)
    private String entityIdentifier;

    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    private String details;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    @Column(name = "performed_by_email", nullable = false, length = 100)
    private String performedByEmail;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    public AdminAuditLog() {
    }

    public AdminAuditLog(String action, String entityType, Long entityId, String entityIdentifier,
                         String details, User performedBy, String performedByEmail) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityIdentifier = entityIdentifier;
        this.details = details;
        this.performedBy = performedBy;
        this.performedByEmail = performedByEmail;
    }

    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
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

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
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
