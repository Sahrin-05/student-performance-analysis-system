import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService'
import type { AlertRiskType, AlertSeverity, AlertStatus } from './riskAlertService'
import type { Grade, ResultPublicationStatus, AssessmentType } from './performanceService'

// ==========================================
// STUDENT REPORT TYPES
// ==========================================

export interface StudentAssessmentScoreItem {
  assessmentId: number
  assessmentTitle: string
  assessmentType: AssessmentType
  maxMarks: number
  score: number | null
  weightPercentage: number
  weightedContribution: number | null
}

export interface StudentRiskAlertItem {
  alertId: number
  riskType: AlertRiskType
  severity: AlertSeverity
  status: AlertStatus
  triggerReason: string
  advisoryGuidance: string
  detectedAt: string
}

export interface StudentSubjectReportItem {
  enrollmentId: number
  subjectId: number
  subjectCode: string
  subjectName: string
  credits: number
  academicYear: string
  semester: string
  publicationStatus: ResultPublicationStatus
  publishedAt: string | null
  totalWeightedScore: number | null
  grade: Grade | null
  gradePoint: number | null
  assessmentScores: StudentAssessmentScoreItem[]
  totalSessions: number
  attendedSessions: number
  absentSessions: number
  excusedSessions: number
  eligibleSessions: number
  attendancePercentage: number
  belowAttendanceThreshold: boolean
  activeAlerts: StudentRiskAlertItem[]
}

export interface StudentReportResponse {
  studentId: number
  studentIdentifier: string
  studentName: string
  studentEmail: string
  gpa: number | null
  totalCreditsEarned: number
  totalEnrolledCredits: number
  totalEnrolledSubjects: number
  publishedSubjectsCount: number
  passedSubjectsCount: number
  failedSubjectsCount: number
  overallAttendancePercentage: number | null
  provisionalNotice: string
  subjects: StudentSubjectReportItem[]
}

// ==========================================
// LECTURER REPORT TYPES
// ==========================================

export interface AssessmentSummaryItem {
  id: number
  title: string
  type: AssessmentType
  maxMarks: number
  weightPercentage: number
}

export interface LecturerStudentReportRow {
  enrollmentId: number
  studentId: number
  studentIdentifier: string
  studentName: string
  studentEmail: string
  enrollmentStatus: string
  assessmentScores: Record<number, number>
  totalWeightedScore: number | null
  grade: Grade | null
  gradePoint: number | null
  attendedSessions: number
  absentSessions: number
  excusedSessions: number
  eligibleSessions: number
  attendancePercentage: number
  belowAttendanceThreshold: boolean
  activeAlertCount: number
  highestAlertSeverity: AlertSeverity | null
}

export interface LecturerAtRiskStudentItem {
  alertId: number
  studentId: number
  studentIdentifier: string
  studentName: string
  riskType: AlertRiskType
  severity: AlertSeverity
  status: AlertStatus
  triggerReason: string
  detectedAt: string
  actionTaken: string | null
  reviewedByName: string | null
}

export interface LecturerSubjectReportResponse {
  subjectId: number
  subjectCode: string
  subjectName: string
  credits: number
  academicYear: string
  semester: string
  publicationStatus: ResultPublicationStatus
  publishedAt: string | null
  enrolledStudentsCount: number
  assessedStudentsCount: number
  averageScore: number | null
  highestScore: number | null
  lowestScore: number | null
  passCount: number
  failCount: number
  passRate: number | null
  gradeDistribution: Record<string, number>
  totalSessionsConducted: number
  averageAttendancePercentage: number | null
  studentsBelowThresholdCount: number
  activeAlertsCount: number
  assessments: AssessmentSummaryItem[]
  students: LecturerStudentReportRow[]
  atRiskStudents: LecturerAtRiskStudentItem[]
  provisionalNotice: string
}

// ==========================================
// ADMIN REPORT TYPES
// ==========================================

export interface AttendanceBracketCounts {
  excellentCount: number // >= 90%
  goodCount: number      // 80 - 89.9%
  warningCount: number   // 70 - 79.9%
  criticalCount: number  // < 70%
}

