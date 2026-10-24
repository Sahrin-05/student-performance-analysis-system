package com.sahrin.spas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "subject_result_publications",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_result_pub_subj_year_sem", columnNames = {"subject_id", "academic_year", "semester"})
    }
)
public class SubjectResultPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    @Column(name = "semester", nullable = false, length = 20)
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ResultPublicationStatus status = ResultPublicationStatus.DRAFT;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "published_by_id")
    private User publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unlocked_by_id")
    private User unlockedBy;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "unlock_reason", length = 500)
    private String unlockReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SubjectResultPublication() {
    }

    public SubjectResultPublication(Subject subject, String academicYear, String semester) {
        this.subject = subject;
        this.academicYear = academicYear;
        this.semester = semester;
        this.status = ResultPublicationStatus.DRAFT;
    }

    public SubjectResultPublication(Subject subject, String academicYear, String semester,
                                    ResultPublicationStatus status, User publishedBy, LocalDateTime publishedAt) {
        this.subject = subject;
        this.academicYear = academicYear;
        this.semester = semester;
        this.status = status;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public ResultPublicationStatus getStatus() {
        return status;
    }

    public void setStatus(ResultPublicationStatus status) {
        this.status = status;
    }

    public User getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(User publishedBy) {
        this.publishedBy = publishedBy;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public User getUnlockedBy() {
        return unlockedBy;
    }

    public void setUnlockedBy(User unlockedBy) {
        this.unlockedBy = unlockedBy;
    }

    public LocalDateTime getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(LocalDateTime unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public String getUnlockReason() {
        return unlockReason;
    }

    public void setUnlockReason(String unlockReason) {
        this.unlockReason = unlockReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
