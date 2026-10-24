import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService'

// ==========================================
// RISK ALERTS ENUMS & TYPES
// ==========================================

export type AlertRiskType =
  | 'ATTENDANCE_WARNING'
  | 'LOW_ACADEMIC_MARKS'
  | 'PROGRESS_DECLINE'
  | 'MISSING_ASSESSMENT'

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type AlertStatus = 'DETECTED' | 'REVIEWED' | 'ACTION_RECORDED' | 'RESOLVED'

export interface RiskAlert {
  id: number
  studentId: number
  studentIdentifier: string
  studentName: string
  studentEmail: string

  subjectId: number
  subjectCode: string
  subjectName: string

  academicYear: string
  semester: string
  riskType: AlertRiskType
  severity: AlertSeverity
  reason: string
  metricValue?: number | null
  metricDetails?: string | null
  status: AlertStatus
  detectedAt: string

  reviewedById?: number | null
  reviewedByName?: string | null
  reviewedAt?: string | null

  actionNotes?: string | null
  actionRecordedById?: number | null
  actionRecordedByName?: string | null
  actionRecordedAt?: string | null

  resolvedById?: number | null
  resolvedByName?: string | null
  resolvedAt?: string | null
  resolutionNotes?: string | null

  createdAt: string
}

export interface StudentRiskAlert {
  id: number
  subjectId: number
  subjectCode: string
  subjectName: string
  academicYear: string
  semester: string
  riskType: AlertRiskType
  severity: AlertSeverity
  reason: string
  metricValue?: number | null
  metricDetails?: string | null
  status: AlertStatus
  detectedAt: string
  resolvedAt?: string | null
  advisoryGuidance: string
}

export interface AlertSummaryStats {
  totalAlerts: number
  activeAlerts: number
  criticalAlerts: number
  highAlerts: number
  mediumAlerts: number
  lowAlerts: number

  attendanceWarnings: number
  lowMarksAlerts: number
  progressDeclineAlerts: number
  missingAssessmentAlerts: number

  detectedCount: number
  reviewedCount: number
  actionRecordedCount: number
  resolvedCount: number
}

export interface EvaluateRiskPayload {
  subjectId?: number | null
  academicYear: string
  semester: string
}

export interface AlertActionPayload {
  actionNotes: string
}

export interface AlertResolutionPayload {
  resolutionNotes: string
}

export interface AlertReviewPayload {
  comment?: string
}

// ==========================================
// RESPONSE HELPER
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

// ==========================================
// ADMIN RISK APIS
// ==========================================

export async function fetchAdminAlerts(filters?: {
  riskType?: AlertRiskType
  severity?: AlertSeverity
  status?: AlertStatus
  subjectId?: number
  studentId?: number
}): Promise<RiskAlert[]> {
  const query = new URLSearchParams()
  if (filters?.riskType) query.set('riskType', filters.riskType)
  if (filters?.severity) query.set('severity', filters.severity)
  if (filters?.status) query.set('status', filters.status)
  if (filters?.subjectId) query.set('subjectId', String(filters.subjectId))
  if (filters?.studentId) query.set('studentId', String(filters.studentId))

  const queryString = query.toString() ? `?${query.toString()}` : ''
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts${queryString}`)
  return handleResponse<RiskAlert[]>(res, 'Failed to fetch institutional risk alerts.')
}

export async function fetchAdminSummaryStats(): Promise<AlertSummaryStats> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts/summary`)
  return handleResponse<AlertSummaryStats>(res, 'Failed to fetch risk alert statistics.')
}

export async function triggerAdminRiskEvaluation(payload: EvaluateRiskPayload): Promise<RiskAlert[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts/evaluate`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
  return handleResponse<RiskAlert[]>(res, 'Failed to trigger risk evaluation.')
}

export async function reviewAlertAdmin(alertId: number, comment?: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts/${alertId}/review`, {
    method: 'PATCH',
    body: JSON.stringify({ comment }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to review alert.')
}

export async function recordActionAdmin(alertId: number, actionNotes: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts/${alertId}/action`, {
    method: 'PATCH',
    body: JSON.stringify({ actionNotes }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to record intervention action.')
}

export async function resolveAlertAdmin(alertId: number, resolutionNotes: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/admin/risk-alerts/${alertId}/resolve`, {
    method: 'PATCH',
    body: JSON.stringify({ resolutionNotes }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to resolve risk alert.')
}

// ==========================================
// LECTURER RISK APIS
// ==========================================

export async function fetchLecturerAlertsForSubject(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<RiskAlert[]> {
  const params = new URLSearchParams({ academicYear, semester })
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/risk-alerts/subjects/${subjectId}?${params}`
  )
  return handleResponse<RiskAlert[]>(res, 'Failed to fetch subject risk alerts.')
}

export async function fetchLecturerAllAlerts(): Promise<RiskAlert[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/risk-alerts/my-subjects`)
  return handleResponse<RiskAlert[]>(res, 'Failed to fetch lecturer risk alerts.')
}

export async function fetchLecturerAlertSummary(): Promise<AlertSummaryStats> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/risk-alerts/summary`)
  return handleResponse<AlertSummaryStats>(res, 'Failed to fetch lecturer risk statistics.')
}

export async function triggerLecturerRiskEvaluation(
  subjectId: number,
  academicYear: string,
  semester: string
): Promise<RiskAlert[]> {
  const res = await authenticatedFetch(
    `${API_BASE_URL}/academic/lecturer/risk-alerts/subjects/${subjectId}/evaluate`,
    {
      method: 'POST',
      body: JSON.stringify({ academicYear, semester }),
    }
  )
  return handleResponse<RiskAlert[]>(res, 'Failed to evaluate subject risk.')
}

export async function reviewAlertLecturer(alertId: number, comment?: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/risk-alerts/${alertId}/review`, {
    method: 'PATCH',
    body: JSON.stringify({ comment }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to review alert.')
}

export async function recordActionLecturer(alertId: number, actionNotes: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/risk-alerts/${alertId}/action`, {
    method: 'PATCH',
    body: JSON.stringify({ actionNotes }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to record intervention action.')
}

export async function resolveAlertLecturer(alertId: number, resolutionNotes: string): Promise<RiskAlert> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/risk-alerts/${alertId}/resolve`, {
    method: 'PATCH',
    body: JSON.stringify({ resolutionNotes }),
  })
  return handleResponse<RiskAlert>(res, 'Failed to resolve risk alert.')
}

// ==========================================
// STUDENT RISK APIS
// ==========================================

export async function fetchStudentMyAlerts(): Promise<StudentRiskAlert[]> {
  const res = await authenticatedFetch(`${API_BASE_URL}/academic/student/risk-alerts/my-alerts`)
  return handleResponse<StudentRiskAlert[]>(res, 'Failed to fetch personal alerts.')
}
