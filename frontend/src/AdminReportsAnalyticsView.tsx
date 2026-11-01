import { useState, useEffect, useCallback } from 'react'
import {
  fetchAdminInstitutionalReport,
  downloadAdminSummaryCsv,
  downloadAdminDepartmentComparisonCsv,
  downloadAdminSubjectComparisonCsv,
  downloadAdminRiskCsv,
  type AdminInstitutionalReportResponse,
  type DepartmentComparisonItem,
  type CourseComparisonItem,
  type SubjectComparisonItem,
} from './services/reportService'
import {
  fetchDepartments,
  fetchCourses,
  fetchSubjects,
  type Department,
  type Course,
  type Subject,
} from './services/academicService'
import type { ApiErrorData } from './services/authService'

export function AdminReportsAnalyticsView() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [courses, setCourses] = useState<Course[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])

  // Filters
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [selectedCourseId, setSelectedCourseId] = useState<string>('')
  const [selectedSubjectId, setSelectedSubjectId] = useState<string>('')
  const [academicYear, setAcademicYear] = useState<string>('')
  const [semester, setSemester] = useState<string>('')

  // Report state
  const [report, setReport] = useState<AdminInstitutionalReportResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [exportingType, setExportingType] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  // Sub-tabs for comparative tables
  const [activeTab, setActiveTab] = useState<'departments' | 'courses' | 'subjects' | 'attendance' | 'risk'>('departments')

  // Load filter options on mount
  useEffect(() => {
    async function loadFilterOptions() {
      try {
        const [depts, crs, subs] = await Promise.all([
          fetchDepartments(),
          fetchCourses(),
          fetchSubjects(),
        ])
        setDepartments(depts)
        setCourses(crs)
        setSubjects(subs)
      } catch (err: unknown) {
        console.error('Failed to load filter choices', err)
      }
    }
    loadFilterOptions()
  }, [])

  const loadReport = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await fetchAdminInstitutionalReport({
        departmentId: selectedDeptId ? Number(selectedDeptId) : undefined,
        courseId: selectedCourseId ? Number(selectedCourseId) : undefined,
        subjectId: selectedSubjectId ? Number(selectedSubjectId) : undefined,
        academicYear: academicYear.trim() || undefined,
        semester: semester.trim() || undefined,
      })
      setReport(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to generate institutional report.')
    } finally {
      setIsLoading(false)
    }
  }, [selectedDeptId, selectedCourseId, selectedSubjectId, academicYear, semester])

  useEffect(() => {
    queueMicrotask(() => {
      loadReport()
    })
  }, [loadReport])

  const handleExport = async (type: 'summary' | 'departments' | 'subjects' | 'risk') => {
    setExportingType(type)
    const filters = {
      departmentId: selectedDeptId ? Number(selectedDeptId) : undefined,
      courseId: selectedCourseId ? Number(selectedCourseId) : undefined,
      subjectId: selectedSubjectId ? Number(selectedSubjectId) : undefined,
      academicYear: academicYear.trim() || undefined,
      semester: semester.trim() || undefined,
    }
    try {
      if (type === 'summary') {
        await downloadAdminSummaryCsv(filters)
      } else if (type === 'departments') {
        await downloadAdminDepartmentComparisonCsv(filters)
      } else if (type === 'subjects') {
        await downloadAdminSubjectComparisonCsv(filters)
      } else if (type === 'risk') {
        await downloadAdminRiskCsv(filters)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      alert(apiErr.message || 'Failed to export CSV report.')
    } finally {
      setExportingType(null)
    }
  }

  const handleResetFilters = () => {
    setSelectedDeptId('')
    setSelectedCourseId('')
    setSelectedSubjectId('')
    setAcademicYear('')
    setSemester('')
  }

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div>
            <span className="text-xs font-semibold text-purple-700 bg-purple-50 border border-purple-200 px-3 py-1 rounded-full uppercase tracking-wide">
              Institutional Governance Analytics
            </span>
            <h2 className="text-xl font-bold text-slate-900 mt-2">
              University-Wide Academic Performance & Compliance Dossier
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Comparative analysis across departments, degree programs, curriculum subjects, and student cohorts
            </p>
          </div>

          {/* Export Action Buttons */}
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => handleExport('summary')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-purple-600 text-white hover:bg-purple-700 disabled:opacity-50 transition cursor-pointer"
            >
              <span>{exportingType === 'summary' ? 'Exporting...' : 'Summary CSV'}</span>
            </button>

            <button
              type="button"
              onClick={() => handleExport('departments')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50 transition cursor-pointer"
            >
              <span>{exportingType === 'departments' ? 'Exporting...' : 'Departments CSV'}</span>
            </button>

            <button
              type="button"
              onClick={() => handleExport('subjects')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50 transition cursor-pointer"
            >
              <span>{exportingType === 'subjects' ? 'Exporting...' : 'Subjects CSV'}</span>
            </button>

            <button
              type="button"
              onClick={() => handleExport('risk')}
              disabled={exportingType !== null || !report}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg bg-amber-600 text-white hover:bg-amber-700 disabled:opacity-50 transition cursor-pointer"
            >
              <span>{exportingType === 'risk' ? 'Exporting...' : 'Risk Alerts CSV'}</span>
            </button>
          </div>
        </div>

        {/* Multi-Facet Filter Bar */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-5 gap-3 pt-3 border-t border-slate-100">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Department</label>
            <select
              value={selectedDeptId}
              onChange={(e) => {
                setSelectedDeptId(e.target.value)
                setSelectedCourseId('')
                setSelectedSubjectId('')
              }}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-800 focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
            >
              <option value="">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.code} - {d.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Degree Course</label>
            <select
              value={selectedCourseId}
              onChange={(e) => {
                setSelectedCourseId(e.target.value)
                setSelectedSubjectId('')
              }}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-800 focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
            >
              <option value="">All Degree Courses</option>
              {courses
                .filter((c) => !selectedDeptId || c.departmentId === Number(selectedDeptId))
                .map((c) => (
                  <option key={c.id} value={c.id}>{c.code} - {c.name}</option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Curriculum Subject</label>
            <select
              value={selectedSubjectId}
              onChange={(e) => setSelectedSubjectId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-800 focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
            >
              <option value="">All Subjects</option>
              {subjects
                .filter((s) => !selectedCourseId || s.courseId === Number(selectedCourseId))
                .map((s) => (
                  <option key={s.id} value={s.id}>{s.code} - {s.name}</option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Academic Year</label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              placeholder="e.g. 2025/2026"
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-800 focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
            />
          </div>

          <div className="flex items-end gap-2">
            <div className="flex-1">
              <label className="block text-xs font-semibold text-slate-700 mb-1">Semester</label>
              <select
                value={semester}
                onChange={(e) => setSemester(e.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-800 focus:border-purple-500 focus:ring-1 focus:ring-purple-500"
              >
                <option value="">All Terms</option>
                <option value="1">Semester 1</option>
                <option value="2">Semester 2</option>
                <option value="SEMESTER_1">SEMESTER_1</option>
                <option value="SEMESTER_2">SEMESTER_2</option>
              </select>
            </div>
            <button
              type="button"
              onClick={handleResetFilters}
              title="Reset Filters"
              className="px-3 py-2 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-600 text-xs font-semibold transition"
            >
              Reset
            </button>
          </div>
        </div>
      </div>

      {/* Provisional Policy Disclosure Notice */}
      <div className="rounded-xl border border-purple-200 bg-purple-50/60 p-4 text-xs text-purple-900 leading-relaxed flex items-start gap-3">
        <svg className="h-5 w-5 text-purple-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div>
          <span className="font-semibold">Provisional System Defaults: </span>
          {report?.provisionalNotice || 'All statistical benchmarks and grading thresholds (A: >=75%, B: >=65%, C: >=50%, S: >=40%, F: <40%) and attendance minimum (80%) are provisional system defaults.'}
        </div>
      </div>

      {isLoading && (
        <div className="flex flex-col items-center justify-center p-12 space-y-4">
          <div className="w-10 h-10 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-slate-600">Aggregating university performance metrics...</p>
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
            Retry Aggregation
          </button>
        </div>
      )}

      {report && !isLoading && (
        <>
          {/* Institutional KPI Cards Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Cohort & Capacity */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Institution Size</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-3xl font-extrabold text-slate-900">{report.totalStudents}</span>
                <span className="text-xs text-slate-400">Students</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.totalLecturers} Faculty • {report.totalDepartments} Depts • {report.totalCourses} Courses
              </p>
            </div>

            {/* Average Performance */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Institutional Mean Score</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-3xl font-extrabold text-purple-700">
                  {report.averageScore !== null ? `${report.averageScore.toFixed(1)}%` : '—'}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.overallPassRate !== null ? `${report.overallPassRate.toFixed(1)}% overall pass rate` : 'No mark records'}
              </p>
            </div>

            {/* Attendance Clearance */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Campus Attendance Rate</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className={`text-3xl font-extrabold ${
                  report.overallAttendanceAverage !== null && report.overallAttendanceAverage < 80
                    ? 'text-amber-600'
                    : 'text-emerald-600'
                }`}>
                  {report.overallAttendanceAverage !== null ? `${report.overallAttendanceAverage.toFixed(1)}%` : '100%'}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.studentsBelowThresholdCount} student enrollments below 80% warning
              </p>
            </div>

            {/* Active Risk Alerts */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Active Risk Alerts</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className={`text-3xl font-extrabold ${
                  report.totalActiveAlerts > 0 ? 'text-amber-600' : 'text-slate-900'
                }`}>
                  {report.totalActiveAlerts}
                </span>
                <span className="text-xs text-slate-400">Total Flagged</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                {report.publishedSubjectsCount} of {report.totalSubjects} subjects published
              </p>
            </div>
          </div>

          {/* Visual Grade Distribution & Attendance Brackets */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {/* Grade Distribution */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                Institutional Grade Breakdown
              </h3>
              <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                {(['A', 'B', 'C', 'S', 'W', 'F'] as const).map((grade) => {
                  const count = report.gradeDistribution[grade] || 0
                  return (
                    <div key={grade} className="rounded-lg bg-slate-50 border border-slate-200 p-2.5 text-center">
                      <span className={`inline-block px-2 py-0.5 rounded text-[11px] font-extrabold mb-1 ${
                        grade === 'A' ? 'bg-emerald-100 text-emerald-800' :
                        grade === 'B' ? 'bg-blue-100 text-blue-800' :
                        grade === 'C' ? 'bg-cyan-100 text-cyan-800' :
                        grade === 'S' ? 'bg-amber-100 text-amber-800' :
                        grade === 'W' ? 'bg-purple-100 text-purple-800' :
                        'bg-red-100 text-red-800'
                      }`}>
                        {grade}
                      </span>
                      <div className="text-base font-bold text-slate-900">{count}</div>
                    </div>
                  )
                })}
              </div>
            </div>

            {/* Attendance Brackets */}
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
              <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                Student Attendance Brackets
              </h3>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                <div className="rounded-lg bg-emerald-50 border border-emerald-200 p-2.5 text-center">
                  <span className="text-[11px] font-bold text-emerald-800 block">≥ 90% (Excellent)</span>
                  <div className="text-base font-extrabold text-emerald-900 mt-1">
                    {report.attendanceBrackets.excellentCount}
                  </div>
                </div>

                <div className="rounded-lg bg-blue-50 border border-blue-200 p-2.5 text-center">
                  <span className="text-[11px] font-bold text-blue-800 block">80-89% (Good)</span>
                  <div className="text-base font-extrabold text-blue-900 mt-1">
                    {report.attendanceBrackets.goodCount}
                  </div>
                </div>

                <div className="rounded-lg bg-amber-50 border border-amber-200 p-2.5 text-center">
                  <span className="text-[11px] font-bold text-amber-800 block">70-79% (Warning)</span>
                  <div className="text-base font-extrabold text-amber-900 mt-1">
                    {report.attendanceBrackets.warningCount}
                  </div>
                </div>

                <div className="rounded-lg bg-red-50 border border-red-200 p-2.5 text-center">
                  <span className="text-[11px] font-bold text-red-800 block">&lt; 70% (Critical)</span>
                  <div className="text-base font-extrabold text-red-900 mt-1">
                    {report.attendanceBrackets.criticalCount}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Comparative Reports Tab Navigation */}
          <div className="border-b border-slate-200 flex items-center gap-4">
            <button
              type="button"
              onClick={() => setActiveTab('departments')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'departments'
                  ? 'border-purple-600 text-purple-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Department Comparisons ({report.departmentComparisons.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('courses')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'courses'
                  ? 'border-purple-600 text-purple-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Degree Course Comparisons ({report.courseComparisons.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('subjects')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'subjects'
                  ? 'border-purple-600 text-purple-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Subject Comparisons ({report.subjectComparisons.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('risk')}
              className={`pb-3 text-xs font-bold border-b-2 transition cursor-pointer ${
                activeTab === 'risk'
                  ? 'border-purple-600 text-purple-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              Risk Alerts Distribution
            </button>
          </div>

          {/* TAB 1: Department Comparisons Table */}
          {activeTab === 'departments' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-xs">
                  <thead className="bg-slate-50 text-slate-600 font-semibold">
                    <tr>
                      <th className="px-4 py-3 text-left">Code</th>
                      <th className="px-4 py-3 text-left">Department Name</th>
                      <th className="px-3 py-3 text-right">Courses</th>
                      <th className="px-3 py-3 text-right">Subjects</th>
                      <th className="px-3 py-3 text-right">Enrollments</th>
                      <th className="px-3 py-3 text-right">Avg Score</th>
                      <th className="px-3 py-3 text-right">Pass Rate</th>
                      <th className="px-3 py-3 text-right">Avg Att. %</th>
                      <th className="px-3 py-3 text-center">Active Alerts</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {report.departmentComparisons.length === 0 ? (
                      <tr>
                        <td colSpan={9} className="p-8 text-center text-slate-400">
                          No department comparison data found for the selected filters.
                        </td>
                      </tr>
                    ) : (
                      report.departmentComparisons.map((d: DepartmentComparisonItem) => (
                        <tr key={d.departmentId} className="hover:bg-slate-50/60 transition">
                          <td className="px-4 py-2.5 font-mono font-bold text-purple-700">{d.code}</td>
                          <td className="px-4 py-2.5 font-medium text-slate-900">{d.name}</td>
                          <td className="px-3 py-2.5 text-right text-slate-600">{d.courseCount}</td>
                          <td className="px-3 py-2.5 text-right text-slate-600">{d.subjectCount}</td>
                          <td className="px-3 py-2.5 text-right font-semibold text-slate-800">{d.enrollmentCount}</td>
                          <td className="px-3 py-2.5 text-right font-bold text-slate-900">
                            {d.averageScore !== null ? `${d.averageScore.toFixed(1)}%` : '—'}
                          </td>
                          <td className="px-3 py-2.5 text-right font-semibold text-emerald-700">
                            {d.passRate !== null ? `${d.passRate.toFixed(1)}%` : '—'}
                          </td>
                          <td className="px-3 py-2.5 text-right text-slate-700">
                            {d.averageAttendanceRate !== null ? `${d.averageAttendanceRate.toFixed(1)}%` : '—'}
                          </td>
                          <td className="px-3 py-2.5 text-center">
                            {d.activeAlertsCount > 0 ? (
                              <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                                {d.activeAlertsCount}
                              </span>
                            ) : (
                              <span className="text-slate-300">0</span>
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

          {/* TAB 2: Course Comparisons Table */}
          {activeTab === 'courses' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-xs">
                  <thead className="bg-slate-50 text-slate-600 font-semibold">
                    <tr>
                      <th className="px-4 py-3 text-left">Course Code</th>
                      <th className="px-4 py-3 text-left">Course Title</th>
                      <th className="px-4 py-3 text-left">Department</th>
                      <th className="px-3 py-3 text-right">Subjects</th>
                      <th className="px-3 py-3 text-right">Enrollments</th>
                      <th className="px-3 py-3 text-right">Avg Score</th>
                      <th className="px-3 py-3 text-right">Pass Rate</th>
                      <th className="px-3 py-3 text-right">Avg Attendance</th>
                      <th className="px-3 py-3 text-center">Active Alerts</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {report.courseComparisons.map((c: CourseComparisonItem) => (
                      <tr key={c.courseId} className="hover:bg-slate-50/60 transition">
                        <td className="px-4 py-2.5 font-mono font-bold text-blue-700">{c.code}</td>
                        <td className="px-4 py-2.5 font-medium text-slate-900">{c.name}</td>
                        <td className="px-4 py-2.5 text-slate-600">{c.departmentName}</td>
                        <td className="px-3 py-2.5 text-right text-slate-600">{c.subjectCount}</td>
                        <td className="px-3 py-2.5 text-right font-semibold text-slate-800">{c.enrollmentCount}</td>
                        <td className="px-3 py-2.5 text-right font-bold text-slate-900">
                          {c.averageScore !== null ? `${c.averageScore.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-right font-semibold text-emerald-700">
                          {c.passRate !== null ? `${c.passRate.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-right text-slate-700">
                          {c.averageAttendanceRate !== null ? `${c.averageAttendanceRate.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-center">
                          {c.activeAlertsCount > 0 ? (
                            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                              {c.activeAlertsCount}
                            </span>
                          ) : (
                            <span className="text-slate-300">0</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 3: Subject Comparisons Table */}
          {activeTab === 'subjects' && (
            <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-200 text-xs">
                  <thead className="bg-slate-50 text-slate-600 font-semibold">
                    <tr>
                      <th className="px-4 py-3 text-left">Code</th>
                      <th className="px-4 py-3 text-left">Subject Name</th>
                      <th className="px-3 py-3 text-left">Course</th>
                      <th className="px-3 py-3 text-right">Credits</th>
                      <th className="px-3 py-3 text-right">Enrolled</th>
                      <th className="px-3 py-3 text-center">Publication</th>
                      <th className="px-3 py-3 text-right">Avg Score</th>
                      <th className="px-3 py-3 text-right">Pass Rate</th>
                      <th className="px-3 py-3 text-right">Avg Att. %</th>
                      <th className="px-3 py-3 text-center">At-Risk</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {report.subjectComparisons.map((s: SubjectComparisonItem) => (
                      <tr key={s.subjectId} className="hover:bg-slate-50/60 transition">
                        <td className="px-4 py-2.5 font-mono font-bold text-slate-800">{s.code}</td>
                        <td className="px-4 py-2.5 font-medium text-slate-900">{s.name}</td>
                        <td className="px-3 py-2.5 text-slate-600">{s.courseCode}</td>
                        <td className="px-3 py-2.5 text-right text-slate-600">{s.credits}</td>
                        <td className="px-3 py-2.5 text-right font-semibold text-slate-800">{s.enrollmentCount}</td>
                        <td className="px-3 py-2.5 text-center">
                          <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            s.publicationStatus === 'PUBLISHED'
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-slate-100 text-slate-600'
                          }`}>
                            {s.publicationStatus}
                          </span>
                        </td>
                        <td className="px-3 py-2.5 text-right font-bold text-slate-900">
                          {s.averageScore !== null ? `${s.averageScore.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-right font-semibold text-emerald-700">
                          {s.passRate !== null ? `${s.passRate.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-right text-slate-700">
                          {s.averageAttendanceRate !== null ? `${s.averageAttendanceRate.toFixed(1)}%` : '—'}
                        </td>
                        <td className="px-3 py-2.5 text-center">
                          {s.atRiskCount > 0 ? (
                            <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                              {s.atRiskCount}
                            </span>
                          ) : (
                            <span className="text-slate-300">0</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 4: Risk Distribution Details */}
          {activeTab === 'risk' && (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              {/* By Risk Factor */}
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
                <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider">By Risk Factor</h4>
                <div className="space-y-2 text-xs">
                  {Object.entries(report.riskAlertsSummary.byRiskType).map(([type, count]) => (
                    <div key={type} className="flex items-center justify-between p-2 rounded bg-slate-50">
                      <span className="font-medium text-slate-700">{type.replace(/_/g, ' ')}</span>
                      <span className="font-bold text-slate-900">{count}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* By Severity */}
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
                <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider">By Severity Level</h4>
                <div className="space-y-2 text-xs">
                  {Object.entries(report.riskAlertsSummary.bySeverity).map(([sev, count]) => (
                    <div key={sev} className="flex items-center justify-between p-2 rounded bg-slate-50">
                      <span className={`font-semibold ${
                        sev === 'CRITICAL' ? 'text-red-700' :
                        sev === 'HIGH' ? 'text-rose-700' :
                        sev === 'MEDIUM' ? 'text-amber-700' : 'text-slate-700'
                      }`}>
                        {sev}
                      </span>
                      <span className="font-bold text-slate-900">{count}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* By Status */}
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-3">
                <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider">By Lifecycle Status</h4>
                <div className="space-y-2 text-xs">
                  {Object.entries(report.riskAlertsSummary.byStatus).map(([st, count]) => (
                    <div key={st} className="flex items-center justify-between p-2 rounded bg-slate-50">
                      <span className="font-medium text-slate-700">{st.replace(/_/g, ' ')}</span>
                      <span className="font-bold text-slate-900">{count}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
