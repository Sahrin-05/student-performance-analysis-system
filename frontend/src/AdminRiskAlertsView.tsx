import React, { useState, useEffect, useCallback, useMemo } from 'react'
import {
  type RiskAlert,
  type AlertRiskType,
  type AlertSeverity,
  type AlertStatus,
  type AlertSummaryStats,
  fetchAdminAlerts,
  fetchAdminSummaryStats,
  triggerAdminRiskEvaluation,
  reviewAlertAdmin,
  recordActionAdmin,
  resolveAlertAdmin,
} from './services/riskAlertService'
import type { ApiErrorData } from './services/authService'

export const AdminRiskAlertsView: React.FC = () => {
  const [alerts, setAlerts] = useState<RiskAlert[]>([])
  const [stats, setStats] = useState<AlertSummaryStats | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  // Filters
  const [filterRiskType, setFilterRiskType] = useState<string>('ALL')
  const [filterSeverity, setFilterSeverity] = useState<string>('ALL')
  const [filterStatus, setFilterStatus] = useState<string>('ACTIVE_ONLY')
  const [searchQuery, setSearchQuery] = useState<string>('')

  // Evaluation Form State
  const [evalYear, setEvalYear] = useState<string>('2025/2026')
  const [evalSemester, setEvalSemester] = useState<string>('SEMESTER_1')
  const [isEvaluating, setIsEvaluating] = useState<boolean>(false)

  // Modal State for Action / Resolution / Details
  const [selectedAlert, setSelectedAlert] = useState<RiskAlert | null>(null)
  const [modalMode, setModalMode] = useState<'VIEW' | 'REVIEW' | 'ACTION' | 'RESOLVE' | null>(null)
  const [actionNotes, setActionNotes] = useState<string>('')
  const [reviewComment, setReviewComment] = useState<string>('')
  const [resolutionNotes, setResolutionNotes] = useState<string>('')
  const [isSubmittingModal, setIsSubmittingModal] = useState<boolean>(false)
  const [modalError, setModalError] = useState<string | null>(null)

  const loadData = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const [alertsData, statsData] = await Promise.all([
        fetchAdminAlerts(),
        fetchAdminSummaryStats(),
      ])
      setAlerts(alertsData)
      setStats(statsData)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load risk alerts data.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const handleTriggerEvaluation = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsEvaluating(true)
    setError(null)
    setSuccessMessage(null)
    try {
      const evaluated = await triggerAdminRiskEvaluation({
        academicYear: evalYear.trim(),
        semester: evalSemester.trim(),
      })
      setSuccessMessage(
        `Evaluation sweep completed for ${evalYear} ${evalSemester}. Evaluated and retrieved ${evaluated.length} alert records.`
      )
      await loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to complete risk evaluation sweep.')
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
        const updated = await reviewAlertAdmin(selectedAlert.id, reviewComment)
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Alert #${updated.id} marked as REVIEWED.`)
      } else if (modalMode === 'ACTION') {
        if (!actionNotes.trim()) {
          setModalError('Action notes are required.')
          setIsSubmittingModal(false)
          return
        }
        const updated = await recordActionAdmin(selectedAlert.id, actionNotes.trim())
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Intervention action documented for Alert #${updated.id}.`)
      } else if (modalMode === 'RESOLVE') {
        if (!resolutionNotes.trim()) {
          setModalError('Resolution notes are required.')
          setIsSubmittingModal(false)
          return
        }
        const updated = await resolveAlertAdmin(selectedAlert.id, resolutionNotes.trim())
        setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)))
        setSuccessMessage(`Alert #${updated.id} has been marked as RESOLVED.`)
      }
      handleCloseModal()
      const updatedStats = await fetchAdminSummaryStats()
      setStats(updatedStats)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setModalError(apiErr.message || 'Failed to update alert.')
    } finally {
      setIsSubmittingModal(false)
    }
  }

  // Filtered Alerts
  const filteredAlerts = useMemo(() => {
    return alerts.filter((a) => {
      // Risk Type Filter
      if (filterRiskType !== 'ALL' && a.riskType !== filterRiskType) return false
      // Severity Filter
      if (filterSeverity !== 'ALL' && a.severity !== filterSeverity) return false
      // Status Filter
      if (filterStatus === 'ACTIVE_ONLY') {
        if (a.status === 'RESOLVED') return false
      } else if (filterStatus !== 'ALL') {
        if (a.status !== filterStatus) return false
      }
      // Search Query
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase()
        const matchesStudent =
          a.studentName.toLowerCase().includes(query) ||
          a.studentIdentifier.toLowerCase().includes(query) ||
          a.studentEmail.toLowerCase().includes(query)
        const matchesSubject =
          a.subjectCode.toLowerCase().includes(query) ||
          a.subjectName.toLowerCase().includes(query)
        const matchesReason = a.reason.toLowerCase().includes(query)
        if (!matchesStudent && !matchesSubject && !matchesReason) return false
      }
      return true
    })
  }, [alerts, filterRiskType, filterSeverity, filterStatus, searchQuery])

  // Badge Helpers
  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-red-100 text-red-800 border border-red-200">
            CRITICAL
          </span>
        )
      case 'HIGH':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-rose-100 text-rose-800 border border-rose-200">
            HIGH
          </span>
        )
      case 'MEDIUM':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
            MEDIUM
          </span>
        )
      case 'LOW':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">
            LOW
          </span>
        )
    }
  }

  const getStatusBadge = (status: AlertStatus) => {
    switch (status) {
      case 'DETECTED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-50 text-red-700 border border-red-200">
            DETECTED
          </span>
        )
      case 'REVIEWED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">
            REVIEWED
          </span>
        )
      case 'ACTION_RECORDED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
            ACTION LOGGED
          </span>
        )
      case 'RESOLVED':
        return (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">
            RESOLVED
          </span>
        )
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
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Institutional Risk Alerts & Pastoral Care</h1>
          <p className="text-sm text-slate-500 mt-1">
            Real-time, rule-based academic monitoring with early detection and recorded interventions.
          </p>
        </div>
        <button
          onClick={loadData}
          disabled={isLoading}
          className="inline-flex items-center gap-2 px-4 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors disabled:opacity-50"
        >
          {isLoading ? 'Refreshing...' : 'Refresh Alerts'}
        </button>
      </div>

      {/* Provisional Notice */}
      <div className="bg-amber-50 border border-amber-200 text-amber-900 rounded-xl p-4 text-xs sm:text-sm flex items-start gap-3">
        <span className="text-amber-600 font-bold text-base">ℹ</span>
        <div>
          <span className="font-semibold">Provisional System Heuristics Notice:</span> Detection rules (Attendance &lt; 80.0%, Low Marks &lt; 40.0%, Progress Decline &ge; 15.0%, Missing Assessments) are provisional system defaults configured to support pastoral care and early advisory intervention. They do not constitute official university policy.
        </div>
      </div>

      {/* Global Notifications */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-red-500 hover:text-red-700 font-bold ml-4">✕</button>
        </div>
      )}
      {successMessage && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl p-4 text-sm flex items-center justify-between">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 hover:text-emerald-800 font-bold ml-4">✕</button>
        </div>
      )}

      {/* Statistics Cards */}
      {stats && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-xs">
            <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Alerts</div>
            <div className="mt-2 flex items-baseline justify-between">
              <div className="text-3xl font-extrabold text-slate-900">{stats.activeAlerts}</div>
              <span className="text-xs font-medium text-slate-500">{stats.totalAlerts} Total</span>
            </div>
            <div className="mt-3 text-xs text-slate-600">Requires academic review or intervention</div>
          </div>

          <div className="bg-white p-5 rounded-xl border border-red-200 bg-red-50/20 shadow-xs">
            <div className="text-xs font-semibold text-red-600 uppercase tracking-wider">Critical Severity</div>
            <div className="mt-2 flex items-baseline justify-between">
              <div className="text-3xl font-extrabold text-red-600">{stats.criticalAlerts}</div>
              <span className="text-xs font-semibold text-rose-600">{stats.highAlerts} High</span>
            </div>
            <div className="mt-3 text-xs text-red-700">Immediate coordinator intervention recommended</div>
          </div>

          <div className="bg-white p-5 rounded-xl border border-amber-200 bg-amber-50/20 shadow-xs">
            <div className="text-xs font-semibold text-amber-700 uppercase tracking-wider">Attendance Warnings</div>
            <div className="mt-2 flex items-baseline justify-between">
              <div className="text-3xl font-extrabold text-amber-800">{stats.attendanceWarnings}</div>
              <span className="text-xs font-medium text-amber-700">&lt; 80% Threshold</span>
            </div>
            <div className="mt-3 text-xs text-amber-800">Isolated from marks; exam debarment risk</div>
          </div>

          <div className="bg-white p-5 rounded-xl border border-indigo-200 bg-indigo-50/20 shadow-xs">
            <div className="text-xs font-semibold text-indigo-700 uppercase tracking-wider">Academic & Decline</div>
            <div className="mt-2 flex items-baseline justify-between">
              <div className="text-3xl font-extrabold text-indigo-900">{stats.lowMarksAlerts + stats.progressDeclineAlerts}</div>
              <span className="text-xs font-medium text-indigo-700">{stats.missingAssessmentAlerts} Missing</span>
            </div>
            <div className="mt-3 text-xs text-indigo-800">Low scores, drops, or missing assessments</div>
          </div>
        </div>
      )}

      {/* Sweep Evaluation Trigger Form */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs">
        <h2 className="text-lg font-bold text-slate-900 mb-2">Automated Risk Detection Sweep</h2>
        <p className="text-xs text-slate-500 mb-4">
          Scan student academic rosters across all active subjects for attendance shortfalls, low assessment mastery, progress drops, and missing coursework.
        </p>

        <form onSubmit={handleTriggerEvaluation} className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2">
            <label className="text-xs font-medium text-slate-600">Academic Year:</label>
            <input
              type="text"
              value={evalYear}
              onChange={(e) => setEvalYear(e.target.value)}
              placeholder="2025/2026"
              required
              className="px-3 py-1.5 text-sm border border-slate-300 rounded-md focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="flex items-center gap-2">
            <label className="text-xs font-medium text-slate-600">Semester:</label>
            <select
              value={evalSemester}
              onChange={(e) => setEvalSemester(e.target.value)}
              className="px-3 py-1.5 text-sm border border-slate-300 rounded-md focus:ring-1 focus:ring-indigo-500"
            >
              <option value="SEMESTER_1">Semester 1</option>
              <option value="SEMESTER_2">Semester 2</option>
              <option value="SEMESTER_3">Semester 3</option>
              <option value="SUMMER">Summer Term</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={isEvaluating}
            className="inline-flex items-center gap-2 px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold shadow-xs disabled:opacity-50 transition-colors"
          >
            {isEvaluating ? 'Running Sweep...' : 'Run Risk Evaluation Sweep'}
          </button>
        </form>
      </div>

      {/* Filters and Search */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="text"
            placeholder="Search student, ID, subject..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm w-64 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />

          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="ACTIVE_ONLY">Active Only (Detected / Reviewed / Action)</option>
            <option value="ALL">All Statuses</option>
            <option value="DETECTED">Detected</option>
            <option value="REVIEWED">Reviewed</option>
            <option value="ACTION_RECORDED">Action Recorded</option>
            <option value="RESOLVED">Resolved</option>
          </select>

          <select
            value={filterRiskType}
            onChange={(e) => setFilterRiskType(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="ALL">All Risk Types</option>
            <option value="ATTENDANCE_WARNING">Attendance Warning (&lt; 80%)</option>
            <option value="LOW_ACADEMIC_MARKS">Low Academic Marks (&lt; 40%)</option>
            <option value="PROGRESS_DECLINE">Progress Decline (≥ 15%)</option>
            <option value="MISSING_ASSESSMENT">Missing Assessment</option>
          </select>

          <select
            value={filterSeverity}
            onChange={(e) => setFilterSeverity(e.target.value)}
            className="px-3 py-2 border border-slate-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="ALL">All Severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>

        <div className="text-xs text-slate-500 font-medium">
          Showing {filteredAlerts.length} of {alerts.length} alerts
        </div>
      </div>

      {/* Alerts Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        {isLoading ? (
          <div className="p-12 text-center text-slate-500 text-sm">Loading institutional risk alerts...</div>
        ) : filteredAlerts.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-3xl mb-2">🎉</div>
            <div className="text-base font-semibold text-slate-800">No Risk Alerts Found</div>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
              There are no alerts matching the selected filters. All active students appear to be meeting current academic and attendance criteria.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
              <thead className="bg-slate-50 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                <tr>
                  <th className="px-4 py-3">Student</th>
                  <th className="px-4 py-3">Subject</th>
                  <th className="px-4 py-3">Risk Type</th>
                  <th className="px-4 py-3">Severity</th>
                  <th className="px-4 py-3">Reason & Metric</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Detected</th>
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

                      <td className="px-4 py-3 whitespace-nowrap">
                        <div className="font-medium text-slate-800">{alert.subjectCode}</div>
                        <div className="text-xs text-slate-500 truncate max-w-[140px]">{alert.subjectName}</div>
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border ${typeInfo.color}`}>
                          {typeInfo.label}
                        </span>
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap">
                        {getSeverityBadge(alert.severity)}
                      </td>

                      <td className="px-4 py-3 max-w-xs">
                        <p className="text-xs text-slate-800 font-medium line-clamp-2">{alert.reason}</p>
                        {alert.metricValue !== null && alert.metricValue !== undefined && (
                          <span className="text-[11px] font-mono text-slate-500">
                            Metric Value: {alert.metricValue.toFixed(2)}%
                          </span>
                        )}
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap">
                        {getStatusBadge(alert.status)}
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap text-xs text-slate-500">
                        {new Date(alert.detectedAt).toLocaleDateString()}
                      </td>

                      <td className="px-4 py-3 whitespace-nowrap text-right space-x-1">
                        <button
                          onClick={() => handleOpenModal(alert, 'VIEW')}
                          className="px-2 py-1 text-xs text-slate-700 bg-slate-100 hover:bg-slate-200 rounded font-medium transition-colors"
                        >
                          Details
                        </button>

                        {alert.status === 'DETECTED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'REVIEW')}
                            className="px-2 py-1 text-xs text-blue-700 bg-blue-50 hover:bg-blue-100 rounded font-medium transition-colors"
                          >
                            Review
                          </button>
                        )}

                        {alert.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'ACTION')}
                            className="px-2 py-1 text-xs text-amber-800 bg-amber-50 hover:bg-amber-100 rounded font-medium transition-colors"
                          >
                            Log Action
                          </button>
                        )}

                        {alert.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleOpenModal(alert, 'RESOLVE')}
                            className="px-2 py-1 text-xs text-emerald-800 bg-emerald-50 hover:bg-emerald-100 rounded font-medium transition-colors"
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
          <div className="bg-white rounded-2xl shadow-xl max-w-xl w-full max-h-[90vh] overflow-y-auto border border-slate-200 p-6">
            <div className="flex justify-between items-start border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-lg font-bold text-slate-900">
                  {modalMode === 'VIEW' && 'Alert Details & History'}
                  {modalMode === 'REVIEW' && 'Acknowledge & Review Alert'}
                  {modalMode === 'ACTION' && 'Document Intervention Action'}
                  {modalMode === 'RESOLVE' && 'Resolve Academic Risk Alert'}
                </h3>
                <p className="text-xs text-slate-500 mt-0.5">
                  Alert #{selectedAlert.id} • {selectedAlert.studentName} ({selectedAlert.studentIdentifier})
                </p>
              </div>
              <button
                onClick={handleCloseModal}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg p-1"
              >
                ✕
              </button>
            </div>

            {modalError && (
              <div className="mt-4 p-3 bg-red-50 text-red-700 border border-red-200 rounded-lg text-xs">
                {modalError}
              </div>
            )}

            <div className="mt-4 space-y-4 text-xs sm:text-sm">
              {/* Alert Info Summary */}
              <div className="bg-slate-50 rounded-lg p-3.5 space-y-2 border border-slate-200">
                <div className="flex justify-between">
                  <span className="text-slate-500">Subject:</span>
                  <span className="font-semibold text-slate-800">{selectedAlert.subjectCode} - {selectedAlert.subjectName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Academic Term:</span>
                  <span className="font-medium text-slate-700">{selectedAlert.academicYear} {selectedAlert.semester}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Risk Type:</span>
                  <span className="font-medium text-slate-800">{getRiskTypeLabel(selectedAlert.riskType).label}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Severity:</span>
                  <span>{getSeverityBadge(selectedAlert.severity)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Current Status:</span>
                  <span>{getStatusBadge(selectedAlert.status)}</span>
                </div>
              </div>

              {/* Explainable Reason and Metric Details */}
              <div>
                <label className="text-xs font-semibold text-slate-700">Detection Reason:</label>
                <div className="mt-1 p-2.5 bg-slate-50 rounded border border-slate-200 text-xs text-slate-800">
                  {selectedAlert.reason}
                </div>
              </div>

              {selectedAlert.metricDetails && (
                <div>
                  <label className="text-xs font-semibold text-slate-700">Detailed Metrics:</label>
                  <div className="mt-1 p-2.5 bg-slate-50 rounded border border-slate-200 text-xs font-mono text-slate-700">
                    {selectedAlert.metricDetails}
                  </div>
                </div>
              )}

              {/* History / Audit Log */}
              {selectedAlert.reviewedByName && (
                <div className="border-t border-slate-100 pt-3 text-xs text-slate-600">
                  <span className="font-semibold">Reviewed By:</span> {selectedAlert.reviewedByName} on{' '}
                  {selectedAlert.reviewedAt ? new Date(selectedAlert.reviewedAt).toLocaleString() : 'N/A'}
                </div>
              )}

              {selectedAlert.actionNotes && (
                <div className="border-t border-slate-100 pt-3">
                  <label className="text-xs font-semibold text-slate-700">
                    Documented Intervention Notes ({selectedAlert.actionRecordedByName || 'Staff'}):
                  </label>
                  <div className="mt-1 p-2.5 bg-amber-50/50 rounded border border-amber-200 text-xs text-slate-800">
                    {selectedAlert.actionNotes}
                  </div>
                </div>
              )}

              {selectedAlert.resolutionNotes && (
                <div className="border-t border-slate-100 pt-3">
                  <label className="text-xs font-semibold text-emerald-800">
                    Resolution Notes ({selectedAlert.resolvedByName || 'Staff'}):
                  </label>
                  <div className="mt-1 p-2.5 bg-emerald-50/50 rounded border border-emerald-200 text-xs text-slate-800">
                    {selectedAlert.resolutionNotes}
                  </div>
                </div>
              )}

              {/* Form Input Depending on Modal Mode */}
              {modalMode === 'REVIEW' && (
                <form onSubmit={handleModalSubmit} className="space-y-3 pt-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700">
                      Reviewer Note (Optional):
                    </label>
                    <textarea
                      rows={2}
                      value={reviewComment}
                      onChange={(e) => setReviewComment(e.target.value)}
                      placeholder="e.g. Acknowledged shortfall, scheduled for departmental review."
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
                      Intervention Action Notes <span className="text-red-500">*</span>:
                    </label>
                    <textarea
                      rows={3}
                      value={actionNotes}
                      onChange={(e) => setActionNotes(e.target.value)}
                      placeholder="Describe the action taken (e.g. conducted 1-on-1 meeting, assigned remedial tutorial, referred to student counseling, contact made with guardian)."
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
                      {isSubmittingModal ? 'Saving...' : 'Record Action'}
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
                      placeholder="Explain how the risk was mitigated (e.g. student achieved 85% attendance, makeup assessment completed with score 70%, academic performance normalized)."
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
                <div className="flex justify-end pt-3">
                  <button
                    onClick={handleCloseModal}
                    className="px-4 py-2 text-xs bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-lg"
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
export default AdminRiskAlertsView
