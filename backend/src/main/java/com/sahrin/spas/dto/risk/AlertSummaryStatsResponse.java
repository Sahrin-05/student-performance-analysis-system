package com.sahrin.spas.dto.risk;

public class AlertSummaryStatsResponse {

    private long totalAlerts;
    private long activeAlerts;
    private long criticalAlerts;
    private long highAlerts;
    private long mediumAlerts;
    private long lowAlerts;

    private long attendanceWarnings;
    private long lowMarksAlerts;
    private long progressDeclineAlerts;
    private long missingAssessmentAlerts;

    private long detectedCount;
    private long reviewedCount;
    private long actionRecordedCount;
    private long resolvedCount;

    public AlertSummaryStatsResponse() {
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(long activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }

    public long getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(long highAlerts) {
        this.highAlerts = highAlerts;
    }

    public long getMediumAlerts() {
        return mediumAlerts;
    }

    public void setMediumAlerts(long mediumAlerts) {
        this.mediumAlerts = mediumAlerts;
    }

    public long getLowAlerts() {
        return lowAlerts;
    }

    public void setLowAlerts(long lowAlerts) {
        this.lowAlerts = lowAlerts;
    }

    public long getAttendanceWarnings() {
        return attendanceWarnings;
    }

    public void setAttendanceWarnings(long attendanceWarnings) {
        this.attendanceWarnings = attendanceWarnings;
    }

    public long getLowMarksAlerts() {
        return lowMarksAlerts;
    }

    public void setLowMarksAlerts(long lowMarksAlerts) {
        this.lowMarksAlerts = lowMarksAlerts;
    }

    public long getProgressDeclineAlerts() {
        return progressDeclineAlerts;
    }

    public void setProgressDeclineAlerts(long progressDeclineAlerts) {
        this.progressDeclineAlerts = progressDeclineAlerts;
    }

    public long getMissingAssessmentAlerts() {
        return missingAssessmentAlerts;
    }

    public void setMissingAssessmentAlerts(long missingAssessmentAlerts) {
        this.missingAssessmentAlerts = missingAssessmentAlerts;
    }

    public long getDetectedCount() {
        return detectedCount;
    }

    public void setDetectedCount(long detectedCount) {
        this.detectedCount = detectedCount;
    }

    public long getReviewedCount() {
        return reviewedCount;
    }

    public void setReviewedCount(long reviewedCount) {
        this.reviewedCount = reviewedCount;
    }

    public long getActionRecordedCount() {
        return actionRecordedCount;
    }

    public void setActionRecordedCount(long actionRecordedCount) {
        this.actionRecordedCount = actionRecordedCount;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }
}
