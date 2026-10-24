package com.sahrin.spas.service.impl;

import com.sahrin.spas.dto.performance.AdminMarkOverrideRequest;
import com.sahrin.spas.dto.performance.AssessmentRequest;
import com.sahrin.spas.dto.performance.AssessmentResponse;
import com.sahrin.spas.dto.performance.BulkMarkEntryRequest;
import com.sahrin.spas.dto.performance.MarkAuditLogResponse;
import com.sahrin.spas.dto.performance.MarkEntryRequest;
import com.sahrin.spas.dto.performance.PublishResultsRequest;
import com.sahrin.spas.dto.performance.StudentMarkResponse;
import com.sahrin.spas.dto.performance.StudentOverallResultResponse;
import com.sahrin.spas.dto.performance.StudentPublishedResultsResponse;
import com.sahrin.spas.dto.performance.SubjectResultPublicationResponse;
import com.sahrin.spas.dto.performance.SubjectRosterMarksResponse;
import com.sahrin.spas.dto.performance.UnlockRequest;
import com.sahrin.spas.entity.Assessment;
import com.sahrin.spas.entity.Enrollment;
import com.sahrin.spas.entity.EnrollmentStatus;
import com.sahrin.spas.entity.Grade;
import com.sahrin.spas.entity.MarkAuditLog;
import com.sahrin.spas.entity.ResultPublicationStatus;
import com.sahrin.spas.entity.StudentMark;
import com.sahrin.spas.entity.Subject;
import com.sahrin.spas.entity.SubjectResultPublication;
import com.sahrin.spas.entity.User;
import com.sahrin.spas.exception.InvalidStatusTransitionException;
import com.sahrin.spas.exception.ResourceNotFoundException;
import com.sahrin.spas.repository.AssessmentRepository;
import com.sahrin.spas.repository.EnrollmentRepository;
import com.sahrin.spas.repository.LecturerAssignmentRepository;
import com.sahrin.spas.repository.MarkAuditLogRepository;
import com.sahrin.spas.repository.StudentMarkRepository;
import com.sahrin.spas.repository.SubjectRepository;
import com.sahrin.spas.repository.SubjectResultPublicationRepository;
import com.sahrin.spas.repository.UserRepository;
import com.sahrin.spas.service.GradingPolicy;
import com.sahrin.spas.service.PerformanceService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {

    private final AssessmentRepository assessmentRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final SubjectResultPublicationRepository publicationRepository;
    private final MarkAuditLogRepository auditLogRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerAssignmentRepository lecturerAssignmentRepository;
    private final UserRepository userRepository;
    private final GradingPolicy gradingPolicy;

    public PerformanceServiceImpl(AssessmentRepository assessmentRepository,
                                  StudentMarkRepository studentMarkRepository,
                                  SubjectResultPublicationRepository publicationRepository,
                                  MarkAuditLogRepository auditLogRepository,
                                  SubjectRepository subjectRepository,
                                  EnrollmentRepository enrollmentRepository,
                                  LecturerAssignmentRepository lecturerAssignmentRepository,
                                  UserRepository userRepository,
                                  GradingPolicy gradingPolicy) {
        this.assessmentRepository = assessmentRepository;
        this.studentMarkRepository = studentMarkRepository;
        this.publicationRepository = publicationRepository;
        this.auditLogRepository = auditLogRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerAssignmentRepository = lecturerAssignmentRepository;
        this.userRepository = userRepository;
        this.gradingPolicy = gradingPolicy;
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

    private void checkSubjectNotPublished(Long subjectId, String academicYear, String semester, String actionDescription) {
        Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(subjectId, academicYear, semester);
        if (pubOpt.isPresent() && pubOpt.get().getStatus() == ResultPublicationStatus.PUBLISHED) {
            throw new InvalidStatusTransitionException("Cannot " + actionDescription + ": Results for this subject are currently published. An administrative unlock is required.");
        }
    }

    // ==========================================
    // ASSESSMENT OPERATIONS
    // ==========================================

    @Override
    public AssessmentResponse createAssessment(AssessmentRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(request.getSubjectId(), lecturer.getId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        checkSubjectNotPublished(subject.getId(), request.getAcademicYear(), request.getSemester(), "create assessment");

        List<Assessment> existing = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subject.getId(), request.getAcademicYear(), request.getSemester());
        double currentTotal = existing.stream().mapToDouble(Assessment::getWeightPercentage).sum();
        double newTotal = currentTotal + request.getWeightPercentage();

        if (newTotal > 100.0001) {
            throw new IllegalArgumentException(String.format(
                    "Total assessment weight cannot exceed 100.0%%. Current total is %.2f%%; adding %.2f%% would equal %.2f%%.",
                    currentTotal, request.getWeightPercentage(), newTotal));
        }

        Assessment assessment = new Assessment(
                request.getTitle().trim(),
                request.getType(),
                request.getMaxMarks(),
                request.getWeightPercentage(),
                subject,
                request.getAcademicYear().trim(),
                request.getSemester().trim(),
                lecturer
        );

        Assessment saved = assessmentRepository.save(assessment);
        return new AssessmentResponse(saved);
    }

    @Override
    public AssessmentResponse updateAssessment(Long assessmentId, AssessmentRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", assessmentId));

        validateLecturerSubjectAssignment(assessment.getSubject().getId(), lecturer.getId());
        checkSubjectNotPublished(assessment.getSubject().getId(), assessment.getAcademicYear(), assessment.getSemester(), "modify assessment");

        List<Assessment> existing = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                assessment.getSubject().getId(), assessment.getAcademicYear(), assessment.getSemester());
        double otherTotal = existing.stream()
                .filter(a -> !a.getId().equals(assessmentId))
                .mapToDouble(Assessment::getWeightPercentage)
                .sum();
        double newTotal = otherTotal + request.getWeightPercentage();

        if (newTotal > 100.0001) {
            throw new IllegalArgumentException(String.format(
                    "Total assessment weight cannot exceed 100.0%%. Other assessments total %.2f%%; new weight %.2f%% would equal %.2f%%.",
                    otherTotal, request.getWeightPercentage(), newTotal));
        }

        List<StudentMark> marks = studentMarkRepository.findByAssessmentId(assessmentId);
        for (StudentMark mark : marks) {
            if (mark.getScore() > request.getMaxMarks()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot reduce maximum marks to %.2f because student %s already has a recorded score of %.2f.",
                        request.getMaxMarks(), mark.getEnrollment().getStudent().getIdentifier(), mark.getScore()));
            }
        }

        assessment.setTitle(request.getTitle().trim());
        assessment.setType(request.getType());
        assessment.setMaxMarks(request.getMaxMarks());
        assessment.setWeightPercentage(request.getWeightPercentage());

        Assessment updated = assessmentRepository.save(assessment);
        return new AssessmentResponse(updated);
    }

    @Override
    public void deleteAssessment(Long assessmentId, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", assessmentId));

        validateLecturerSubjectAssignment(assessment.getSubject().getId(), lecturer.getId());
        checkSubjectNotPublished(assessment.getSubject().getId(), assessment.getAcademicYear(), assessment.getSemester(), "delete assessment");

        List<StudentMark> marks = studentMarkRepository.findByAssessmentId(assessmentId);
        if (!marks.isEmpty()) {
            throw new InvalidStatusTransitionException("Cannot delete assessment '" + assessment.getTitle() + "' because it already contains " + marks.size() + " recorded student mark(s). Delete or clear the marks first.");
        }

        assessmentRepository.delete(assessment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> getSubjectAssessments(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(subjectId, lecturer.getId());

        List<Assessment> list = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(subjectId, academicYear, semester);
        return list.stream().map(AssessmentResponse::new).toList();
    }

    // ==========================================
    // MARKS OPERATIONS
    // ==========================================

    @Override
    public StudentMarkResponse enterOrUpdateMark(MarkEntryRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", request.getAssessmentId()));

        validateLecturerSubjectAssignment(assessment.getSubject().getId(), lecturer.getId());
        checkSubjectNotPublished(assessment.getSubject().getId(), assessment.getAcademicYear(), assessment.getSemester(), "record student marks");

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", request.getEnrollmentId()));

        if (!enrollment.getSubject().getId().equals(assessment.getSubject().getId())) {
            throw new IllegalArgumentException("The specified enrollment does not belong to the subject of this assessment.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            throw new InvalidStatusTransitionException("Cannot record marks for a dropped student enrollment.");
        }

        if (request.getScore() < 0.0 || request.getScore() > assessment.getMaxMarks()) {
            throw new IllegalArgumentException(String.format(
                    "Score must be between 0.00 and the maximum mark of %.2f for %s.",
                    assessment.getMaxMarks(), assessment.getTitle()));
        }

        Optional<StudentMark> existingOpt = studentMarkRepository.findByAssessmentIdAndEnrollmentId(
                assessment.getId(), enrollment.getId());

        StudentMark mark;
        if (existingOpt.isPresent()) {
            mark = existingOpt.get();
            mark.setScore(request.getScore());
            mark.setComments(request.getComments());
            mark.setLastModifiedBy(lecturer);
        } else {
            mark = new StudentMark(
                    assessment,
                    enrollment,
                    request.getScore(),
                    request.getComments(),
                    lecturer
            );
        }

        StudentMark saved = studentMarkRepository.save(mark);
        return new StudentMarkResponse(saved);
    }

    @Override
    public List<StudentMarkResponse> bulkEnterMarks(BulkMarkEntryRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        Assessment assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", request.getAssessmentId()));

        validateLecturerSubjectAssignment(assessment.getSubject().getId(), lecturer.getId());
        checkSubjectNotPublished(assessment.getSubject().getId(), assessment.getAcademicYear(), assessment.getSemester(), "record student marks");

        List<StudentMarkResponse> responses = new ArrayList<>();
        for (BulkMarkEntryRequest.StudentMarkItem item : request.getItems()) {
            MarkEntryRequest singleReq = new MarkEntryRequest(assessment.getId(), item.getEnrollmentId(), item.getScore(), item.getComments());
            responses.add(enterOrUpdateMark(singleReq, lecturerEmail));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectRosterMarksResponse getSubjectMarksRoster(Long subjectId, String academicYear, String semester, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(subjectId, lecturer.getId());

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subjectId, academicYear, semester);
        List<Enrollment> enrollments = enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);

        Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);

        double totalWeight = assessments.stream().mapToDouble(Assessment::getWeightPercentage).sum();

        SubjectRosterMarksResponse response = new SubjectRosterMarksResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setAcademicYear(academicYear);
        response.setSemester(semester);
        response.setTotalAssessmentWeight(Math.round(totalWeight * 100.0) / 100.0);

        if (pubOpt.isPresent()) {
            SubjectResultPublication pub = pubOpt.get();
            response.setPublicationStatus(pub.getStatus());
            response.setPublishedAt(pub.getPublishedAt());
            if (pub.getPublishedBy() != null) {
                response.setPublishedByName(pub.getPublishedBy().getFullName());
            }
        } else {
            response.setPublicationStatus(ResultPublicationStatus.DRAFT);
        }

        boolean canPublish = Math.abs(totalWeight - 100.0) < 0.001
                && (response.getPublicationStatus() == ResultPublicationStatus.DRAFT);
        response.setCanPublish(canPublish);
        response.setAssessments(assessments.stream().map(AssessmentResponse::new).toList());

        List<Long> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();
        List<StudentMark> allMarks = enrollmentIds.isEmpty() ? List.of() : studentMarkRepository.findByEnrollmentIdIn(enrollmentIds);

        Map<Long, Map<Long, StudentMark>> marksByEnrollmentAndAssessment = new HashMap<>();
        for (StudentMark m : allMarks) {
            marksByEnrollmentAndAssessment
                    .computeIfAbsent(m.getEnrollment().getId(), k -> new HashMap<>())
                    .put(m.getAssessment().getId(), m);
        }

        List<SubjectRosterMarksResponse.StudentRosterRow> rows = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            SubjectRosterMarksResponse.StudentRosterRow row = new SubjectRosterMarksResponse.StudentRosterRow();
            row.setEnrollmentId(enr.getId());
            row.setStudentId(enr.getStudent().getId());
            row.setStudentIdentifier(enr.getStudent().getIdentifier());
            row.setStudentName(enr.getStudent().getFullName());
            row.setStudentEmail(enr.getStudent().getEmail());
            row.setEnrollmentStatus(enr.getStatus().name());

            Map<Long, StudentMark> studentMarksMap = marksByEnrollmentAndAssessment.getOrDefault(enr.getId(), Map.of());
            Map<Long, StudentMarkResponse> rowMarks = new HashMap<>();

            double totalWeightedScore = 0.0;
            for (Assessment ass : assessments) {
                StudentMark m = studentMarksMap.get(ass.getId());
                if (m != null) {
                    StudentMarkResponse smResp = new StudentMarkResponse(m);
                    rowMarks.put(ass.getId(), smResp);
                    totalWeightedScore += smResp.getWeightedContribution();
                }
            }

            row.setMarks(rowMarks);
            row.setTotalWeightedScore(Math.round(totalWeightedScore * 100.0) / 100.0);

            Grade projectedGrade = gradingPolicy.calculateGrade(row.getTotalWeightedScore(), enr.getStatus());
            row.setProjectedGrade(projectedGrade);
            row.setProjectedGradePoint(gradingPolicy.calculateGradePoint(projectedGrade));

            rows.add(row);
        }

        response.setStudentRows(rows);
        return response;
    }

    @Override
    public SubjectResultPublicationResponse publishSubjectResults(PublishResultsRequest request, String lecturerEmail) {
        User lecturer = getAuthenticatedUser(lecturerEmail);
        validateLecturerSubjectAssignment(request.getSubjectId(), lecturer.getId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subject.getId(), request.getAcademicYear(), request.getSemester());

        if (assessments.isEmpty()) {
            throw new InvalidStatusTransitionException("Cannot publish results: No assessments have been defined for this subject.");
        }

        double totalWeight = assessments.stream().mapToDouble(Assessment::getWeightPercentage).sum();
        if (Math.abs(totalWeight - 100.0) > 0.001) {
            throw new InvalidStatusTransitionException(String.format(
                    "Cannot publish results: Total assessment weight must equal exactly 100.00%% before results can be published. Current total is %.2f%%.",
                    totalWeight));
        }

        SubjectResultPublication publication = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subject.getId(), request.getAcademicYear(), request.getSemester())
                .orElseGet(() -> new SubjectResultPublication(subject, request.getAcademicYear(), request.getSemester()));

        publication.setStatus(ResultPublicationStatus.PUBLISHED);
        publication.setPublishedBy(lecturer);
        publication.setPublishedAt(LocalDateTime.now());
        publication.setUnlockedBy(null);
        publication.setUnlockedAt(null);
        publication.setUnlockReason(null);

        SubjectResultPublication saved = publicationRepository.save(publication);
        return new SubjectResultPublicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResultPublicationResponse getPublicationStatus(Long subjectId, String academicYear, String semester) {
        Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester);
        if (pubOpt.isPresent()) {
            return new SubjectResultPublicationResponse(pubOpt.get());
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        SubjectResultPublication defaultDraft = new SubjectResultPublication(subject, academicYear, semester);
        return new SubjectResultPublicationResponse(defaultDraft);
    }

    // ==========================================
    // ADMIN UNLOCK & AUDIT OVERRIDE
    // ==========================================

    @Override
    public SubjectResultPublicationResponse unlockSubjectResults(Long subjectId, String academicYear, String semester,
                                                                 UnlockRequest request, String adminEmail) {
        User admin = getAuthenticatedUser(adminEmail);

        SubjectResultPublication publication = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subjectId, academicYear, semester)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SubjectResultPublication", "subjectId and term", subjectId + " " + academicYear + " " + semester));

        if (publication.getStatus() != ResultPublicationStatus.PUBLISHED) {
            throw new InvalidStatusTransitionException("Cannot unlock results: Results for this subject are currently in DRAFT status.");
        }

        publication.setStatus(ResultPublicationStatus.DRAFT);
        publication.setUnlockedBy(admin);
        publication.setUnlockedAt(LocalDateTime.now());
        publication.setUnlockReason(request.getReason().trim());

        SubjectResultPublication saved = publicationRepository.save(publication);

        // Record audit log entry for administrative unlock
        MarkAuditLog auditLog = new MarkAuditLog(
                0L,
                0L,
                0L,
                "ALL",
                null,
                0.0,
                "ADMIN_UNLOCK",
                request.getReason().trim(),
                admin
        );
        auditLogRepository.save(auditLog);

        return new SubjectResultPublicationResponse(saved);
    }

    @Override
    public StudentMarkResponse adminOverrideMark(Long markId, AdminMarkOverrideRequest request, String adminEmail) {
        User admin = getAuthenticatedUser(adminEmail);
        StudentMark mark = studentMarkRepository.findById(markId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentMark", "id", markId));

        Assessment assessment = mark.getAssessment();
        if (request.getNewScore() < 0.0 || request.getNewScore() > assessment.getMaxMarks()) {
            throw new IllegalArgumentException(String.format(
                    "Override score must be between 0.00 and the maximum mark of %.2f.",
                    assessment.getMaxMarks()));
        }

        double oldScore = mark.getScore();
        mark.setScore(request.getNewScore());
        mark.setLastModifiedBy(admin);

        StudentMark saved = studentMarkRepository.save(mark);

        MarkAuditLog auditLog = new MarkAuditLog(
                mark.getId(),
                assessment.getId(),
                mark.getEnrollment().getId(),
                mark.getEnrollment().getStudent().getIdentifier(),
                oldScore,
                request.getNewScore(),
                "ADMIN_OVERRIDE",
                request.getReason().trim(),
                admin
        );
        auditLogRepository.save(auditLog);

        return new StudentMarkResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkAuditLogResponse> getAuditLogsForMark(Long markId) {
        List<MarkAuditLog> logs = auditLogRepository.findByMarkIdOrderByPerformedAtDesc(markId);
        return logs.stream().map(MarkAuditLogResponse::new).toList();
    }

    // ==========================================
    // STUDENT RESULTS RETRIEVAL
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public StudentPublishedResultsResponse getMySubjectResults(Long enrollmentId, String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getStudent().getId().equals(student.getId())) {
            throw new AccessDeniedException("Access denied: You may only view your own academic results.");
        }

        Subject subject = enrollment.getSubject();
        Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                subject.getId(), enrollment.getAcademicYear(), enrollment.getSemester());

        StudentPublishedResultsResponse resp = new StudentPublishedResultsResponse();
        resp.setEnrollmentId(enrollment.getId());
        resp.setSubjectId(subject.getId());
        resp.setSubjectCode(subject.getCode());
        resp.setSubjectName(subject.getName());
        resp.setCredits(subject.getCredits());
        resp.setAcademicYear(enrollment.getAcademicYear());
        resp.setSemester(enrollment.getSemester());

        boolean isPublished = pubOpt.isPresent() && pubOpt.get().getStatus() == ResultPublicationStatus.PUBLISHED;

        if (!isPublished) {
            resp.setPublicationStatus(ResultPublicationStatus.DRAFT);
            resp.setStatusMessage("Assessment marks for this subject have not been published by the instructor yet.");
            resp.setAssessmentResults(List.of());
            resp.setTotalWeightedScore(null);
            resp.setGrade(null);
            resp.setGradePoint(null);
            return resp;
        }

        resp.setPublicationStatus(ResultPublicationStatus.PUBLISHED);
        resp.setPublishedAt(pubOpt.get().getPublishedAt());

        List<Assessment> assessments = assessmentRepository.findBySubjectIdAndAcademicYearAndSemesterOrderByCreatedAtAsc(
                subject.getId(), enrollment.getAcademicYear(), enrollment.getSemester());
        List<StudentMark> marks = studentMarkRepository.findByEnrollmentId(enrollment.getId());

        Map<Long, StudentMark> marksByAssessmentId = new HashMap<>();
        for (StudentMark m : marks) {
            marksByAssessmentId.put(m.getAssessment().getId(), m);
        }

        List<StudentPublishedResultsResponse.StudentAssessmentResultItem> resultItems = new ArrayList<>();
        double totalWeightedScore = 0.0;

        for (Assessment ass : assessments) {
            StudentMark m = marksByAssessmentId.get(ass.getId());
            Double score = m != null ? m.getScore() : null;
            Double weightedContribution = null;
            String comments = m != null ? m.getComments() : null;

            if (score != null && ass.getMaxMarks() > 0) {
                weightedContribution = (score / ass.getMaxMarks()) * ass.getWeightPercentage();
                weightedContribution = Math.round(weightedContribution * 100.0) / 100.0;
                totalWeightedScore += weightedContribution;
            }

            resultItems.add(new StudentPublishedResultsResponse.StudentAssessmentResultItem(
                    ass.getId(),
                    ass.getTitle(),
                    ass.getType(),
                    ass.getMaxMarks(),
                    score,
                    ass.getWeightPercentage(),
                    weightedContribution,
                    comments
            ));
        }

        totalWeightedScore = Math.round(totalWeightedScore * 100.0) / 100.0;
        resp.setAssessmentResults(resultItems);
        resp.setTotalWeightedScore(totalWeightedScore);

        Grade finalGrade = gradingPolicy.calculateGrade(totalWeightedScore, enrollment.getStatus());
        resp.setGrade(finalGrade);
        resp.setGradePoint(gradingPolicy.calculateGradePoint(finalGrade));
        resp.setGradingNotice(gradingPolicy.getProvisionalNotice());
        resp.setStatusMessage("Results published.");

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentOverallResultResponse> getMyAllPublishedResults(String studentEmail) {
        User student = getAuthenticatedUser(studentEmail);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(student.getId());

        List<StudentOverallResultResponse> overallList = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            Optional<SubjectResultPublication> pubOpt = publicationRepository.findBySubjectIdAndAcademicYearAndSemester(
                    enr.getSubject().getId(), enr.getAcademicYear(), enr.getSemester());

            boolean isPublished = pubOpt.isPresent() && pubOpt.get().getStatus() == ResultPublicationStatus.PUBLISHED;
            if (isPublished) {
                StudentPublishedResultsResponse subResult = getMySubjectResults(enr.getId(), studentEmail);
                overallList.add(new StudentOverallResultResponse(
                        enr.getId(),
                        enr.getSubject().getId(),
                        enr.getSubject().getCode(),
                        enr.getSubject().getName(),
                        enr.getSubject().getCredits(),
                        enr.getAcademicYear(),
                        enr.getSemester(),
                        ResultPublicationStatus.PUBLISHED,
                        subResult.getTotalWeightedScore(),
                        subResult.getGrade(),
                        subResult.getGradePoint()
                ));
            } else {
                overallList.add(new StudentOverallResultResponse(
                        enr.getId(),
                        enr.getSubject().getId(),
                        enr.getSubject().getCode(),
                        enr.getSubject().getName(),
                        enr.getSubject().getCredits(),
                        enr.getAcademicYear(),
                        enr.getSemester(),
                        ResultPublicationStatus.DRAFT,
                        null,
                        null,
                        null
                ));
            }
        }

        return overallList;
    }
}
