package com.sahrin.spas.service.impl;

import com.sahrin.spas.config.RiskAlertProperties;
import com.sahrin.spas.dto.risk.AlertActionRequest;
import com.sahrin.spas.dto.risk.AlertResolutionRequest;
import com.sahrin.spas.dto.risk.AlertReviewRequest;
import com.sahrin.spas.dto.risk.AlertSummaryStatsResponse;
import com.sahrin.spas.dto.risk.RiskAlertResponse;
import com.sahrin.spas.dto.risk.StudentRiskAlertResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceSession;
import com.sahrin.spas.entity.AttendanceStatus;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.LecturerAssignment;
import com.sahrin.spas.entity.RiskAlert;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.RiskAlertRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.RiskAlertService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RiskAlertServiceImpl implements RiskAlertService {

    private final RiskAlertRepository riskAlertRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerAssignmentRepository lecturerAssignmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final RiskAlertProperties riskProperties;

    public RiskAlertServiceImpl(
            RiskAlertRepository riskAlertRepository,
            SubjectRepository subjectRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerAssignmentRepository lecturerAssignmentRepository,
            AssessmentRepository assessmentRepository,
            StudentMarkRepository studentMarkRepository,
            AttendanceSessionRepository sessionRepository,
            AttendanceRecordRepository recordRepository,
            UserRepository userRepository,
            RiskAlertProperties riskProperties
    ) {
        this.riskAlertRepository = riskAlertRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerAssignmentRepository = lecturerAssignmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.studentMarkRepository = studentMarkRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.riskProperties = riskProperties;
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void validateLecturerAccess(Long subjectId, Long lecturerId) {
        boolean isAssigned = lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(subjectId, lecturerId);
        if (!isAssigned) {
            throw new AccessDeniedException("Access denied: You are not actively assigned to teach this subject.");
        }
    }

    // ==========================================
    // EVALUATION ENGINE
    // ==========================================

    @Override
    public List<RiskAlertResponse> evaluateSubjectRisk(Long subjectId, String academicYear, String semester, String initiatedByEmail) {
        User user = getAuthenticatedUser(initiatedByEmail);
        if (user.getRole() == Role.LECTURER) {
            validateLecturerAccess(subjectId, user.getId());
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);

        // Filter strictly to ACTIVE enrollments (exclude DROPPED students)
        List<Enrollment> activeEnrollments = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .toList();

        if (activeEnrollments.isEmpty()) {
            return List.of();
        }

        List<RiskAlert> generatedAlerts = new ArrayList<>();

        // 1. Evaluate Attendance Alerts
        evaluateAttendanceRisk(subject, academicYear, semester, activeEnrollments, generatedAlerts);

        // 2. Evaluate Academic Marks, Progress Decline, and Missing Assessments
        evaluatePerformanceRisk(subject, academicYear, semester, activeEnrollments, generatedAlerts);

        // Return all alerts (both newly saved and pre-existing active) for this subject
        List<RiskAlert> currentSubjectAlerts = riskAlertRepository
                .findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(subjectId, academicYear, semester);

        return currentSubjectAlerts.stream().map(RiskAlertResponse::new).toList();
    }

    @Override
    public List<RiskAlertResponse> evaluateAllSubjectsRisk(String academicYear, String semester, String adminEmail) {
        User admin = getAuthenticatedUser(adminEmail);
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: Only administrators can trigger institution-wide risk evaluations.");
        }

        List<Subject> allSubjects = subjectRepository.findAll();
        List<RiskAlertResponse> allEvaluated = new ArrayList<>();

        for (Subject subject : allSubjects) {
            List<RiskAlertResponse> subAlerts = evaluateSubjectRisk(subject.getId(), academicYear, semester, adminEmail);
            allEvaluated.addAll(subAlerts);
        }

        return allEvaluated;
    }

    /**
     * Rule 1: Attendance Warning Evaluation
     * Formula: Attended = PRESENT + LATE, Eligible = TotalSessions - EXCUSED.
     * Rate = (Attended / Eligible) * 100.0.
     * Condition: Rate < 80.0% (strictly below, 80.0% clean) AND Eligible > 0.
     * Excused sessions are strictly excluded from the denominator.
     */
    private void evaluateAttendanceRisk(Subject subject, String academicYear, String semester,
                                        List<Enrollment> enrollments, List<RiskAlert> generatedAlerts) {
        List<AttendanceSession> sessions = sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                subject.getId(), academicYear, semester);

        int totalSessions = sessions.size();
        if (totalSessions == 0) {
            // Insufficient data: No attendance sessions recorded yet
            return;
        }

        List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        List<AttendanceRecord> allRecords = recordRepository.findBySessionIdIn(sessionIds);

        Map<Long, List<AttendanceRecord>> recordsByEnrollment = new HashMap<>();
        for (AttendanceRecord r : allRecords) {
            recordsByEnrollment.computeIfAbsent(r.getEnrollment().getId(), k -> new ArrayList<>()).add(r);
        }

        double attendanceThreshold = riskProperties.getAttendanceThreshold();

        for (Enrollment enr : enrollments) {
            List<AttendanceRecord> records = recordsByEnrollment.getOrDefault(enr.getId(), List.of());
            int excused = 0;
            int attended = 0;
            int absent = 0;

            for (AttendanceRecord r : records) {
                if (r.getStatus() == AttendanceStatus.EXCUSED) {
                    excused++;
                } else if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) {
                    attended++;
                } else if (r.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }
            }

            int eligible = totalSessions - excused;
            if (eligible <= 0) {
                // Zero eligible sessions guard: Avoid divide-by-zero or false flags
                continue;
            }

            double percentage = Math.round(((double) attended / eligible) * 10000.0) / 100.0;

            // Strict threshold check: strictly below threshold (e.g. 79.99% flags, 80.00% does NOT)
            if (percentage < attendanceThreshold) {
                boolean alreadyActive = riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                        enr.getStudent().getId(), subject.getId(), academicYear, semester,
                        AlertRiskType.ATTENDANCE_WARNING, AlertStatus.RESOLVED);

                if (!alreadyActive) {
                    AlertSeverity severity = percentage < 60.0 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH;
                    String reason = String.format(
                            "Attendance rate of %.2f%% is below the provisional warning threshold of %.2f%% (Attended: %d, Excused: %d, Total Eligible: %d).",
                            percentage, attendanceThreshold, attended, excused, eligible);
                    String details = String.format(
                            "Attended Sessions: %d (Present/Late), Absent Sessions: %d, Excused Sessions: %d, Total Scheduled: %d, Eligible: %d, Attendance Rate: %.2f%%",
                            attended, absent, excused, totalSessions, eligible, percentage);

                    RiskAlert alert = new RiskAlert(
                            enr.getStudent(), subject, academicYear, semester,
                            AlertRiskType.ATTENDANCE_WARNING, severity, reason, percentage, details);

                    RiskAlert saved = riskAlertRepository.save(alert);
                    generatedAlerts.add(saved);
                }
            }
        }
    }

    /**
     * Rules 2, 3, and 4: Performance Risk Evaluation
     * Rule 2: Low Academic Marks (< 40.0% configurable threshold)
     * Rule 3: Academic Progress Decline (>= 15.0% drop between sequential graded assessments with >= 2 assessments)
     * Rule 4: Missing or Incomplete Assessments (peer marks exist, student mark missing)
     */
    private void evaluatePerformanceRisk(Subject subject, String academicYear, String semester,
                                         List<Enrollment> enrollments, List<RiskAlert> generatedAlerts) {
        List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subject.getId(), academicYear, semester);

        if (assessments.isEmpty()) {
            // Insufficient data: No assessments created
            return;
        }

        List<Long> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        List<StudentMark> allMarks = studentMarkRepository.findByEnrollmentIdIn(enrollmentIds);

        // Map: enrollmentId -> list of marks
        Map<Long, List<StudentMark>> marksByEnrollment = new HashMap<>();
        // Map: assessmentId -> list of marks across the cohort
        Map<Long, List<StudentMark>> marksByAssessment = new HashMap<>();

        for (StudentMark m : allMarks) {
            marksByEnrollment.computeIfAbsent(m.getEnrollment().getId(), k -> new ArrayList<>()).add(m);
            marksByAssessment.computeIfAbsent(m.getAssessment().getId(), k -> new ArrayList<>()).add(m);
        }

        double lowMarksThreshold = riskProperties.getLowMarksThreshold();
        double declineThreshold = riskProperties.getDeclineDropThreshold();

        for (Enrollment enr : enrollments) {
            User student = enr.getStudent();
            List<StudentMark> studentMarks = marksByEnrollment.getOrDefault(enr.getId(), new ArrayList<>());

            // Filter to marks where maxMarks > 0
            List<StudentMark> validMarks = studentMarks.stream()
                    .filter(m -> m.getAssessment().getMaxMarks() > 0)
                    .sorted(Comparator.comparing(m -> m.getAssessment().getCreatedAt()))
                    .toList();

            // ----------------------------------------------------
            // Rule 2: Low Academic Marks (Proportional Weighted Score)
            // ----------------------------------------------------
            if (!validMarks.isEmpty()) {
                double earnedWeighted = 0.0;
                double gradedWeight = 0.0;

                for (StudentMark mark : validMarks) {
                    Assessment ass = mark.getAssessment();
                    if (ass.getMaxMarks() > 0 && ass.getWeightPercentage() > 0) {
                        double contribution = (mark.getScore() / ass.getMaxMarks()) * ass.getWeightPercentage();
                        earnedWeighted += contribution;
                        gradedWeight += ass.getWeightPercentage();
                    }
                }

                if (gradedWeight > 0.0) {
                    double performancePct = Math.round((earnedWeighted / gradedWeight) * 10000.0) / 100.0;
                    if (performancePct < lowMarksThreshold) {
                        boolean alreadyActive = riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                                student.getId(), subject.getId(), academicYear, semester,
                                AlertRiskType.LOW_ACADEMIC_MARKS, AlertStatus.RESOLVED);

                        if (!alreadyActive) {
                            AlertSeverity severity = performancePct < 25.0 ? AlertSeverity.CRITICAL :
                                    (performancePct < 35.0 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM);
                            String reason = String.format(
                                    "Cumulative performance of %.2f%% is below the provisional warning threshold of %.2f%% across %d graded assessment(s).",
                                    performancePct, lowMarksThreshold, validMarks.size());
                            String details = String.format(
                                    "Earned Weighted Contribution: %.2f out of Total Graded Weight: %.2f%% (Current Mastery: %.2f%%)",
                                    earnedWeighted, gradedWeight, performancePct);

                            RiskAlert alert = new RiskAlert(
                                    student, subject, academicYear, semester,
                                    AlertRiskType.LOW_ACADEMIC_MARKS, severity, reason, performancePct, details);

                            generatedAlerts.add(riskAlertRepository.save(alert));
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // Rule 3: Academic Progress Decline (>= 15% drop, >= 2 assessments)
            // ----------------------------------------------------
            if (validMarks.size() >= 2) {
                StudentMark latestMark = validMarks.get(validMarks.size() - 1);
                StudentMark previousMark = validMarks.get(validMarks.size() - 2);

                double latestPct = Math.round((latestMark.getScore() / latestMark.getAssessment().getMaxMarks()) * 10000.0) / 100.0;
                double prevPct = Math.round((previousMark.getScore() / previousMark.getAssessment().getMaxMarks()) * 10000.0) / 100.0;
                double drop = Math.round((prevPct - latestPct) * 100.0) / 100.0;

                if (drop >= declineThreshold) {
                    boolean alreadyActive = riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                            student.getId(), subject.getId(), academicYear, semester,
                            AlertRiskType.PROGRESS_DECLINE, AlertStatus.RESOLVED);

                    if (!alreadyActive) {
                        AlertSeverity severity = drop >= 25.0 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM;
                        String reason = String.format(
                                "Academic performance declined by %.2f%% from '%s' (%.2f%%) to '%s' (%.2f%%).",
                                drop, previousMark.getAssessment().getTitle(), prevPct,
                                latestMark.getAssessment().getTitle(), latestPct);
                        String details = String.format(
                                "Previous Assessment: '%s' = %.2f/%.2f (%.2f%%), Latest Assessment: '%s' = %.2f/%.2f (%.2f%%), Net Decline: %.2f%% (Threshold: %.2f%%)",
                                previousMark.getAssessment().getTitle(), previousMark.getScore(),
                                previousMark.getAssessment().getMaxMarks(), prevPct,
                                latestMark.getAssessment().getTitle(), latestMark.getScore(),
                                latestMark.getAssessment().getMaxMarks(), latestPct,
                                drop, declineThreshold);

                        RiskAlert alert = new RiskAlert(
                                student, subject, academicYear, semester,
                                AlertRiskType.PROGRESS_DECLINE, severity, reason, drop, details);

                        generatedAlerts.add(riskAlertRepository.save(alert));
                    }
                }
            }

            // ----------------------------------------------------
            // Rule 4: Missing or Incomplete Assessments
            // ----------------------------------------------------
            Set<Long> studentMarkedAssessmentIds = studentMarks.stream()
                    .map(m -> m.getAssessment().getId())
                    .collect(Collectors.toSet());

            for (Assessment ass : assessments) {
                List<StudentMark> cohortMarks = marksByAssessment.getOrDefault(ass.getId(), List.of());
                // Only flag if at least one peer has received a mark for this assessment
                if (!cohortMarks.isEmpty() && !studentMarkedAssessmentIds.contains(ass.getId())) {
                    boolean alreadyActive = riskAlertRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemesterAndRiskTypeAndStatusNot(
                            student.getId(), subject.getId(), academicYear, semester,
                            AlertRiskType.MISSING_ASSESSMENT, AlertStatus.RESOLVED);

                    if (!alreadyActive) {
                        String reason = String.format(
                                "Missing required mark for assessment '%s' (%s, Max: %.1f, Weight: %.1f%%) where peer marks have been recorded.",
                                ass.getTitle(), ass.getType(), ass.getMaxMarks(), ass.getWeightPercentage());
                        String details = String.format(
                                "Assessment: '%s' (Type: %s, Weight: %.2f%%, Max Marks: %.2f). Other enrolled students in this subject have recorded scores.",
                                ass.getTitle(), ass.getType(), ass.getWeightPercentage(), ass.getMaxMarks());

                        RiskAlert alert = new RiskAlert(
                                student, subject, academicYear, semester,
                                AlertRiskType.MISSING_ASSESSMENT, AlertSeverity.MEDIUM, reason,
                                ass.getWeightPercentage(), details);

                        generatedAlerts.add(riskAlertRepository.save(alert));
                    }
                }
            }
        }
    }

    // ==========================================
    // ROLE-BASED RETRIEVAL
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<RiskAlertResponse> getAllAlerts(AlertRiskType riskType, AlertSeverity severity,
                                                AlertStatus status, Long subjectId, Long studentId) {
        List<RiskAlert> alerts = riskAlertRepository.findAllByOrderByDetectedAtDesc();

        return alerts.stream()
                .filter(a -> riskType == null || a.getRiskType() == riskType)
                .filter(a -> severity == null || a.getSeverity() == severity)
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> subjectId == null || a.getSubject().getId().equals(subjectId))
                .filter(a -> studentId == null || a.getStudent().getId().equals(studentId))
                .map(RiskAlertResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertSummaryStatsResponse getAdminSummaryStats() {
        AlertSummaryStatsResponse stats = new AlertSummaryStatsResponse();
        stats.setTotalAlerts(riskAlertRepository.count());
        stats.setActiveAlerts(riskAlertRepository.countByStatusNot(AlertStatus.RESOLVED));
        stats.setCriticalAlerts(riskAlertRepository.countBySeverityAndStatusNot(AlertSeverity.CRITICAL, AlertStatus.RESOLVED));
        stats.setHighAlerts(riskAlertRepository.countBySeverityAndStatusNot(AlertSeverity.HIGH, AlertStatus.RESOLVED));
        stats.setMediumAlerts(riskAlertRepository.countBySeverityAndStatusNot(AlertSeverity.MEDIUM, AlertStatus.RESOLVED));
        stats.setLowAlerts(riskAlertRepository.countBySeverityAndStatusNot(AlertSeverity.LOW, AlertStatus.RESOLVED));

        stats.setAttendanceWarnings(riskAlertRepository.countByRiskTypeAndStatusNot(AlertRiskType.ATTENDANCE_WARNING, AlertStatus.RESOLVED));
        stats.setLowMarksAlerts(riskAlertRepository.countByRiskTypeAndStatusNot(AlertRiskType.LOW_ACADEMIC_MARKS, AlertStatus.RESOLVED));
        stats.setProgressDeclineAlerts(riskAlertRepository.countByRiskTypeAndStatusNot(AlertRiskType.PROGRESS_DECLINE, AlertStatus.RESOLVED));
        stats.setMissingAssessmentAlerts(riskAlertRepository.countByRiskTypeAndStatusNot(AlertRiskType.MISSING_ASSESSMENT, AlertStatus.RESOLVED));

        stats.setDetectedCount(riskAlertRepository.countByStatus(AlertStatus.DETECTED));
        stats.setReviewedCount(riskAlertRepository.countByStatus(AlertStatus.REVIEWED));
        stats.setActionRecordedCount(riskAlertRepository.countByStatus(AlertStatus.ACTION_RECORDED));
        stats.setResolvedCount(riskAlertRepository.countByStatus(AlertStatus.RESOLVED));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskAlertResponse> getLecturerAlertsForSubject(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerAccess(subjectId, lecturer.getId());

        List<RiskAlert> alerts = riskAlertRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(
                subjectId, academicYear, semester);
        return alerts.stream().map(RiskAlertResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskAlertResponse> getLecturerAllAlerts(String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        List<LecturerAssignment> assignments = lecturerAssignmentRepository.findByLecturerIdAndActiveTrue(lecturer.getId());
        List<Long> subjectIds = assignments.stream().map(a -> a.getSubject().getId()).toList();

        if (subjectIds.isEmpty()) {
            return List.of();
        }

        List<RiskAlert> alerts = riskAlertRepository.findBySubjectIdInOrderByDetectedAtDesc(subjectIds);
        return alerts.stream().map(RiskAlertResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertSummaryStatsResponse getLecturerSummaryStats(String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        List<LecturerAssignment> assignments = lecturerAssignmentRepository.findByLecturerIdAndActiveTrue(lecturer.getId());
        List<Long> subjectIds = assignments.stream().map(a -> a.getSubject().getId()).toList();

        AlertSummaryStatsResponse stats = new AlertSummaryStatsResponse();
        if (subjectIds.isEmpty()) {
            return stats;
        }

        List<RiskAlert> lecturerAlerts = riskAlertRepository.findBySubjectIdInOrderByDetectedAtDesc(subjectIds);
        stats.setTotalAlerts(lecturerAlerts.size());

        long active = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED).count();
        long critical = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getSeverity() == AlertSeverity.CRITICAL).count();
        long high = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getSeverity() == AlertSeverity.HIGH).count();
        long medium = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getSeverity() == AlertSeverity.MEDIUM).count();
        long low = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getSeverity() == AlertSeverity.LOW).count();

        long att = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getRiskType() == AlertRiskType.ATTENDANCE_WARNING).count();
        long lowMarks = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getRiskType() == AlertRiskType.LOW_ACADEMIC_MARKS).count();
        long decline = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getRiskType() == AlertRiskType.PROGRESS_DECLINE).count();
        long missing = lecturerAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED && a.getRiskType() == AlertRiskType.MISSING_ASSESSMENT).count();

        long detected = lecturerAlerts.stream().filter(a -> a.getStatus() == AlertStatus.DETECTED).count();
        long reviewed = lecturerAlerts.stream().filter(a -> a.getStatus() == AlertStatus.REVIEWED).count();
        long action = lecturerAlerts.stream().filter(a -> a.getStatus() == AlertStatus.ACTION_RECORDED).count();
        long resolved = lecturerAlerts.stream().filter(a -> a.getStatus() == AlertStatus.RESOLVED).count();

        stats.setActiveAlerts(active);
        stats.setCriticalAlerts(critical);
        stats.setHighAlerts(high);
        stats.setMediumAlerts(medium);
        stats.setLowAlerts(low);

        stats.setAttendanceWarnings(att);
        stats.setLowMarksAlerts(lowMarks);
        stats.setProgressDeclineAlerts(decline);
        stats.setMissingAssessmentAlerts(missing);

        stats.setDetectedCount(detected);
        stats.setReviewedCount(reviewed);
        stats.setActionRecordedCount(action);
        stats.setResolvedCount(resolved);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentRiskAlertResponse> getMyAlerts(String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        List<RiskAlert> alerts = riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(student.getId());

        // Strictly sanitized responses: no staff notes, no reviewer identities
        return alerts.stream().map(StudentRiskAlertResponse::new).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAlertResponse getAlertById(Long alertId, String staffEmail) {
        User user = getAuthenticatedUser(staffEmail);
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAlert", "id", alertId));

        if (user.getRole() == Role.LECTURER) {
            validateLecturerAccess(alert.getSubject().getId(), user.getId());
        }

        return new RiskAlertResponse(alert);
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================

    @Override
    public RiskAlertResponse reviewAlert(Long alertId, AlertReviewRequest request, String reviewerEmail) {
        User reviewer = getAuthenticatedUser(reviewerEmail);
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAlert", "id", alertId));

        if (reviewer.getRole() == Role.LECTURER) {
            validateLecturerAccess(alert.getSubject().getId(), reviewer.getId());
        }

        alert.setStatus(AlertStatus.REVIEWED);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());

        RiskAlert updated = riskAlertRepository.save(alert);
        return new RiskAlertResponse(updated);
    }

    @Override
    public RiskAlertResponse recordAction(Long alertId, AlertActionRequest request, String staffEmail) {
        User staff = getAuthenticatedUser(staffEmail);
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAlert", "id", alertId));

        if (staff.getRole() == Role.LECTURER) {
            validateLecturerAccess(alert.getSubject().getId(), staff.getId());
        }

        if (alert.getReviewedBy() == null) {
            alert.setReviewedBy(staff);
            alert.setReviewedAt(LocalDateTime.now());
        }

        alert.setStatus(AlertStatus.ACTION_RECORDED);
        alert.setActionNotes(request.getActionNotes().trim());
        alert.setActionRecordedBy(staff);
        alert.setActionRecordedAt(LocalDateTime.now());

        RiskAlert updated = riskAlertRepository.save(alert);
        return new RiskAlertResponse(updated);
    }

    @Override
    public RiskAlertResponse resolveAlert(Long alertId, AlertResolutionRequest request, String staffEmail) {
        User staff = getAuthenticatedUser(staffEmail);
        RiskAlert alert = riskAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAlert", "id", alertId));

        if (staff.getRole() == Role.LECTURER) {
            validateLecturerAccess(alert.getSubject().getId(), staff.getId());
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolutionNotes(request.getResolutionNotes().trim());
        alert.setResolvedBy(staff);
        alert.setResolvedAt(LocalDateTime.now());

        RiskAlert updated = riskAlertRepository.save(alert);
        return new RiskAlertResponse(updated);
    }
}
