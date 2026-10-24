package com.sahrin.spas.service;

import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Clean, configurable grading policy for the Student Performance Analysis System (SPAS).
 *
 * IMPORTANT: The default numeric boundaries and grade-point equivalents configured here
 * are provisional system defaults and do NOT claim to represent official University of Ruhuna
 * regulations. Official policy thresholds should be confirmed by academic administration.
 *
 * Rules:
 * 1. Grade 'W' (Withdrawn) is assigned EXCLUSIVELY when a student's enrollment status is DROPPED
 *    or explicitly flagged as withdrawn. It is NEVER assigned based on a numeric mark.
 * 2. Active students with scores are evaluated against configurable percentage thresholds:
 *    - A: >= boundaryA (default 75.0%)
 *    - B: >= boundaryB (default 65.0%)
 *    - C: >= boundaryC (default 50.0%)
 *    - S: >= boundaryS (default 40.0% - Satisfactory / Pass)
 *    - F: < boundaryS  (Fail)
 * 3. Attendance records do NOT factor into grade or marks calculation.
 */
@Component
public class GradingPolicy {

    private final double boundaryA;
    private final double boundaryB;
    private final double boundaryC;
    private final double boundaryS;
    private final double gradePointA;
    private final double gradePointB;
    private final double gradePointC;
    private final double gradePointS;
    private final double gradePointF;

    public GradingPolicy(
            @Value("${app.grading.boundary.a:75.0}") double boundaryA,
            @Value("${app.grading.boundary.b:65.0}") double boundaryB,
            @Value("${app.grading.boundary.c:50.0}") double boundaryC,
            @Value("${app.grading.boundary.s:40.0}") double boundaryS,
            @Value("${app.grading.point.a:4.0}") double gradePointA,
            @Value("${app.grading.point.b:3.0}") double gradePointB,
            @Value("${app.grading.point.c:2.0}") double gradePointC,
            @Value("${app.grading.point.s:1.0}") double gradePointS,
            @Value("${app.grading.point.f:0.0}") double gradePointF
    ) {
        this.boundaryA = boundaryA;
        this.boundaryB = boundaryB;
        this.boundaryC = boundaryC;
        this.boundaryS = boundaryS;
        this.gradePointA = gradePointA;
        this.gradePointB = gradePointB;
        this.gradePointC = gradePointC;
        this.gradePointS = gradePointS;
        this.gradePointF = gradePointF;
    }

    public GradingPolicy() {
        this(75.0, 65.0, 50.0, 40.0, 4.0, 3.0, 2.0, 1.0, 0.0);
    }

    public boolean isPassingGrade(Grade grade) {
        return grade != null && grade != Grade.F && grade != Grade.W;
    }

    public Grade calculateGrade(double finalPercentage, EnrollmentStatus enrollmentStatus) {
        if (enrollmentStatus == EnrollmentStatus.DROPPED) {
            return Grade.W;
        }

        if (finalPercentage >= boundaryA) {
            return Grade.A;
        } else if (finalPercentage >= boundaryB) {
            return Grade.B;
        } else if (finalPercentage >= boundaryC) {
            return Grade.C;
        } else if (finalPercentage >= boundaryS) {
            return Grade.S;
        } else {
            return Grade.F;
        }
    }

    public Double calculateGradePoint(Grade grade) {
        if (grade == null || grade == Grade.W) {
            return null;
        }
        return switch (grade) {
            case A -> gradePointA;
            case B -> gradePointB;
            case C -> gradePointC;
            case S -> gradePointS;
            case F -> gradePointF;
            case W -> null;
        };
    }

    public String getProvisionalNotice() {
        return "Note: Grade boundaries (A >= " + boundaryA + "%, B >= " + boundaryB + "%, C >= " + boundaryC + "%, S >= " + boundaryS + "%) are provisional system defaults awaiting final institutional confirmation.";
    }

    public double getBoundaryA() {
        return boundaryA;
    }

    public double getBoundaryB() {
        return boundaryB;
    }

    public double getBoundaryC() {
        return boundaryC;
    }

    public double getBoundaryS() {
        return boundaryS;
    }

    public double getGradePointA() {
        return gradePointA;
    }

    public double getGradePointB() {
        return gradePointB;
    }

    public double getGradePointC() {
        return gradePointC;
    }

    public double getGradePointS() {
        return gradePointS;
    }

    public double getGradePointF() {
        return gradePointF;
    }
}
