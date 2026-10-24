import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService'

// ==========================================
// ASSESSMENT & MARKS TYPES
// ==========================================

export type AssessmentType = 'QUIZ' | 'ASSIGNMENT' | 'MIDTERM' | 'FINAL_EXAM' | 'PROJECT' | 'LAB'
export type ResultPublicationStatus = 'DRAFT' | 'PUBLISHED'
export type Grade = 'A' | 'B' | 'C' | 'S' | 'W' | 'F'
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED'

export interface Assessment {
  id: number
  title: string
  type: AssessmentType
  maxMarks: number
  weightPercentage: number
  subjectId: number
  subjectCode: string
  subjectName: string
  academicYear: string
  semester: string
  creatorName: string
  createdAt: string
  updatedAt?: string
}

export interface AssessmentPayload {
  title: string
  type: AssessmentType
  maxMarks: number
  weightPercentage: number
  subjectId: number
  academicYear: string
  semester: string
}

export interface StudentMark {
  id: number
  assessmentId: number
  assessmentTitle: string
  assessmentType: AssessmentType
  maxMarks: number
  weightPercentage: number
  enrollmentId: number
  studentId: number
  studentIdentifier: string
  studentName: string
  studentEmail: string
  score: number
  comments?: string | null
  weightedContribution: number
  markedByName: string
  updatedAt: string
}

export interface MarkEntryPayload {
  assessmentId: number
  enrollmentId: number
  score: number
  comments?: string
}

export interface BulkMarkEntryPayload {
  assessmentId: number
  marks: Array<{
    enrollmentId: number
    score: number
    comments?: string
  }>
}

export interface SubjectRosterMarks {
  subjectId: number
  subjectCode: string
  subjectName: string
  academicYear: string
  semester: string
  publicationStatus: ResultPublicationStatus
  canPublish: boolean
  totalWeightConfigured: number
  assessments: Assessment[]
  students: Array<{
    enrollmentId: number
    studentId: number
    studentIdentifier: string
    studentName: string
    studentEmail: string
    enrollmentStatus: string
    marks: Record<number, StudentMark>
    totalWeightedScore: number
    projectedGrade: Grade
  }>
}

export interface SubjectResultPublication {
  id: number
  subjectId: number
  subjectCode: string
  subjectName: string
  academicYear: string
  semester: string
  status: ResultPublicationStatus
  publishedByName?: string | null
  publishedAt?: string | null
  unlockedByName?: string | null
  unlockedAt?: string | null
  unlockReason?: string | null
}

export interface PublishResultsPayload {
  subjectId: number
  academicYear: string
  semester: string
}

export interface UnlockPayload {
  reason: string
}

export interface AdminMarkOverridePayload {
  score: number
  reason: string
}

export interface MarkAuditLog {
  id: number
  studentMarkId: number
  assessmentId: number
  enrollmentId: number
  actionType: string
  oldScore?: number | null
  newScore: number
  reason: string
  performedByName: string
  timestamp: string
}

export interface StudentAssessmentResultItem {
  assessmentId: number
  title: string
  type: AssessmentType
  maxMarks: number
  studentScore?: number | null
  weightPercentage: number
  weightedContribution?: number | null
  comments?: string | null
}

export interface StudentPublishedResults {
  enrollmentId: number
  subjectId: number
  subjectCode: string
  subjectName: string
  credits: number
  academicYear: string
  semester: string
  publicationStatus: ResultPublicationStatus
  publishedAt?: string | null
  statusMessage?: string | null
  totalWeightedScore?: number | null
  grade?: Grade | null
  gradePoint?: number | null
  gradingNotice?: string | null
  assessmentResults: StudentAssessmentResultItem[]
}

export interface StudentOverallResult {
  enrollmentId: number
  subjectId: number
  subjectCode: string
  subjectName: string
  credits: number
  academicYear: string
  semester: string
  enrollmentStatus: string
  totalWeightedScore?: number | null
  grade?: Grade | null
  gradePoint?: number | null
  publicationStatus: ResultPublicationStatus
  publishedAt?: string | null
}