export interface RiskAlertsSummaryStats {
  byRiskType: Record<string, number>
  bySeverity: Record<string, number>
  byStatus: Record<string, number>
}

export interface DepartmentComparisonItem {
  departmentId: number
  code: string
  name: string
  courseCount: number
  subjectCount: number
  enrollmentCount: number
  averageScore: number | null
  passRate: number | null
  averageAttendanceRate: number | null
  activeAlertsCount: number
}

export interface CourseComparisonItem {
  courseId: number
  code: string
  name: string
  departmentName: string
  subjectCount: number
  enrollmentCount: number
  averageScore: number | null
  passRate: number | null
  averageAttendanceRate: number | null
  activeAlertsCount: number
}

export interface SubjectComparisonItem {
  subjectId: number
  code: string
  name: string
  courseCode: string
  departmentName: string
  credits: number
  enrollmentCount: number
  publicationStatus: ResultPublicationStatus
  averageScore: number | null
  passRate: number | null
  averageAttendanceRate: number | null
  atRiskCount: number
}

export interface AdminInstitutionalReportResponse {
  totalStudents: number
  totalLecturers: number
  totalDepartments: number
  totalCourses: number
  totalSubjects: number
  totalEnrollments: number
  publishedSubjectsCount: number
  averageScore: number | null
  overallPassRate: number | null
  overallAttendanceAverage: number | null
  studentsBelowThresholdCount: number
  totalActiveAlerts: number
  gradeDistribution: Record<string, number>
  attendanceBrackets: AttendanceBracketCounts
  riskAlertsSummary: RiskAlertsSummaryStats
  departmentComparisons: DepartmentComparisonItem[]
  courseComparisons: CourseComparisonItem[]
  subjectComparisons: SubjectComparisonItem[]
  provisionalNotice: string
}

export interface AdminReportFilters {
  departmentId?: number
  courseId?: number
  subjectId?: number
  academicYear?: string
  semester?: string
}

// ==========================================
// API CLIENT & FILE DOWNLOAD HELPERS
// ==========================================

async function handleResponse<T>(res: Response, fallbackError: string): Promise<T> {
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    const error = new Error(data?.message || fallbackError) as Error & ApiErrorData
    error.status = res.status
    error.errors = data?.errors
    error.errorCode = data?.errorCode
    throw error
  }
  return data as T
}

function triggerDownload(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  window.URL.revokeObjectURL(url)
  a.remove()
}

async function handleCsvDownload(res: Response, defaultFilename: string) {
  if (!res.ok) {
    const text = await res.text().catch(() => null)
    throw new Error(text || 'Failed to download CSV export.')
  }
  let filename = defaultFilename
  const disposition = res.headers.get('Content-Disposition')
  if (disposition && disposition.includes('filename=')) {
    const match = disposition.match(/filename="?([^";]+)"?/)
    if (match && match[1]) {
      filename = match[1]
    }
  }
  const blob = await res.blob()
  triggerDownload(blob, filename)
}

// ==========================================
// STUDENT APIS
// ==========================================

export async function fetchStudentReport(): Promise<StudentReportResponse> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/reports/my-report`)
  return handleResponse<StudentReportResponse>(res, 'Failed to fetch personal academic report.')
}

export async function downloadStudentReportCsv(): Promise<void> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/reports/export/my-report`)
  await handleCsvDownload(res, 'my_academic_transcript_report.csv')
}

// ==========================================
// LECTURER APIS
// ==========================================

export async function fetchLecturerSubjectReport(
  subjectId: number,
  academicYear?: string,
  semester?: string
): Promise<LecturerSubjectReportResponse> {
  const query = new URLSearchParams()
  if (academicYear) query.set('academicYear', academicYear)
  if (semester) query.set('semester', semester)

  const url = `${API_BASE_URL}/academic/lecturer/reports/subject/${subjectId}${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  return handleResponse<LecturerSubjectReportResponse>(res, 'Failed to fetch class report.')
}

export async function downloadLecturerPerformanceCsv(
  subjectId: number,
  academicYear?: string,
  semester?: string
): Promise<void> {
  const query = new URLSearchParams()
  if (academicYear) query.set('academicYear', academicYear)
  if (semester) query.set('semester', semester)

  const url = `${API_BASE_URL}/academic/lecturer/reports/export/subject/${subjectId}${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, `subject_${subjectId}_performance_report.csv`)
}

