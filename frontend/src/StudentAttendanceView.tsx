import { useState, useEffect } from 'react'
import {
  fetchStudentMyEnrolments,
  type Enrollment,
} from './services/academicService'
import {
  fetchMySubjectAttendance,
  type StudentAttendanceSummary,
} from './services/performanceService'
import type { ApiErrorData } from './services/authService'

export default function StudentAttendanceView() {
  const [enrolments, setEnrolments] = useState<Enrollment[]>([])
  const [selectedEnrolment, setSelectedEnrolment] = useState<Enrollment | null>(null)
  const [attendance, setAttendance] = useState<StudentAttendanceSummary | null>(null)

  const [isLoadingEnrolments, setIsLoadingEnrolments] = useState(true)
  const [isLoadingAttendance, setIsLoadingAttendance] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadEnrolments() {
      setIsLoadingEnrolments(true)
      try {
        const data = await fetchStudentMyEnrolments()
        setEnrolments(data)
        if (data.length > 0) {
          setSelectedEnrolment(data[0])
        }
      } catch (err: unknown) {
        const apiErr = err as Error & ApiErrorData
        setError(apiErr.message || 'Failed to load your enrolled subjects.')
      } finally {
        setIsLoadingEnrolments(false)
      }
    }
    loadEnrolments()
  }, [])

  useEffect(() => {
    async function loadAttendance() {
      if (!selectedEnrolment) return
      setIsLoadingAttendance(true)
      setError(null)
      try {
        const data = await fetchMySubjectAttendance(selectedEnrolment.id)
        setAttendance(data)
      } catch (err: unknown) {
        const apiErr = err as Error & ApiErrorData
        setError(apiErr.message || 'Failed to fetch attendance summary.')
      } finally {
        setIsLoadingAttendance(false)
      }
    }
    loadAttendance()
  }, [selectedEnrolment])

  const isBelowThreshold = attendance?.belowWarningThreshold ?? false

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="rounded-xl border border-emerald-200 bg-white p-5 shadow-xs">
        <h2 className="text-xl font-bold tracking-tight text-slate-900">Student Attendance & Exam Eligibility</h2>
        <p className="text-xs text-slate-500">
          Track your attendance percentage across registered subjects to maintain regular standing and examination clearance.
        </p>
      </div>

      {/* Error banner */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="font-bold text-rose-700 hover:text-rose-900 ml-4 cursor-pointer">×</button>
        </div>
      )}

      {isLoadingEnrolments ? (
        <div className="py-16 text-center text-xs text-slate-500">Loading your enrolled subjects...</div>
      ) : enrolments.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-xs text-slate-500 shadow-xs">
          No registered subjects found for your student account.
        </div>
      ) : (
        <div className="space-y-6">
          {/* Subject Pills */}
          <div className="flex flex-wrap gap-2">
            {enrolments.map((enr) => (
              <button
                key={enr.id}
                onClick={() => setSelectedEnrolment(enr)}
                className={`rounded-lg px-4 py-2 text-xs font-semibold transition-all cursor-pointer ${
                  selectedEnrolment?.id === enr.id
                    ? 'bg-emerald-700 text-white shadow-xs'
                    : 'bg-white border border-slate-200 text-slate-700 hover:border-slate-300'
                }`}
              >
                {enr.subjectCode} — {enr.subjectName}
              </button>
            ))}
          </div>

          {selectedEnrolment && (
            <div className="space-y-6">
              {isLoadingAttendance ? (
                <div className="py-12 text-center text-xs text-slate-500">Calculating attendance metrics...</div>
              ) : attendance && (
                <>
                  {/* Strict Warning Banner if < 80.0% */}
                  {isBelowThreshold ? (
                    <div className="rounded-xl border border-rose-300 bg-rose-50 p-5 shadow-xs space-y-2">
                      <div className="flex items-center gap-2.5">
                        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-rose-600 text-white font-bold text-xs">!</span>
                        <h3 className="text-sm font-bold text-rose-900">
                          Critical Attendance Alert: In Danger of Examination Debarment
                        </h3>
                      </div>
                      <p className="text-xs text-rose-800 leading-relaxed">
                        Your attendance in <strong>{attendance.subjectCode}</strong> is currently{' '}
                        <strong className="underline decoration-rose-500">{attendance.attendancePercentage.toFixed(1)}%</strong>, which is strictly below the required{' '}
                        <strong>80.0%</strong> institutional eligibility threshold. You must attend upcoming lectures regularly or submit valid documentation for excused absences.
                      </p>
                    </div>
                  ) : (
                    <div className="rounded-xl border border-emerald-300 bg-emerald-50/80 p-5 shadow-xs space-y-2">
                      <div className="flex items-center gap-2.5">
                        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-emerald-600 text-white font-bold text-xs">✓</span>
                        <h3 className="text-sm font-bold text-emerald-900">
                          Good Standing: Exam Admission Eligible ({attendance.attendancePercentage.toFixed(1)}%)
                        </h3>
                      </div>
                      <p className="text-xs text-emerald-800 leading-relaxed">
                        Your attendance in <strong>{attendance.subjectCode}</strong> meets or exceeds the minimum 80.0% regulatory threshold.
                      </p>
                    </div>
                  )}

                  {/* Attendance Stats Cards */}
                  <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Percentage</p>
                      <p className={`text-2xl font-black mt-1 ${isBelowThreshold ? 'text-rose-600' : 'text-emerald-700'}`}>
                        {attendance.attendancePercentage.toFixed(1)}%
                      </p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Total Held</p>
                      <p className="text-2xl font-black text-slate-900 mt-1">{attendance.totalSessions}</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Excused</p>
                      <p className="text-2xl font-black text-blue-700 mt-1">{attendance.excusedSessions}</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Eligible (Denom.)</p>
                      <p className="text-2xl font-black text-slate-900 mt-1">{attendance.eligibleSessions}</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Attended (P+L)</p>
                      <p className="text-2xl font-black text-emerald-700 mt-1">{attendance.attendedSessions}</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-slate-500">Unexcused Absent</p>
                      <p className="text-2xl font-black text-rose-600 mt-1">{attendance.absentSessions}</p>
                    </div>
                  </div>

                  {/* Attendance Log Table */}
                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                    <h3 className="text-sm font-bold text-slate-900">Session Attendance Log</h3>

                    {attendance.records.length === 0 ? (
                      <div className="rounded-lg border border-dashed border-slate-200 p-8 text-center text-xs text-slate-500">
                        No individual class session records have been logged yet for this subject.
                      </div>
                    ) : (
                      <div className="overflow-x-auto rounded-lg border border-slate-200">
                        <table className="w-full text-left text-xs text-slate-700">
                          <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                            <tr>
                              <th className="px-4 py-3">Date</th>
                              <th className="px-4 py-3">Session Topic</th>
                              <th className="px-4 py-3 text-center">Your Status</th>
                              <th className="px-4 py-3">Remarks / Medical Note</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {attendance.records.map((rec) => (
                              <tr key={rec.id} className="hover:bg-slate-50/50">
                                <td className="px-4 py-3 font-semibold text-slate-900">{rec.sessionDate}</td>
                                <td className="px-4 py-3 text-slate-800 font-medium">{rec.sessionTopic}</td>
                                <td className="px-4 py-3 text-center">
                                  <span
                                    className={`inline-block rounded-md px-2.5 py-0.5 text-[11px] font-bold ${
                                      rec.status === 'PRESENT'
                                        ? 'bg-emerald-100 text-emerald-800'
                                        : rec.status === 'LATE'
                                        ? 'bg-amber-100 text-amber-800'
                                        : rec.status === 'EXCUSED'
                                        ? 'bg-blue-100 text-blue-800'
                                        : 'bg-rose-100 text-rose-800'
                                    }`}
                                  >
                                    {rec.status}
                                  </span>
                                </td>
                                <td className="px-4 py-3 text-slate-500 italic">
                                  {rec.remarks || '—'}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>

                  {/* Formula Context */}
                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-xs text-slate-600 space-y-1">
                    <p className="font-bold text-slate-800">Attendance Calculation Formula:</p>
                    <p className="font-mono text-emerald-800 font-bold bg-white p-2 rounded border border-slate-200 inline-block">
                      Attendance % = (Present + Late) / (Total Sessions - Excused Sessions) × 100%
                    </p>
                    <p className="text-[11px] text-slate-500 mt-1">
                      Excused sessions are deducted from the eligible denominator so that approved absences do not penalize your standing.
                    </p>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
