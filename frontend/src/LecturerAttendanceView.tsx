import { useState, useEffect, useCallback } from 'react'
import {
  fetchLecturerMySubjects,
  type Subject,
} from './services/academicService'
import {
  fetchSubjectAttendanceSessions,
  createAttendanceSession,
  deleteAttendanceSession,
  fetchSessionRoster,
  recordSessionAttendance,
  fetchSubjectAttendanceSummary,
  type AttendanceSession,
  type SessionRosterAttendance,
  type SubjectAttendanceSummary,
  type AttendanceStatus,
} from './services/performanceService'
import type { ApiErrorData } from './services/authService'

export default function LecturerAttendanceView() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [academicYear, setAcademicYear] = useState('2025/2026')
  const [semester, setSemester] = useState('SEMESTER_1')

  const [sessions, setSessions] = useState<AttendanceSession[]>([])
  const [summary, setSummary] = useState<SubjectAttendanceSummary | null>(null)
  const [activeSession, setActiveSession] = useState<SessionRosterAttendance | null>(null)

  const [isLoading, setIsLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // New session modal
  const [showAddSessionModal, setShowAddSessionModal] = useState(false)
  const [newSessionDate, setNewSessionDate] = useState(() => new Date().toISOString().split('T')[0])
  const [newSessionTopic, setNewSessionTopic] = useState('')

  // Roster attendance edits map: enrollmentId -> { status, remarks }
  const [rosterEdits, setRosterEdits] = useState<Record<number, { status: AttendanceStatus; remarks: string }>>({})

  // Active sub-tab: 'sessions' | 'summary'
  const [activeTab, setActiveTab] = useState<'sessions' | 'summary'>('sessions')

  useEffect(() => {
    async function loadSubjects() {
      setIsLoading(true)
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
        setIsLoading(false)
      }
    }
    loadSubjects()
  }, [])

  const loadSessionsAndSummary = useCallback(async (subjectId: number, year: string, sem: string) => {
    queueMicrotask(() => {
      setIsLoading(true)
      setError(null)
    })
    try {
      const [sessData, sumData] = await Promise.all([
        fetchSubjectAttendanceSessions(subjectId, year, sem),
        fetchSubjectAttendanceSummary(subjectId, year, sem),
      ])
      setSessions(sessData)
      setSummary(sumData)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load attendance records.')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (selectedSubject) {
      loadSessionsAndSummary(selectedSubject.id, academicYear, semester)
      queueMicrotask(() => setActiveSession(null))
    }
  }, [selectedSubject, academicYear, semester, loadSessionsAndSummary])

  const handleOpenRoster = async (sessionId: number) => {
    setActionLoading(true)
    setError(null)
    try {
      const rosterData = await fetchSessionRoster(sessionId)
      setActiveSession(rosterData)
      const initialMap: Record<number, { status: AttendanceStatus; remarks: string }> = {}
      rosterData.roster.forEach((item) => {
        initialMap[item.enrollmentId] = {
          status: item.status,
          remarks: item.remarks || '',
        }
      })
      setRosterEdits(initialMap)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load session roster.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedSubject) return

    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await createAttendanceSession({
        subjectId: selectedSubject.id,
        sessionDate: newSessionDate,
        sessionTopic: newSessionTopic.trim(),
        academicYear,
        semester,
      })
      setSuccessMsg(`Session "${newSessionTopic}" created successfully.`)
      setShowAddSessionModal(false)
      setNewSessionTopic('')
      await loadSessionsAndSummary(selectedSubject.id, academicYear, semester)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to create attendance session.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleDeleteSession = async (sessionId: number, topic: string) => {
    if (!window.confirm(`Delete session "${topic}"? All attendance marks for this session will be permanently erased.`)) {
      return
    }
    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await deleteAttendanceSession(sessionId)
      setSuccessMsg(`Session "${topic}" deleted.`)
      if (activeSession?.session.id === sessionId) {
        setActiveSession(null)
      }
      if (selectedSubject) {
        await loadSessionsAndSummary(selectedSubject.id, academicYear, semester)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to delete attendance session.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleSaveRosterAttendance = async () => {
    if (!activeSession) return

    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      const recordsToSave = Object.entries(rosterEdits).map(([enrId, edit]) => ({
        enrollmentId: Number(enrId),
        status: edit.status,
        remarks: edit.remarks,
      }))

      await recordSessionAttendance({
        sessionId: activeSession.session.id,
        records: recordsToSave,
      })

      setSuccessMsg('Session attendance recorded successfully.')
      if (selectedSubject) {
        await loadSessionsAndSummary(selectedSubject.id, academicYear, semester)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to record attendance.')
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 rounded-xl border border-emerald-200 bg-white p-5 shadow-xs">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Faculty Attendance Register</h2>
          <p className="text-xs text-slate-500">
            Log class sessions, mark attendance status (Present, Absent, Late, Excused), and track compliance against the 80% threshold.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <select
            value={academicYear}
            onChange={(e) => setAcademicYear(e.target.value)}
            className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 shadow-2xs focus:border-emerald-600 focus:outline-hidden"
          >
            <option value="2025/2026">2025/2026</option>
            <option value="2024/2025">2024/2025</option>
          </select>
          <select
            value={semester}
            onChange={(e) => setSemester(e.target.value)}
            className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 shadow-2xs focus:border-emerald-600 focus:outline-hidden"
          >
            <option value="SEMESTER_1">Semester 1</option>
            <option value="SEMESTER_2">Semester 2</option>
          </select>
        </div>
      </div>

      {/* Notifications */}
      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="font-bold text-rose-700 hover:text-rose-900 ml-4 cursor-pointer">×</button>
        </div>
      )}

      {successMsg && (
        <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-xs text-emerald-900 shadow-xs flex items-center justify-between">
          <span>{successMsg}</span>
          <button onClick={() => setSuccessMsg(null)} className="font-bold text-emerald-700 hover:text-emerald-900 ml-4 cursor-pointer">×</button>
        </div>
      )}

      {/* Subject Pills */}
      {subjects.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {subjects.map((sub) => (
            <button
              key={sub.id}
              onClick={() => setSelectedSubject(sub)}
              className={`rounded-lg px-4 py-2 text-xs font-semibold transition-all cursor-pointer ${
                selectedSubject?.id === sub.id
                  ? 'bg-emerald-700 text-white shadow-xs'
                  : 'bg-white border border-slate-200 text-slate-700 hover:border-slate-300'
              }`}
            >
              {sub.code} — {sub.name}
            </button>
          ))}
        </div>
      )}

      {selectedSubject && (
        <div className="space-y-6">
          {/* Sub-tab navigation */}
          <div className="flex border-b border-slate-200 gap-4 text-xs font-bold text-slate-600">
            <button
              onClick={() => setActiveTab('sessions')}
              className={`pb-2 border-b-2 transition-all cursor-pointer ${
                activeTab === 'sessions'
                  ? 'border-emerald-600 text-emerald-700 font-extrabold'
                  : 'border-transparent hover:text-slate-900'
              }`}
            >
              Sessions & Marking ({sessions.length})
            </button>
            <button
              onClick={() => setActiveTab('summary')}
              className={`pb-2 border-b-2 transition-all cursor-pointer ${
                activeTab === 'summary'
                  ? 'border-emerald-600 text-emerald-700 font-extrabold'
                  : 'border-transparent hover:text-slate-900'
              }`}
            >
              Subject Attendance Summary & Alerts
              {summary && summary.studentsBelowThresholdCount > 0 && (
                <span className="ml-2 rounded-full bg-rose-100 px-2 py-0.5 text-[10px] text-rose-800">
                  {summary.studentsBelowThresholdCount} at risk
                </span>
              )}
            </button>
          </div>

          {activeTab === 'sessions' && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Sessions List */}
              <div className="space-y-4 lg:col-span-1">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-bold text-slate-900">Lecture Sessions</h3>
                  <button
                    onClick={() => setShowAddSessionModal(true)}
                    className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 transition-all cursor-pointer shadow-2xs"
                  >
                    + New Session
                  </button>
                </div>

                {isLoading ? (
                  <div className="py-8 text-center text-xs text-slate-500">Loading sessions...</div>
                ) : sessions.length === 0 ? (
                  <div className="rounded-lg border border-dashed border-slate-200 p-6 text-center text-xs text-slate-500">
                    No lecture sessions recorded yet. Click "+ New Session" to begin tracking attendance.
                  </div>
                ) : (
                  <div className="space-y-2">
                    {sessions.map((sess) => (
                      <div
                        key={sess.id}
                        onClick={() => handleOpenRoster(sess.id)}
                        className={`p-3.5 rounded-xl border transition-all cursor-pointer ${
                          activeSession?.session.id === sess.id
                            ? 'border-emerald-600 bg-emerald-50/60 shadow-xs'
                            : 'border-slate-200 bg-white hover:border-slate-300'
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold text-slate-900">{sess.sessionDate}</span>
                          <button
                            onClick={(e) => {
                              e.stopPropagation()
                              handleDeleteSession(sess.id, sess.sessionTopic)
                            }}
                            className="text-[10px] text-rose-600 hover:text-rose-800 font-bold cursor-pointer"
                          >
                            Delete
                          </button>
                        </div>
                        <p className="font-medium text-xs text-slate-800 mt-1">{sess.sessionTopic}</p>
                        <p className="text-[10px] text-slate-400 mt-1">Recorded by: {sess.recordedByName}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Session Roster Marking Panel */}
              <div className="space-y-4 lg:col-span-2">
                {activeSession ? (
                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
                      <div>
                        <h3 className="text-sm font-bold text-slate-900">
                          {activeSession.session.sessionTopic}
                        </h3>
                        <p className="text-xs text-slate-500">
                          Date: {activeSession.session.sessionDate} • {selectedSubject.code}
                        </p>
                      </div>
                      <button
                        onClick={handleSaveRosterAttendance}
                        disabled={actionLoading}
                        className="rounded-lg bg-emerald-600 px-4 py-2 text-xs font-bold text-white hover:bg-emerald-700 disabled:opacity-50 transition-all cursor-pointer shadow-xs"
                      >
                        {actionLoading ? 'Saving...' : 'Save Attendance'}
                      </button>
                    </div>

                    <div className="overflow-x-auto rounded-lg border border-slate-200">
                      <table className="w-full text-left text-xs text-slate-700">
                        <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                          <tr>
                            <th className="px-3 py-2.5">Student ID</th>
                            <th className="px-3 py-2.5">Student Name</th>
                            <th className="px-3 py-2.5 text-center">Status</th>
                            <th className="px-3 py-2.5">Remarks</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {activeSession.roster.map((row) => {
                            const edit = rosterEdits[row.enrollmentId] || { status: 'PRESENT', remarks: '' }

                            return (
                              <tr key={row.enrollmentId} className="hover:bg-slate-50/50">
                                <td className="px-3 py-2 font-mono font-bold text-slate-900">
                                  {row.studentIdentifier}
                                </td>
                                <td className="px-3 py-2 font-medium text-slate-800">
                                  {row.studentName}
                                </td>
                                <td className="px-3 py-2 text-center">
                                  <div className="inline-flex rounded-lg border border-slate-200 p-0.5 bg-slate-50 text-[10px]">
                                    {(['PRESENT', 'LATE', 'EXCUSED', 'ABSENT'] as AttendanceStatus[]).map((st) => (
                                      <button
                                        key={st}
                                        type="button"
                                        onClick={() =>
                                          setRosterEdits((prev) => ({
                                            ...prev,
                                            [row.enrollmentId]: { ...edit, status: st },
                                          }))
                                        }
                                        className={`px-2 py-1 rounded-md font-bold transition-all cursor-pointer ${
                                          edit.status === st
                                            ? st === 'PRESENT'
                                              ? 'bg-emerald-600 text-white shadow-2xs'
                                              : st === 'LATE'
                                              ? 'bg-amber-500 text-white shadow-2xs'
                                              : st === 'EXCUSED'
                                              ? 'bg-blue-600 text-white shadow-2xs'
                                              : 'bg-rose-600 text-white shadow-2xs'
                                            : 'text-slate-600 hover:text-slate-900'
                                        }`}
                                      >
                                        {st === 'PRESENT' ? 'P' : st === 'LATE' ? 'L' : st === 'EXCUSED' ? 'E' : 'A'}
                                      </button>
                                    ))}
                                  </div>
                                </td>
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    value={edit.remarks}
                                    placeholder="Optional note..."
                                    onChange={(e) =>
                                      setRosterEdits((prev) => ({
                                        ...prev,
                                        [row.enrollmentId]: { ...edit, remarks: e.target.value },
                                      }))
                                    }
                                    className="w-full rounded border border-slate-200 px-2 py-1 text-xs text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                                  />
                                </td>
                              </tr>
                            )
                          })}
                        </tbody>
                      </table>
                    </div>

                    <div className="flex flex-wrap gap-4 text-[11px] text-slate-500 pt-1">
                      <span><strong>P:</strong> Present (Counted)</span>
                      <span><strong>L:</strong> Late (Counted)</span>
                      <span><strong>E:</strong> Excused (Excluded from Denominator)</span>
                      <span><strong>A:</strong> Absent (Unexcused)</span>
                    </div>
                  </div>
                ) : (
                  <div className="rounded-xl border border-dashed border-slate-200 bg-white p-12 text-center text-xs text-slate-500 shadow-xs">
                    Select a session from the list on the left to mark or view student attendance.
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'summary' && summary && (
            <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-5">
              {/* Summary Stats Overview */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 border-b border-slate-100 pb-5">
                <div className="rounded-lg bg-slate-50 p-4 border border-slate-200">
                  <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Sessions Held</p>
                  <p className="text-2xl font-black text-slate-900 mt-1">{summary.totalSessionsConducted}</p>
                </div>
                <div className="rounded-lg bg-slate-50 p-4 border border-slate-200">
                  <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Class Average</p>
                  <p className="text-2xl font-black text-emerald-700 mt-1">{summary.averageAttendancePercentage.toFixed(1)}%</p>
                </div>
                <div className="rounded-lg bg-slate-50 p-4 border border-slate-200">
                  <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Total Enrolled</p>
                  <p className="text-2xl font-black text-slate-900 mt-1">{summary.studentsCount}</p>
                </div>
                <div className="rounded-lg bg-slate-50 p-4 border border-slate-200">
                  <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Below 80% Threshold</p>
                  <p className={`text-2xl font-black mt-1 ${summary.studentsBelowThresholdCount > 0 ? 'text-rose-600' : 'text-slate-900'}`}>
                    {summary.studentsBelowThresholdCount}
                  </p>
                </div>
              </div>

              {/* Student breakdown table */}
              <div className="overflow-x-auto rounded-lg border border-slate-200">
                <table className="w-full text-left text-xs text-slate-700">
                  <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                    <tr>
                      <th className="px-4 py-3">Student ID</th>
                      <th className="px-4 py-3 text-center">Total Sessions</th>
                      <th className="px-4 py-3 text-center">Excused</th>
                      <th className="px-4 py-3 text-center">Eligible</th>
                      <th className="px-4 py-3 text-center">Attended (P+L)</th>
                      <th className="px-4 py-3 text-center">Absent</th>
                      <th className="px-4 py-3 text-center">Attendance %</th>
                      <th className="px-4 py-3 text-center">Status Flag</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {summary.studentSummaries.map((s) => (
                      <tr key={s.enrollmentId} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-mono font-bold text-slate-900">{s.subjectCode} - ID #{s.enrollmentId}</td>
                        <td className="px-4 py-3 text-center">{s.totalSessions}</td>
                        <td className="px-4 py-3 text-center text-blue-700 font-semibold">{s.excusedSessions}</td>
                        <td className="px-4 py-3 text-center font-semibold">{s.eligibleSessions}</td>
                        <td className="px-4 py-3 text-center text-emerald-700 font-bold">{s.attendedSessions}</td>
                        <td className="px-4 py-3 text-center text-rose-700 font-semibold">{s.absentSessions}</td>
                        <td className="px-4 py-3 text-center font-extrabold text-slate-900">
                          {s.attendancePercentage.toFixed(1)}%
                        </td>
                        <td className="px-4 py-3 text-center">
                          {s.belowWarningThreshold ? (
                            <span className="inline-flex rounded-full bg-rose-100 px-2.5 py-0.5 text-[10px] font-bold text-rose-800">
                              Below 80% Warning
                            </span>
                          ) : (
                            <span className="inline-flex rounded-full bg-emerald-100 px-2.5 py-0.5 text-[10px] font-bold text-emerald-800">
                              Good Standing
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="rounded-lg bg-amber-50 p-3 text-[11px] text-amber-900 border border-amber-200">
                <strong>University Attendance Regulation:</strong> Students with attendance strictly below 80.0% of eligible class sessions are ineligible to sit for final semester examinations. Medical certificates and official leaves must be logged under <em>Excused</em> to adjust the eligible denominator.
              </div>
            </div>
          )}
        </div>
      )}

      {/* Add Session Modal */}
      {showAddSessionModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">Create Attendance Session</h3>
              <button
                onClick={() => setShowAddSessionModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg cursor-pointer"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleCreateSession} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Session Date</label>
                <input
                  type="date"
                  required
                  value={newSessionDate}
                  onChange={(e) => setNewSessionDate(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Session Topic / Lesson Focus</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Lecture 4: Object-Oriented Design Patterns"
                  value={newSessionTopic}
                  onChange={(e) => setNewSessionTopic(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddSessionModal(false)}
                  className="rounded-lg border border-slate-200 px-4 py-2 font-semibold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="rounded-lg bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 disabled:opacity-50 cursor-pointer shadow-xs"
                >
                  {actionLoading ? 'Creating...' : 'Create Session'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
