package com.sahrin.spas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for automated risk detection heuristics.
 *
 * NOTE: The threshold values defined here (e.g., 80.0% attendance, 40.0% low marks,
 * 15.0% performance drop) are provisional system defaults intended for early academic
 * intervention and monitoring. They do NOT constitute official university policy or
 * academic regulations.
 */
@Component
@ConfigurationProperties(prefix = "app.risk")
public class RiskAlertProperties {

    /**
     * Attendance percentage threshold below which an attendance warning alert is triggered.
     * Default: 80.0%
     */
    private double attendanceThreshold = 80.0;

    /**
     * Cumulative/proportional assessment score percentage threshold below which
     * a low academic marks alert is triggered.
     * Default: 40.0% (aligned with the satisfactory pass mark)
     */
    private double lowMarksThreshold = 40.0;

    /**
     * Percentage decline between sequential graded assessments that triggers
     * a progress decline warning alert.
     * Default: 15.0%
     */
    private double declineDropThreshold = 15.0;

    public RiskAlertProperties() {
    }

    public RiskAlertProperties(double attendanceThreshold, double lowMarksThreshold, double declineDropThreshold) {
        this.attendanceThreshold = attendanceThreshold;
        this.lowMarksThreshold = lowMarksThreshold;
        this.declineDropThreshold = declineDropThreshold;
    }

    public double getAttendanceThreshold() {
        return attendanceThreshold;
    }

    public void setAttendanceThreshold(double attendanceThreshold) {
        this.attendanceThreshold = attendanceThreshold;
    }

    public double getLowMarksThreshold() {
        return lowMarksThreshold;
    }

    public void setLowMarksThreshold(double lowMarksThreshold) {
        this.lowMarksThreshold = lowMarksThreshold;
    }

    public double getDeclineDropThreshold() {
        return declineDropThreshold;
    }

    public void setDeclineDropThreshold(double declineDropThreshold) {
        this.declineDropThreshold = declineDropThreshold;
    }
}
