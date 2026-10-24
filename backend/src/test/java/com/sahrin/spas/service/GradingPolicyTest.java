package com.sahrin.spas.service;

import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradingPolicyTest {

    private GradingPolicy gradingPolicy;

    @BeforeEach
    void setUp() {
        gradingPolicy = new GradingPolicy();
    }

    @Test
    @DisplayName("Score >= 75.0 yields Grade A")
    void testGradeA() {
        assertEquals(Grade.A, gradingPolicy.calculateGrade(75.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.A, gradingPolicy.calculateGrade(100.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.A, gradingPolicy.calculateGrade(89.5, EnrollmentStatus.ACTIVE));
    }

    @Test
    @DisplayName("65.0 <= Score < 75.0 yields Grade B")
    void testGradeB() {
        assertEquals(Grade.B, gradingPolicy.calculateGrade(65.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.B, gradingPolicy.calculateGrade(74.99, EnrollmentStatus.ACTIVE));
    }

    @Test
    @DisplayName("50.0 <= Score < 65.0 yields Grade C")
    void testGradeC() {
        assertEquals(Grade.C, gradingPolicy.calculateGrade(50.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.C, gradingPolicy.calculateGrade(64.99, EnrollmentStatus.ACTIVE));
    }

    @Test
    @DisplayName("40.0 <= Score < 50.0 yields Grade S")
    void testGradeS() {
        assertEquals(Grade.S, gradingPolicy.calculateGrade(40.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.S, gradingPolicy.calculateGrade(49.99, EnrollmentStatus.ACTIVE));
    }

    @Test
    @DisplayName("Score < 40.0 yields Grade F")
    void testGradeF() {
        assertEquals(Grade.F, gradingPolicy.calculateGrade(39.99, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.F, gradingPolicy.calculateGrade(0.0, EnrollmentStatus.ACTIVE));
        assertEquals(Grade.F, gradingPolicy.calculateGrade(-5.0, EnrollmentStatus.ACTIVE));
    }

    @Test
    @DisplayName("DROPPED enrollment always yields Grade W regardless of score")
    void testGradeWForDropped() {
        assertEquals(Grade.W, gradingPolicy.calculateGrade(95.0, EnrollmentStatus.DROPPED));
        assertEquals(Grade.W, gradingPolicy.calculateGrade(50.0, EnrollmentStatus.DROPPED));
        assertEquals(Grade.W, gradingPolicy.calculateGrade(10.0, EnrollmentStatus.DROPPED));
        assertEquals(Grade.W, gradingPolicy.calculateGrade(0.0, EnrollmentStatus.DROPPED));
    }

    @Test
    @DisplayName("Passing grade checks correctly identify pass/fail")
    void testPassingGrade() {
        assertTrue(gradingPolicy.isPassingGrade(Grade.A));
        assertTrue(gradingPolicy.isPassingGrade(Grade.B));
        assertTrue(gradingPolicy.isPassingGrade(Grade.C));
        assertTrue(gradingPolicy.isPassingGrade(Grade.S));
        assertFalse(gradingPolicy.isPassingGrade(Grade.F));
        assertFalse(gradingPolicy.isPassingGrade(Grade.W));
    }

    @Test
    @DisplayName("Provisional notice is non-empty and explicit")
    void testProvisionalNotice() {
        assertNotNull(gradingPolicy.getProvisionalNotice());
        assertTrue(gradingPolicy.getProvisionalNotice().contains("provisional"));
    }
}
