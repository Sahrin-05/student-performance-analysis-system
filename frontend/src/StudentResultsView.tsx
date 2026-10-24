import { useState, useEffect } from 'react'
import {
  fetchStudentMyEnrolments,
  type Enrollment,
} from './services/academicService'
import {
  fetchMySubjectResults,
  type StudentPublishedResults,
} from './services/performanceService'
import type { ApiErrorData } from './services/authService'

export default function StudentResultsView() {
  const [enrolments, setEnrolments] = useState<Enrollment[]>([])
  const [selectedEnrolment, setSelectedEnrolment] = useState<Enrollment | null>(null)
  const [results, setResults] = useState<StudentPublishedResults | null>(null)

  const [isLoadingEnrolments, setIsLoadingEnrolments] = useState(true)
  const [isLoadingResults, setIsLoadingResults] = useState(false)
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
    async function loadResults() {
      if (!selectedEnrolment) return
      setIsLoadingResults(true)
      setError(null)
      try {
        const data = await fetchMySubjectResults(selectedEnrolment.id)
        setResults(data)
      } catch (err: unknown) {
        const apiErr = err as Error & ApiErrorData
        setError(apiErr.message || 'Failed to fetch academic results.')
      } finally {
        setIsLoadingResults(false)
      }
    }
    loadResults()
  }, [selectedEnrolment])

  const isPublished = results?.publicationStatus === 'PUBLISHED'

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="rounded-xl border border-emerald-200 bg-white p-5 shadow-xs">
        <h2 className="text-xl font-bold tracking-tight text-slate-900">Student Academic Results & Marks</h2>
        <p className="text-xs text-slate-500">
          View your published continuous assessments, examination marks, and provisional final grades.
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
          You are not currently enrolled in any subjects. Please contact the academic registry.
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
              {isLoadingResults ? (
                <div className="py-12 text-center text-xs text-slate-500">Loading published results...</div>
              ) : !isPublished ? (
                /* Unpublished / Draft Notice */
                <div className="rounded-xl border border-amber-200 bg-amber-50/70 p-8 text-center shadow-xs space-y-3">
                  <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-amber-100 text-amber-700">
                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <h3 className="text-base font-bold text-slate-900">Results Pending Publication</h3>
                  <p className="max-w-md mx-auto text-xs text-slate-600">
                    Continuous assessment and examination marks for <strong>{selectedEnrolment.subjectCode} ({selectedEnrolment.subjectName})</strong> are currently in progress or awaiting administrative release. Marks will appear here once officially published by the course lecturer.
                  </p>
                  <p className="text-[11px] text-amber-800 font-semibold">
                    Academic Year: {selectedEnrolment.academicYear} • {selectedEnrolment.semester}
                  </p>
                </div>
              ) : (
                /* Published Results View */
                <div className="space-y-6">
                  {/* Summary Card */}
                  <div className="rounded-xl border border-emerald-200 bg-white p-6 shadow-xs grid grid-cols-1 md:grid-cols-4 gap-6">
                    <div className="border-b md:border-b-0 md:border-r border-slate-100 pb-4 md:pb-0 md:pr-4">
                      <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Subject</p>
                      <h4 className="text-lg font-black text-slate-900 mt-1">{results.subjectCode}</h4>
                      <p className="text-xs text-slate-600 mt-0.5">{results.subjectName}</p>
                      <p className="text-[11px] text-slate-400 mt-1">{results.credits} Academic Credits</p>
                    </div>

                    <div className="border-b md:border-b-0 md:border-r border-slate-100 pb-4 md:pb-0 md:pr-4">
                      <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Overall Weighted Score</p>
                      <h4 className="text-3xl font-black text-slate-900 mt-1">
                        {results.totalWeightedScore !== null && results.totalWeightedScore !== undefined
                          ? `${results.totalWeightedScore.toFixed(1)}%`
                          : '—'}
                      </h4>
                      <p className="text-[11px] text-slate-400 mt-1">Based on 100% total assessment weighting</p>
                    </div>

                    <div className="border-b md:border-b-0 md:border-r border-slate-100 pb-4 md:pb-0 md:pr-4">
                      <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Provisional Grade</p>
                      <div className="mt-1 flex items-baseline gap-2">
                        <span
                          className={`inline-block rounded-xl px-4 py-1 text-2xl font-black ${
                            results.grade === 'A'
                              ? 'bg-emerald-100 text-emerald-900'
                              : results.grade === 'B'
                              ? 'bg-blue-100 text-blue-900'
                              : results.grade === 'C'
                              ? 'bg-teal-100 text-teal-900'
                              : results.grade === 'S'
                              ? 'bg-amber-100 text-amber-900'
                              : results.grade === 'W'
                              ? 'bg-purple-100 text-purple-900'
                              : 'bg-rose-100 text-rose-900'
                          }`}
                        >
                          {results.grade || '—'}
                        </span>
                        {results.gradePoint !== null && results.gradePoint !== undefined && (
                          <span className="text-xs font-bold text-slate-500">
                            (GP: {results.gradePoint.toFixed(1)})
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] text-slate-400 mt-1">
                        {results.grade === 'W' ? 'Withdrawn / Dropped' : 'Subject Final Evaluation'}
                      </p>
                    </div>

                    <div>
                      <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Status & Release</p>
                      <div className="mt-1 inline-flex items-center gap-1.5 rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800">
                        <span className="h-2 w-2 rounded-full bg-emerald-600"></span>
                        OFFICIALLY PUBLISHED
                      </div>
                      <p className="text-[11px] text-slate-400 mt-2">
                        Released: {results.publishedAt ? new Date(results.publishedAt).toLocaleDateString() : 'Confirmed'}
                      </p>
                    </div>
                  </div>

                  {/* Assessments Breakdown Table */}
                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                    <h3 className="text-sm font-bold text-slate-900">Assessment Breakdown</h3>
                    <div className="overflow-x-auto rounded-lg border border-slate-200">
                      <table className="w-full text-left text-xs text-slate-700">
                        <thead className="bg-slate-50 text-[11px] font-semibold uppercase text-slate-600 border-b border-slate-200">
                          <tr>
                            <th className="px-4 py-3">Assessment Title</th>
                            <th className="px-4 py-3">Type</th>
                            <th className="px-4 py-3 text-center">Your Score / Max</th>
                            <th className="px-4 py-3 text-center">Score %</th>
                            <th className="px-4 py-3 text-center">Weight</th>
                            <th className="px-4 py-3 text-center">Weighted Contribution</th>
                            <th className="px-4 py-3">Instructor Feedback</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                          {results.assessmentResults.map((item) => {
                            const rawScore = item.studentScore !== null && item.studentScore !== undefined ? item.studentScore : null
                            const percentage = rawScore !== null && item.maxMarks > 0 ? (rawScore / item.maxMarks) * 100 : null

                            return (
                              <tr key={item.assessmentId} className="hover:bg-slate-50/50">
                                <td className="px-4 py-3 font-semibold text-slate-900">{item.title}</td>
                                <td className="px-4 py-3">
                                  <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[10px] font-bold text-slate-700">
                                    {item.type}
                                  </span>
                                </td>
                                <td className="px-4 py-3 text-center font-bold text-slate-800">
                                  {rawScore !== null ? `${rawScore.toFixed(1)} / ${item.maxMarks}` : '—'}
                                </td>
                                <td className="px-4 py-3 text-center font-medium">
                                  {percentage !== null ? `${percentage.toFixed(1)}%` : '—'}
                                </td>
                                <td className="px-4 py-3 text-center font-semibold text-slate-600">
                                  {item.weightPercentage}%
                                </td>
                                <td className="px-4 py-3 text-center font-bold text-emerald-700">
                                  {item.weightedContribution !== null && item.weightedContribution !== undefined
                                    ? `+${item.weightedContribution.toFixed(2)}%`
                                    : '—'}
                                </td>
                                <td className="px-4 py-3 text-slate-500 italic">
                                  {item.comments || 'No remarks'}
                                </td>
                              </tr>
                            )
                          })}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* Policy & Regulation Notices */}
                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-xs text-slate-600 space-y-1.5">
                    <p className="font-bold text-slate-800">Official Academic Notices:</p>
                    <p>• {results.gradingNotice || 'Grade boundaries (A >= 75%, B >= 65%, C >= 50%, S >= 40%, F < 40%) are provisional system defaults.'}</p>
                    <p>• Course grade calculation is strictly decoupled from attendance hours. Attendance requirements are tracked separately for examination admission eligibility.</p>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
