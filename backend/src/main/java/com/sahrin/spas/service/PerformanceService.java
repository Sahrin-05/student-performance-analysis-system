package com.sahrin.spas.service;

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

import java.util.List;

public interface PerformanceService {

    // Lecturer assessment management
    AssessmentResponse createAssessment(AssessmentRequest request, String lecturerEmail);
    AssessmentResponse updateAssessment(Long assessmentId, AssessmentRequest request, String lecturerEmail);
    void deleteAssessment(Long assessmentId, String lecturerEmail);
    List<AssessmentResponse> getSubjectAssessments(Long subjectId, String academicYear, String semester, String lecturerEmail);

    // Lecturer marks entry & publication
    StudentMarkResponse enterOrUpdateMark(MarkEntryRequest request, String lecturerEmail);
    List<StudentMarkResponse> bulkEnterMarks(BulkMarkEntryRequest request, String lecturerEmail);
    SubjectRosterMarksResponse getSubjectMarksRoster(Long subjectId, String academicYear, String semester, String lecturerEmail);
    SubjectResultPublicationResponse publishSubjectResults(PublishResultsRequest request, String lecturerEmail);
    SubjectResultPublicationResponse getPublicationStatus(Long subjectId, String academicYear, String semester);

    // Admin unlock & audit override
    SubjectResultPublicationResponse unlockSubjectResults(Long subjectId, String academicYear, String semester, UnlockRequest request, String adminEmail);
    StudentMarkResponse adminOverrideMark(Long markId, AdminMarkOverrideRequest request, String adminEmail);
    List<MarkAuditLogResponse> getAuditLogsForMark(Long markId);

    // Student results retrieval
    StudentPublishedResultsResponse getMySubjectResults(Long enrollmentId, String studentEmail);
    List<StudentOverallResultResponse> getMyAllPublishedResults(String studentEmail);
}