// ==========================================
// ATTENDANCE TYPES
// ==========================================

export interface AttendanceSession {
  id: number
  subjectId: number
  subjectCode: string
  subjectName: string
  sessionDate: string
  sessionTopic: string
  academicYear: string
  semester: string
  recordedByName: string
  createdAt: string
  updatedAt?: string
}

export interface AttendanceSessionPayload {
  subjectId: number
  sessionDate: string
  sessionTopic: string
  academicYear: string
  semester: string
}

export interface AttendanceRecord {
  id: number
  sessionId: number
  sessionDate: string
  sessionTopic: string
  enrollmentId: number
  studentId: number
  studentIdentifier: string
  studentName: string
  studentEmail: string
  status: AttendanceStatus
  remarks?: string | null
  recordedAt: string
}

export interface SessionRosterAttendance {
  session: AttendanceSession
  roster: Array<{
    enrollmentId: number
    studentId: number
    studentIdentifier: string
    studentName: string
    studentEmail: string
    status: AttendanceStatus
    remarks?: string | null
  }>
}

export interface RecordAttendancePayload {
  sessionId: number
  records: Array<{
    enrollmentId: number
    status: AttendanceStatus
    remarks?: string
  }>
}

export interface StudentAttendanceSummary {
  enrollmentId: number
  subjectId: number
  subjectCode: string
  subjectName: string
  credits: number
  academicYear: string
  semester: string
  totalSessions: number
  excusedSessions: number
  eligibleSessions: number
  attendedSessions: number
  absentSessions: number
  attendancePercentage: number
  belowWarningThreshold: boolean
  warningThreshold: number
  warningMessage?: string | null
  records: AttendanceRecord[]
}

export interface SubjectAttendanceSummary {
  subjectId: number
  subjectCode: string
  subjectName: string
  academicYear: string
  semester: string
  totalSessionsConducted: number
  averageAttendancePercentage: number
  studentsCount: number
  studentsBelowThresholdCount: number
  studentSummaries: StudentAttendanceSummary[]
}

// ==========================================
// API HELPER
// ==========================================

async function handleResponse<T>(res: Response, defaultError: string): Promise<T> {
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    const errorMsg = data?.message || defaultError
    const error = new Error(errorMsg) as Error & ApiErrorData
    error.status = res.status
    error.errors = data?.errors
    error.errorCode = data?.errorCode
    throw error
  }
  return data as T
}

// ==========================================
// LECTURER PERFORMANCE APIs
// ==========================================

export async function createAssessment(payload: AssessmentPayload): Promise<Assessment> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/assessments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<Assessment>(res, 'Failed to create assessment.')
}

export async function updateAssessment(id: number, payload: AssessmentPayload): Promise<Assessment> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/assessments/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  return handleResponse<Assessment>(res, 'Failed to update assessment.')
}

export async function deleteAssessment(id: number): Promise<void> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/assessments/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.message || 'Failed to delete assessment.')
  }
}

export async function fetchSubjectAssessments(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<Assessment[]> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/performance/subjects/${subjectId}/assessments?${params}`
  )
  return handleResponse<Assessment[]>(res, 'Failed to fetch assessments.')
}

export async function fetchSubjectMarksRoster(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<SubjectRosterMarks> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/performance/subjects/${subjectId}/marks-roster?${params}`
  )
  return handleResponse<SubjectRosterMarks>(res, 'Failed to fetch marks roster.')
}

export async function enterOrUpdateMark(payload: MarkEntryPayload): Promise<StudentMark> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/marks`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<StudentMark>(res, 'Failed to save student mark.')
}

export async function bulkEnterMarks(payload: BulkMarkEntryPayload): Promise<StudentMark[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/marks/bulk`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<StudentMark[]>(res, 'Failed to save bulk marks.')
}

