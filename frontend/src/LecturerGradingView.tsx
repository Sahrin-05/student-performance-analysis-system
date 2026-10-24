import { useState, useEffect, useCallback } from 'react'
import {
  fetchLecturerMySubjects,
  type Subject,
} from './services/academicService'
import {
  fetchSubjectMarksRoster,
  createAssessment,
  deleteAssessment,
  enterOrUpdateMark,
  publishSubjectResults,
  type SubjectRosterMarks,
  type AssessmentType,
} from './services/performanceService'
import type { ApiErrorData } from './services/authService'

export default function LecturerGradingView() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [academicYear, setAcademicYear] = useState('2025/2026')
  const [semester, setSemester] = useState('SEMESTER_1')

  const [roster, setRoster] = useState<SubjectRosterMarks | null>(null)
  const [isLoadingSubjects, setIsLoadingSubjects] = useState(true)
  const [isLoadingRoster, setIsLoadingRoster] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)

  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Assessment creation form modal
  const [showAddModal, setShowAddModal] = useState(false)
  const [newTitle, setNewTitle] = useState('')
  const [newType, setNewType] = useState<AssessmentType>('QUIZ')
  const [newMaxMarks, setNewMaxMarks] = useState('100')
  const [newWeight, setNewWeight] = useState('20')

  // Inline edit marks map: enrollmentId_assessmentId -> score string
  const [editingScores, setEditingScores] = useState<Record<string, string>>({})

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

  const loadRoster = useCallback(async (subjectId: number, year: string, sem: string) => {
    queueMicrotask(() => {
      setIsLoadingRoster(true)
      setError(null)
    })
    try {
      const data = await fetchSubjectMarksRoster(subjectId, year, sem)
      setRoster(data)
      // Initialize editing scores
      const initialMap: Record<string, string> = {}
      data.students.forEach((s) => {
        data.assessments.forEach((a) => {
          const mark = s.marks[a.id]
          if (mark) {
            initialMap[`${s.enrollmentId}_${a.id}`] = mark.score.toString()
          }
        })
      })
      setEditingScores(initialMap)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load subject marks roster.')
    } finally {
      setIsLoadingRoster(false)
    }
  }, [])

  useEffect(() => {
    if (selectedSubject) {
      loadRoster(selectedSubject.id, academicYear, semester)
    }
  }, [selectedSubject, academicYear, semester, loadRoster])

  const handleCreateAssessment = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedSubject) return

    const max = parseFloat(newMaxMarks)
    const weight = parseFloat(newWeight)

    if (isNaN(max) || max <= 0) {
      setError('Max marks must be greater than 0.')
      return
    }
    if (isNaN(weight) || weight <= 0 || weight > 100) {
      setError('Weight percentage must be between 0.01% and 100%.')
      return
    }

    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await createAssessment({
        subjectId: selectedSubject.id,
        academicYear,
        semester,
        title: newTitle.trim(),
        type: newType,
        maxMarks: max,
        weightPercentage: weight,
      })
      setSuccessMsg(`Assessment "${newTitle}" created successfully.`)
      setShowAddModal(false)
      setNewTitle('')
      setNewMaxMarks('100')
      setNewWeight('20')
      await loadRoster(selectedSubject.id, academicYear, semester)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to create assessment.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleDeleteAssessment = async (assessmentId: number, title: string) => {
    if (!window.confirm(`Are you sure you want to delete assessment "${title}"? All entered student marks for it will be removed.`)) {
      return
    }
    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await deleteAssessment(assessmentId)
      setSuccessMsg(`Assessment "${title}" deleted successfully.`)
      if (selectedSubject) {
        await loadRoster(selectedSubject.id, academicYear, semester)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to delete assessment.')
    } finally {
      setActionLoading(false)
    }
  }

  const handleScoreChange = (enrollmentId: number, assessmentId: number, value: string) => {
    setEditingScores((prev) => ({
      ...prev,
      [`${enrollmentId}_${assessmentId}`]: value,
    }))
  }

  const handleSaveMark = async (enrollmentId: number, assessmentId: number, maxMarks: number) => {
    const rawVal = editingScores[`${enrollmentId}_${assessmentId}`]
    if (rawVal === undefined || rawVal.trim() === '') return

    const numVal = parseFloat(rawVal)
    if (isNaN(numVal) || numVal < 0 || numVal > maxMarks) {
      setError(`Score must be between 0 and maximum marks (${maxMarks}).`)
      return
    }

    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await enterOrUpdateMark({
        assessmentId,
        enrollmentId,
        score: numVal,
      })
      setSuccessMsg('Mark updated successfully.')
      if (selectedSubject) {
        await loadRoster(selectedSubject.id, academicYear, semester)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to save student mark.')
    } finally {
      setActionLoading(false)
    }
  }

  const handlePublishResults = async () => {
    if (!selectedSubject || !roster) return

    if (!window.confirm(
      'Publishing will calculate final course grades and make them immediately visible to all enrolled students. Once published, marks are locked and can only be altered with an administrative override. Do you wish to publish?'
    )) {
      return
    }

    setActionLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      await publishSubjectResults({
        subjectId: selectedSubject.id,
        academicYear,
        semester,
      })
      setSuccessMsg('Academic results published successfully! Students can now view their grades.')
      await loadRoster(selectedSubject.id, academicYear, semester)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to publish results.')
    } finally {
      setActionLoading(false)
    }
  }

  const totalWeight = roster?.totalWeightConfigured ?? 0
  const isPublished = roster?.publicationStatus === 'PUBLISHED'

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 rounded-xl border border-emerald-200 bg-white p-5 shadow-xs">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Lecturer Assessment & Marks Management</h2>
          <p className="text-xs text-slate-500">
            Define syllabus assessments, input student marks, calculate provisional grades, and publish finalized results.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {/* Academic Year select */}
          <select
            value={academicYear}
            onChange={(e) => setAcademicYear(e.target.value)}
            className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 shadow-2xs focus:border-emerald-600 focus:outline-hidden"
          >
            <option value="2025/2026">2025/2026</option>
            <option value="2024/2025">2024/2025</option>
          </select>
          {/* Semester select */}
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

      {isLoadingSubjects ? (
        <div className="py-16 text-center text-xs text-slate-500">Loading faculty teaching assignments...</div>
      ) : subjects.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-xs text-slate-500 shadow-xs">
          No teaching assignments found. You must be assigned to a course subject before you can manage assessments or marks.
        </div>
      ) : (
        <div className="space-y-6">
          {/* Subject Pills */}
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

          {selectedSubject && (
            <>
              {/* Publication Status & Weight Progress Card */}
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-bold text-slate-900">Results Publication Status:</span>
                    {isPublished ? (
                      <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800">
                        <span className="h-2 w-2 rounded-full bg-emerald-600"></span>
                        PUBLISHED & LOCKED
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3 py-1 text-xs font-bold text-amber-800">
                        <span className="h-2 w-2 rounded-full bg-amber-600"></span>
                        DRAFT (HIDDEN FROM STUDENTS)
                      </span>
                    )}
                  </div>

                  <div className="flex items-center gap-2">
                    {!isPublished ? (
                      <button
                        onClick={handlePublishResults}
                        disabled={actionLoading || !roster?.canPublish}
                        title={
                          roster?.canPublish
                            ? 'Publish results to enrolled students'
                            : `Total weight must be exactly 100.0% (currently ${totalWeight}%) to publish.`
                        }
                        className={`rounded-lg px-4 py-2 text-xs font-bold text-white transition-all cursor-pointer ${
                          roster?.canPublish
                            ? 'bg-emerald-600 hover:bg-emerald-700 shadow-xs'
                            : 'bg-slate-300 cursor-not-allowed text-slate-500'
                        }`}
                      >
                        Publish Final Results
                      </button>
                    ) : (
                      <div className="text-xs text-slate-500 italic">
                        Marks locked. To adjust marks, request an Administrative Unlock.
                      </div>
                    )}
                  </div>
                </div>

                {/* Weight bar */}
                <div>
                  <div className="flex justify-between text-xs font-medium text-slate-600 mb-1.5">
                    <span>Configured Assessment Weight: <strong>{totalWeight.toFixed(1)}%</strong> / 100.0%</span>
                    <span>{100 - totalWeight > 0 ? `${(100 - totalWeight).toFixed(1)}% remaining to publish` : 'Full 100% allocation met'}</span>
                  </div>
                  <div className="h-2.5 w-full rounded-full bg-slate-100 overflow-hidden">
                    <div
                      className={`h-full transition-all duration-300 ${
                        totalWeight === 100
                          ? 'bg-emerald-600'
                          : totalWeight > 100
                          ? 'bg-rose-500'
                          : 'bg-blue-600'
                      }`}
                      style={{ width: `${Math.min(totalWeight, 100)}%` }}
                    ></div>
                  </div>
                  <p className="mt-1.5 text-[11px] text-slate-400">
                    Draft assessments can be configured with any weight $\le 100\%$. However, exactly 100.0% total weight is strictly required to publish results.
                  </p>
                </div>
              </div>

              {/* Assessment Scheme Section */}
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-sm font-bold text-slate-900">Assessments Scheme</h3>
                  {!isPublished && (
                    <button
                      onClick={() => setShowAddModal(true)}
                      className="rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 transition-all cursor-pointer shadow-2xs"
                    >
                      + Add Assessment
                    </button>
                  )}
                </div>

                {isLoadingRoster ? (
                  <div className="py-8 text-center text-xs text-slate-500">Loading scheme and students...</div>
                ) : roster?.assessments.length === 0 ? (
                  <div className="rounded-lg border border-dashed border-slate-200 p-6 text-center text-xs text-slate-500">
                    No assessments defined yet for this subject and semester. Click "+ Add Assessment" above to define quizzes, assignments, midterms, or exams.
                  </div>
                ) : (
                  <div className="overflow-x-auto rounded-lg border border-slate-200">
                    <table className="w-full text-left text-xs text-slate-700">
                      <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                        <tr>
                          <th className="px-4 py-3">Title</th>
                          <th className="px-4 py-3">Type</th>
                          <th className="px-4 py-3">Max Marks</th>
                          <th className="px-4 py-3">Weight</th>
                          <th className="px-4 py-3">Created By</th>
                          {!isPublished && <th className="px-4 py-3 text-right">Actions</th>}
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {roster?.assessments.map((ass) => (
                          <tr key={ass.id} className="hover:bg-slate-50/50">
                            <td className="px-4 py-3 font-semibold text-slate-900">{ass.title}</td>
                            <td className="px-4 py-3">
                              <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[10px] font-bold text-slate-700">
                                {ass.type}
                              </span>
                            </td>
                            <td className="px-4 py-3 font-medium">{ass.maxMarks}</td>
                            <td className="px-4 py-3 font-bold text-emerald-700">{ass.weightPercentage}%</td>
                            <td className="px-4 py-3 text-slate-500">{ass.creatorName}</td>
                            {!isPublished && (
                              <td className="px-4 py-3 text-right">
                                <button
                                  onClick={() => handleDeleteAssessment(ass.id, ass.title)}
                                  disabled={actionLoading}
                                  className="text-[11px] font-bold text-rose-600 hover:text-rose-800 hover:underline cursor-pointer"
                                >
                                  Delete
                                </button>
                              </td>
                            )}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              {/* Student Marks Matrix */}
              {roster && roster.assessments.length > 0 && (
                <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-slate-900">Student Marks Matrix</h3>
                      <p className="text-[11px] text-slate-500">
                        Enter raw scores out of each assessment's maximum mark. Total weighted contribution and projected grade update automatically.
                      </p>
                    </div>
                  </div>

                  {roster.students.length === 0 ? (
                    <div className="rounded-lg border border-dashed border-slate-200 p-8 text-center text-xs text-slate-500">
                      No active student enrolments found for this subject and semester.
                    </div>
                  ) : (
                    <div className="overflow-x-auto rounded-lg border border-slate-200">
                      <table className="w-full text-left text-xs text-slate-700">
                        <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                          <tr>
                            <th className="px-4 py-3">Student ID</th>
                            <th className="px-4 py-3">Name</th>
                            <th className="px-4 py-3">Status</th>
                            {roster.assessments.map((a) => (
                              <th key={a.id} className="px-3 py-3 text-center">
                                <div>{a.title}</div>
                                <div className="text-[10px] text-slate-400 font-normal">
                                  / {a.maxMarks} ({a.weightPercentage}%)
                                </div>
                              </th>
                            ))}
                            <th className="px-4 py-3 text-center">Weighted Total</th>
                            <th className="px-4 py-3 text-center">Grade</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {roster.students.map((studentRow) => {
                            const isDropped = studentRow.enrollmentStatus === 'DROPPED'

                            return (
                              <tr key={studentRow.enrollmentId} className="hover:bg-slate-50/50">
                                <td className="px-4 py-3 font-mono font-bold text-slate-900">
                                  {studentRow.studentIdentifier}
                                </td>
                                <td className="px-4 py-3 font-medium text-slate-800">
                                  {studentRow.studentName}
                                </td>
                                <td className="px-4 py-3">
                                  <span
                                    className={`rounded-md px-2 py-0.5 text-[10px] font-bold ${
                                      isDropped
                                        ? 'bg-rose-100 text-rose-800'
                                        : 'bg-emerald-100 text-emerald-800'
                                    }`}
                                  >
                                    {studentRow.enrollmentStatus}
                                  </span>
                                </td>

                                {roster.assessments.map((a) => {
                                  const cellKey = `${studentRow.enrollmentId}_${a.id}`
                                  const currentVal = editingScores[cellKey] ?? ''

                                  return (
                                    <td key={a.id} className="px-3 py-2 text-center">
                                      {isPublished || isDropped ? (
                                        <span className="font-semibold text-slate-800">
                                          {currentVal !== '' ? currentVal : '—'}
                                        </span>
                                      ) : (
                                        <div className="flex items-center justify-center gap-1">
                                          <input
                                            type="number"
                                            min="0"
                                            max={a.maxMarks}
                                            step="0.5"
                                            value={currentVal}
                                            onChange={(e) => handleScoreChange(studentRow.enrollmentId, a.id, e.target.value)}
                                            onBlur={() => handleSaveMark(studentRow.enrollmentId, a.id, a.maxMarks)}
                                            placeholder={`/${a.maxMarks}`}
                                            className="w-16 rounded border border-slate-300 px-2 py-1 text-center text-xs font-medium text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                                          />
                                        </div>
                                      )}
                                    </td>
                                  )
                                })}

                                <td className="px-4 py-3 text-center font-bold text-slate-900">
                                  {studentRow.totalWeightedScore.toFixed(1)}%
                                </td>

                                <td className="px-4 py-3 text-center">
                                  <span
                                    className={`inline-block rounded-md px-2 py-0.5 text-xs font-extrabold ${
                                      studentRow.projectedGrade === 'A'
                                        ? 'bg-emerald-100 text-emerald-900'
                                        : studentRow.projectedGrade === 'B'
                                        ? 'bg-blue-100 text-blue-900'
                                        : studentRow.projectedGrade === 'C'
                                        ? 'bg-teal-100 text-teal-900'
                                        : studentRow.projectedGrade === 'S'
                                        ? 'bg-amber-100 text-amber-900'
                                        : studentRow.projectedGrade === 'W'
                                        ? 'bg-purple-100 text-purple-900'
                                        : 'bg-rose-100 text-rose-900'
                                    }`}
                                  >
                                    {studentRow.projectedGrade}
                                  </span>
                                </td>
                              </tr>
                            )
                          })}
                        </tbody>
                      </table>
                    </div>
                  )}

                  <div className="rounded-lg bg-slate-50 p-3 text-[11px] text-slate-500 border border-slate-200">
                    <strong>Grading Policy Notice:</strong> Grades (A $\ge$ 75%, B $\ge$ 65%, C $\ge$ 50%, S $\ge$ 40%, F &lt; 40%) are provisional default thresholds. Dropped enrollments receive Grade <strong>W</strong> regardless of marks. Attendance does not modify academic marks.
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* Add Assessment Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold text-slate-900">Define New Assessment</h3>
              <button
                onClick={() => setShowAddModal(false)}
                className="text-slate-400 hover:text-slate-600 font-bold text-lg cursor-pointer"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleCreateAssessment} className="space-y-4 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Assessment Title</label>
                <input
                  type="text"
                  required
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="e.g. Midterm Examination, Quiz 1, Project Report"
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Type</label>
                  <select
                    value={newType}
                    onChange={(e) => setNewType(e.target.value as AssessmentType)}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                  >
                    <option value="QUIZ">Quiz</option>
                    <option value="ASSIGNMENT">Assignment</option>
                    <option value="MIDTERM">Midterm</option>
                    <option value="FINAL_EXAM">Final Exam</option>
                    <option value="PROJECT">Project</option>
                    <option value="LAB">Lab</option>
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Max Raw Marks</label>
                  <input
                    type="number"
                    min="1"
                    max="1000"
                    step="1"
                    required
                    value={newMaxMarks}
                    onChange={(e) => setNewMaxMarks(e.target.value)}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                  />
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Weight Percentage (%) <span className="font-normal text-slate-400">Total cannot exceed 100%</span>
                </label>
                <input
                  type="number"
                  min="0.01"
                  max="100"
                  step="0.5"
                  required
                  value={newWeight}
                  onChange={(e) => setNewWeight(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-800 focus:border-emerald-600 focus:outline-hidden"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="rounded-lg border border-slate-200 px-4 py-2 font-semibold text-slate-600 hover:bg-slate-50 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={actionLoading}
                  className="rounded-lg bg-emerald-600 px-4 py-2 font-bold text-white hover:bg-emerald-700 disabled:opacity-50 cursor-pointer shadow-xs"
                >
                  {actionLoading ? 'Saving...' : 'Add Assessment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
