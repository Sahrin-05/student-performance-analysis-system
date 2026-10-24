package com.sahrin.spas.dto.performance;

import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.SubjectResultPublication;

import java.time.LocalDateTime;

public class SubjectResultPublicationResponse {

    private Long id;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private String semester;
    private ResultPublicationStatus status;
    private String publishedByName;
    private LocalDateTime publishedAt;
    private String unlockedByName;
    private LocalDateTime unlockedAt;
    private String unlockReason;

    public SubjectResultPublicationResponse() {
    }

    public SubjectResultPublicationResponse(SubjectResultPublication p) {
        this.id = p.getId();
        if (p.getSubject() != null) {
            this.subjectId = p.getSubject().getId();
            this.subjectCode = p.getSubject().getCode();
            this.subjectName = p.getSubject().getName();
        }
        this.academicYear = p.getAcademicYear();
        this.semester = p.getSemester();
        this.status = p.getStatus();
        if (p.getPublishedBy() != null) {
            this.publishedByName = p.getPublishedBy().getFullName();
        }
        this.publishedAt = p.getPublishedAt();
        if (p.getUnlockedBy() != null) {
            this.unlockedByName = p.getUnlockedBy().getFullName();
        }
        this.unlockedAt = p.getUnlockedAt();
        this.unlockReason = p.getUnlockReason();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getPublishedByName() {
        return publishedByName;
    }

    public void setPublishedByName(String publishedByName) {
        this.publishedByName = publishedByName;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getUnlockedByName() {
        return unlockedByName;
    }

    public void setUnlockedByName(String unlockedByName) {
        this.unlockedByName = unlockedByName;
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
}