export async function downloadLecturerAttendanceCsv(
  subjectId: number,
  academicYear?: string,
  semester?: string
): Promise<void> {
  const query = new URLSearchParams()
  if (academicYear) query.set('academicYear', academicYear)
  if (semester) query.set('semester', semester)

  const url = `${API_BASE_URL}/academic/lecturer/reports/export/subject/${subjectId}/attendance${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, `subject_${subjectId}_attendance_report.csv`)
}

export async function downloadLecturerAtRiskCsv(
  subjectId: number,
  academicYear?: string,
  semester?: string
): Promise<void> {
  const query = new URLSearchParams()
  if (academicYear) query.set('academicYear', academicYear)
  if (semester) query.set('semester', semester)

  const url = `${API_BASE_URL}/academic/lecturer/reports/export/subject/${subjectId}/at-risk${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, `subject_${subjectId}_at_risk_students.csv`)
}

// ==========================================
// ADMIN APIS
// ==========================================

export async function fetchAdminInstitutionalReport(
  filters?: AdminReportFilters
): Promise<AdminInstitutionalReportResponse> {
  const query = new URLSearchParams()
  if (filters?.departmentId) query.set('departmentId', String(filters.departmentId))
  if (filters?.courseId) query.set('courseId', String(filters.courseId))
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.academicYear) query.set('academicYear', filters.academicYear)
  if (filters?.semester) query.set('semester', filters.semester)

  const url = `${API_BASE_URL}/admin/reports/institutional${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  return handleResponse<AdminInstitutionalReportResponse>(res, 'Failed to fetch institutional report.')
}

export async function downloadAdminSummaryCsv(filters?: AdminReportFilters): Promise<void> {
  const query = new URLSearchParams()
  if (filters?.departmentId) query.set('departmentId', String(filters.departmentId))
  if (filters?.courseId) query.set('courseId', String(filters.courseId))
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.academicYear) query.set('academicYear', filters.academicYear)
  if (filters?.semester) query.set('semester', filters.semester)

  const url = `${API_BASE_URL}/admin/reports/export/summary${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, 'institutional_summary_report.csv')
}

export async function downloadAdminDepartmentComparisonCsv(filters?: AdminReportFilters): Promise<void> {
  const query = new URLSearchParams()
  if (filters?.departmentId) query.set('departmentId', String(filters.departmentId))
  if (filters?.courseId) query.set('courseId', String(filters.courseId))
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.academicYear) query.set('academicYear', filters.academicYear)
  if (filters?.semester) query.set('semester', filters.semester)

  const url = `${API_BASE_URL}/admin/reports/export/departments${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, 'departments_performance_comparison.csv')
}

export async function downloadAdminSubjectComparisonCsv(filters?: AdminReportFilters): Promise<void> {
  const query = new URLSearchParams()
  if (filters?.departmentId) query.set('departmentId', String(filters.departmentId))
  if (filters?.courseId) query.set('courseId', String(filters.courseId))
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.academicYear) query.set('academicYear', filters.academicYear)
  if (filters?.semester) query.set('semester', filters.semester)

  const url = `${API_BASE_URL}/admin/reports/export/subjects${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, 'subjects_performance_comparison.csv')
}

export async function downloadAdminRiskCsv(filters?: AdminReportFilters): Promise<void> {
  const query = new URLSearchParams()
  if (filters?.departmentId) query.set('departmentId', String(filters.departmentId))
  if (filters?.courseId) query.set('courseId', String(filters.courseId))
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.academicYear) query.set('academicYear', filters.academicYear)
  if (filters?.semester) query.set('semester', filters.semester)

  const url = `${API_BASE_URL}/admin/reports/export/risk${query.toString() ? `?${query.toString()}` : ''}`
  const res = await authenticatedFetch(url)
  await handleCsvDownload(res, 'institutional_risk_alerts_report.csv')
}
