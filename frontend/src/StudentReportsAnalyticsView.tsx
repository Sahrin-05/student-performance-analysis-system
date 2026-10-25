import { useState, useEffect, useCallback } from 'react'
import {
  fetchStudentReport,
  downloadStudentReportCsv,
  type StudentReportResponse,
  type StudentSubjectReportItem,
} from './services/reportService'
import type { ApiErrorData } from './services/authService'

export function StudentReportsAnalyticsView() {
  const [report, setReport] = useState<StudentReportResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isExporting, setIsExporting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [expandedSubjectId, setExpandedSubjectId] = useState<number | null>(null)

  const loadData = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await fetchStudentReport()
      setReport(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load personal academic report.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const handleExportCsv = async () => {
    setIsExporting(true)
    try {
      await downloadStudentReportCsv()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      alert(apiErr.message || 'Failed to export CSV report.')
    } finally {
      setIsExporting(false)
    }
  }

  const toggleSubjectExpand = (enrollmentId: number) => {
    setExpandedSubjectId(expandedSubjectId === enrollmentId ? null : enrollmentId)
  }

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 space-y-4">
        <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="text-sm font-medium text-slate-600">Generating student academic report & transcript...</p>
      </div>
    )
  }

  if (error || !report) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center space-y-3">
        <p className="text-sm font-semibold text-red-800">{error || 'Unable to load academic report.'}</p>
        <button
          type="button"
          onClick={loadData}
          className="px-4 py-2 bg-red-600 text-white rounded-lg text-xs font-semibold hover:bg-red-700 transition"
        >
          Try Again
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-gradient-to-r from-blue-700 to-indigo-800 rounded-2xl p-6 text-white shadow-xs">
        <div>
          <span className="inline-flex items-center rounded-full bg-blue-500/30 px-3 py-1 text-xs font-semibold tracking-wide uppercase border border-blue-400/40">
            Student Academic Standing & Transcript
          </span>
          <h2 className="text-2xl font-bold mt-2">{report.studentName}</h2>
          <p className="text-xs text-blue-100 mt-1">
            Student ID: <span className="font-semibold text-white">{report.studentIdentifier}</span> • {report.studentEmail}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={handleExportCsv}
            disabled={isExporting}
            className="inline-flex items-center gap-2 rounded-lg bg-white/10 hover:bg-white/20 px-4 py-2.5 text-xs font-semibold text-white border border-white/20 transition backdrop-blur-xs cursor-pointer disabled:opacity-50"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            <span>{isExporting ? 'Exporting...' : 'Export Transcript (CSV)'}</span>
          </button>
        </div>
      </div>

      {/* Provisional Policy Disclosure Notice */}
      <div className="rounded-xl border border-blue-200 bg-blue-50/60 p-4 text-xs text-blue-900 leading-relaxed flex items-start gap-3">
        <svg className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <span className="font-semibold">Provisional System Defaults: </span>
          {report.provisionalNotice}
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* GPA */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Cumulative GPA</span>
          <div className="flex items-baseline gap-2 mt-2">
            <span className="text-3xl font-extrabold text-blue-600">
              {report.gpa !== null ? report.gpa.toFixed(2) : '—'}
            </span>
            <span className="text-xs text-slate-400">/ 4.00</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">
            {report.publishedSubjectsCount > 0
              ? `Based on ${report.publishedSubjectsCount} published subjects`
              : 'Awaiting published grades'}
          </p>
        </div>

        {/* Credits Earned */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Credits Earned</span>
          <div className="flex items-baseline gap-2 mt-2">
            <span className="text-3xl font-extrabold text-emerald-600">
              {report.totalCreditsEarned}
            </span>
            <span className="text-xs text-slate-400">/ {report.totalEnrolledCredits} Enrolled</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">
            {report.passedSubjectsCount} passed • {report.failedSubjectsCount} failed
          </p>
        </div>

        {/* Subjects Progress */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Course Modules</span>
          <div className="flex items-baseline gap-2 mt-2">
            <span className="text-3xl font-extrabold text-slate-900">
              {report.publishedSubjectsCount}
            </span>
            <span className="text-xs text-slate-400">/ {report.totalEnrolledSubjects} Published</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">
            {report.totalEnrolledSubjects - report.publishedSubjectsCount} ongoing / in draft
          </p>
        </div>

        {/* Overall Attendance */}
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Attendance Compliance</span>
          <div className="flex items-baseline gap-2 mt-2">
            <span className={`text-3xl font-extrabold ${
              report.overallAttendancePercentage !== null && report.overallAttendancePercentage < 80.0
                ? 'text-amber-600'
                : 'text-emerald-600'
            }`}>
              {report.overallAttendancePercentage !== null ? `${report.overallAttendancePercentage.toFixed(1)}%` : '100%'}
            </span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">
            {report.overallAttendancePercentage !== null && report.overallAttendancePercentage < 80.0
              ? 'Below 80% advisory threshold'
              : 'Cleared for examinations'}
          </p>
        </div>
      </div>

      {/* Subject-Wise Academic Modules */}
      <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
        <div className="border-b border-slate-100 bg-slate-50 px-6 py-4 flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900">Subject Performance & Attendance Breakdown</h3>
            <p className="text-xs text-slate-500">Official continuous evaluation marks are displayed only when published by faculty</p>
          </div>
          <span className="text-xs font-semibold text-slate-600 bg-slate-200/80 px-2.5 py-1 rounded-full">
            {report.subjects.length} Enrolled Subjects
          </span>
        </div>

        {report.subjects.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">
            You are not currently enrolled in any academic subjects.
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {report.subjects.map((s: StudentSubjectReportItem) => {
              const isExpanded = expandedSubjectId === s.enrollmentId
              const hasAlerts = s.activeAlerts.length > 0

              return (
                <div key={s.enrollmentId} className="p-5 hover:bg-slate-50/60 transition">
                  {/* Top Bar for Subject */}
                  <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded">
                          {s.subjectCode}
                        </span>
                        <h4 className="text-sm font-bold text-slate-900">{s.subjectName}</h4>
                        <span className="text-xs text-slate-400">({s.credits} Credits)</span>
                      </div>
                      <p className="text-xs text-slate-500">
                        Academic Year: {s.academicYear} • Semester: {s.semester}
                      </p>
                    </div>

                    <div className="flex flex-wrap items-center gap-3">
                      {/* Attendance Pill */}
                      <div className="text-right">
                        <span className="text-[11px] font-medium text-slate-400 block">Attendance</span>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${
                          s.belowAttendanceThreshold
                            ? 'bg-amber-50 text-amber-800 border-amber-200'
                            : 'bg-emerald-50 text-emerald-800 border-emerald-200'
                        }`}>
                          {s.attendancePercentage.toFixed(1)}% ({s.attendedSessions}/{s.eligibleSessions})
                        </span>
                      </div>

                      {/* Grade Pill */}
                      <div className="text-right min-w-[90px]">
                        <span className="text-[11px] font-medium text-slate-400 block">Grade</span>
                        {s.publicationStatus === 'PUBLISHED' ? (
                          <div className="flex items-center gap-1.5 justify-end">
                            <span className="text-sm font-extrabold text-slate-900">
                              {s.totalWeightedScore?.toFixed(1)}%
                            </span>
                            <span className={`px-2 py-0.5 rounded font-bold text-xs ${
                              s.grade === 'A' ? 'bg-emerald-100 text-emerald-800 border border-emerald-200' :
                              s.grade === 'B' ? 'bg-blue-100 text-blue-800 border border-blue-200' :
                              s.grade === 'C' ? 'bg-cyan-100 text-cyan-800 border border-cyan-200' :
                              s.grade === 'S' ? 'bg-amber-100 text-amber-800 border border-amber-200' :
                              'bg-red-100 text-red-800 border border-red-200'
                            }`}>
                              {s.grade}
                            </span>
                          </div>
                        ) : (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                            Draft Pending
                          </span>
                        )}
                      </div>

                      {/* Expand / Details Toggle */}
                      <button
                        type="button"
                        onClick={() => toggleSubjectExpand(s.enrollmentId)}
                        className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg hover:bg-slate-200/60 transition cursor-pointer"
                        aria-label="Toggle details"
                      >
                        <svg className={`h-5 w-5 transform transition-transform ${isExpanded ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                    </div>
                  </div>

                  {/* Expanded Breakdown Accordion */}
                  {isExpanded && (
                    <div className="mt-4 pt-4 border-t border-slate-100 space-y-4">
                      {/* Continuous Assessment Breakdown */}
                      {s.publicationStatus === 'PUBLISHED' && s.assessmentScores.length > 0 ? (
                        <div>
                          <h5 className="text-xs font-semibold text-slate-700 uppercase tracking-wide mb-2">
                            Assessment Components
                          </h5>
                          <div className="overflow-x-auto rounded-lg border border-slate-200">
                            <table className="min-w-full divide-y divide-slate-200 text-xs">
                              <thead className="bg-slate-50 font-medium text-slate-500">
                                <tr>
                                  <th className="px-3 py-2 text-left">Assessment</th>
                                  <th className="px-3 py-2 text-left">Type</th>
                                  <th className="px-3 py-2 text-right">Max Marks</th>
                                  <th className="px-3 py-2 text-right">Weight</th>
                                  <th className="px-3 py-2 text-right">Score</th>
                                  <th className="px-3 py-2 text-right">Weighted Score</th>
                                </tr>
                              </thead>
                              <tbody className="divide-y divide-slate-100">
                                {s.assessmentScores.map((ass) => (
                                  <tr key={ass.assessmentId} className="hover:bg-slate-50/50">
                                    <td className="px-3 py-2 font-medium text-slate-900">{ass.assessmentTitle}</td>
                                    <td className="px-3 py-2 text-slate-500">{ass.assessmentType}</td>
                                    <td className="px-3 py-2 text-right text-slate-600">{ass.maxMarks.toFixed(1)}</td>
                                    <td className="px-3 py-2 text-right text-slate-600">{ass.weightPercentage}%</td>
                                    <td className="px-3 py-2 text-right font-semibold text-slate-900">
                                      {ass.score !== null ? ass.score.toFixed(1) : '—'}
                                    </td>
                                    <td className="px-3 py-2 text-right font-bold text-blue-600">
                                      {ass.weightedContribution !== null ? `${ass.weightedContribution.toFixed(2)}%` : '—'}
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        </div>
                      ) : s.publicationStatus !== 'PUBLISHED' ? (
                        <div className="rounded-lg bg-slate-50 p-3 text-xs text-slate-600 border border-slate-200">
                          Final assessment marks and continuous evaluation components have not yet been released by the instructor.
                        </div>
                      ) : null}

                      {/* Attendance Breakdown Details */}
                      <div className="rounded-lg bg-slate-50 p-3.5 border border-slate-200 text-xs">
                        <span className="font-semibold text-slate-700 block mb-1">Session Attendance Detail:</span>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-slate-600">
                          <div>Total Conducted: <span className="font-semibold text-slate-900">{s.totalSessions}</span></div>
                          <div>Attended (Present+Late): <span className="font-semibold text-emerald-700">{s.attendedSessions}</span></div>
                          <div>Absences: <span className="font-semibold text-red-600">{s.absentSessions}</span></div>
                          <div>Excused (Excluded): <span className="font-semibold text-slate-700">{s.excusedSessions}</span></div>
                        </div>
                      </div>

                      {/* Personal Alerts & Supportive Guidance */}
                      {hasAlerts && (
                        <div className="space-y-2">
                          <span className="text-xs font-semibold text-amber-800 block uppercase tracking-wide">
                            Active Academic Advising Notices
                          </span>
                          {s.activeAlerts.map((alt) => (
                            <div key={alt.alertId} className="rounded-lg border border-amber-200 bg-amber-50/70 p-3.5 text-xs space-y-1.5">
                              <div className="flex items-center justify-between">
                                <span className="font-bold text-amber-900">{alt.riskType.replace(/_/g, ' ')}</span>
                                <span className="text-[11px] font-semibold text-amber-700 bg-amber-100 px-2 py-0.5 rounded-full">
                                  {alt.severity} Priority
                                </span>
                              </div>
                              <p className="text-amber-800">{alt.triggerReason}</p>
                              <div className="rounded bg-white/70 p-2.5 border border-amber-200/60 text-slate-700">
                                <span className="font-semibold text-amber-900">Supportive Guidance: </span>
                                {alt.advisoryGuidance}
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