export async function publishSubjectResults(payload: PublishResultsPayload): Promise<SubjectResultPublication> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/performance/publish`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<SubjectResultPublication>(res, 'Failed to publish subject results.')
}

export async function fetchPublicationStatus(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<SubjectResultPublication> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/performance/subjects/${subjectId}/publication-status?${params}`
  )
  return handleResponse<SubjectResultPublication>(res, 'Failed to fetch publication status.')
}

// ==========================================
// ADMIN PERFORMANCE APIs
// ==========================================

export async function adminUnlockResults(
  subjectId: number,
  academicYear: string,
  semester: string,
  payload: UnlockPayload
): Promise<SubjectResultPublication> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/admin/performance/subjects/${subjectId}/unlock?${params}`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    }
  )
  return handleResponse<SubjectResultPublication>(res, 'Failed to unlock subject results.')
}

export async function adminOverrideMark(markId: number, payload: AdminMarkOverridePayload): Promise<StudentMark> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/performance/marks/${markId}/override`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
  return handleResponse<StudentMark>(res, 'Failed to override mark.')
}

export async function fetchAuditLogsForMark(markId: number): Promise<MarkAuditLog[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/performance/marks/${markId}/audit-logs`)
  return handleResponse<MarkAuditLog[]>(res, 'Failed to fetch audit logs.')
}

// ==========================================
// STUDENT PERFORMANCE APIs
// ==========================================

export async function fetchMySubjectResults(enrollmentId: number): Promise<StudentPublishedResults> {
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/student/performance/enrolments/${enrollmentId}/results`
  )
  return handleResponse<StudentPublishedResults>(res, 'Failed to fetch student results.')
}

export async function fetchMyAllResults(): Promise<StudentOverallResult[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/performance/my-results`)
  return handleResponse<StudentOverallResult[]>(res, 'Failed to fetch all student results.')
}

// ==========================================
// LECTURER ATTENDANCE APIs
// ==========================================

export async function createAttendanceSession(payload: AttendanceSessionPayload): Promise<AttendanceSession> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/attendance/sessions`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<AttendanceSession>(res, 'Failed to create attendance session.')
}

export async function updateAttendanceSession(
  id: number,
  payload: AttendanceSessionPayload
): Promise<AttendanceSession> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/attendance/sessions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
  return handleResponse<AttendanceSession>(res, 'Failed to update attendance session.')
}

export async function deleteAttendanceSession(id: number): Promise<void> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/attendance/sessions/${id}`, {
    method: 'DELETE',
  })
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new Error(data?.message || 'Failed to delete attendance session.')
  }
}

export async function fetchSubjectAttendanceSessions(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<AttendanceSession[]> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/attendance/subjects/${subjectId}/sessions?${params}`
  )
  return handleResponse<AttendanceSession[]>(res, 'Failed to fetch attendance sessions.')
}

export async function fetchSessionRoster(sessionId: number): Promise<SessionRosterAttendance> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/attendance/sessions/${sessionId}/roster`)
  return handleResponse<SessionRosterAttendance>(res, 'Failed to fetch session roster.')
}

export async function recordSessionAttendance(payload: RecordAttendancePayload): Promise<AttendanceRecord[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/attendance/records`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<AttendanceRecord[]>(res, 'Failed to record attendance.')
}

export async function fetchSubjectAttendanceSummary(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<SubjectAttendanceSummary> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/attendance/subjects/${subjectId}/summary?${params}`
  )
  return handleResponse<SubjectAttendanceSummary>(res, 'Failed to fetch subject attendance summary.')
}

// ==========================================
// STUDENT ATTENDANCE APIs
// ==========================================

export async function fetchMySubjectAttendance(enrollmentId: number): Promise<StudentAttendanceSummary> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/attendance/enrolments/${enrollmentId}`)
  return handleResponse<StudentAttendanceSummary>(res, 'Failed to fetch subject attendance summary.')
}

export async function fetchMyAllAttendance(): Promise<StudentAttendanceSummary[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/attendance/my-attendance`)
  return handleResponse<StudentAttendanceSummary[]>(res, 'Failed to fetch student attendance records.')
}
