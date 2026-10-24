package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.attendance.AttendanceRecordResponse;
import com.sahrin.spas.dto.attendance.AttendanceSessionRequest;
import com.sahrin.spas.dto.attendance.AttendanceSessionResponse;
import com.sahrin.spas.dto.attendance.RecordAttendanceRequest;
import com.sahrin.spas.dto.attendance.SessionRosterAttendanceResponse;
import com.sahrin.spas.dto.attendance.StudentAttendanceSummaryResponse;
import com.sahrin.spas.dto.attendance.SubjectAttendanceSummaryResponse;
import com.sahrin.spas.entity.AttendanceRecord;
import com.sahrin.spas.entity.AttendanceSession;
import com.sahrin.spas.entity.AttendanceStatus;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.AttendanceRecordRepository;
import com.sahrin.spas.repository.AttendanceSessionRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.AttendanceService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerAssignmentRepository lecturerAssignmentRepository;
    private final UserRepository userRepository;

    public AttendanceServiceImpl(AttendanceSessionRepository sessionRepository,
                                 AttendanceRecordRepository recordRepository,
                                 SubjectRepository subjectRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 LecturerAssignmentRepository lecturerAssignmentRepository,
                                 UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerAssignmentRepository = lecturerAssignmentRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void validateLecturerSubjectAssignment(Long subjectId, Long lecturerId) {
        boolean isAssigned = lecturerAssignmentRepository.existsBySubjectIdAndLecturerIdAndActiveTrue(subjectId, lecturerId);
        if (!isAssigned) {
            throw new AccessDeniedException("Access denied: You are not actively assigned to teach this subject.");
        }
    }

    // ==========================================
    // LECTURER SESSION MANAGEMENT
    // ==========================================

    @Override
    public AttendanceSessionResponse createSession(AttendanceSessionRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(request.getSubjectId(), lecturer.getId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        AttendanceSession session = new AttendanceSession(
                subject,
                request.getSessionDate(),
                request.getSessionTopic().trim(),
                request.getAcademicYear().trim(),
                request.getSemester().trim(),
                lecturer
        );

        AttendanceSession saved = sessionRepository.save(session);
        return new AttendanceSessionResponse(saved);
    }

    @Override
    public AttendanceSessionResponse updateSession(Long sessionId, AttendanceSessionRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        validateLecturerSubjectAssignment(session.getSubject().getId(), lecturer.getId());

        session.setSessionDate(request.getSessionDate());
        session.setSessionTopic(request.getSessionTopic().trim());

        AttendanceSession updated = sessionRepository.save(session);
        return new AttendanceSessionResponse(updated);
    }

    @Override
    public void deleteSession(Long sessionId, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        validateLecturerSubjectAssignment(session.getSubject().getId(), lecturer.getId());

        List<AttendanceRecord> records = recordRepository.findBySessionId(sessionId);
        recordRepository.deleteAll(records);
        sessionRepository.delete(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> getSubjectSessions(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(subjectId, lecturer.getId());

        List<AttendanceSession> sessions = sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                subjectId, academicYear, semester);
        return sessions.stream().map(AttendanceSessionResponse::new).toList();
    }

    // ==========================================
    // LECTURER ATTENDANCE MARKING
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public SessionRosterAttendanceResponse getSessionRoster(Long sessionId, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));

        validateLecturerSubjectAssignment(session.getSubject().getId(), lecturer.getId());

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(
                session.getSubject().getId(), session.getAcademicYear(), session.getSemester());

        List<AttendanceRecord> existingRecords = recordRepository.findBySessionId(sessionId);
        Map<Long, AttendanceRecord> recordByEnrollmentId = new HashMap<>();
        for (AttendanceRecord r : existingRecords) {
            recordByEnrollmentId.put(r.getEnrollment().getId(), r);
        }

        List<SessionRosterAttendanceResponse.StudentAttendanceItem> rosterItems = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            AttendanceRecord rec = recordByEnrollmentId.get(enr.getId());
            String status = rec != null ? rec.getStatus().name() : "PRESENT"; // Default to PRESENT for easy roster marking
            String remarks = rec != null ? rec.getRemarks() : "";

            rosterItems.add(new SessionRosterAttendanceResponse.StudentAttendanceItem(
                    enr.getId(),
                    enr.getStudent().getId(),
                    enr.getStudent().getIdentifier(),
                    enr.getStudent().getFullName(),
                    enr.getStudent().getEmail(),
                    status,
                    remarks
            ));
        }

        return new SessionRosterAttendanceResponse(new AttendanceSessionResponse(session), rosterItems);
    }

    @Override
    public List<AttendanceRecordResponse> recordSessionAttendance(RecordAttendanceRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", request.getSessionId()));

        validateLecturerSubjectAssignment(session.getSubject().getId(), lecturer.getId());

        List<AttendanceRecordResponse> responses = new ArrayList<>();

        for (RecordAttendanceRequest.StudentAttendanceItem item : request.getRecords()) {
            Enrollment enrollment = enrollmentRepository.findById(item.getEnrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", item.getEnrollmentId()));

            if (!enrollment.getSubject().getId().equals(session.getSubject().getId())) {
                throw new IllegalArgumentException("Enrollment " + item.getEnrollmentId() + " does not belong to session subject.");
            }

            if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
                throw new IllegalArgumentException("Cannot record attendance for non-active enrollment: " + item.getEnrollmentId());
            }

            Optional<AttendanceRecord> existingOpt = recordRepository.findBySessionIdAndEnrollmentId(
                    session.getId(), enrollment.getId());

            AttendanceRecord record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.setStatus(item.getStatus());
                record.setRemarks(item.getRemarks());
            } else {
                record = new AttendanceRecord(
                        session,
                        enrollment,
                        item.getStatus(),
                        item.getRemarks()
                );
            }

            AttendanceRecord saved = recordRepository.save(record);
            responses.add(new AttendanceRecordResponse(saved));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectAttendanceSummaryResponse getSubjectAttendanceSummary(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(subjectId, lecturer.getId());

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<AttendanceSession> sessions = sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                subjectId, academicYear, semester);
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);

        int totalSessions = sessions.size();
        List<Long> sessionIds = sessions.stream().map(AttendanceSession::getId).toList();
        List<AttendanceRecord> allRecords = sessionIds.isEmpty() ? List.of() : recordRepository.findBySessionIdIn(sessionIds);

        Map<Long, List<AttendanceRecord>> recordsByEnrollment = new HashMap<>();
        for (AttendanceRecord r : allRecords) {
            recordsByEnrollment.computeIfAbsent(r.getEnrollment().getId(), k -> new ArrayList<>()).add(r);
        }

        List<SubjectAttendanceSummaryResponse.StudentSubjectAttendanceRate> studentSummaries = new ArrayList<>();
        double sumPercentages = 0.0;

        for (Enrollment enr : enrollments) {
            List<AttendanceRecord> studentRecords = recordsByEnrollment.getOrDefault(enr.getId(), List.of());

            int excused = 0;
            int attended = 0;
            int absent = 0;

            for (AttendanceRecord r : studentRecords) {
                if (r.getStatus() == AttendanceStatus.EXCUSED) {
                    excused++;
                } else if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) {
                    attended++;
                } else if (r.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }
            }

            int eligible = totalSessions - excused;
            double percentage;
            if (eligible <= 0) {
                percentage = 100.0; // Avoid divide by zero when no eligible sessions exist
            } else {
                percentage = Math.round(((double) attended / eligible) * 10000.0) / 100.0;
            }

            boolean belowWarning = (percentage < 80.0) && (eligible > 0);

            SubjectAttendanceSummaryResponse.StudentSubjectAttendanceRate rate = new SubjectAttendanceSummaryResponse.StudentSubjectAttendanceRate();
            rate.setEnrollmentId(enr.getId());
            rate.setStudentId(enr.getStudent().getId());
            rate.setStudentIdentifier(enr.getStudent().getIdentifier());
            rate.setStudentName(enr.getStudent().getFullName());
            rate.setTotalSessions(totalSessions);
            rate.setExcusedSessions(excused);
            rate.setEligibleSessions(eligible);
            rate.setAttendedSessions(attended);
            rate.setAbsentSessions(absent);
            rate.setAttendancePercentage(percentage);
            rate.setBelowWarningThreshold(belowWarning);

            studentSummaries.add(rate);
            sumPercentages += percentage;
        }

        double overallAverage = studentSummaries.isEmpty() ? 100.0 : Math.round((sumPercentages / studentSummaries.size()) * 100.0) / 100.0;

        SubjectAttendanceSummaryResponse response = new SubjectAttendanceSummaryResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setAcademicYear(academicYear);
        response.setSemester(semester);
        response.setTotalSessions(totalSessions);
        response.setOverallAttendanceRate(overallAverage);
        response.setStudentSummaries(studentSummaries);

        return response;
    }

    // ==========================================
    // STUDENT ATTENDANCE TRACKING
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public StudentAttendanceSummaryResponse getMySubjectAttendance(Long enrollmentId, String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("Access denied: You may only view your own attendance records.");
        }

        Subject subject = enrollment.getSubject();
        List<AttendanceSession> sessions = sessionRepository.findBySubjectIdAndAcademicYearAndSemesterOrderBySessionDateAsc(
                subject.getId(), enrollment.getAcademicYear(), enrollment.getSemester());

        List<AttendanceRecord> records = recordRepository.findByEnrollmentId(enrollment.getId());
        Map<Long, AttendanceRecord> recordBySessionId = new HashMap<>();
        for (AttendanceRecord r : records) {
            recordBySessionId.put(r.getSession().getId(), r);
        }

        int totalSessions = sessions.size();
        int excused = 0;
        int attended = 0;
        int absent = 0;

        List<AttendanceRecordResponse> recordResponses = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            AttendanceRecord r = recordBySessionId.get(s.getId());
            if (r != null) {
                if (r.getStatus() == AttendanceStatus.EXCUSED) {
                    excused++;
                } else if (r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE) {
                    attended++;
                } else if (r.getStatus() == AttendanceStatus.ABSENT) {
                    absent++;
                }
                recordResponses.add(new AttendanceRecordResponse(r));
            } else {
                // Session occurred but no individual record found
                absent++;
            }
        }

        int eligible = totalSessions - excused;
        double percentage;
        if (eligible <= 0) {
            percentage = 100.0;
        } else {
            percentage = Math.round(((double) attended / eligible) * 10000.0) / 100.0;
        }

        // Below-80% warning condition: strictly less than 80.0% when eligible sessions exist
        boolean isBelowThreshold = (percentage < 80.0) && (eligible > 0);

        StudentAttendanceSummaryResponse response = new StudentAttendanceSummaryResponse();
        response.setEnrollmentId(enrollment.getId());
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setCredits(subject.getCredits());
        response.setAcademicYear(enrollment.getAcademicYear());
        response.setSemester(enrollment.getSemester());
        response.setTotalSessions(totalSessions);
        response.setExcusedSessions(excused);
        response.setEligibleSessions(eligible);
        response.setAttendedSessions(attended);
        response.setAbsentSessions(absent);
        response.setAttendancePercentage(percentage);
        response.setBelowWarningThreshold(isBelowThreshold);
        response.setWarningThreshold(80.0);

        if (isBelowThreshold) {
            response.setWarningMessage(String.format(
                    "Attendance Warning: Your attendance for %s (%s) is currently %.2f%%, which is below the mandatory 80.0%% requirement. Please consult your lecturer.",
                    subject.getCode(), subject.getName(), percentage));
        }

        response.setRecords(recordResponses);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentAttendanceSummaryResponse> getMyAllAttendance(String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(student.getId());

        List<StudentAttendanceSummaryResponse> list = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            list.add(getMySubjectAttendance(enr.getId(), studentEmail));
        }
        return list;
    }
}
