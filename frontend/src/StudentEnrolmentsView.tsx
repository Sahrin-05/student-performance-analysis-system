import { useState, useEffect } from 'react'
import {
  fetchStudentMyEnrolments,
  type Enrollment,
} from './services/academicService'
import type { ApiErrorData } from './services/authService'

export default function StudentEnrolmentsView() {
  const [enrolments, setEnrolments] = useState<Enrollment[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedTerm, setSelectedTerm] = useState<string>('ALL')

  useEffect(() => {
    let ignore = false

    async function loadInitial() {
      try {
        const data = await fetchStudentMyEnrolments()
        if (!ignore) {
          setEnrolments(data)
        }
      } catch (err: unknown) {
        if (!ignore) {
          const apiErr = err as Error & ApiErrorData
          setError(apiErr.message || 'Failed to load your enrolled subjects.')
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    loadInitial()
    return () => {
      ignore = true
    }
  }, [])

  const handleRefresh = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await fetchStudentMyEnrolments()
      setEnrolments(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load your enrolled subjects.')
    } finally {
      setIsLoading(false)
    }
  }

  // Extract unique academic years / semesters for filtering
  const terms = Array.from(new Set(enrolments.map((e) => `${e.academicYear} - ${e.semester}`)))

  const filteredEnrolments = selectedTerm === 'ALL'
    ? enrolments
    : enrolments.filter((e) => `${e.academicYear} - ${e.semester}` === selectedTerm)

  const totalCredits = filteredEnrolments.reduce((acc, curr) => acc + (curr.credits || 0), 0)
  const activeEnrolmentsCount = filteredEnrolments.filter((e) => e.status === 'ACTIVE').length

  const getStatusBadge = (status: Enrollment['status']) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-semibold text-emerald-700 border border-emerald-200">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-600"></span>
            Active
          </span>
        )
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-semibold text-blue-700 border border-blue-200">
            <span className="h-1.5 w-1.5 rounded-full bg-blue-600"></span>
            Completed
          </span>
        )
      case 'DROPPED':
        return (
          <span className="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-0.5 text-xs font-semibold text-rose-700 border border-rose-200">
            <span className="h-1.5 w-1.5 rounded-full bg-rose-600"></span>
            Dropped
          </span>
        )
      default:
        return (
          <span className="inline-flex items-center rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800">
            {status}
          </span>
        )
    }
  }

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 rounded-xl border border-blue-200 bg-white p-5 shadow-xs">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Enrolled Courses & Academic Curriculum</h2>
          <p className="text-xs text-slate-500 mt-0.5">
            View your enrolled subjects, registered credit hours, and active semester schedules.
          </p>
        </div>
        <button
          type="button"
          onClick={handleRefresh}
          disabled={isLoading}
          className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 transition-colors shadow-xs self-start sm:self-auto cursor-pointer"
        >
          <svg className={`h-4 w-4 text-slate-500 ${isLoading ? 'animate-spin' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>Refresh</span>
        </button>
      </div>

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs">
          {error}
        </div>
      )}

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
          <span className="text-xs text-slate-500 font-medium">Total Subjects</span>
          <h4 className="text-2xl font-bold text-slate-900 mt-1">{filteredEnrolments.length}</h4>
          <p className="text-[11px] text-slate-500 mt-1">
            {activeEnrolmentsCount} currently active
          </p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
          <span className="text-xs text-slate-500 font-medium">Registered Credits</span>
          <h4 className="text-2xl font-bold text-blue-600 mt-1">{totalCredits}</h4>
          <p className="text-[11px] text-slate-500 mt-1">Accumulated credit weighting</p>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
          <span className="text-xs text-slate-500 font-medium">Academic Standing</span>
          <h4 className="text-2xl font-bold text-emerald-600 mt-1">Enrolled</h4>
          <p className="text-[11px] text-slate-500 mt-1">Verified student matriculation</p>
        </div>
      </div>

      {/* Main Content Card */}
      <div className="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden">
        {/* Filter bar */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-100 p-4 bg-slate-50/50">
          <h3 className="text-sm font-semibold text-slate-900">Subject Enrolment Records</h3>
          {terms.length > 1 && (
            <div className="flex items-center gap-2">
              <label htmlFor="term-filter" className="text-xs text-slate-600 font-medium">Filter Term:</label>
              <select
                id="term-filter"
                value={selectedTerm}
                onChange={(e) => setSelectedTerm(e.target.value)}
                className="rounded-lg border border-slate-300 bg-white px-2.5 py-1 text-xs text-slate-800 shadow-xs focus:border-blue-500 focus:outline-hidden"
              >
                <option value="ALL">All Terms</option>
                {terms.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>
          )}
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-16 text-xs text-slate-500 gap-2">
            <svg className="h-5 w-5 animate-spin text-blue-600" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>Loading enrolment records...</span>
          </div>
        ) : filteredEnrolments.length === 0 ? (
          <div className="py-12 px-4 text-center text-xs text-slate-500 space-y-1">
            <p className="font-semibold text-slate-700">No enrolment records found.</p>
            <p>You have not been assigned to any course subjects yet. Please check with your faculty administrator.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600">
              <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                <tr>
                  <th className="px-4 py-3">Subject Code</th>
                  <th className="px-4 py-3">Subject Title</th>
                  <th className="px-4 py-3">Degree Programme / Course</th>
                  <th className="px-4 py-3 text-center">Credits</th>
                  <th className="px-4 py-3">Academic Term</th>
                  <th className="px-4 py-3 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredEnrolments.map((enr) => (
                  <tr key={enr.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-4 py-3 font-bold text-slate-900">{enr.subjectCode}</td>
                    <td className="px-4 py-3 font-medium text-slate-800">{enr.subjectName}</td>
                    <td className="px-4 py-3 text-slate-600">
                      <div>{enr.courseName}</div>
                      <div className="text-[11px] text-slate-400">{enr.courseCode}</div>
                    </td>
                    <td className="px-4 py-3 text-center font-semibold text-slate-700">{enr.credits}</td>
                    <td className="px-4 py-3">
                      <div>{enr.academicYear}</div>
                      <div className="text-[11px] text-slate-400">{enr.semester}</div>
                    </td>
                    <td className="px-4 py-3 text-center">
                      {getStatusBadge(enr.status)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
