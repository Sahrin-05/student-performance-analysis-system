import { useState, useEffect, useCallback } from 'react'
import {
  fetchLecturerSubjectReport,
  downloadLecturerPerformanceCsv,
  downloadLecturerAttendanceCsv,
  downloadLecturerAtRiskCsv,
  type LecturerSubjectReportResponse,
  type LecturerStudentReportRow,
} from './services/reportService'
import { fetchLecturerMySubjects, type Subject } from './services/academicService'
import type { ApiErrorData } from './services/authService'

export function LecturerReportsAnalyticsView() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [academicYear, setAcademicYear] = useState('2025/2026')
  const [semester, setSemester] = useState('1')

  const [report, setReport] = useState<LecturerSubjectReportResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingSubjects, setIsLoadingSubjects] = useState(true)
  const [exportingType, setExportingType] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  // Student filter
  const [studentSearch, setStudentSearch] = useState('')
  const [activeTab, setActiveTab] = useState<'matrix' | 'attendance' | 'atRisk'>('matrix')

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

  const loadReport = useCallback(async () => {
    if (!selectedSubject) return
    setIsLoading(true)
    setError(null)
    try {
      const data = await fetchLecturerSubjectReport(selectedSubject.id, academicYear, semester)
      setReport(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to generate class performance report.')
    } finally {
      setIsLoading(false)
    }
  }, [selectedSubject, academicYear, semester])

  useEffect(() => {
    if (selectedSubject) {
      queueMicrotask(() => {
        loadReport()
      })
    }
  }, [selectedSubject, loadReport])

  const handleExport = async (type: 'performance' | 'attendance' | 'atRisk') => {
    if (!selectedSubject) return
    setExportingType(type)
    try {
      if (type === 'performance') {
        await downloadLecturerPerformanceCsv(selectedSubject.id, academicYear, semester)
      } else if (type === 'attendance') {
        await downloadLecturerAttendanceCsv(selectedSubject.id, academicYear, semester)
      } else if (type === 'atRisk') {
        await downloadLecturerAtRiskCsv(selectedSubject.id, academicYear, semester)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      alert(apiErr.message || 'Failed to export CSV report.')
    } finally {
      setExportingType(null)
    }
  }

  // Filter students by search term
  const filteredStudents = (report?.students || []).filter((s: LecturerStudentReportRow) => {
    if (!studentSearch.trim()) return true
    const q = studentSearch.toLowerCase()
    return (
      s.studentIdentifier.toLowerCase().includes(q) ||
      s.studentName.toLowerCase().includes(q) ||
      s.studentEmail.toLowerCase().includes(q)
    )
  })

  return (
    <div className="space-y-6">
      {/* Subject & Term Filter Header */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <span className="text-xs font-semibold text-emerald-600 uppercase tracking-wide">
              Faculty Class Analytics & Reporting
            </span>
            <h2 className="text-xl font-bold text-slate-900 mt-1">Subject Performance & Attendance Dossier</h2>
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => handleExport('performance')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50 transition cursor-pointer"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              <span>{exportingType === 'performance' ? 'Exporting...' : 'Marks CSV'}</span>
            </button>

            <button
              type="button"
              onClick={() => handleExport('attendance')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50 transition cursor-pointer"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              <span>{exportingType === 'attendance' ? 'Exporting...' : 'Attendance CSV'}</span>
            </button>

            <button
              type="button"
              onClick={() => handleExport('atRisk')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-amber-600 text-white hover:bg-amber-700 disabled:opacity-50 transition cursor-pointer"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              <span>{exportingType === 'atRisk' ? 'Exporting...' : 'At-Risk CSV'}</span>
            </button>
          </div>
        </div>

        {/* Filter Controls Bar */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-3 border-t border-slate-100">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Select Assigned Subject</label>
            <select
              value={selectedSubject?.id || ''}
              onChange={(e) => {
                const sub = subjects.find((s) => s.id === Number(e.target.value))
                if (sub) setSelectedSubject(sub)
              }}
              disabled={isLoadingSubjects}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            >
              {subjects.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.code} - {s.name} ({s.courseCode || 'Course'})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Academic Year</label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
              placeholder="e.g. 2025/2026"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Semester</label>
            <select
              value={semester}
              onChange={(e) => setSemester(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
            >
              <option value="1">Semester 1</option>
              <option value="2">Semester 2</option>
              <option value="SEMESTER_1">SEMESTER_1</option>
              <option value="SEMESTER_2">SEMESTER_2</option>
            </select>
          </div>
        </div>
      </div>

      {/* Provisional Policy Disclosure */}
      <div className="rounded-xl border border-amber-200 bg-amber-50/70 p-4 text-xs text-amber-900 leading-relaxed flex items-start gap-3">
        <svg className="h-5 w-5 text-amber-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <span className="font-semibold">Provisional System Defaults: </span>
          {report?.provisionalNotice || 'Grading boundaries (A: >=75%, B: >=65%, C: >=50%, S: >=40%, F: <40%) and attendance minimum (80%) are provisional defaults.'}
        </div>
      </div>

      {isLoading && (
        <div className="flex flex-col items-center justify-center p-12 space-y-4">
          <div className="w-10 h-10 border-4 border-emerald-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-slate-600">Generating class performance analytics...</p>
        </div>
      )}

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center space-y-3">
          <p className="text-sm font-semibold text-red-800">{error}</p>
          <button
            type="button"
            onClick={loadReport}
            className="px-4 py-2 bg-red-600 text-white rounded-lg text-xs font-semibold hover:bg-red-700 transition"
          >
            Retry Report
          </button>
        </div>
      )}

      {report && !isLoading && (
        <>
          {/* Class Overview KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Class Size & Assessed */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Class Enrolment</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-3xl font-extrabold text-slate-900">{report.enrolledStudentsCount}</span>
                <span className="text-xs text-slate-400">Students</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.assessedStudentsCount} assessed ({report.enrolledStudentsCount - report.assessedStudentsCount} without marks)
              </p>
            </div>

            {/* Average Score */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Class Average Score</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-3xl font-extrabold text-emerald-600">
                  {report.averageScore !== null ? `${report.averageScore.toFixed(1)}%` : '—'}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                Range: {report.lowestScore !== null ? `${report.lowestScore.toFixed(0)}%` : '—'} min to{' '}
                {report.highestScore !== null ? `${report.highestScore.toFixed(0)}%` : '—'} max
              </p>
            </div>

            {/* Pass Rate */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Academic Pass Rate</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className={`text-3xl font-extrabold ${
                  report.passRate !== null && report.passRate < 50 ? 'text-red-600' : 'text-slate-900'
                }`}>
                  {report.passRate !== null ? `${report.passRate.toFixed(1)}%` : '—'}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.passCount} passing (A/B/C/S) • {report.failCount} failing (F)
              </p>
            </div>

            {/* Attendance & Alerts */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Attendance & Risk</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className={`text-3xl font-extrabold ${
                  report.studentsBelowThresholdCount > 0 ? 'text-amber-600' : 'text-emerald-600'
                }`}>
                  {report.studentsBelowThresholdCount}
                </span>
                <span className="text-xs text-slate-400">Below 80%</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.activeAlertsCount} active risk alerts flagged
              </p>
            </div>
          </div>

          {/* Visual Grade Distribution Bar */}
          <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                Grade Distribution (Assessed: {report.assessedStudentsCount})
              </h3>
              <span className="text-[11px] text-slate-500">
                Status: {report.publicationStatus === 'PUBLISHED' ? 'Published' : 'Draft / Unreleased'}
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-6 gap-3">
              {(['A', 'B', 'C', 'S', 'W', 'F'] as const).map((grade) => {
                const count = report.gradeDistribution[grade] || 0
                const pct = report.assessedStudentsCount > 0
                  ? Math.round((count / report.assessedStudentsCount) * 100)
                  : 0

                return (
                  <div key={grade} className="rounded-lg bg-slate-50 border border-slate-200 p-3 text-center">
                    <span className={`inline-block px-2.5 py-0.5 rounded text-xs font-extrabold mb-1 ${
                      grade === 'A' ? 'bg-emerald-100 text-emerald-800' :
                      grade === 'B' ? 'bg-blue-100 text-blue-800' :
                      grade === 'C' ? 'bg-cyan-100 text-cyan-800' :
                      grade === 'S' ? 'bg-amber-100 text-amber-800' :
                      grade === 'W' ? 'bg-purple-100 text-purple-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      Grade {grade}
                    </span>
                    <div className="text-lg font-bold text-slate-900">{count}</div>
                    <div className="text-[11px] text-slate-500">{pct}%</div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="border-b border-slate-200 flex items-center gap-4">
            <button
              type="button"
              onClick={() => setActiveTab('matrix')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'matrix'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Continuous Marks Matrix ({filteredStudents.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('attendance')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'attendance'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Attendance Compliance & Sessions ({report.studentsBelowThresholdCount} flagged)
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('atRisk')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'atRisk'
                  ? 'border-emerald-600 text-emerald-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              At-Risk Students Dossier ({report.atRiskStudents.length})
            </button>
          </div>

          {/* Search Bar */}
          <div className="flex items-center gap-3">
            <div className="relative flex-1 max-w-md">
              <input
                type="text"
                value={studentSearch}
                onChange={(e) => setStudentSearch(e.target.value)}
                placeholder="Search by student ID, name, or email..."
                className="w-full rounded-lg border border-slate-300 bg-white pl-9 pr-3 py-2 text-xs font-medium text-slate-800 focus:border-emerald-500 focus:ring-1 focus:ring-emerald-500"
              />
              <svg className="h-4 w-4 text-slate-400 absolute left-3 top-2.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>

          {/* TAB 1: Marks Matrix */}
          {activeTab === 'matrix' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-xs">
                  <thead className="bg-slate-50 text-slate-600 font-semibold">
                    <tr>
                      <th className="px-3.5 py-3 text-left">Student</th>
                      {report.assessments.map((ass) => (
                        <th key={ass.id} className="px-3 py-3 text-right">
                          <div>{ass.title}</div>
                          <div className="text-[10px] text-slate-400 font-normal">
                            Max: {ass.maxMarks} ({ass.weightPercentage}%)
                          </div>
                        </th>
                      ))}
                      <th className="px-3.5 py-3 text-right font-bold text-slate-900">Total Score</th>
                      <th className="px-3 py-3 text-center">Grade</th>
                      <th className="px-3 py-3 text-right">Att. %</th>
                      <th className="px-3 py-3 text-center">Alerts</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredStudents.length === 0 ? (
                      <tr>
                        <td colSpan={report.assessments.length + 5} className="p-8 text-center text-slate-400 text-xs">
                          No students matched your search criteria.
                        </td>
                      </tr>
                    ) : (
                      filteredStudents.map((s: LecturerStudentReportRow) => (
                        <tr key={s.enrollmentId} className="hover:bg-slate-50/60 transition">
                          <td className="px-3.5 py-2.5">
                            <div className="font-semibold text-slate-900">{s.studentName}</div>
                            <div className="text-[11px] font-mono text-slate-500">{s.studentIdentifier}</div>
                          </td>

                          {report.assessments.map((ass) => {
                            const score = s.assessmentScores[ass.id]
                            return (
                              <td key={ass.id} className="px-3 py-2.5 text-right font-medium text-slate-700">
                                {score !== undefined && score !== null ? score.toFixed(1) : '—'}
                              </td>
                            )
                          })}

                          <td className="px-3.5 py-2.5 text-right font-bold text-slate-900">
                            {s.totalWeightedScore !== null ? `${s.totalWeightedScore.toFixed(1)}%` : '—'}
                          </td>

                          <td className="px-3 py-2.5 text-center">
                            {s.grade ? (
                              <span className={`px-2 py-0.5 rounded font-extrabold text-[11px] ${
                                s.grade === 'A' ? 'bg-emerald-100 text-emerald-800' :
                                s.grade === 'B' ? 'bg-blue-100 text-blue-800' :
                                s.grade === 'C' ? 'bg-cyan-100 text-cyan-800' :
                                s.grade === 'S' ? 'bg-amber-100 text-amber-800' :
                                'bg-red-100 text-red-800'
                              }`}>
                                {s.grade}
                              </span>
                            ) : '—'}
                          </td>

                          <td className="px-3 py-2.5 text-right">
                            <span className={`font-semibold ${
                              s.belowAttendanceThreshold ? 'text-amber-600' : 'text-slate-700'
                            }`}>
                              {s.attendancePercentage.toFixed(1)}%
                            </span>
                          </td>

                          <td className="px-3 py-2.5 text-center">
                            {s.activeAlertCount > 0 ? (
                              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                                {s.activeAlertCount} Alert{s.activeAlertCount > 1 ? 's' : ''}
                              </span>
                            ) : (
                              <span className="text-slate-300">—</span>
                            )}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 2: Attendance Compliance */}
          {activeTab === 'attendance' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              <div className="p-4 bg-slate-50 border-b border-slate-100 flex items-center justify-between text-xs">
                <div>
                  <span className="font-semibold text-slate-800">Conducted Sessions: </span>
                  <span className="font-bold text-slate-900">{report.totalSessionsConducted}</span>
                </div>
                <span className="text-slate-500">
                  Attendance Rule: Numerator = Present + Late, Denominator = Total - Excused
                </span>
              </div>

              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-xs">
                  <thead className="bg-slate-50 text-slate-600 font-semibold">
                    <tr>
                      <th className="px-3.5 py-3 text-left">Student ID</th>
                      <th className="px-3.5 py-3 text-left">Student Name</th>
                      <th className="px-3 py-3 text-right">Attended</th>
                      <th className="px-3 py-3 text-right">Absent</th>
                      <th className="px-3 py-3 text-right">Excused</th>
                      <th className="px-3 py-3 text-right">Eligible</th>
                      <th className="px-3.5 py-3 text-right font-bold">Attendance Rate</th>
                      <th className="px-3.5 py-3 text-center">Warning Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredStudents.map((s: LecturerStudentReportRow) => (
                      <tr key={s.enrollmentId} className="hover:bg-slate-50/60 transition">
                        <td className="px-3.5 py-2.5 font-mono text-slate-600">{s.studentIdentifier}</td>
                        <td className="px-3.5 py-2.5 font-medium text-slate-900">{s.studentName}</td>
                        <td className="px-3 py-2.5 text-right font-semibold text-emerald-700">{s.attendedSessions}</td>
                        <td className="px-3 py-2.5 text-right font-semibold text-red-600">{s.absentSessions}</td>
                        <td className="px-3 py-2.5 text-right text-slate-500">{s.excusedSessions}</td>
                        <td className="px-3 py-2.5 text-right text-slate-700">{s.eligibleSessions}</td>
                        <td className="px-3.5 py-2.5 text-right font-bold text-slate-900">
                          {s.attendancePercentage.toFixed(1)}%
                        </td>
                        <td className="px-3.5 py-2.5 text-center">
                          {s.belowAttendanceThreshold ? (
                            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-200">
                              Below 80% Advisory
                            </span>
                          ) : (
                            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                              Compliant
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 3: At-Risk Students Dossier */}
          {activeTab === 'atRisk' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              {report.atRiskStudents.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  No active risk alerts recorded for this class.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-slate-200 text-xs">
                    <thead className="bg-slate-50 text-slate-600 font-semibold">
                      <tr>
                        <th className="px-3.5 py-3 text-left">Student</th>
                        <th className="px-3 py-3 text-left">Risk Factor</th>
                        <th className="px-3 py-3 text-center">Severity</th>
                        <th className="px-3 py-3 text-center">Status</th>
                        <th className="px-4 py-3 text-left">Trigger Reason</th>
                        <th className="px-3 py-3 text-left">Recorded Action</th>
                        <th className="px-3 py-3 text-left">Reviewed By</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {report.atRiskStudents.map((alt) => (
                        <tr key={alt.alertId} className="hover:bg-slate-50/60 transition">
                          <td className="px-3.5 py-2.5">
                            <div className="font-semibold text-slate-900">{alt.studentName}</div>
                            <div className="text-[11px] font-mono text-slate-500">{alt.studentIdentifier}</div>
                          </td>
                          <td className="px-3 py-2.5 font-medium text-slate-800">
                            {alt.riskType.replace(/_/g, ' ')}
                          </td>
                          <td className="px-3 py-2.5 text-center">
                            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                              alt.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                              alt.severity === 'HIGH' ? 'bg-rose-100 text-rose-800' :
                              alt.severity === 'MEDIUM' ? 'bg-amber-100 text-amber-800' :
                              'bg-slate-100 text-slate-700'
                            }`}>
                              {alt.severity}
                            </span>
                          </td>
                          <td className="px-3 py-2.5 text-center">
                            <span className="font-medium text-slate-700">{alt.status}</span>
                          </td>
                          <td className="px-4 py-2.5 text-slate-600 max-w-xs">{alt.triggerReason}</td>
                          <td className="px-3 py-2.5 text-slate-700 italic max-w-xs">
                            {alt.actionTaken || 'Pending faculty action'}
                          </td>
                          <td className="px-3 py-2.5 text-slate-500">
                            {alt.reviewedByName || '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  )
}
