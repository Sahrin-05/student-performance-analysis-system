import React, { useState, useEffect, useCallback, useMemo } from 'react'
import {
  fetchLecturerMySubjects,
  type Subject,
} from './services/academicService'
import {
  type RiskAlert,
  type AlertSeverity,
  type AlertStatus,
  type AlertRiskType,
  type AlertSummaryStats,
  fetchLecturerAlertsForSubject,
  fetchLecturerAllAlerts,
  fetchLecturerAlertSummary,
  triggerLecturerRiskEvaluation,
  reviewAlertLecturer,
  recordActionLecturer,
  resolveAlertLecturer,
} from './services/riskAlertService'
import type { ApiErrorData } from './services/authService'

export const LecturerRiskAlertsView: React.FC = () => {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [academicYear, setAcademicYear] = useState<string>('2025/2026')
  const [semester, setSemester] = useState<string>('SEMESTER_1')
  const [viewScope, setViewScope] = useState<'SELECTED_SUBJECT' | 'ALL_MY_SUBJECTS'>('SELECTED_SUBJECT')

  const [alerts, setAlerts] = useState<RiskAlert[]>([])
  const [summaryStats, setSummaryStats] = useState<AlertSummaryStats | null>(null)
  const [isLoadingSubjects, setIsLoadingSubjects] = useState<boolean>(true)
  const [isLoadingAlerts, setIsLoadingAlerts] = useState<boolean>(false)
  const [isEvaluating, setIsEvaluating] = useState<boolean>(false)

  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  // Filters
  const [statusFilter, setStatusFilter] = useState<string>('ACTIVE_ONLY')
  const [typeFilter, setTypeFilter] = useState<string>('ALL')
  const [searchQuery, setSearchQuery] = useState<string>('')

  // Modal State
  const [selectedAlert, setSelectedAlert] = useState<RiskAlert | null>(null)
  const [modalMode, setModalMode] = useState<'VIEW' | 'REVIEW' | 'ACTION' | 'RESOLVE' | null>(null)
  const [actionNotes, setActionNotes] = useState<string>('')
  const [reviewComment, setReviewComment] = useState<string>('')
  const [resolutionNotes, setResolutionNotes] = useState<string>('')
  const [isSubmittingModal, setIsSubmittingModal] = useState<boolean>(false)
  const [modalError, setModalError] = useState<string | null>(null)

  // Load lecturer's assigned subjects on mount
  useEffect(() => {
    async function loadSubjects() {
      setIsLoadingSubjects(true)
      try {
        const data = await fetchLecturerMySubjects()
        setSubjects(data)
        if (data.length > 0) {
          setSelectedSubject(data[0])
        }
      } catch (err: unknown) {
        const apiErr = err as Error & ApiErrorData
        setError(apiErr.message || 'Failed to load assigned subjects.')
      } finally {
        setIsLoadingSubjects(false)
      }
    }
    loadSubjects()
  }, [])

  // Load alerts when subject or filters change
  const loadAlerts = useCallback(async () => {
    if (viewScope === 'SELECTED_SUBJECT' && !selectedSubject) {
      setAlerts([])
      return
    }

    setIsLoadingAlerts(true)
    setError(null)
    try {
      const [alertsData, statsData] = await Promise.all([
        viewScope === 'SELECTED_SUBJECT' && selectedSubject
          ? fetchLecturerAlertsForSubject(selectedSubject.id, academicYear, semester)
          : fetchLecturerAllAlerts(),
        fetchLecturerAlertSummary(),
      ])
      setAlerts(alertsData)
      setSummaryStats(statsData)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load risk alerts.')
    } finally {
      setIsLoadingAlerts(false)
    }
  }, [viewScope, selectedSubject, academicYear, semester])

  useEffect(() => {
    if (selectedSubject || viewScope === 'ALL_MY_SUBJECTS') {
      loadAlerts()
    }
  }, [loadAlerts, selectedSubject, viewScope])

  const handleEvaluateSubject = async () => {
    if (!selectedSubject) return
    setIsEvaluating(true)
    setError(null)
    setSuccessMessage(null)
    try {
      const results = await triggerLecturerRiskEvaluation(selectedSubject.id, academicYear, semester)
      setSuccessMessage(
        `Class risk evaluation completed for ${selectedSubject.code}. Checked ${results.length} active alert records.`
      )
      await loadAlerts()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to evaluate class risks.')
    } finally {
      setIsEvaluating(false)
    }
  }

  const handleOpenModal = (alert: RiskAlert, mode: 'VIEW' | 'REVIEW' | 'ACTION' | 'RESOLVE') => {
    setSelectedAlert(alert)
    setModalMode(mode)
    setActionNotes(alert.actionNotes || '')
    setReviewComment('')
    setResolutionNotes(alert.resolutionNotes || '')
    setModalError(null)
  }

  const handleCloseModal = () => {
    setSelectedAlert(null)
    setModalMode(null)
    setActionNotes('')
    setReviewComment('')
    setResolutionNotes('')
    setModalError(null)
  }

  const handleModalSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedAlert || !modalMode) return

    setIsSubmittingModal(true)
    setModalError(null)
    try {
      if (modalMode === 'REVIEW') {
        const updated = await reviewAlertLecturer(selectedAlert.id, reviewComment)
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Alert #${updated.id} marked as REVIEWED.`)
      } else if (modalMode === 'ACTION') {
        if (!actionNotes.trim()) {
          setModalError('Action notes are required.')
          setIsSubmittingModal(false)
          return
        }
        const updated = await recordActionLecturer(selectedAlert.id, actionNotes.trim())
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Intervention action documented for ${selectedAlert.studentName}.`)
      } else if (modalMode === 'RESOLVE') {
        if (!resolutionNotes.trim()) {
          setModalError('Resolution notes are required.')
          setIsSubmittingModal(false)
          return
        }
        const updated = await resolveAlertLecturer(selectedAlert.id, resolutionNotes.trim())
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Risk alert #${updated.id} successfully marked as RESOLVED.`)
      }
      handleCloseModal()
      const updatedStats = await fetchLecturerAlertSummary()
      setSummaryStats(updatedStats)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setModalError(apiErr.message || 'Failed to update alert.')
    } finally {
      setIsSubmittingModal(false)
    }
  }

  const filteredAlerts = useMemo(() => {
    return alerts.filter((a) => {
      if (typeFilter !== 'ALL' && a.riskType !== typeFilter) return false
      if (statusFilter === 'ACTIVE_ONLY') {
        if (a.status === 'RESOLVED') return false
      } else if (statusFilter !== 'ALL') {
        if (a.status !== statusFilter) return false
      }
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase()
        const matchesStudent =
          a.studentName.toLowerCase().includes(q) ||
          a.studentIdentifier.toLowerCase().includes(q)
        const matchesSubject = a.subjectCode.toLowerCase().includes(q)
        const matchesReason = a.reason.toLowerCase().includes(q)
        if (!matchesStudent && !matchesSubject && !matchesReason) return false
      }
      return true
    })
  }, [alerts, typeFilter, statusFilter, searchQuery])

  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2 py-0.5 rounded text-xs font-bold bg-red-100 text-red-800 border border-red-200">CRITICAL</span>
      case 'HIGH':
        return <span className="px-2 py-0.5 rounded text-xs font-bold bg-rose-100 text-rose-800 border border-rose-200">HIGH</span>
      case 'MEDIUM':
        return <span className="px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">MEDIUM</span>
      case 'LOW':
        return <span className="px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">LOW</span>
    }
  }

  const getStatusBadge = (status: AlertStatus) => {
    switch (status) {
      case 'DETECTED':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-red-50 text-red-700 border border-red-200">DETECTED</span>
      case 'REVIEWED':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">REVIEWED</span>
      case 'ACTION_RECORDED':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">ACTION LOGGED</span>
      case 'RESOLVED':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">RESOLVED</span>
    }
  }

  const getRiskTypeLabel = (riskType: AlertRiskType) => {
    switch (riskType) {
      case 'ATTENDANCE_WARNING':
        return { label: 'Attendance Warning (< 80%)', color: 'text-amber-700 bg-amber-50 border-amber-200' }
      case 'LOW_ACADEMIC_MARKS':
        return { label: 'Low Marks (< 40%)', color: 'text-rose-700 bg-rose-50 border-rose-200' }
      case 'PROGRESS_DECLINE':
        return { label: 'Progress Decline (≥ 15%)', color: 'text-purple-700 bg-purple-50 border-purple-200' }
      case 'MISSING_ASSESSMENT':
        return { label: 'Missing Assessment', color: 'text-orange-700 bg-orange-50 border-orange-200' }
    }
  }

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-xs border border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Student Academic Risk Alerts & Advising</h1>
          <p className="text-sm text-slate-500 mt-1">
            Detect early warning indicators in your classes, document pastoral support, and log remediation actions.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {selectedSubject && viewScope === 'SELECTED_SUBJECT' && (
            <button
              onClick={handleEvaluateSubject}
              disabled={isEvaluating}
              className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-sm font-semibold shadow-xs disabled:opacity-50 transition-colors"
            >
              {isEvaluating ? 'Evaluating...' : 'Evaluate Class Risk'}
            </button>
          )}
          <button
            onClick={loadAlerts}
            disabled={isLoadingAlerts}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
          >
            {isLoadingAlerts ? 'Loading...' : 'Refresh'}
          </button>
        </div>
      </div>

      {/* Provisional Notice */}
      <div className="bg-amber-50 border border-amber-200 text-amber-900 rounded-xl p-4 text-xs sm:text-sm flex items-start gap-3">
        <span className="text-amber-600 font-bold text-base">ℹ</span>
        <div>
          <span className="font-semibold">Provisional System Heuristics Notice:</span> Early alert thresholds (Attendance &lt; 80.0%, Low Marks &lt; 40.0%, Progress Decline &ge; 15.0%) are provisional system defaults designed to facilitate proactive student mentoring and advising. They do not constitute official university policy.
        </div>
      </div>

      {/* Notifications */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-red-500 font-bold ml-4">✕</button>
        </div>
      )}
      {successMessage && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl p-4 text-sm flex items-center justify-between">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 font-bold ml-4">✕</button>
        </div>
      )}

      {/* Subject and Scope Selector */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <label className="text-xs font-semibold text-slate-700">Scope:</label>
            <select
              value={viewScope}
              onChange={(e) => setViewScope(e.target.value as 'SELECTED_SUBJECT' | 'ALL_MY_SUBJECTS')}
              className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg bg-white focus:ring-1 focus:ring-indigo-500 font-medium"
            >
              <option value="SELECTED_SUBJECT">By Assigned Subject</option>
              <option value="ALL_MY_SUBJECTS">All My Assigned Subjects</option>
            </select>
          </div>

          {viewScope === 'SELECTED_SUBJECT' && (
            <div className="flex items-center gap-2">
              <label className="text-xs font-semibold text-slate-700">Subject:</label>
              <select
                value={selectedSubject?.id || ''}
                onChange={(e) => {
                  const s = subjects.find((sub) => sub.id === Number(e.target.value))
                  setSelectedSubject(s || null)
                }}
                disabled={isLoadingSubjects || subjects.length === 0}
                className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg bg-white focus:ring-1 focus:ring-indigo-500 font-medium min-w-[200px]"
              >
                {subjects.map((sub) => (
                  <option key={sub.id} value={sub.id}>
                    {sub.code} - {sub.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className="flex items-center gap-2">
            <label className="text-xs font-semibold text-slate-700">Term:</label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              className="px-2.5 py-1.5 text-sm border border-slate-300 rounded-md w-28 text-center"
            />
            <select
              value={semester}
              onChange={(e) => setSemester(e.target.value)}
              className="px-2.5 py-1.5 text-sm border border-slate-300 rounded-md bg-white text-center"
            >
              <option value="SEMESTER_1">Sem 1</option>
              <option value="SEMESTER_2">Sem 2</option>
              <option value="SEMESTER_3">Sem 3</option>
              <option value="SUMMER">Summer</option>
            </select>
          </div>
        </div>

        {/* Quick Summary Pill */}
        {summaryStats && (
          <div className="flex items-center gap-2 text-xs">
            <span className="px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 font-semibold">
              {summaryStats.activeAlerts} Active Alerts
            </span>
            {summaryStats.criticalAlerts > 0 && (
              <span className="px-2.5 py-1 rounded-full bg-red-100 text-red-800 font-bold">
                {summaryStats.criticalAlerts} Critical
              </span>
            )}
          </div>
        )}
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white p-3 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="text"
            placeholder="Search student or reason..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs sm:text-sm w-56 focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs sm:text-sm bg-white"
          >
            <option value="ACTIVE_ONLY">Active Alerts (Pending Action)</option>
            <option value="ALL">All Alerts (Including Resolved)</option>
            <option value="DETECTED">Detected</option>
            <option value="REVIEWED">Reviewed</option>
            <option value="ACTION_RECORDED">Action Recorded</option>
            <option value="RESOLVED">Resolved</option>
          </select>

          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs sm:text-sm bg-white"
          >
            <option value="ALL">All Risk Types</option>
            <option value="ATTENDANCE_WARNING">Attendance Shortfall (&lt; 80%)</option>
            <option value="LOW_ACADEMIC_MARKS">Low Marks (&lt; 40%)</option>
            <option value="PROGRESS_DECLINE">Progress Decline (≥ 15%)</option>
            <option value="MISSING_ASSESSMENT">Missing Assessment</option>
          </select>
        </div>

        <div className="text-xs text-slate-500 font-medium">
          {filteredAlerts.length} student alert(s)
        </div>
      </div>

      {/* Alerts Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {isLoadingAlerts ? (
          <div className="p-12 text-center text-slate-500 text-sm">Loading risk alerts...</div>
        ) : filteredAlerts.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-3xl mb-2">✅</div>
            <div className="text-base font-semibold text-slate-800">No Alerts for this Class</div>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
              No students are currently flagged under the specified criteria. Click "Evaluate Class Risk" anytime to run a fresh scan on student performance.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                <tr>
                  <th className="px-4 py-3">Student</th>
                  {viewScope === 'ALL_MY_SUBJECTS' && <th className="px-4 py-3">Subject</th>}
                  <th className="px-4 py-3">Risk Type</th>
                  <th className="px-4 py-3">Severity</th>
                  <th className="px-4 py-3">Details & Supporting Metric</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {filteredAlerts.map((alert) => {
                  const typeInfo = getRiskTypeLabel(alert.riskType)
                  return (
                    <tr key={alert.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="px-4 py-3 whitespace-nowrap">
                        <div className="font-semibold text-slate-900">{alert.studentName}</div>
                        <div className="text-xs text-slate-500">
                          {alert.studentIdentifier} • {alert.studentEmail}
                        </div>
                      </td>

                      {viewScope === 'ALL_MY_SUBJECTS' && (
                        <td className="px-4 py-3 whitespace-nowrap">
                          <div className="font-semibold text-slate-800">{alert.subjectCode}</div>
                        </td>
                      )}

                      <td className="px-4 py-3 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${typeInfo.color}`}>
                          {typeInfo.label}
                        </span>
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap">
                        {getSeverityBadge(alert.severity)}
                      </td>

                      <td className="px-4 py-3 max-w-sm">
                        <p className="text-xs text-slate-800 font-medium">{alert.reason}</p>
                        {alert.metricDetails && (
                          <p className="text-[11px] font-mono text-slate-500 mt-0.5 truncate">{alert.metricDetails}</p>
                        )}
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap">
                        {getStatusBadge(alert.status)}
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap text-right space-x-1">
                        <button
                          onClick={() => handleOpenModal(alert, 'VIEW')}
                          className="px-2 py-1 text-xs text-slate-700 bg-slate-100 hover:bg-slate-200 rounded font-medium"
                        >
                          View
                        </button>

                        {alert.status === 'DETECTED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'REVIEW')}
                            className="px-2 py-1 text-xs text-blue-700 bg-blue-50 hover:bg-blue-100 rounded font-medium"
                          >
                            Review
                          </button>
                        )}

                        {alert.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'ACTION')}
                            className="px-2 py-1 text-xs text-amber-800 bg-amber-50 hover:bg-amber-100 rounded font-medium"
                          >
                            Log Action
                          </button>
                        )}

                        {alert.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'RESOLVE')}
                            className="px-2 py-1 text-xs text-emerald-800 bg-emerald-50 hover:bg-emerald-100 rounded font-medium"
                          >
                            Resolve
                          </button>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal for Details / Review / Action / Resolution */}
      {selectedAlert && modalMode && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto border border-slate-200 p-6">
            <div className="flex justify-between items-start border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-lg font-bold text-slate-900">
                  {modalMode === 'VIEW' && 'Alert History & Audit'}
                  {modalMode === 'REVIEW' && 'Acknowledge Alert'}
                  {modalMode === 'ACTION' && 'Log Student Intervention'}
                  {modalMode === 'RESOLVE' && 'Resolve Academic Risk'}
                </h3>
                <p className="text-xs text-slate-500 mt-0.5">
                  Student: {selectedAlert.studentName} ({selectedAlert.studentIdentifier}) • {selectedAlert.subjectCode}
                </p>
              </div>
              <button onClick={handleCloseModal} className="text-slate-400 hover:text-slate-600 font-bold p-1">✕</button>
            </div>

            {modalError && (
              <div className="mt-4 p-3 bg-red-50 text-red-700 border border-red-200 rounded-lg text-xs">
                {modalError}
              </div>
            )}

            <div className="mt-4 space-y-4 text-xs sm:text-sm">
              <div className="bg-slate-50 rounded-lg p-3 space-y-1.5 border border-slate-200">
                <div className="flex justify-between">
                  <span className="text-slate-500">Risk Type:</span>
                  <span className="font-semibold text-slate-800">{getRiskTypeLabel(selectedAlert.riskType).label}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Severity:</span>
                  <span>{getSeverityBadge(selectedAlert.severity)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Status:</span>
                  <span>{getStatusBadge(selectedAlert.status)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Flagged Date:</span>
                  <span className="text-slate-700">{new Date(selectedAlert.detectedAt).toLocaleString()}</span>
                </div>
              </div>

              <div>
                <label className="text-xs font-semibold text-slate-700">Reason:</label>
                <div className="mt-1 p-2.5 bg-slate-50 rounded border border-slate-200 text-xs text-slate-800">
                  {selectedAlert.reason}
                </div>
              </div>

              {selectedAlert.metricDetails && (
                <div>
                  <label className="text-xs font-semibold text-slate-700">Metric Details:</label>
                  <div className="mt-1 p-2.5 bg-slate-50 rounded border border-slate-200 text-xs font-mono text-slate-700">
                    {selectedAlert.metricDetails}
                  </div>
                </div>
              )}

              {selectedAlert.reviewedByName && (
                <div className="border-t border-slate-100 pt-2 text-xs text-slate-600">
                  <span className="font-semibold">Reviewed by:</span> {selectedAlert.reviewedByName} on{' '}
                  {selectedAlert.reviewedAt ? new Date(selectedAlert.reviewedAt).toLocaleString() : ''}
                </div>
              )}

              {selectedAlert.actionNotes && (
                <div className="border-t border-slate-100 pt-2">
                  <label className="text-xs font-semibold text-slate-700">
                    Logged Intervention Action ({selectedAlert.actionRecordedByName || 'Faculty'}):
                  </label>
                  <div className="mt-1 p-2 bg-amber-50 rounded border border-amber-200 text-xs text-slate-800">
                    {selectedAlert.actionNotes}
                  </div>
                </div>
              )}

              {selectedAlert.resolutionNotes && (
                <div className="border-t border-slate-100 pt-2">
                  <label className="text-xs font-semibold text-emerald-800">
                    Resolution Notes ({selectedAlert.resolvedByName || 'Faculty'}):
                  </label>
                  <div className="mt-1 p-2 bg-emerald-50 rounded border border-emerald-200 text-xs text-slate-800">
                    {selectedAlert.resolutionNotes}
                  </div>
                </div>
              )}

              {modalMode === 'REVIEW' && (
                <form onSubmit={handleModalSubmit} className="space-y-3 pt-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700">
                      Acknowledgment Note (Optional):
                    </label>
                    <textarea
                      rows={2}
                      value={reviewComment}
                      onChange={(e) => setReviewComment(e.target.value)}
                      placeholder="e.g. Acknowledged issue, scheduled tutorial meeting with student."
                      className="mt-1 w-full p-2 border border-slate-300 rounded-md text-xs focus:ring-1 focus:ring-blue-500"
                    />
                  </div>
                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      type="button"
                      onClick={handleCloseModal}
                      className="px-3 py-1.5 text-xs border rounded-md text-slate-700 hover:bg-slate-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmittingModal}
                      className="px-4 py-1.5 text-xs bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-md disabled:opacity-50"
                    >
                      {isSubmittingModal ? 'Saving...' : 'Confirm Review'}
                    </button>
                  </div>
                </form>
              )}

              {modalMode === 'ACTION' && (
                <form onSubmit={handleModalSubmit} className="space-y-3 pt-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700">
                      Intervention Action Taken <span className="text-red-500">*</span>:
                    </label>
                    <textarea
                      rows={3}
                      value={actionNotes}
                      onChange={(e) => setActionNotes(e.target.value)}
                      placeholder="Document action taken (e.g. held 1-on-1 discussion, assigned revision exercises, arranged peer tutoring, reminded regarding attendance policies)."
                      required
                      className="mt-1 w-full p-2 border border-slate-300 rounded-md text-xs focus:ring-1 focus:ring-amber-500"
                    />
                  </div>
                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      type="button"
                      onClick={handleCloseModal}
                      className="px-3 py-1.5 text-xs border rounded-md text-slate-700 hover:bg-slate-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmittingModal}
                      className="px-4 py-1.5 text-xs bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-md disabled:opacity-50"
                    >
                      {isSubmittingModal ? 'Saving...' : 'Save Intervention'}
                    </button>
                  </div>
                </form>
              )}

              {modalMode === 'RESOLVE' && (
                <form onSubmit={handleModalSubmit} className="space-y-3 pt-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700">
                      Resolution Notes <span className="text-red-500">*</span>:
                    </label>
                    <textarea
                      rows={3}
                      value={resolutionNotes}
                      onChange={(e) => setResolutionNotes(e.target.value)}
                      placeholder="Explain how the risk was mitigated (e.g. student attendance reached 82%, makeup quiz submitted with mark 75%, performance normalized)."
                      required
                      className="mt-1 w-full p-2 border border-slate-300 rounded-md text-xs focus:ring-1 focus:ring-emerald-500"
                    />
                  </div>
                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      type="button"
                      onClick={handleCloseModal}
                      className="px-3 py-1.5 text-xs border rounded-md text-slate-700 hover:bg-slate-50"
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      disabled={isSubmittingModal}
                      className="px-4 py-1.5 text-xs bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-md disabled:opacity-50"
                    >
                      {isSubmittingModal ? 'Saving...' : 'Resolve Alert'}
                    </button>
                  </div>
                </form>
              )}

              {modalMode === 'VIEW' && (
                <div className="flex justify-end pt-2">
                  <button
                    onClick={handleCloseModal}
                    className="px-4 py-1.5 text-xs bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-md"
                  >
                    Close
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
export default LecturerRiskAlertsView
