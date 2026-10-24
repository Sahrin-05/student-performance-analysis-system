import React, { useState, useEffect, useCallback } from 'react'
import {
  type StudentRiskAlert,
  type AlertSeverity,
  type AlertStatus,
  type AlertRiskType,
  fetchStudentMyAlerts,
} from './services/riskAlertService'
import type { ApiErrorData } from './services/authService'

export const StudentRiskAlertsView: React.FC = () => {
  const [alerts, setAlerts] = useState<StudentRiskAlert[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<'ACTIVE_ONLY' | 'ALL'>('ACTIVE_ONLY')

  const loadAlerts = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await fetchStudentMyAlerts()
      setAlerts(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load personal academic alerts.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAlerts()
  }, [loadAlerts])

  const filteredAlerts = alerts.filter((a) => {
    if (statusFilter === 'ACTIVE_ONLY') {
      return a.status !== 'RESOLVED'
    }
    return true
  })

  const getSeverityBadge = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-800 border border-red-200">Urgent Attention</span>
      case 'HIGH':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-100 text-rose-800 border border-rose-200">High Priority</span>
      case 'MEDIUM':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">Advisory</span>
      case 'LOW':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 border border-emerald-200">Notice</span>
    }
  }

  const getStatusBadge = (status: AlertStatus) => {
    switch (status) {
      case 'DETECTED':
        return <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-red-50 text-red-700 border border-red-200">Under Review</span>
      case 'REVIEWED':
        return <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-blue-50 text-blue-700 border border-blue-200">Faculty Acknowledged</span>
      case 'ACTION_RECORDED':
        return <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">Support in Progress</span>
      case 'RESOLVED':
        return <span className="px-2 py-0.5 rounded-md text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200">Resolved</span>
    }
  }

  const getRiskTypeInfo = (riskType: AlertRiskType) => {
    switch (riskType) {
      case 'ATTENDANCE_WARNING':
        return {
          icon: '📅',
          title: 'Attendance Shortfall Notice',
          color: 'border-amber-200 bg-amber-50/40 text-amber-900',
        }
      case 'LOW_ACADEMIC_MARKS':
        return {
          icon: '📊',
          title: 'Academic Performance Advisory',
          color: 'border-rose-200 bg-rose-50/40 text-rose-900',
        }
      case 'PROGRESS_DECLINE':
        return {
          icon: '📉',
          title: 'Assessment Score Decline',
          color: 'border-purple-200 bg-purple-50/40 text-purple-900',
        }
      case 'MISSING_ASSESSMENT':
        return {
          icon: '⚠️',
          title: 'Pending / Missing Assessment Work',
          color: 'border-orange-200 bg-orange-50/40 text-orange-900',
        }
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-xs border border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Academic Advising & Early Alerts</h1>
          <p className="text-sm text-slate-500 mt-1">
            Proactive indicators to help you stay on track with class attendance, coursework, and continuous assessments.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as 'ACTIVE_ONLY' | 'ALL')}
            className="px-3 py-2 border border-slate-300 rounded-lg text-xs sm:text-sm bg-white"
          >
            <option value="ACTIVE_ONLY">Active Alerts</option>
            <option value="ALL">All Alerts (Including Resolved)</option>
          </select>
          <button
            onClick={loadAlerts}
            disabled={isLoading}
            className="px-3 py-2 border border-slate-300 rounded-lg text-xs sm:text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 transition-colors"
          >
            {isLoading ? 'Refreshing...' : 'Refresh'}
          </button>
        </div>
      </div>

      {/* Pastoral Notice */}
      <div className="bg-indigo-50 border border-indigo-200 text-indigo-900 rounded-xl p-4 text-xs sm:text-sm flex items-start gap-3">
        <span className="text-indigo-600 font-bold text-base">💡</span>
        <div>
          <span className="font-semibold">How Early Alerts Work:</span> Academic alerts are designed to support your learning journey by identifying attendance shortfalls or assessment dips early. Thresholds are provisional system heuristics designed for student advising and do not reflect official university disciplinary policy. If you see an alert, take advantage of faculty office hours and academic support services.
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-red-500 font-bold ml-4">✕</button>
        </div>
      )}

      {/* Alerts Content */}
      {isLoading ? (
        <div className="bg-white p-12 text-center rounded-xl border border-slate-200 text-slate-500 text-sm">
          Loading your personal academic alerts...
        </div>
      ) : filteredAlerts.length === 0 ? (
        <div className="bg-white p-12 text-center rounded-xl border border-slate-200 shadow-xs">
          <div className="text-4xl mb-3">🌟</div>
          <h3 className="text-lg font-bold text-slate-900">All Clear! No Active Risk Alerts</h3>
          <p className="text-sm text-slate-500 mt-2 max-w-md mx-auto">
            You do not currently have any active academic or attendance alerts. Keep attending your class sessions regularly and stay on top of upcoming assessments!
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredAlerts.map((alert) => {
            const typeInfo = getRiskTypeInfo(alert.riskType)
            const isResolved = alert.status === 'RESOLVED'

            return (
              <div
                key={alert.id}
                className={`bg-white rounded-xl border p-5 shadow-xs transition-shadow hover:shadow-sm ${
                  isResolved ? 'border-slate-200 opacity-75' : 'border-slate-300'
                }`}
              >
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 border-b border-slate-100 pb-3">
                  <div className="flex items-center gap-2">
                    <span className="text-xl">{typeInfo.icon}</span>
                    <div>
                      <h3 className="text-base font-bold text-slate-900">{typeInfo.title}</h3>
                      <p className="text-xs text-slate-500">
                        {alert.subjectCode} • {alert.subjectName} ({alert.academicYear} {alert.semester})
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    {getSeverityBadge(alert.severity)}
                    {getStatusBadge(alert.status)}
                  </div>
                </div>

                <div className="mt-3 space-y-3">
                  {/* Explanation */}
                  <div>
                    <p className="text-sm font-medium text-slate-800">{alert.reason}</p>
                    {alert.metricDetails && (
                      <p className="text-xs font-mono text-slate-500 mt-1">{alert.metricDetails}</p>
                    )}
                  </div>

                  {/* Advisory Guidance Box */}
                  <div className={`p-3.5 rounded-lg border text-xs sm:text-sm ${typeInfo.color}`}>
                    <div className="font-semibold mb-1 flex items-center gap-1.5">
                      <span>📌</span>
                      <span>Recommended Action for You:</span>
                    </div>
                    <p>{alert.advisoryGuidance}</p>
                  </div>

                  {/* Timestamps */}
                  <div className="flex flex-wrap items-center justify-between text-[11px] text-slate-400 pt-1">
                    <span>Identified on {new Date(alert.detectedAt).toLocaleDateString()}</span>
                    {alert.resolvedAt && (
                      <span className="text-emerald-600 font-medium">
                        Resolved on {new Date(alert.resolvedAt).toLocaleDateString()}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
export default StudentRiskAlertsView
