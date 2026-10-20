import { useState, useEffect } from 'react'
import {
  fetchDepartments,
  createDepartment,
  updateDepartment,
  setDepartmentStatus,
  fetchCourses,
  createCourse,
  setCourseStatus,
  fetchSubjects,
  createSubject,
  setSubjectStatus,
  fetchAssignments,
  assignLecturer,
  setAssignmentStatus,
  fetchEnrollments,
  enrolStudent,
  updateEnrollmentStatus,
  fetchActiveLecturers,
  fetchActiveStudents,
  type Department,
  type Course,
  type Subject,
  type LecturerAssignment,
  type Enrollment,
  type EnrollmentStatus,
  type ActiveUserOption,
} from './services/academicService'
import type { ApiErrorData } from './services/authService'

interface AdminAcademicHubProps {
  onBackToOverview: () => void
}

type TabType = 'departments' | 'courses' | 'subjects' | 'assignments' | 'enrolments'

export default function AdminAcademicHub({ onBackToOverview }: AdminAcademicHubProps) {
  const [activeTab, setActiveTab] = useState<TabType>('departments')
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null)

  // Data states
  const [departments, setDepartments] = useState<Department[]>([])
  const [courses, setCourses] = useState<Course[]>([])
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [assignments, setAssignments] = useState<LecturerAssignment[]>([])
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [activeLecturers, setActiveLecturers] = useState<ActiveUserOption[]>([])
  const [activeStudents, setActiveStudents] = useState<ActiveUserOption[]>([])

  // Modal states
  const [showDeptModal, setShowDeptModal] = useState(false)
  const [editingDept, setEditingDept] = useState<Department | null>(null)
  const [deptForm, setDeptForm] = useState({ code: '', name: '', description: '' })

  const [showCourseModal, setShowCourseModal] = useState(false)
  const [courseForm, setCourseForm] = useState({ code: '', name: '', description: '', departmentId: 0 })

  const [showSubjectModal, setShowSubjectModal] = useState(false)
  const [subjectForm, setSubjectForm] = useState({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: 0 })

  const [showAssignModal, setShowAssignModal] = useState(false)
  const [assignForm, setAssignForm] = useState({ subjectId: 0, lecturerId: 0, academicYear: '2025/2026', semester: 'Semester 1' })

  const [showEnrolModal, setShowEnrolModal] = useState(false)
  const [enrolForm, setEnrolForm] = useState({ studentId: 0, courseId: 0, subjectId: 0, academicYear: '2025/2026', semester: 'Semester 1' })

  useEffect(() => {
    let ignore = false

    async function loadInitial() {
      try {
        if (activeTab === 'departments') {
          const data = await fetchDepartments()
          if (!ignore) setDepartments(data)
        } else if (activeTab === 'courses') {
          const [cData, dData] = await Promise.all([fetchCourses(), fetchDepartments(true)])
          if (!ignore) {
            setCourses(cData)
            setDepartments(dData)
          }
        } else if (activeTab === 'subjects') {
          const [sData, cData] = await Promise.all([fetchSubjects(), fetchCourses(undefined, true)])
          if (!ignore) {
            setSubjects(sData)
            setCourses(cData)
          }
        } else if (activeTab === 'assignments') {
          const [aData, sData, lData] = await Promise.all([fetchAssignments(), fetchSubjects(undefined, undefined, true), fetchActiveLecturers()])
          if (!ignore) {
            setAssignments(aData)
            setSubjects(sData)
            setActiveLecturers(lData)
          }
        } else if (activeTab === 'enrolments') {
          const [eData, cData, sData, stData] = await Promise.all([
            fetchEnrollments(),
            fetchCourses(undefined, true),
            fetchSubjects(undefined, undefined, true),
            fetchActiveStudents(),
          ])
          if (!ignore) {
            setEnrollments(eData)
            setCourses(cData)
            setSubjects(sData)
            setActiveStudents(stData)
          }
        }
      } catch (err: unknown) {
        if (!ignore) {
          const apiErr = err as Error & ApiErrorData
          setFeedback({ type: 'error', message: apiErr.message || 'Failed to load academic data.' })
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
  }, [activeTab])

  const loadData = async () => {
    try {
      if (activeTab === 'departments') {
        const data = await fetchDepartments()
        setDepartments(data)
      } else if (activeTab === 'courses') {
        const [cData, dData] = await Promise.all([fetchCourses(), fetchDepartments(true)])
        setCourses(cData)
        setDepartments(dData)
      } else if (activeTab === 'subjects') {
        const [sData, cData] = await Promise.all([fetchSubjects(), fetchCourses(undefined, true)])
        setSubjects(sData)
        setCourses(cData)
      } else if (activeTab === 'assignments') {
        const [aData, sData, lData] = await Promise.all([fetchAssignments(), fetchSubjects(undefined, undefined, true), fetchActiveLecturers()])
        setAssignments(aData)
        setSubjects(sData)
        setActiveLecturers(lData)
      } else if (activeTab === 'enrolments') {
        const [eData, cData, sData, stData] = await Promise.all([
          fetchEnrollments(),
          fetchCourses(undefined, true),
          fetchSubjects(undefined, undefined, true),
          fetchActiveStudents(),
        ])
        setEnrollments(eData)
        setCourses(cData)
        setSubjects(sData)
        setActiveStudents(stData)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message || 'Failed to reload academic data.' })
    }
  }

  // ==========================================
  // HANDLERS
  // ==========================================

  const handleSaveDepartment = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      if (editingDept) {
        await updateDepartment(editingDept.id, deptForm)
        setFeedback({ type: 'success', message: `Department ${deptForm.name} updated successfully.` })
      } else {
        await createDepartment(deptForm)
        setFeedback({ type: 'success', message: `Department ${deptForm.name} created successfully.` })
      }
      setShowDeptModal(false)
      setEditingDept(null)
      setDeptForm({ code: '', name: '', description: '' })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleToggleDepartmentStatus = async (dept: Department) => {
    try {
      await setDepartmentStatus(dept.id, !dept.active)
      setFeedback({ type: 'success', message: `Department status updated to ${!dept.active ? 'Active' : 'Inactive'}.` })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleSaveCourse = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!courseForm.departmentId) {
      setFeedback({ type: 'error', message: 'Please select a valid department.' })
      return
    }
    try {
      await createCourse(courseForm)
      setFeedback({ type: 'success', message: `Course ${courseForm.name} created successfully.` })
      setShowCourseModal(false)
      setCourseForm({ code: '', name: '', description: '', departmentId: 0 })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleToggleCourseStatus = async (course: Course) => {
    try {
      await setCourseStatus(course.id, !course.active)
      setFeedback({ type: 'success', message: `Course status updated to ${!course.active ? 'Active' : 'Inactive'}.` })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleSaveSubject = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!subjectForm.courseId) {
      setFeedback({ type: 'error', message: 'Please select a valid course.' })
      return
    }
    try {
      await createSubject(subjectForm)
      setFeedback({ type: 'success', message: `Subject ${subjectForm.name} created successfully.` })
      setShowSubjectModal(false)
      setSubjectForm({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: 0 })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleToggleSubjectStatus = async (sub: Subject) => {
    try {
      await setSubjectStatus(sub.id, !sub.active)
      setFeedback({ type: 'success', message: `Subject status updated to ${!sub.active ? 'Active' : 'Inactive'}.` })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleAssignLecturer = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!assignForm.subjectId || !assignForm.lecturerId) {
      setFeedback({ type: 'error', message: 'Please select both a subject and an active lecturer.' })
      return
    }
    try {
      await assignLecturer(assignForm)
      setFeedback({ type: 'success', message: 'Lecturer assigned successfully.' })
      setShowAssignModal(false)
      setAssignForm({ subjectId: 0, lecturerId: 0, academicYear: '2025/2026', semester: 'Semester 1' })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleToggleAssignmentStatus = async (assign: LecturerAssignment) => {
    try {
      await setAssignmentStatus(assign.id, !assign.active)
      setFeedback({ type: 'success', message: `Assignment status updated to ${!assign.active ? 'Active' : 'Inactive'}.` })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleEnrolStudent = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!enrolForm.studentId || !enrolForm.courseId || !enrolForm.subjectId) {
      setFeedback({ type: 'error', message: 'Please select student, course, and subject.' })
      return
    }
    try {
      await enrolStudent(enrolForm)
      setFeedback({ type: 'success', message: 'Student successfully enrolled in subject.' })
      setShowEnrolModal(false)
      setEnrolForm({ studentId: 0, courseId: 0, subjectId: 0, academicYear: '2025/2026', semester: 'Semester 1' })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  const handleUpdateEnrollmentStatus = async (enrolId: number, status: EnrollmentStatus) => {
    try {
      await updateEnrollmentStatus(enrolId, status)
      setFeedback({ type: 'success', message: `Enrolment status updated to ${status}.` })
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({ type: 'error', message: apiErr.message })
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 rounded-xl border border-blue-200 bg-white p-5 shadow-xs">
        <div>
          <button
            type="button"
            onClick={onBackToOverview}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-600 hover:text-blue-800 transition-colors mb-2 cursor-pointer"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span>Back to Dashboard Overview</span>
          </button>
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Academic Structure & Enrolment Management</h2>
          <p className="text-xs text-slate-500">Configure university departments, curriculum courses, subjects, lecturer assignments, and student enrolments.</p>
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'departments' && (
            <button
              onClick={() => {
                setEditingDept(null)
                setDeptForm({ code: '', name: '', description: '' })
                setShowDeptModal(true)
              }}
              className="rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <span>+ Add Department</span>
            </button>
          )}
          {activeTab === 'courses' && (
            <button
              onClick={() => {
                setCourseForm({ code: '', name: '', description: '', departmentId: departments[0]?.id || 0 })
                setShowCourseModal(true)
              }}
              className="rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <span>+ Add Course</span>
            </button>
          )}
          {activeTab === 'subjects' && (
            <button
              onClick={() => {
                setSubjectForm({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: courses[0]?.id || 0 })
                setShowSubjectModal(true)
              }}
              className="rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <span>+ Add Subject</span>
            </button>
          )}
          {activeTab === 'assignments' && (
            <button
              onClick={() => {
                setAssignForm({ subjectId: subjects[0]?.id || 0, lecturerId: activeLecturers[0]?.id || 0, academicYear: '2025/2026', semester: 'Semester 1' })
                setShowAssignModal(true)
              }}
              className="rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <span>+ Assign Lecturer</span>
            </button>
          )}
          {activeTab === 'enrolments' && (
            <button
              onClick={() => {
                const firstCourseId = courses[0]?.id || 0
                const availableSubs = subjects.filter((s) => s.courseId === firstCourseId)
                setEnrolForm({
                  studentId: activeStudents[0]?.id || 0,
                  courseId: firstCourseId,
                  subjectId: availableSubs[0]?.id || 0,
                  academicYear: '2025/2026',
                  semester: 'Semester 1',
                })
                setShowEnrolModal(true)
              }}
              className="rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors inline-flex items-center gap-1.5"
            >
              <span>+ Enrol Student</span>
            </button>
          )}
        </div>
      </div>

      {/* Feedback banner */}
      {feedback && (
        <div
          className={`rounded-xl border p-4 text-xs shadow-xs flex items-center justify-between ${
            feedback.type === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-900' : 'border-rose-200 bg-rose-50 text-rose-900'
          }`}
        >
          <span>{feedback.message}</span>
          <button onClick={() => setFeedback(null)} className="font-semibold text-xs px-2 py-0.5 rounded hover:bg-black/5">
            ✕
          </button>
        </div>
      )}

      {/* Tabs navigation */}
      <div className="flex border-b border-slate-200 overflow-x-auto">
        {(['departments', 'courses', 'subjects', 'assignments', 'enrolments'] as TabType[]).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-4 py-2.5 text-xs font-semibold capitalize border-b-2 whitespace-nowrap transition-colors cursor-pointer ${
              activeTab === tab
                ? 'border-blue-600 text-blue-600 bg-blue-50/40'
                : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300'
            }`}
          >
            {tab === 'assignments' ? 'Lecturer Assignments' : tab === 'enrolments' ? 'Student Enrolments' : tab}
          </button>
        ))}
      </div>

      {/* Tab Panels */}
      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-xs text-slate-500 gap-2">
          <svg className="h-5 w-5 animate-spin text-blue-600" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>Loading academic records...</span>
        </div>
      ) : (
        <>
          {/* DEPARTMENTS TAB */}
          {activeTab === 'departments' && (
            <div className="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-xs">
              {departments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">No departments configured yet. Click "+ Add Department" to create one.</div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                    <tr>
                      <th className="px-4 py-3">Code</th>
                      <th className="px-4 py-3">Department Name</th>
                      <th className="px-4 py-3">Description</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {departments.map((dept) => (
                      <tr key={dept.id} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-semibold text-slate-900">{dept.code}</td>
                        <td className="px-4 py-3 text-slate-900">{dept.name}</td>
                        <td className="px-4 py-3 text-slate-500 truncate max-w-xs">{dept.description || '—'}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${dept.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {dept.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() => {
                              setEditingDept(dept)
                              setDeptForm({ code: dept.code, name: dept.name, description: dept.description || '' })
                              setShowDeptModal(true)
                            }}
                            className="font-semibold text-blue-600 hover:text-blue-800 cursor-pointer"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleToggleDepartmentStatus(dept)}
                            className={`font-semibold cursor-pointer ${dept.active ? 'text-rose-600 hover:text-rose-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {dept.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* COURSES TAB */}
          {activeTab === 'courses' && (
            <div className="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-xs">
              {courses.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">No courses configured yet. Click "+ Add Course" to create one.</div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                    <tr>
                      <th className="px-4 py-3">Code</th>
                      <th className="px-4 py-3">Course Name</th>
                      <th className="px-4 py-3">Department</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {courses.map((course) => (
                      <tr key={course.id} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-semibold text-slate-900">{course.code}</td>
                        <td className="px-4 py-3 text-slate-900">{course.name}</td>
                        <td className="px-4 py-3 text-slate-600">
                          <span className="font-medium text-slate-800">{course.departmentCode}</span> — {course.departmentName}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${course.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {course.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => handleToggleCourseStatus(course)}
                            className={`font-semibold cursor-pointer ${course.active ? 'text-rose-600 hover:text-rose-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {course.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* SUBJECTS TAB */}
          {activeTab === 'subjects' && (
            <div className="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-xs">
              {subjects.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">No subjects configured yet. Click "+ Add Subject" to create one.</div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                    <tr>
                      <th className="px-4 py-3">Code</th>
                      <th className="px-4 py-3">Subject Name</th>
                      <th className="px-4 py-3">Course / Dept</th>
                      <th className="px-4 py-3">Credits</th>
                      <th className="px-4 py-3">Semester</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {subjects.map((sub) => (
                      <tr key={sub.id} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-semibold text-slate-900">{sub.code}</td>
                        <td className="px-4 py-3 text-slate-900">{sub.name}</td>
                        <td className="px-4 py-3 text-slate-600">{sub.courseCode} ({sub.departmentCode})</td>
                        <td className="px-4 py-3">{sub.credits} Units</td>
                        <td className="px-4 py-3">Sem {sub.semester}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${sub.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {sub.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => handleToggleSubjectStatus(sub)}
                            className={`font-semibold cursor-pointer ${sub.active ? 'text-rose-600 hover:text-rose-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {sub.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* LECTURER ASSIGNMENTS TAB */}
          {activeTab === 'assignments' && (
            <div className="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-xs">
              {assignments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">No lecturer assignments recorded yet. Click "+ Assign Lecturer" to create one.</div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                    <tr>
                      <th className="px-4 py-3">Subject</th>
                      <th className="px-4 py-3">Assigned Lecturer</th>
                      <th className="px-4 py-3">Academic Period</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {assignments.map((assign) => (
                      <tr key={assign.id} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3 font-semibold text-slate-900">
                          {assign.subjectCode} — {assign.subjectName}
                        </td>
                        <td className="px-4 py-3">
                          <p className="font-medium text-slate-900">{assign.lecturerName}</p>
                          <p className="text-[11px] text-slate-500">{assign.lecturerIdentifier} • {assign.lecturerEmail}</p>
                        </td>
                        <td className="px-4 py-3">{assign.academicYear} • {assign.semester}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${assign.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {assign.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => handleToggleAssignmentStatus(assign)}
                            className={`font-semibold cursor-pointer ${assign.active ? 'text-rose-600 hover:text-rose-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {assign.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* STUDENT ENROLMENTS TAB */}
          {activeTab === 'enrolments' && (
            <div className="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-xs">
              {enrollments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">No student enrolments recorded yet. Click "+ Enrol Student" to create one.</div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800">
                    <tr>
                      <th className="px-4 py-3">Student</th>
                      <th className="px-4 py-3">Course / Subject</th>
                      <th className="px-4 py-3">Period</th>
                      <th className="px-4 py-3">Credits</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3 text-right">Update Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {enrollments.map((enrol) => (
                      <tr key={enrol.id} className="hover:bg-slate-50/50">
                        <td className="px-4 py-3">
                          <p className="font-semibold text-slate-900">{enrol.studentName}</p>
                          <p className="text-[11px] text-slate-500">{enrol.studentIdentifier} • {enrol.studentEmail}</p>
                        </td>
                        <td className="px-4 py-3">
                          <p className="font-medium text-slate-900">{enrol.subjectCode} — {enrol.subjectName}</p>
                          <p className="text-[11px] text-slate-500">{enrol.courseName}</p>
                        </td>
                        <td className="px-4 py-3">{enrol.academicYear} • {enrol.semester}</td>
                        <td className="px-4 py-3">{enrol.credits} Units</td>
                        <td className="px-4 py-3">
                          <span
                            className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${
                              enrol.status === 'ACTIVE'
                                ? 'bg-emerald-100 text-emerald-800'
                                : enrol.status === 'COMPLETED'
                                ? 'bg-blue-100 text-blue-800'
                                : 'bg-rose-100 text-rose-800'
                            }`}
                          >
                            {enrol.status}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <select
                            value={enrol.status}
                            onChange={(e) => handleUpdateEnrollmentStatus(enrol.id, e.target.value as EnrollmentStatus)}
                            className="rounded border border-slate-300 bg-white px-2 py-1 text-xs text-slate-800 focus:outline-hidden focus:ring-1 focus:ring-blue-600"
                          >
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="DROPPED">DROPPED</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </>
      )}

      {/* ==========================================
          MODALS
      ========================================== */}

      {/* DEPARTMENT MODAL */}
      {showDeptModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <h3 className="text-base font-bold text-slate-900">{editingDept ? 'Edit Department' : 'Create Department'}</h3>
            <form onSubmit={handleSaveDepartment} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Department Code</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. CS, ENG, BUS"
                  value={deptForm.code}
                  onChange={(e) => setDeptForm({ ...deptForm, code: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600 uppercase"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Department Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Computer Science and Engineering"
                  value={deptForm.name}
                  onChange={(e) => setDeptForm({ ...deptForm, name: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={deptForm.description}
                  onChange={(e) => setDeptForm({ ...deptForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowDeptModal(false)}
                  className="rounded px-3 py-1.5 font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 py-1.5 font-semibold text-white hover:bg-blue-700 cursor-pointer"
                >
                  Save Department
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* COURSE MODAL */}
      {showCourseModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <h3 className="text-base font-bold text-slate-900">Create Course / Degree Program</h3>
            <form onSubmit={handleSaveCourse} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Department</label>
                <select
                  required
                  value={courseForm.departmentId}
                  onChange={(e) => setCourseForm({ ...courseForm, departmentId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Department...</option>
                  {departments.filter((d) => d.active).map((d) => (
                    <option key={d.id} value={d.id}>{d.code} — {d.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Course Code</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. BCS, BSE, BIT"
                  value={courseForm.code}
                  onChange={(e) => setCourseForm({ ...courseForm, code: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600 uppercase"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Course Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Bachelor of Computer Science"
                  value={courseForm.name}
                  onChange={(e) => setCourseForm({ ...courseForm, name: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={courseForm.description}
                  onChange={(e) => setCourseForm({ ...courseForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCourseModal(false)}
                  className="rounded px-3 py-1.5 font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 py-1.5 font-semibold text-white hover:bg-blue-700 cursor-pointer"
                >
                  Save Course
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* SUBJECT MODAL */}
      {showSubjectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <h3 className="text-base font-bold text-slate-900">Create Academic Subject</h3>
            <form onSubmit={handleSaveSubject} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Associated Course</label>
                <select
                  required
                  value={subjectForm.courseId}
                  onChange={(e) => setSubjectForm({ ...subjectForm, courseId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Course...</option>
                  {courses.filter((c) => c.active).map((c) => (
                    <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Subject Code</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. CS101"
                    value={subjectForm.code}
                    onChange={(e) => setSubjectForm({ ...subjectForm, code: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600 uppercase"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Credits (1–10)</label>
                  <input
                    type="number"
                    min={1}
                    max={10}
                    required
                    value={subjectForm.credits}
                    onChange={(e) => setSubjectForm({ ...subjectForm, credits: Number(e.target.value) })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Semester (1–12)</label>
                  <input
                    type="number"
                    min={1}
                    max={12}
                    required
                    value={subjectForm.semester}
                    onChange={(e) => setSubjectForm({ ...subjectForm, semester: Number(e.target.value) })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Subject Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Programming I"
                    value={subjectForm.name}
                    onChange={(e) => setSubjectForm({ ...subjectForm, name: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={subjectForm.description}
                  onChange={(e) => setSubjectForm({ ...subjectForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowSubjectModal(false)}
                  className="rounded px-3 py-1.5 font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 py-1.5 font-semibold text-white hover:bg-blue-700 cursor-pointer"
                >
                  Save Subject
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* LECTURER ASSIGNMENT MODAL */}
      {showAssignModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <h3 className="text-base font-bold text-slate-900">Assign Active Lecturer to Subject</h3>
            <form onSubmit={handleAssignLecturer} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Subject</label>
                <select
                  required
                  value={assignForm.subjectId}
                  onChange={(e) => setAssignForm({ ...assignForm, subjectId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Subject...</option>
                  {subjects.filter((s) => s.active).map((s) => (
                    <option key={s.id} value={s.id}>{s.code} — {s.name} ({s.courseCode})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Active Lecturer</label>
                <select
                  required
                  value={assignForm.lecturerId}
                  onChange={(e) => setAssignForm({ ...assignForm, lecturerId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Lecturer...</option>
                  {activeLecturers.map((l) => (
                    <option key={l.id} value={l.id}>{l.fullName} ({l.identifier})</option>
                  ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Academic Year</label>
                  <input
                    type="text"
                    required
                    value={assignForm.academicYear}
                    onChange={(e) => setAssignForm({ ...assignForm, academicYear: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Semester</label>
                  <input
                    type="text"
                    required
                    value={assignForm.semester}
                    onChange={(e) => setAssignForm({ ...assignForm, semester: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowAssignModal(false)}
                  className="rounded px-3 py-1.5 font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 py-1.5 font-semibold text-white hover:bg-blue-700 cursor-pointer"
                >
                  Confirm Assignment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ENROLMENT MODAL */}
      {showEnrolModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <h3 className="text-base font-bold text-slate-900">Enrol Active Student</h3>
            <form onSubmit={handleEnrolStudent} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Active Student</label>
                <select
                  required
                  value={enrolForm.studentId}
                  onChange={(e) => setEnrolForm({ ...enrolForm, studentId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Student...</option>
                  {activeStudents.map((st) => (
                    <option key={st.id} value={st.id}>{st.fullName} ({st.identifier})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Course</label>
                <select
                  required
                  value={enrolForm.courseId}
                  onChange={(e) => {
                    const cId = Number(e.target.value)
                    const matchingSubs = subjects.filter((s) => s.courseId === cId && s.active)
                    setEnrolForm({
                      ...enrolForm,
                      courseId: cId,
                      subjectId: matchingSubs[0]?.id || 0,
                    })
                  }}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Course...</option>
                  {courses.filter((c) => c.active).map((c) => (
                    <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Subject (Within Course)</label>
                <select
                  required
                  value={enrolForm.subjectId}
                  onChange={(e) => setEnrolForm({ ...enrolForm, subjectId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                >
                  <option value={0}>Select Subject...</option>
                  {subjects
                    .filter((s) => s.active && (!enrolForm.courseId || s.courseId === enrolForm.courseId))
                    .map((s) => (
                      <option key={s.id} value={s.id}>{s.code} — {s.name} ({s.credits} Credits)</option>
                    ))}
                </select>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Academic Year</label>
                  <input
                    type="text"
                    required
                    value={enrolForm.academicYear}
                    onChange={(e) => setEnrolForm({ ...enrolForm, academicYear: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Semester</label>
                  <input
                    type="text"
                    required
                    value={enrolForm.semester}
                    onChange={(e) => setEnrolForm({ ...enrolForm, semester: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-blue-600"
                  />
                </div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowEnrolModal(false)}
                  className="rounded px-3 py-1.5 font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 py-1.5 font-semibold text-white hover:bg-blue-700 cursor-pointer"
                >
                  Confirm Enrolment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
