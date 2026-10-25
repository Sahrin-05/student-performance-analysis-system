package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.report.AdminInstitutionalReportResponse;
import com.sahrin.spas.dto.report.LecturerSubjectReportResponse;
import com.sahrin.spas.dto.report.StudentReportResponse;
import com.sahrin.spas.entity.AlertRiskType;
import com.sahrin.spas.entity.AlertSeverity;
import com.sahrin.spas.entity.AlertStatus;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceSession;
import com.sahrin.spas.entity.AttendanceStatus;
import com.sahrin.spas.entity.Course;
import com.sahrin.spas.entity.Department;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.LecturerAssignment;
import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.RiskAlert;
import com.sahrin.spas.entity.Role;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.SubjectResultPublication;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.CourseRepository;
import com.sahrin.spas.repository.DepartmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.RiskAlertRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.SubjectResultPublicationRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.GradingPolicy;
import com.sahrin.spas.service.ReportService;
import com.sahrin.spas.util.CsvExportUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerAssignmentRepository assignmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final SubjectResultPublicationRepository publicationRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final RiskAlertRepository riskAlertRepository;
    private final GradingPolicy gradingPolicy;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportServiceImpl(UserRepository userRepository,
                             DepartmentRepository departmentRepository,
                             CourseRepository courseRepository,
                             SubjectRepository subjectRepository,
                             EnrollmentRepository enrollmentRepository,
                             LecturerAssignmentRepository assignmentRepository,
                             AssessmentRepository assessmentRepository,
                             StudentMarkRepository studentMarkRepository,
                             SubjectResultPublicationRepository publicationRepository,
                             AttendanceSessionRepository attendanceSessionRepository,
                             AttendanceRecordRepository attendanceRecordRepository,
                             RiskAlertRepository riskAlertRepository,
                             GradingPolicy gradingPolicy) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assignmentRepository = assignmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.studentMarkRepository = studentMarkRepository;
        this.publicationRepository = publicationRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.riskAlertRepository = riskAlertRepository;
        this.gradingPolicy = gradingPolicy;
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ==========================================
    // STUDENT REPORTS & EXPORTS
    // ==========================================

    @Override
    public StudentReportResponse getStudentReport(String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        if (student.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Access denied: Only students may access personal academic reports.");
        }

        StudentReportResponse response = new StudentReportResponse();
        response.setStudentId(student.getId());
        response.setStudentIdentifier(student.getIdentifier());
        response.setStudentName(student.getFullName());
        response.setStudentEmail(student.getEmail());
        response.setProvisionalNotice(gradingPolicy.getProvisionalNotice());

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(student.getId());
        if (enrollments.isEmpty()) {
            response.setTotalEnrolledSubjects(0);
            response.setTotalEnrolledCredits(0);
            response.setTotalCreditsEarned(0);
            response.setPublishedSubjectsCount(0);
            response.setPassedSubjectsCount(0);
            response.setFailedSubjectsCount(0);
            response.setGpa(null);
            response.setOverallAttendancePercentage(null);
            response.setSubjects(List.of());
            return response;
        }

        List<StudentReportResponse.StudentSubjectReportItem> subjectItems = new ArrayList<>();
        double totalWeightedGradePoints = 0.0;
        int totalGradedCredits = 0;
        int totalCreditsEarned = 0;
        int totalEnrolledCredits = 0;
        int publishedCount = 0;
        int passedCount = 0;
        int failedCount = 0;

        double sumAttendancePercentages = 0.0;
        int subjectsWithAttendanceCount = 0;

        for (Enrollment enr : enrollments) {
            Subject subject = enr.getSubject();
            totalEnrolledCredits += subject.getCredits();

            StudentReportResponse.StudentSubjectReportItem item = new StudentReportResponse.StudentSubjectReportItem();
            item.setEnrollmentId(enr.getId());
            item.setSubjectId(subject.getId());
            item.setSubjectCode(subject.getCode());
            item.setSubjectName(subject.getName());
            item.setCredits(subject.getCredits());
            item.setAcademicYear(enr.getAcademicYear());
            item.setSemester(enr.getSemester());

            // Check publication
            Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                    subject.getId(), enr.getAcademicYear(), enr.getSemester());
            boolean isPublished = pubOpt.isPresent() && pubOpt.get().getStatus() == ResultPublicationStatus.PUBLISHED;

            if (isPublished) {
                item.setPublicationStatus(ResultPublicationStatus.PUBLISHED);
                item.setPublishedAt(pubOpt.get().getPublishedAt());
                publishedCount++;

                List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                        subject.getId(), enr.getAcademicYear(), enr.getSemester());
                List<StudentMark> marks = studentMarkRepository.findByEnrollmentId(enr.getId());

                Map<Long, StudentMark> marksByAssId = new HashMap<>();
                for (StudentMark m : marks) {
                    marksByAssId.put(m.getAssessment().getId(), m);
                }

                double totalWeighted = 0.0;
                List<StudentReportResponse.StudentAssessmentScoreItem> assessmentScoreItems = new ArrayList<>();

                for (Assessment ass : assessments) {
                    StudentMark m = marksByAssId.get(ass.getId());
                    Double score = m != null ? m.getScore() : null;
                    Double weightedContrib = null;

                    if (score != null && ass.getMaxMarks() > 0) {
                        weightedContrib = (score / ass.getMaxMarks()) * ass.getWeightPercentage();
                        weightedContrib = Math.round(weightedContrib * 100.0) / 100.0;
                        totalWeighted += weightedContrib;
                    }

                    assessmentScoreItems.add(new StudentReportResponse.StudentAssessmentScoreItem(
                            ass.getId(),
                            ass.getTitle(),
                            ass.getType(),
                            ass.getMaxMarks(),
                            score,
                            ass.getWeightPercentage(),
                            weightedContrib
                    ));
                }

                totalWeighted = Math.round(totalWeighted * 100.0) / 100.0;
                item.setTotalWeightedScore(totalWeighted);
                item.setAssessmentScores(assessmentScoreItems);

                Grade grade = gradingPolicy.calculateGrade(totalWeighted, enr.getStatus());
                Double gradePoint = gradingPolicy.calculateGradePoint(grade);
                item.setGrade(grade);
                item.setGradePoint(gradePoint);

                if (gradingPolicy.isPassingGrade(grade)) {
                    passedCount++;
                    totalCreditsEarned += subject.getCredits();
                } else {
                    failedCount++;
                }

                if (gradePoint != null) {
                    totalWeightedGradePoints += (gradePoint * subject.getCredits());
                    totalGradedCredits += subject.getCredits();
                }
            } else {
                item.setPublicationStatus(ResultPublicationStatus.DRAFT);
                item.setPublishedAt(null);
                item.setTotalWeightedScore(null);
                item.setGrade(null);
                item.setGradePoint(null);
                item.setAssessmentScores(List.of());
            }

            // Attendance calculation
            List<AttendanceSession> sessions = attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                    subject.getId(), enr.getAcademicYear(), enr.getSemester());
            int totalSessions = sessions.size();
            List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
            List<AttendanceRecord> records = sessionIds.isEmpty() ? List.of() :
                    attendanceRecordRepository.findByEnrollmentId(enr.getId());

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
            double attendancePct;
            if (eligible <= 0) {
                attendancePct = 100.0;
            } else {
                attendancePct = Math.round(((double) attended / eligible) * 10000.0) / 100.0;
            }

            boolean belowWarning = (attendancePct < 80.0) && (eligible > 0);
            item.setTotalSessions(totalSessions);
            item.setAttendedSessions(attended);
            item.setAbsentSessions(absent);
            item.setExcusedSessions(excused);
            item.setEligibleSessions(eligible);
            item.setAttendancePercentage(attendancePct);
            item.setBelowAttendanceThreshold(belowWarning);

            if (eligible > 0) {
                sumAttendancePercentages += attendancePct;
                subjectsWithAttendanceCount++;
            }

            // Risk alerts (sanitized - no internal staff notes)
            List<RiskAlert> alerts = riskAlertRepository.findByStudentIdOrderByDetectedAtDesc(student.getId())
                    .stream()
                    .filter(a -> a.getSubject().getId().equals(subject.getId()) &&
                            a.getAcademicYear().equals(enr.getAcademicYear()) &&
                            a.getSemester().equals(enr.getSemester()))
                    .toList();

            List<StudentReportResponse.StudentRiskAlertItem> alertItems = alerts.stream().map(a ->
                    new StudentReportResponse.StudentRiskAlertItem(
                            a.getId(),
                            a.getRiskType(),
                            a.getSeverity(),
                            a.getStatus(),
                            a.getReason(),
                            buildAdvisoryGuidance(a.getRiskType()),
                            a.getDetectedAt()
                    )
            ).toList();
            item.setActiveAlerts(alertItems);

            subjectItems.add(item);
        }

        response.setTotalEnrolledSubjects(enrollments.size());
        response.setTotalEnrolledCredits(totalEnrolledCredits);
        response.setTotalCreditsEarned(totalCreditsEarned);
        response.setPublishedSubjectsCount(publishedCount);
        response.setPassedSubjectsCount(passedCount);
        response.setFailedSubjectsCount(failedCount);

        if (totalGradedCredits > 0) {
            double gpa = Math.round((totalWeightedGradePoints / totalGradedCredits) * 100.0) / 100.0;
            response.setGpa(gpa);
        } else {
            response.setGpa(null);
        }

        if (subjectsWithAttendanceCount > 0) {
            double overallAtt = Math.round((sumAttendancePercentages / subjectsWithAttendanceCount) * 100.0) / 100.0;
            response.setOverallAttendancePercentage(overallAtt);
        } else {
            response.setOverallAttendancePercentage(100.0);
        }

        response.setSubjects(subjectItems);
        return response;
    }

    private String buildAdvisoryGuidance(AlertRiskType riskType) {
        return switch (riskType) {
            case ATTENDANCE_WARNING ->
                    "Your attendance is below the 80% advisory requirement. Please consult your instructor to discuss attendance catch-up options.";
            case LOW_ACADEMIC_MARKS ->
                    "Your marks in this subject indicate you may need additional support. We recommend booking office hours with your lecturer or academic tutor.";
            case PROGRESS_DECLINE ->
                    "We noticed a recent dip in your assessment scores. Reach out to peer tutoring or counseling services for study assistance.";
            case MISSING_ASSESSMENT ->
                    "You have missing assessment submissions. Check the course portal and speak with your lecturer as soon as possible.";
        };
    }

    @Override
    public byte[] exportStudentReportCsv(String studentEmail) {
        StudentReportResponse report = getStudentReport(studentEmail);

        List<String> headers = List.of(
                "Subject Code", "Subject Name", "Credits", "Academic Year", "Semester",
                "Publication Status", "Total Score (%)", "Grade", "Grade Point",
                "Attendance (%)", "Attended Sessions", "Absent Sessions", "Excused Sessions",
                "Active Alerts Count"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (StudentReportResponse.StudentSubjectReportItem s : report.getSubjects()) {
            rows.add(List.of(
                    s.getSubjectCode(),
                    s.getSubjectName(),
                    s.getCredits(),
                    s.getAcademicYear(),
                    s.getSemester(),
                    s.getPublicationStatus().name(),
                    s.getTotalWeightedScore() != null ? s.getTotalWeightedScore() : "Pending Publication",
                    s.getGrade() != null ? s.getGrade().name() : "N/A",
                    s.getGradePoint() != null ? s.getGradePoint() : "N/A",
                    s.getAttendancePercentage(),
                    s.getAttendedSessions(),
                    s.getAbsentSessions(),
                    s.getExcusedSessions(),
                    s.getActiveAlerts().size()
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    // ==========================================
    // LECTURER REPORTS & EXPORTS
    // ==========================================

    @Override
    public LecturerSubjectReportResponse getLecturerSubjectReport(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        if (lecturer.getRole() != Role.LECTURER && lecturer.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied: Only faculty members or administrators can access class performance reports.");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        // Auto-resolve academicYear / semester if missing
        if (academicYear == null || academicYear.isBlank() || semester == null || semester.isBlank()) {
            List<LecturerAssignment> assignments = assignmentRepository.findByLecturerIdAndActiveTrue(lecturer.getId())
                    .stream().filter(a -> a.getSubject().getId().equals(subjectId)).toList();
            if (assignments.isEmpty() && lecturer.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Access denied: You are not assigned to instruct this subject.");
            }
            if (!assignments.isEmpty()) {
                academicYear = assignments.get(0).getAcademicYear();
                semester = assignments.get(0).getSemester();
            } else {
                academicYear = "2025/2026";
                semester = "1";
            }
        } else if (lecturer.getRole() != Role.ADMIN) {
            boolean assigned = assignmentRepository.existsBySubjectIdAndLecturerIdAndAcademicYearAndSemesterAndActiveTrue(
                    subjectId, lecturer.getId(), academicYear, semester);
            if (!assigned) {
                throw new AccessDeniedException("Access denied: You are not assigned to instruct this subject for the specified academic year and semester.");
            }
        }

        LecturerSubjectReportResponse response = new LecturerSubjectReportResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setCredits(subject.getCredits());
        response.setAcademicYear(academicYear);
        response.setSemester(semester);
        response.setProvisionalNotice(gradingPolicy.getProvisionalNotice());

        Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);
        if (pubOpt.isPresent()) {
            response.setPublicationStatus(pubOpt.get().getStatus());
            response.setPublishedAt(pubOpt.get().getPublishedAt());
        } else {
            response.setPublicationStatus(ResultPublicationStatus.DRAFT);
            response.setPublishedAt(null);
        }

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(subjectId, academicYear, semester);
        response.setEnrolledStudentsCount(enrollments.size());

        List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subjectId, academicYear, semester);
        List<LecturerSubjectReportResponse.AssessmentSummaryItem> assessmentItems = assessments.stream()
                .map(a -> new LecturerSubjectReportResponse.AssessmentSummaryItem(a.getId(), a.getTitle(), a.getType(), a.getMaxMarks(), a.getWeightPercentage()))
                .toList();
        response.setAssessments(assessmentItems);

        List<AttendanceSession> sessions = attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                subjectId, academicYear, semester);
        response.setTotalSessionsConducted(sessions.size());
        List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        List<AttendanceRecord> records = sessionIds.isEmpty() ? List.of() : attendanceRecordRepository.findBySessionIdIn(sessionIds);

        Map<Long, List<AttendanceRecord>> recordsByEnrollment = new HashMap<>();
        for (AttendanceRecord r : records) {
            recordsByEnrollment.computeIfAbsent(r.getEnrollment().getId(), k -> new ArrayList<>()).add(r);
        }

        List<Long> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        List<StudentMark> marks = enrollmentIds.isEmpty() ? List.of() : studentMarkRepository.findByEnrollmentIdIn(enrollmentIds);

        Map<Long, Map<Long, Double>> marksByEnrollmentAndAss = new HashMap<>();
        for (StudentMark m : marks) {
            marksByEnrollmentAndAss.computeIfAbsent(m.getEnrollment().getId(), k -> new HashMap<>())
                    .put(m.getAssessment().getId(), m.getScore());
        }

        List<RiskAlert> alerts = riskAlertRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByDetectedAtDesc(
                subjectId, academicYear, semester);
        response.setActiveAlertsCount((int) alerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED).count());

        Map<Long, List<RiskAlert>> alertsByStudent = new HashMap<>();
        for (RiskAlert a : alerts) {
            if (a.getStatus() != AlertStatus.RESOLVED) {
                alertsByStudent.computeIfAbsent(a.getStudent().getId(), k -> new ArrayList<>()).add(a);
            }
        }

        List<LecturerSubjectReportResponse.LecturerStudentReportRow> studentRows = new ArrayList<>();
        Map<String, Integer> gradeDist = new HashMap<>();
        for (Grade g : Grade.values()) {
            gradeDist.put(g.name(), 0);
        }

        double sumScores = 0.0;
        int assessedCount = 0;
        double highest = -1.0;
        double lowest = 101.0;
        int passCount = 0;
        int failCount = 0;

        double sumAttendancePct = 0.0;
        int belowThresholdAttendanceCount = 0;

        for (Enrollment enr : enrollments) {
            LecturerSubjectReportResponse.LecturerStudentReportRow row = new LecturerSubjectReportResponse.LecturerStudentReportRow();
            row.setEnrollmentId(enr.getId());
            row.setStudentId(enr.getStudent().getId());
            row.setStudentIdentifier(enr.getStudent().getIdentifier());
            row.setStudentName(enr.getStudent().getFullName());
            row.setStudentEmail(enr.getStudent().getEmail());
            row.setEnrollmentStatus(enr.getStatus().name());

            Map<Long, Double> studentMarks = marksByEnrollmentAndAss.getOrDefault(enr.getId(), Map.of());
            row.setAssessmentScores(studentMarks);

            double totalWeighted = 0.0;
            boolean hasAnyMark = !studentMarks.isEmpty();

            for (Assessment ass : assessments) {
                Double score = studentMarks.get(ass.getId());
                if (score != null && ass.getMaxMarks() > 0) {
                    double contrib = (score / ass.getMaxMarks()) * ass.getWeightPercentage();
                    totalWeighted += contrib;
                }
            }

            totalWeighted = Math.round(totalWeighted * 100.0) / 100.0;
            row.setTotalWeightedScore(totalWeighted);

            Grade grade = gradingPolicy.calculateGrade(totalWeighted, enr.getStatus());
            Double gp = gradingPolicy.calculateGradePoint(grade);
            row.setGrade(grade);
            row.setGradePoint(gp);

            if (hasAnyMark) {
                assessedCount++;
                sumScores += totalWeighted;
                if (totalWeighted > highest) highest = totalWeighted;
                if (totalWeighted < lowest) lowest = totalWeighted;

                if (gradingPolicy.isPassingGrade(grade)) {
                    passCount++;
                } else {
                    failCount++;
                }

                gradeDist.put(grade.name(), gradeDist.getOrDefault(grade.name(), 0) + 1);
            }

            // Attendance
            List<AttendanceRecord> enrRecords = recordsByEnrollment.getOrDefault(enr.getId(), List.of());
            int excused = 0;
            int attended = 0;
            int absent = 0;
            for (AttendanceRecord r : enrRecords) {
                if (r.getStatus() == AttendanceStatus.EXCUSED) excused++;
                else if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) attended++;
                else if (r.getStatus() == AttendanceStatus.ABSENT) absent++;
            }

            int eligible = sessions.size() - excused;
            double attPct;
            if (eligible <= 0) {
                attPct = 100.0;
            } else {
                attPct = Math.round(((double) attended / eligible) * 10000.0) / 100.0;
            }

            boolean belowWarn = (attPct < 80.0) && (eligible > 0);
            if (belowWarn) {
                belowThresholdAttendanceCount++;
            }

            row.setAttendedSessions(attended);
            row.setAbsentSessions(absent);
            row.setExcusedSessions(excused);
            row.setEligibleSessions(eligible);
            row.setAttendancePercentage(attPct);
            row.setBelowAttendanceThreshold(belowWarn);
            sumAttendancePct += attPct;

            // Alerts
            List<RiskAlert> studentAlerts = alertsByStudent.getOrDefault(enr.getStudent().getId(), List.of());
            row.setActiveAlertCount(studentAlerts.size());

            AlertSeverity maxSev = null;
            for (RiskAlert sa : studentAlerts) {
                if (maxSev == null || sa.getSeverity().compareTo(maxSev) > 0) {
                    maxSev = sa.getSeverity();
                }
            }
            row.setHighestAlertSeverity(maxSev);

            studentRows.add(row);
        }

        response.setAssessedStudentsCount(assessedCount);
        if (assessedCount > 0) {
            response.setAverageScore(Math.round((sumScores / assessedCount) * 100.0) / 100.0);
            response.setHighestScore(highest >= 0 ? highest : null);
            response.setLowestScore(lowest <= 100 ? lowest : null);
            response.setPassCount(passCount);
            response.setFailCount(failCount);
            response.setPassRate(Math.round(((double) passCount / assessedCount) * 10000.0) / 100.0);
        } else {
            response.setAverageScore(null);
            response.setHighestScore(null);
            response.setLowestScore(null);
            response.setPassCount(0);
            response.setFailCount(0);
            response.setPassRate(null);
        }

        response.setGradeDistribution(gradeDist);

        if (!enrollments.isEmpty()) {
            response.setAverageAttendancePercentage(Math.round((sumAttendancePct / enrollments.size()) * 100.0) / 100.0);
        } else {
            response.setAverageAttendancePercentage(100.0);
        }
        response.setStudentsBelowThresholdCount(belowThresholdAttendanceCount);

        List<LecturerSubjectReportResponse.LecturerAtRiskStudentItem> atRiskItems = alerts.stream().map(a -> {
            LecturerSubjectReportResponse.LecturerAtRiskStudentItem item = new LecturerSubjectReportResponse.LecturerAtRiskStudentItem();
            item.setAlertId(a.getId());
            item.setStudentId(a.getStudent().getId());
            item.setStudentIdentifier(a.getStudent().getIdentifier());
            item.setStudentName(a.getStudent().getFullName());
            item.setRiskType(a.getRiskType());
            item.setSeverity(a.getSeverity());
            item.setStatus(a.getStatus());
            item.setTriggerReason(a.getReason());
            item.setDetectedAt(a.getDetectedAt());
            item.setActionTaken(a.getActionNotes());
            item.setReviewedByName(a.getReviewedBy() != null ? a.getReviewedBy().getFullName() : null);
            return item;
        }).toList();

        response.setStudents(studentRows);
        response.setAtRiskStudents(atRiskItems);

        return response;
    }

    @Override
    public byte[] exportLecturerSubjectPerformanceCsv(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        LecturerSubjectReportResponse report = getLecturerSubjectReport(subjectId, academicYear, semester, lecturerEmail);

        List<String> headers = new ArrayList<>();
        headers.add("Student ID");
        headers.add("Student Name");
        headers.add("Email");
        headers.add("Enrollment Status");

        for (LecturerSubjectReportResponse.AssessmentSummaryItem ass : report.getAssessments()) {
            headers.add(String.format("%s (Max: %.1f, W: %.0f%%)", ass.getTitle(), ass.getMaxMarks(), ass.getWeightPercentage()));
        }

        headers.add("Total Weighted Score (%)");
        headers.add("Projected Grade");
        headers.add("Grade Point");
        headers.add("Attendance (%)");
        headers.add("Active Alerts");

        List<List<Object>> rows = new ArrayList<>();
        for (LecturerSubjectReportResponse.LecturerStudentReportRow row : report.getStudents()) {
            List<Object> r = new ArrayList<>();
            r.add(row.getStudentIdentifier());
            r.add(row.getStudentName());
            r.add(row.getStudentEmail());
            r.add(row.getEnrollmentStatus());

            for (LecturerSubjectReportResponse.AssessmentSummaryItem ass : report.getAssessments()) {
                Double mark = row.getAssessmentScores().get(ass.getId());
                r.add(mark != null ? mark : "-");
            }

            r.add(row.getTotalWeightedScore() != null ? row.getTotalWeightedScore() : "-");
            r.add(row.getGrade() != null ? row.getGrade().name() : "-");
            r.add(row.getGradePoint() != null ? row.getGradePoint() : "-");
            r.add(row.getAttendancePercentage());
            r.add(row.getActiveAlertCount());

            rows.add(r);
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    @Override
    public byte[] exportLecturerSubjectAttendanceCsv(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        LecturerSubjectReportResponse report = getLecturerSubjectReport(subjectId, academicYear, semester, lecturerEmail);

        List<String> headers = List.of(
                "Student ID", "Student Name", "Email", "Total Sessions", "Attended Sessions",
                "Absent Sessions", "Excused Sessions", "Eligible Sessions", "Attendance (%)",
                "Below 80% Threshold Warning"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (LecturerSubjectReportResponse.LecturerStudentReportRow row : report.getStudents()) {
            rows.add(List.of(
                    row.getStudentIdentifier(),
                    row.getStudentName(),
                    row.getStudentEmail(),
                    report.getTotalSessionsConducted(),
                    row.getAttendedSessions(),
                    row.getAbsentSessions(),
                    row.getExcusedSessions(),
                    row.getEligibleSessions(),
                    row.getAttendancePercentage(),
                    row.isBelowAttendanceThreshold() ? "YES" : "NO"
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    @Override
    public byte[] exportLecturerSubjectAtRiskCsv(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        LecturerSubjectReportResponse report = getLecturerSubjectReport(subjectId, academicYear, semester, lecturerEmail);

        List<String> headers = List.of(
                "Alert ID", "Student ID", "Student Name", "Risk Type", "Severity",
                "Status", "Trigger Reason", "Detected At", "Action Taken", "Reviewed By"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (LecturerSubjectReportResponse.LecturerAtRiskStudentItem a : report.getAtRiskStudents()) {
            rows.add(List.of(
                    a.getAlertId(),
                    a.getStudentIdentifier(),
                    a.getStudentName(),
                    a.getRiskType().name(),
                    a.getSeverity().name(),
                    a.getStatus().name(),
                    a.getTriggerReason(),
                    a.getDetectedAt() != null ? a.getDetectedAt().format(DATE_FMT) : "",
                    a.getActionTaken() != null ? a.getActionTaken() : "",
                    a.getReviewedByName() != null ? a.getReviewedByName() : ""
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    // ==========================================
    // ADMIN REPORTS & EXPORTS
    // ==========================================

    @Override
    public AdminInstitutionalReportResponse getAdminInstitutionalReport(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester) {
        AdminInstitutionalReportResponse response = new AdminInstitutionalReportResponse();
        response.setProvisionalNotice(gradingPolicy.getProvisionalNotice());

        // Baseline counts
        response.setTotalStudents(userRepository.countByRoleAndStatus(Role.STUDENT, User.UserStatus.ACTIVE));
        response.setTotalLecturers(userRepository.countByRoleAndStatus(Role.LECTURER, User.UserStatus.ACTIVE));
        response.setTotalDepartments(departmentRepository.count());
        response.setTotalCourses(courseRepository.count());
        response.setTotalSubjects(subjectRepository.count());

        List<Subject> allSubjects = subjectRepository.findAllByOrderByNameAsc();
        List<Subject> filteredSubjects = allSubjects.stream()
                .filter(s -> departmentId == null || s.getDepartment().getId().equals(departmentId))
                .filter(s -> courseId == null || s.getCourse().getId().equals(courseId))
                .filter(s -> subjectId == null || s.getId().equals(subjectId))
                .toList();

        List<Long> filteredSubjectIds = filteredSubjects.stream().map(Subject::getId).toList();

        List<Enrollment> allEnrollments = enrollmentRepository.findAllByOrderByEnrolledAtDesc();
        List<Enrollment> filteredEnrollments = allEnrollments.stream()
                .filter(e -> filteredSubjectIds.contains(e.getSubject().getId()))
                .filter(e -> academicYear == null || academicYear.isBlank() || e.getAcademicYear().equals(academicYear))
                .filter(e -> semester == null || semester.isBlank() || e.getSemester().equals(semester))
                .toList();

        response.setTotalEnrollments(filteredEnrollments.size());

        // Grade distribution & score calculations across filtered enrollments
        Map<String, Integer> gradeDist = new HashMap<>();
        for (Grade g : Grade.values()) {
            gradeDist.put(g.name(), 0);
        }

        double sumScores = 0.0;
        int assessedCount = 0;
        int passCount = 0;
        long publishedCount = 0;

        // Group enrollments by subject and year/semester to batch fetch publications and assessments
        Map<String, List<Enrollment>> enrBySubjectAndTerm = new HashMap<>();
        for (Enrollment enr : filteredEnrollments) {
            String key = enr.getSubject().getId() + ":" + enr.getAcademicYear() + ":" + enr.getSemester();
            enrBySubjectAndTerm.computeIfAbsent(key, k -> new ArrayList<>()).add(enr);
        }

        // Attendance stats
        int excellentCount = 0;
        int goodCount = 0;
        int warningCount = 0;
        int criticalCount = 0;
        int below80Count = 0;
        double sumAttendancePct = 0.0;
        int studentAttendanceEvaluatedCount = 0;

        // Subject comparison items map
        Map<Long, AdminInstitutionalReportResponse.SubjectComparisonItem> subjectCompMap = new HashMap<>();
        for (Subject s : filteredSubjects) {
            AdminInstitutionalReportResponse.SubjectComparisonItem sci = new AdminInstitutionalReportResponse.SubjectComparisonItem();
            sci.setSubjectId(s.getId());
            sci.setCode(s.getCode());
            sci.setName(s.getName());
            sci.setCourseCode(s.getCourse().getCode());
            sci.setDepartmentName(s.getDepartment().getName());
            sci.setCredits(s.getCredits());
            sci.setEnrollmentCount(0);
            sci.setPublicationStatus(ResultPublicationStatus.DRAFT);
            sci.setAverageScore(null);
            sci.setPassRate(null);
            sci.setAverageAttendanceRate(null);
            sci.setAtRiskCount(0);
            subjectCompMap.put(s.getId(), sci);
        }

        // Process each subject-term cluster
        for (Map.Entry<String, List<Enrollment>> entry : enrBySubjectAndTerm.entrySet()) {
            List<Enrollment> cluster = entry.getValue();
            if (cluster.isEmpty()) continue;
            Enrollment first = cluster.get(0);
            Long sId = first.getSubject().getId();
            String y = first.getAcademicYear();
            String sem = first.getSemester();

            AdminInstitutionalReportResponse.SubjectComparisonItem sci = subjectCompMap.get(sId);
            if (sci != null) {
                sci.setEnrollmentCount(sci.getEnrollmentCount() + cluster.size());
            }

            Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(sId, y, sem);
            boolean isPublished = pubOpt.isPresent() && pubOpt.get().getStatus() == ResultPublicationStatus.PUBLISHED;
            if (isPublished) {
                publishedCount++;
                if (sci != null) {
                    sci.setPublicationStatus(ResultPublicationStatus.PUBLISHED);
                }
            }

            List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(sId, y, sem);
            List<Long> enrIds = cluster.stream().map(Enrollment::getId).toList();
            List<StudentMark> marks = studentMarkRepository.findByEnrollmentIdIn(enrIds);

            Map<Long, Map<Long, Double>> marksByEnr = new HashMap<>();
            for (StudentMark m : marks) {
                marksByEnr.computeIfAbsent(m.getEnrollment().getId(), k -> new HashMap<>())
                        .put(m.getAssessment().getId(), m.getScore());
            }

            double clusterSumScores = 0.0;
            int clusterAssessedCount = 0;
            int clusterPassCount = 0;

            for (Enrollment enr : cluster) {
                Map<Long, Double> sMarks = marksByEnr.getOrDefault(enr.getId(), Map.of());
                if (!sMarks.isEmpty()) {
                    double totalWeighted = 0.0;
                    for (Assessment ass : assessments) {
                        Double sc = sMarks.get(ass.getId());
                        if (sc != null && ass.getMaxMarks() > 0) {
                            totalWeighted += (sc / ass.getMaxMarks()) * ass.getWeightPercentage();
                        }
                    }
                    totalWeighted = Math.round(totalWeighted * 100.0) / 100.0;
                    sumScores += totalWeighted;
                    assessedCount++;
                    clusterSumScores += totalWeighted;
                    clusterAssessedCount++;

                    Grade grade = gradingPolicy.calculateGrade(totalWeighted, enr.getStatus());
                    gradeDist.put(grade.name(), gradeDist.getOrDefault(grade.name(), 0) + 1);

                    if (gradingPolicy.isPassingGrade(grade)) {
                        passCount++;
                        clusterPassCount++;
                    }
                }
            }

            if (sci != null && clusterAssessedCount > 0) {
                double avg = Math.round((clusterSumScores / clusterAssessedCount) * 100.0) / 100.0;
                double pr = Math.round(((double) clusterPassCount / clusterAssessedCount) * 10000.0) / 100.0;
                sci.setAverageScore(avg);
                sci.setPassRate(pr);
            }

            // Attendance in cluster
            List<AttendanceSession> sessions = attendanceSessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(sId, y, sem);
            int totSessions = sessions.size();
            List<Long> sessIds = sessions.stream().map(AttendanceSession::getId).toList();
            List<AttendanceRecord> records = sessIds.isEmpty() ? List.of() : attendanceRecordRepository.findBySessionIdIn(sessIds);

            Map<Long, List<AttendanceRecord>> recByEnr = new HashMap<>();
            for (AttendanceRecord r : records) {
                recByEnr.computeIfAbsent(r.getEnrollment().getId(), k -> new ArrayList<>()).add(r);
            }

            double clusterSumAtt = 0.0;
            int clusterAttCount = 0;

            for (Enrollment enr : cluster) {
                List<AttendanceRecord> enrRecs = recByEnr.getOrDefault(enr.getId(), List.of());
                int exc = 0;
                int att = 0;
                for (AttendanceRecord r : enrRecs) {
                    if (r.getStatus() == AttendanceStatus.EXCUSED) exc++;
                    else if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) att++;
                }

                int elig = totSessions - exc;
                double pct;
                if (elig <= 0) {
                    pct = 100.0;
                } else {
                    pct = Math.round(((double) att / elig) * 10000.0) / 100.0;
                }

                sumAttendancePct += pct;
                studentAttendanceEvaluatedCount++;
                clusterSumAtt += pct;
                clusterAttCount++;

                if (pct >= 90.0) {
                    excellentCount++;
                } else if (pct >= 80.0) {
                    goodCount++;
                } else if (pct >= 70.0) {
                    warningCount++;
                    below80Count++;
                } else {
                    criticalCount++;
                    below80Count++;
                }
            }

            if (sci != null && clusterAttCount > 0) {
                double avgAtt = Math.round((clusterSumAtt / clusterAttCount) * 100.0) / 100.0;
                sci.setAverageAttendanceRate(avgAtt);
            }
        }

        response.setPublishedSubjectsCount(publishedCount);
        if (assessedCount > 0) {
            response.setAverageScore(Math.round((sumScores / assessedCount) * 100.0) / 100.0);
            response.setOverallPassRate(Math.round(((double) passCount / assessedCount) * 10000.0) / 100.0);
        } else {
            response.setAverageScore(null);
            response.setOverallPassRate(null);
        }

        response.setGradeDistribution(gradeDist);

        if (studentAttendanceEvaluatedCount > 0) {
            response.setOverallAttendanceAverage(Math.round((sumAttendancePct / studentAttendanceEvaluatedCount) * 100.0) / 100.0);
        } else {
            response.setOverallAttendanceAverage(100.0);
        }
        response.setStudentsBelowThresholdCount(below80Count);

        response.setAttendanceBrackets(new AdminInstitutionalReportResponse.AttendanceBracketCounts(
                excellentCount, goodCount, warningCount, criticalCount
        ));

        // Risk alerts breakdown
        List<RiskAlert> allRiskAlerts = riskAlertRepository.findAllByOrderByDetectedAtDesc().stream()
                .filter(a -> filteredSubjectIds.contains(a.getSubject().getId()))
                .filter(a -> academicYear == null || academicYear.isBlank() || a.getAcademicYear().equals(academicYear))
                .filter(a -> semester == null || semester.isBlank() || a.getSemester().equals(semester))
                .toList();

        long activeAlertsTotal = allRiskAlerts.stream().filter(a -> a.getStatus() != AlertStatus.RESOLVED).count();
        response.setTotalActiveAlerts(activeAlertsTotal);

        AdminInstitutionalReportResponse.RiskAlertsSummaryStats riskSummary = new AdminInstitutionalReportResponse.RiskAlertsSummaryStats();
        Map<String, Long> byType = new HashMap<>();
        for (AlertRiskType t : AlertRiskType.values()) byType.put(t.name(), 0L);
        Map<String, Long> bySev = new HashMap<>();
        for (AlertSeverity s : AlertSeverity.values()) bySev.put(s.name(), 0L);
        Map<String, Long> bySt = new HashMap<>();
        for (AlertStatus st : AlertStatus.values()) bySt.put(st.name(), 0L);

        for (RiskAlert a : allRiskAlerts) {
            bySt.put(a.getStatus().name(), bySt.getOrDefault(a.getStatus().name(), 0L) + 1);
            if (a.getStatus() != AlertStatus.RESOLVED) {
                byType.put(a.getRiskType().name(), byType.getOrDefault(a.getRiskType().name(), 0L) + 1);
                bySev.put(a.getSeverity().name(), bySev.getOrDefault(a.getSeverity().name(), 0L) + 1);

                AdminInstitutionalReportResponse.SubjectComparisonItem sci = subjectCompMap.get(a.getSubject().getId());
                if (sci != null) {
                    sci.setAtRiskCount(sci.getAtRiskCount() + 1);
                }
            }
        }
        riskSummary.setByRiskType(byType);
        riskSummary.setBySeverity(bySev);
        riskSummary.setByStatus(bySt);
        response.setRiskAlertsSummary(riskSummary);

        // Subject comparisons list
        List<AdminInstitutionalReportResponse.SubjectComparisonItem> subjectComparisons = new ArrayList<>(subjectCompMap.values());
        subjectComparisons.sort((a, b) -> a.getCode().compareToIgnoreCase(b.getCode()));
        response.setSubjectComparisons(subjectComparisons);

        // Course comparisons
        List<Course> courses = courseRepository.findAllByOrderByNameAsc().stream()
                .filter(c -> departmentId == null || c.getDepartment().getId().equals(departmentId))
                .filter(c -> courseId == null || c.getId().equals(courseId))
                .toList();

        List<AdminInstitutionalReportResponse.CourseComparisonItem> courseComparisons = new ArrayList<>();
        for (Course c : courses) {
            AdminInstitutionalReportResponse.CourseComparisonItem cci = new AdminInstitutionalReportResponse.CourseComparisonItem();
            cci.setCourseId(c.getId());
            cci.setCode(c.getCode());
            cci.setName(c.getName());
            cci.setDepartmentName(c.getDepartment().getName());

            List<AdminInstitutionalReportResponse.SubjectComparisonItem> cSubjects = subjectComparisons.stream()
                    .filter(s -> s.getCourseCode().equalsIgnoreCase(c.getCode()))
                    .toList();
            cci.setSubjectCount(cSubjects.size());
            int cEnr = cSubjects.stream().mapToInt(AdminInstitutionalReportResponse.SubjectComparisonItem::getEnrollmentCount).sum();
            cci.setEnrollmentCount(cEnr);

            double sumSubjAvg = 0.0;
            int countSubjAvg = 0;
            double sumSubjPass = 0.0;
            int countSubjPass = 0;
            double sumSubjAtt = 0.0;
            int countSubjAtt = 0;
            int totalActiveAlertsCourse = 0;

            for (AdminInstitutionalReportResponse.SubjectComparisonItem sci : cSubjects) {
                if (sci.getAverageScore() != null) {
                    sumSubjAvg += sci.getAverageScore();
                    countSubjAvg++;
                }
                if (sci.getPassRate() != null) {
                    sumSubjPass += sci.getPassRate();
                    countSubjPass++;
                }
                if (sci.getAverageAttendanceRate() != null) {
                    sumSubjAtt += sci.getAverageAttendanceRate();
                    countSubjAtt++;
                }
                totalActiveAlertsCourse += sci.getAtRiskCount();
            }

            cci.setAverageScore(countSubjAvg > 0 ? Math.round((sumSubjAvg / countSubjAvg) * 100.0) / 100.0 : null);
            cci.setPassRate(countSubjPass > 0 ? Math.round((sumSubjPass / countSubjPass) * 100.0) / 100.0 : null);
            cci.setAverageAttendanceRate(countSubjAtt > 0 ? Math.round((sumSubjAtt / countSubjAtt) * 100.0) / 100.0 : null);
            cci.setActiveAlertsCount(totalActiveAlertsCourse);
            courseComparisons.add(cci);
        }
        response.setCourseComparisons(courseComparisons);

        // Department comparisons
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc().stream()
                .filter(d -> departmentId == null || d.getId().equals(departmentId))
                .toList();

        List<AdminInstitutionalReportResponse.DepartmentComparisonItem> departmentComparisons = new ArrayList<>();
        for (Department d : departments) {
            AdminInstitutionalReportResponse.DepartmentComparisonItem dci = new AdminInstitutionalReportResponse.DepartmentComparisonItem();
            dci.setDepartmentId(d.getId());
            dci.setCode(d.getCode());
            dci.setName(d.getName());

            List<AdminInstitutionalReportResponse.CourseComparisonItem> dCourses = courseComparisons.stream()
                    .filter(c -> c.getDepartmentName().equalsIgnoreCase(d.getName()))
                    .toList();
            dci.setCourseCount(dCourses.size());

            List<AdminInstitutionalReportResponse.SubjectComparisonItem> dSubjects = subjectComparisons.stream()
                    .filter(s -> s.getDepartmentName().equalsIgnoreCase(d.getName()))
                    .toList();
            dci.setSubjectCount(dSubjects.size());
            dci.setEnrollmentCount(dSubjects.stream().mapToInt(AdminInstitutionalReportResponse.SubjectComparisonItem::getEnrollmentCount).sum());

            double sumDAvg = 0.0;
            int countDAvg = 0;
            double sumDPass = 0.0;
            int countDPass = 0;
            double sumDAtt = 0.0;
            int countDAtt = 0;
            int totalActiveAlertsDept = 0;

            for (AdminInstitutionalReportResponse.SubjectComparisonItem sci : dSubjects) {
                if (sci.getAverageScore() != null) {
                    sumDAvg += sci.getAverageScore();
                    countDAvg++;
                }
                if (sci.getPassRate() != null) {
                    sumDPass += sci.getPassRate();
                    countDPass++;
                }
                if (sci.getAverageAttendanceRate() != null) {
                    sumDAtt += sci.getAverageAttendanceRate();
                    countDAtt++;
                }
                totalActiveAlertsDept += sci.getAtRiskCount();
            }

            dci.setAverageScore(countDAvg > 0 ? Math.round((sumDAvg / countDAvg) * 100.0) / 100.0 : null);
            dci.setPassRate(countDPass > 0 ? Math.round((sumDPass / countDPass) * 100.0) / 100.0 : null);
            dci.setAverageAttendanceRate(countDAtt > 0 ? Math.round((sumDAtt / countDAtt) * 100.0) / 100.0 : null);
            dci.setActiveAlertsCount(totalActiveAlertsDept);
            departmentComparisons.add(dci);
        }
        response.setDepartmentComparisons(departmentComparisons);

        return response;
    }

    @Override
    public byte[] exportAdminSummaryCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester) {
        AdminInstitutionalReportResponse report = getAdminInstitutionalReport(departmentId, courseId, subjectId, academicYear, semester);

        List<String> headers = List.of("Metric Category", "Indicator", "Value");
        List<List<Object>> rows = new ArrayList<>();

        rows.add(List.of("Institutional Capacity", "Total Enrolled Students", report.getTotalStudents()));
        rows.add(List.of("Institutional Capacity", "Total Active Faculty", report.getTotalLecturers()));
        rows.add(List.of("Institutional Capacity", "Academic Departments", report.getTotalDepartments()));
        rows.add(List.of("Institutional Capacity", "Degree Courses", report.getTotalCourses()));
        rows.add(List.of("Institutional Capacity", "Curriculum Subjects", report.getTotalSubjects()));
        rows.add(List.of("Institutional Capacity", "Filtered Enrollments", report.getTotalEnrollments()));
        rows.add(List.of("Academic Performance", "Published Subjects Count", report.getPublishedSubjectsCount()));
        rows.add(List.of("Academic Performance", "Institutional Average Score (%)", report.getAverageScore() != null ? report.getAverageScore() : "No Mark Data"));
        rows.add(List.of("Academic Performance", "Overall Pass Rate (%)", report.getOverallPassRate() != null ? report.getOverallPassRate() : "No Mark Data"));
        rows.add(List.of("Attendance & Compliance", "Overall Average Attendance (%)", report.getOverallAttendanceAverage()));
        rows.add(List.of("Attendance & Compliance", "Students Below 80% Threshold", report.getStudentsBelowThresholdCount()));
        rows.add(List.of("Attendance & Compliance", "Band: Excellent (>=90%)", report.getAttendanceBrackets().getExcellentCount()));
        rows.add(List.of("Attendance & Compliance", "Band: Compliant (80-89%)", report.getAttendanceBrackets().getGoodCount()));
        rows.add(List.of("Attendance & Compliance", "Band: Warning (70-79%)", report.getAttendanceBrackets().getWarningCount()));
        rows.add(List.of("Attendance & Compliance", "Band: Critical (<70%)", report.getAttendanceBrackets().getCriticalCount()));
        rows.add(List.of("Early Warning System", "Total Active Risk Alerts", report.getTotalActiveAlerts()));

        for (Map.Entry<String, Integer> e : report.getGradeDistribution().entrySet()) {
            rows.add(List.of("Grade Distribution", "Grade " + e.getKey(), e.getValue()));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    @Override
    public byte[] exportAdminDepartmentComparisonCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester) {
        AdminInstitutionalReportResponse report = getAdminInstitutionalReport(departmentId, courseId, subjectId, academicYear, semester);

        List<String> headers = List.of(
                "Department Code", "Department Name", "Degree Courses", "Subjects Count",
                "Enrollments", "Average Score (%)", "Pass Rate (%)", "Average Attendance (%)", "Active Alerts"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (AdminInstitutionalReportResponse.DepartmentComparisonItem d : report.getDepartmentComparisons()) {
            rows.add(List.of(
                    d.getCode(),
                    d.getName(),
                    d.getCourseCount(),
                    d.getSubjectCount(),
                    d.getEnrollmentCount(),
                    d.getAverageScore() != null ? d.getAverageScore() : "N/A",
                    d.getPassRate() != null ? d.getPassRate() : "N/A",
                    d.getAverageAttendanceRate() != null ? d.getAverageAttendanceRate() : "N/A",
                    d.getActiveAlertsCount()
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    @Override
    public byte[] exportAdminSubjectComparisonCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester) {
        AdminInstitutionalReportResponse report = getAdminInstitutionalReport(departmentId, courseId, subjectId, academicYear, semester);

        List<String> headers = List.of(
                "Subject Code", "Subject Name", "Course", "Department", "Credits",
                "Enrollments", "Publication Status", "Average Score (%)", "Pass Rate (%)",
                "Average Attendance (%)", "At-Risk Students"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (AdminInstitutionalReportResponse.SubjectComparisonItem s : report.getSubjectComparisons()) {
            rows.add(List.of(
                    s.getCode(),
                    s.getName(),
                    s.getCourseCode(),
                    s.getDepartmentName(),
                    s.getCredits(),
                    s.getEnrollmentCount(),
                    s.getPublicationStatus().name(),
                    s.getAverageScore() != null ? s.getAverageScore() : "N/A",
                    s.getPassRate() != null ? s.getPassRate() : "N/A",
                    s.getAverageAttendanceRate() != null ? s.getAverageAttendanceRate() : "N/A",
                    s.getAtRiskCount()
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }

    @Override
    public byte[] exportAdminRiskAlertsCsv(Long departmentId, Long courseId, Long subjectId, String academicYear, String semester) {
        List<Subject> allSubjects = subjectRepository.findAllByOrderByNameAsc();
        List<Long> filteredSubjectIds = allSubjects.stream()
                .filter(s -> departmentId == null || s.getDepartment().getId().equals(departmentId))
                .filter(s -> courseId == null || s.getCourse().getId().equals(courseId))
                .filter(s -> subjectId == null || s.getId().equals(subjectId))
                .map(Subject::getId)
                .toList();

        List<RiskAlert> alerts = riskAlertRepository.findAllByOrderByDetectedAtDesc().stream()
                .filter(a -> filteredSubjectIds.contains(a.getSubject().getId()))
                .filter(a -> academicYear == null || academicYear.isBlank() || a.getAcademicYear().equals(academicYear))
                .filter(a -> semester == null || semester.isBlank() || a.getSemester().equals(semester))
                .toList();

        List<String> headers = List.of(
                "Alert ID", "Student ID", "Student Name", "Subject Code", "Subject Name",
                "Academic Year", "Semester", "Risk Type", "Severity", "Status",
                "Trigger Reason", "Detected At", "Action Taken", "Reviewed By"
        );

        List<List<Object>> rows = new ArrayList<>();
        for (RiskAlert a : alerts) {
            rows.add(List.of(
                    a.getId(),
                    a.getStudent().getIdentifier(),
                    a.getStudent().getFullName(),
                    a.getSubject().getCode(),
                    a.getSubject().getName(),
                    a.getAcademicYear(),
                    a.getSemester(),
                    a.getRiskType().name(),
                    a.getSeverity().name(),
                    a.getStatus().name(),
                    a.getReason(),
                    a.getDetectedAt() != null ? a.getDetectedAt().format(DATE_FMT) : "",
                    a.getActionNotes() != null ? a.getActionNotes() : "",
                    a.getReviewedBy() != null ? a.getReviewedBy().getFullName() : ""
            ));
        }

        String csvString = CsvExportUtil.buildCsv(headers, rows);
        return CsvExportUtil.toCsvBytesWithBom(csvString);
    }
}
