import { useState, useEffect } from 'react'
import {
  fetchLecturerMySubjects,
  fetchLecturerSubjectStudents,
  type Subject,
  type Enrollment,
} from './services/academicService'
import type { ApiErrorData } from './services/authService'

export default function LecturerSubjectsView() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [selectedSubject, setSelectedSubject] = useState<Subject | null>(null)
  const [students, setStudents] = useState<Enrollment[]>([])
  const [isLoadingStudents, setIsLoadingStudents] = useState(false)

  useEffect(() => {
    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        const data = await fetchLecturerMySubjects()
        setSubjects(data)
      } catch (err: unknown) {
        const apiErr = err as Error & ApiErrorData
        setError(apiErr.message || 'Failed to load assigned subjects.')
      } finally {
        setIsLoading(false)
      }
    }
    load()
  }, [])

  const handleSelectSubject = async (sub: Subject) => {
    setSelectedSubject(sub)
    setIsLoadingStudents(true)
    try {
      const data = await fetchLecturerSubjectStudents(sub.id)
      setStudents(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setError(apiErr.message || 'Failed to load enrolled students.')
    } finally {
      setIsLoadingStudents(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="rounded-xl border border-emerald-200 bg-white p-5 shadow-xs">
        <h2 className="text-xl font-bold tracking-tight text-slate-900">Faculty Course Roster & Enrolled Students</h2>
        <p className="text-xs text-slate-500">View your actively assigned teaching subjects and access student rosters for grading and attendance.</p>
      </div>

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-xs text-slate-500 gap-2">
          <svg className="h-5 w-5 animate-spin text-emerald-600" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>Loading assigned subjects...</span>
        </div>
      ) : subjects.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-xs text-slate-500 shadow-xs">
          No active subjects currently assigned to your instructor account. Please contact academic administration if your teaching schedule is missing.
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Subjects list */}
          <div className="space-y-3 lg:col-span-1">
            <h3 className="text-xs font-semibold text-slate-700 uppercase tracking-wider">Your Assigned Subjects</h3>
            <div className="space-y-2">
              {subjects.map((sub) => (
                <button
                  key={sub.id}
                  onClick={() => handleSelectSubject(sub)}
                  className={`w-full text-left p-4 rounded-xl border transition-all cursor-pointer ${
                    selectedSubject?.id === sub.id
                      ? 'border-emerald-600 bg-emerald-50/60 shadow-xs'
                      : 'border-slate-200 bg-white hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-900">{sub.code}</span>
                    <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold text-emerald-800">
                      {sub.credits} Credits
                    </span>
                  </div>
                  <p className="font-medium text-xs text-slate-800 mt-1">{sub.name}</p>
                  <p className="text-[11px] text-slate-500 mt-1">{sub.courseName} • Sem {sub.semester}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Enrolled students for selected subject */}
          <div className="lg:col-span-2">
            {selectedSubject ? (
              <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs space-y-4">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                  <div>
                    <h3 className="text-sm font-bold text-slate-900">
                      Enrolled Students: {selectedSubject.code} — {selectedSubject.name}
                    </h3>
                    <p className="text-xs text-slate-500">{selectedSubject.courseName} ({selectedSubject.departmentName})</p>
                  </div>
                  <span className="rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700 border border-blue-200">
                    {students.length} Students Enrolled
                  </span>
                </div>

                {isLoadingStudents ? (
                  <div className="py-8 text-center text-xs text-slate-500">Loading student roster...</div>
                ) : students.length === 0 ? (
                  <div className="py-8 text-center text-xs text-slate-500">No students are actively enrolled in this subject for the current semester.</div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs text-slate-600">
                      <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                        <tr>
                          <th className="px-3 py-2">Student ID</th>
                          <th className="px-3 py-2">Student Name</th>
                          <th className="px-3 py-2">Institutional Email</th>
                          <th className="px-3 py-2">Period</th>
                          <th className="px-3 py-2">Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {students.map((e) => (
                          <tr key={e.id}>
                            <td className="px-3 py-2 font-medium text-slate-900">{e.studentIdentifier}</td>
                            <td className="px-3 py-2 text-slate-800">{e.studentName}</td>
                            <td className="px-3 py-2 text-slate-500">{e.studentEmail}</td>
                            <td className="px-3 py-2">{e.academicYear} • {e.semester}</td>
                            <td className="px-3 py-2">
                              <span className="inline-flex rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold text-emerald-800">
                                {e.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            ) : (
              <div className="rounded-xl border border-slate-200 bg-slate-50/50 p-12 text-center text-xs text-slate-500">
                Select a subject on the left to view the registered student roster.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
