import { useState, useEffect, useMemo } from 'react'
import {
  fetchDepartments,
  createDepartment,
  updateDepartment,
  setDepartmentStatus,
  deleteDepartment,
  fetchCourses,
  createCourse,
  updateCourse,
  setCourseStatus,
  deleteCourse,
  fetchSubjects,
  createSubject,
  updateSubject,
  setSubjectStatus,
  deleteSubject,
  fetchAssignments,
  assignLecturer,
  setAssignmentStatus,
  deleteAssignment,
  fetchEnrollments,
  enrolStudent,
  updateEnrollmentStatus,
  deleteEnrollment,
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

interface DeleteConfirmation {
  type: 'department' | 'course' | 'subject' | 'assignment' | 'enrolment'
  id: number
  code: string
  name: string
  parentOrContext?: string
}

interface ItemDetails {
  title: string
  badge?: { label: string; color: string }
  fields: Array<{ label: string; value: string | number | undefined | null }>
}

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

  // Search & Filter states
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL')
  const [deptFilter, setDeptFilter] = useState<number | 'ALL'>('ALL')
  const [courseFilter, setCourseFilter] = useState<number | 'ALL'>('ALL')
  const [semesterFilter, setSemesterFilter] = useState<number | 'ALL'>('ALL')
  const [enrolmentStatusFilter, setEnrolmentStatusFilter] = useState<'ALL' | EnrollmentStatus>('ALL')

  // Sorting state
  const [sortField, setSortField] = useState<string>('code')
  const [sortAsc, setSortAsc] = useState<boolean>(true)

  // Modal states
  const [showDeptModal, setShowDeptModal] = useState(false)
  const [editingDept, setEditingDept] = useState<Department | null>(null)
  const [deptForm, setDeptForm] = useState({ code: '', name: '', description: '' })

  const [showCourseModal, setShowCourseModal] = useState(false)
  const [editingCourse, setEditingCourse] = useState<Course | null>(null)
  const [courseForm, setCourseForm] = useState({ code: '', name: '', description: '', departmentId: 0 })

  const [showSubjectModal, setShowSubjectModal] = useState(false)
  const [editingSubject, setEditingSubject] = useState<Subject | null>(null)
  const [subjectForm, setSubjectForm] = useState({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: 0 })

  const [showAssignModal, setShowAssignModal] = useState(false)
  const [assignForm, setAssignForm] = useState({ subjectId: 0, lecturerId: 0, academicYear: '2025/2026', semester: 'Semester 1' })

  const [showEnrolModal, setShowEnrolModal] = useState(false)
  const [enrolForm, setEnrolForm] = useState({ studentId: 0, courseId: 0, subjectId: 0, academicYear: '2025/2026', semester: 'Semester 1' })

  // Delete & Details modals
  const [deleteConfirmation, setDeleteConfirmation] = useState<DeleteConfirmation | null>(null)
  const [viewingDetails, setViewingDetails] = useState<ItemDetails | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  // Tab change handler with filter reset
  const handleTabChange = (tab: TabType) => {
    setActiveTab(tab)
    setSearchTerm('')
    setStatusFilter('ALL')
    setDeptFilter('ALL')
    setCourseFilter('ALL')
    setSemesterFilter('ALL')
    setEnrolmentStatusFilter('ALL')
    setSortField(tab === 'enrolments' ? 'student' : tab === 'assignments' ? 'subject' : 'code')
    setSortAsc(true)
  }

  useEffect(() => {
    let ignore = false

    async function loadInitial() {
      setIsLoading(true)
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

  // Sorting helper
  const handleSort = (field: string) => {
    if (sortField === field) {
      setSortAsc(!sortAsc)
    } else {
      setSortField(field)
      setSortAsc(true)
    }
  }

  // ==========================================
  // FILTERED & SORTED LISTS
  // ==========================================

  const filteredDepartments = useMemo(() => {
    let list = departments.filter((d) => {
      const matchSearch =
        d.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
        d.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (d.description && d.description.toLowerCase().includes(searchTerm.toLowerCase()))
      const matchStatus = statusFilter === 'ALL' ? true : statusFilter === 'ACTIVE' ? d.active : !d.active
      return matchSearch && matchStatus
    })

    list = [...list].sort((a, b) => {
      let cmp = 0
      if (sortField === 'code') cmp = a.code.localeCompare(b.code)
      else if (sortField === 'name') cmp = a.name.localeCompare(b.name)
      else if (sortField === 'status') cmp = Number(a.active) - Number(b.active)
      return sortAsc ? cmp : -cmp
    })

    return list
  }, [departments, searchTerm, statusFilter, sortField, sortAsc])

  const filteredCourses = useMemo(() => {
    let list = courses.filter((c) => {
      const matchSearch =
        c.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.departmentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        c.departmentCode.toLowerCase().includes(searchTerm.toLowerCase())
      const matchDept = deptFilter === 'ALL' || c.departmentId === deptFilter
      const matchStatus = statusFilter === 'ALL' ? true : statusFilter === 'ACTIVE' ? c.active : !c.active
      return matchSearch && matchDept && matchStatus
    })

    list = [...list].sort((a, b) => {
      let cmp = 0
      if (sortField === 'code') cmp = a.code.localeCompare(b.code)
      else if (sortField === 'name') cmp = a.name.localeCompare(b.name)
      else if (sortField === 'department') cmp = a.departmentCode.localeCompare(b.departmentCode)
      else if (sortField === 'status') cmp = Number(a.active) - Number(b.active)
      return sortAsc ? cmp : -cmp
    })

    return list
  }, [courses, searchTerm, deptFilter, statusFilter, sortField, sortAsc])

  const filteredSubjects = useMemo(() => {
    let list = subjects.filter((s) => {
      const matchSearch =
        s.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
        s.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        s.courseName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        s.courseCode.toLowerCase().includes(searchTerm.toLowerCase())
      const matchCourse = courseFilter === 'ALL' || s.courseId === courseFilter
      const matchSemester = semesterFilter === 'ALL' || s.semester === semesterFilter
      const matchStatus = statusFilter === 'ALL' ? true : statusFilter === 'ACTIVE' ? s.active : !s.active
      return matchSearch && matchCourse && matchSemester && matchStatus
    })

    list = [...list].sort((a, b) => {
      let cmp = 0
      if (sortField === 'code') cmp = a.code.localeCompare(b.code)
      else if (sortField === 'name') cmp = a.name.localeCompare(b.name)
      else if (sortField === 'course') cmp = a.courseCode.localeCompare(b.courseCode)
      else if (sortField === 'credits') cmp = a.credits - b.credits
      else if (sortField === 'semester') cmp = a.semester - b.semester
      else if (sortField === 'status') cmp = Number(a.active) - Number(b.active)
      return sortAsc ? cmp : -cmp
    })

    return list
  }, [subjects, searchTerm, courseFilter, semesterFilter, statusFilter, sortField, sortAsc])

  const filteredAssignments = useMemo(() => {
    let list = assignments.filter((a) => {
      const matchSearch =
        a.subjectCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        a.subjectName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        a.lecturerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        a.lecturerIdentifier.toLowerCase().includes(searchTerm.toLowerCase())
      const matchStatus = statusFilter === 'ALL' ? true : statusFilter === 'ACTIVE' ? a.active : !a.active
      return matchSearch && matchStatus
    })

    list = [...list].sort((a, b) => {
      let cmp = 0
      if (sortField === 'subject') cmp = a.subjectCode.localeCompare(b.subjectCode)
      else if (sortField === 'lecturer') cmp = a.lecturerName.localeCompare(b.lecturerName)
      else if (sortField === 'period') cmp = `${a.academicYear} ${a.semester}`.localeCompare(`${b.academicYear} ${b.semester}`)
      else if (sortField === 'status') cmp = Number(a.active) - Number(b.active)
      return sortAsc ? cmp : -cmp
    })

    return list
  }, [assignments, searchTerm, statusFilter, sortField, sortAsc])

  const filteredEnrollments = useMemo(() => {
    let list = enrollments.filter((e) => {
      const matchSearch =
        e.studentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.studentIdentifier.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.studentEmail.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.subjectCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.subjectName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.courseName.toLowerCase().includes(searchTerm.toLowerCase())
      const matchCourse = courseFilter === 'ALL' || e.courseId === courseFilter
      const matchStatus = enrolmentStatusFilter === 'ALL' || e.status === enrolmentStatusFilter
      return matchSearch && matchCourse && matchStatus
    })

    list = [...list].sort((a, b) => {
      let cmp = 0
      if (sortField === 'student') cmp = a.studentName.localeCompare(b.studentName)
      else if (sortField === 'subject') cmp = a.subjectCode.localeCompare(b.subjectCode)
      else if (sortField === 'period') cmp = `${a.academicYear} ${a.semester}`.localeCompare(`${b.academicYear} ${b.semester}`)
      else if (sortField === 'credits') cmp = a.credits - b.credits
      else if (sortField === 'status') cmp = a.status.localeCompare(b.status)
      return sortAsc ? cmp : -cmp
    })

    return list
  }, [enrollments, searchTerm, courseFilter, enrolmentStatusFilter, sortField, sortAsc])

  // ==========================================
  // HANDLERS: CREATE / UPDATE
  // ==========================================

  const handleSaveDepartment = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      if (editingDept) {
        await updateDepartment(editingDept.id, deptForm)
        setFeedback({ type: 'success', message: `Department "${deptForm.name}" updated successfully.` })
      } else {
        await createDepartment(deptForm)
        setFeedback({ type: 'success', message: `Department "${deptForm.name}" created successfully.` })
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

  const handleSaveCourse = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!courseForm.departmentId) {
      setFeedback({ type: 'error', message: 'Please select a valid department.' })
      return
    }
    try {
      if (editingCourse) {
        await updateCourse(editingCourse.id, courseForm)
        setFeedback({ type: 'success', message: `Course "${courseForm.name}" updated successfully.` })
      } else {
        await createCourse(courseForm)
        setFeedback({ type: 'success', message: `Course "${courseForm.name}" created successfully.` })
      }
      setShowCourseModal(false)
      setEditingCourse(null)
      setCourseForm({ code: '', name: '', description: '', departmentId: 0 })
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
      if (editingSubject) {
        await updateSubject(editingSubject.id, subjectForm)
        setFeedback({ type: 'success', message: `Subject "${subjectForm.name}" updated successfully.` })
      } else {
        await createSubject(subjectForm)
        setFeedback({ type: 'success', message: `Subject "${subjectForm.name}" created successfully.` })
      }
      setShowSubjectModal(false)
      setEditingSubject(null)
      setSubjectForm({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: 0 })
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

  // ==========================================
  // HANDLERS: STATUS TOGGLE & SAFE DELETION
  // ==========================================

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

  const handleConfirmDelete = async () => {
    if (!deleteConfirmation) return
    setActionLoading(true)
    const { type, id, code, name } = deleteConfirmation

    try {
      if (type === 'department') {
        await deleteDepartment(id)
        setFeedback({ type: 'success', message: `Department "${code} — ${name}" deleted successfully.` })
      } else if (type === 'course') {
        await deleteCourse(id)
        setFeedback({ type: 'success', message: `Course "${code} — ${name}" deleted successfully.` })
      } else if (type === 'subject') {
        await deleteSubject(id)
        setFeedback({ type: 'success', message: `Subject "${code} — ${name}" deleted successfully.` })
      } else if (type === 'assignment') {
        await deleteAssignment(id)
        setFeedback({ type: 'success', message: `Lecturer assignment for "${name}" removed.` })
      } else if (type === 'enrolment') {
        await deleteEnrollment(id)
        setFeedback({ type: 'success', message: `Enrolment of "${name}" removed.` })
      }
      setDeleteConfirmation(null)
      loadData()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({
        type: 'error',
        message: apiErr.message || `Cannot delete this ${type}. If it has linked records, consider deactivating it instead.`,
      })
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 rounded-xl border border-indigo-200 bg-white p-5 shadow-xs">
        <div>
          <button
            type="button"
            onClick={onBackToOverview}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-600 hover:text-indigo-800 transition-colors mb-2 cursor-pointer"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span>Back to Dashboard Overview</span>
          </button>
          <h2 className="text-xl font-bold tracking-tight text-slate-900">Academic Structure & Enrolment Management</h2>
          <p className="text-xs text-slate-500">
            Configure university departments, curriculum courses, subjects, lecturer assignments, and student enrolments with full relational safety.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'departments' && (
            <button
              onClick={() => {
                setEditingDept(null)
                setDeptForm({ code: '', name: '', description: '' })
                setShowDeptModal(true)
              }}
              className="rounded-lg bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-indigo-700 transition-colors inline-flex items-center gap-1.5 cursor-pointer"
            >
              <span>+ Add Department</span>
            </button>
          )}
          {activeTab === 'courses' && (
            <button
              onClick={() => {
                setEditingCourse(null)
                setCourseForm({ code: '', name: '', description: '', departmentId: departments.find((d) => d.active)?.id || departments[0]?.id || 0 })
                setShowCourseModal(true)
              }}
              className="rounded-lg bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-indigo-700 transition-colors inline-flex items-center gap-1.5 cursor-pointer"
            >
              <span>+ Add Course</span>
            </button>
          )}
          {activeTab === 'subjects' && (
            <button
              onClick={() => {
                setEditingSubject(null)
                setSubjectForm({ code: '', name: '', description: '', credits: 3, semester: 1, courseId: courses.find((c) => c.active)?.id || courses[0]?.id || 0 })
                setShowSubjectModal(true)
              }}
              className="rounded-lg bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-indigo-700 transition-colors inline-flex items-center gap-1.5 cursor-pointer"
            >
              <span>+ Add Subject</span>
            </button>
          )}
          {activeTab === 'assignments' && (
            <button
              onClick={() => {
                setAssignForm({
                  subjectId: subjects.find((s) => s.active)?.id || subjects[0]?.id || 0,
                  lecturerId: activeLecturers[0]?.id || 0,
                  academicYear: '2025/2026',
                  semester: 'Semester 1',
                })
                setShowAssignModal(true)
              }}
              className="rounded-lg bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-indigo-700 transition-colors inline-flex items-center gap-1.5 cursor-pointer"
            >
              <span>+ Assign Lecturer</span>
            </button>
          )}
          {activeTab === 'enrolments' && (
            <button
              onClick={() => {
                const firstCourseId = courses.find((c) => c.active)?.id || courses[0]?.id || 0
                const availableSubs = subjects.filter((s) => s.courseId === firstCourseId && s.active)
                setEnrolForm({
                  studentId: activeStudents[0]?.id || 0,
                  courseId: firstCourseId,
                  subjectId: availableSubs[0]?.id || 0,
                  academicYear: '2025/2026',
                  semester: 'Semester 1',
                })
                setShowEnrolModal(true)
              }}
              className="rounded-lg bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-indigo-700 transition-colors inline-flex items-center gap-1.5 cursor-pointer"
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
          <div className="flex items-center gap-2">
            <span className="font-bold">{feedback.type === 'success' ? '✓' : '⚠'}</span>
            <span>{feedback.message}</span>
          </div>
          <button onClick={() => setFeedback(null)} className="font-semibold text-xs px-2 py-0.5 rounded hover:bg-black/5 cursor-pointer">
            ✕
          </button>
        </div>
      )}

      {/* Tabs navigation */}
      <div className="flex border-b border-slate-200 overflow-x-auto">
        {(['departments', 'courses', 'subjects', 'assignments', 'enrolments'] as TabType[]).map((tab) => (
          <button
            key={tab}
            onClick={() => handleTabChange(tab)}
            className={`px-4 py-2.5 text-xs font-semibold capitalize border-b-2 whitespace-nowrap transition-colors cursor-pointer ${
              activeTab === tab
                ? 'border-indigo-600 text-indigo-600 bg-indigo-50/40'
                : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300'
            }`}
          >
            {tab === 'departments' && `Departments (${departments.length})`}
            {tab === 'courses' && `Courses & Degrees (${courses.length})`}
            {tab === 'subjects' && `Academic Subjects (${subjects.length})`}
            {tab === 'assignments' && `Lecturer Assignments (${assignments.length})`}
            {tab === 'enrolments' && `Student Enrolments (${enrollments.length})`}
          </button>
        ))}
      </div>

      {/* SEARCH & FILTER CONTROLS */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-white p-3.5 rounded-xl border border-slate-200 shadow-2xs">
        <div className="flex flex-wrap items-center gap-2 flex-1 min-w-[280px]">
          {/* Quick Search */}
          <div className="relative flex-1 min-w-[200px]">
            <input
              type="text"
              placeholder={`Search ${activeTab}...`}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-slate-50/50 pl-8 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 focus:bg-white focus:border-indigo-600 focus:outline-hidden"
            />
            <svg className="absolute left-2.5 top-2 h-3.5 w-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>

          {/* Tab Specific Filters */}
          {activeTab === 'courses' && (
            <select
              value={deptFilter}
              onChange={(e) => setDeptFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
              className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
            >
              <option value="ALL">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>{d.code} — {d.name}</option>
              ))}
            </select>
          )}

          {activeTab === 'subjects' && (
            <>
              <select
                value={courseFilter}
                onChange={(e) => setCourseFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
              >
                <option value="ALL">All Courses</option>
                {courses.map((c) => (
                  <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                ))}
              </select>
              <select
                value={semesterFilter}
                onChange={(e) => setSemesterFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
              >
                <option value="ALL">All Semesters</option>
                {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                  <option key={s} value={s}>Semester {s}</option>
                ))}
              </select>
            </>
          )}

          {activeTab === 'enrolments' && (
            <>
              <select
                value={courseFilter}
                onChange={(e) => setCourseFilter(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
                className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
              >
                <option value="ALL">All Courses</option>
                {courses.map((c) => (
                  <option key={c.id} value={c.id}>{c.code} — {c.name}</option>
                ))}
              </select>
              <select
                value={enrolmentStatusFilter}
                onChange={(e) => setEnrolmentStatusFilter(e.target.value as 'ALL' | EnrollmentStatus)}
                className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
              >
                <option value="ALL">All Enrolment Statuses</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="DROPPED">DROPPED</option>
              </select>
            </>
          )}

          {/* General Status Filter (Departments, Courses, Subjects, Assignments) */}
          {activeTab !== 'enrolments' && (
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as 'ALL' | 'ACTIVE' | 'INACTIVE')}
              className="rounded-lg border border-slate-300 bg-white px-2.5 py-1.5 text-xs text-slate-700 focus:outline-hidden"
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">Active Only</option>
              <option value="INACTIVE">Inactive Only</option>
            </select>
          )}
        </div>

        {/* Count & Reset */}
        <div className="flex items-center gap-2 text-xs text-slate-500">
          <span>
            {activeTab === 'departments' && `${filteredDepartments.length} of ${departments.length}`}
            {activeTab === 'courses' && `${filteredCourses.length} of ${courses.length}`}
            {activeTab === 'subjects' && `${filteredSubjects.length} of ${subjects.length}`}
            {activeTab === 'assignments' && `${filteredAssignments.length} of ${assignments.length}`}
            {activeTab === 'enrolments' && `${filteredEnrollments.length} of ${enrollments.length}`} records
          </span>
          {(searchTerm || statusFilter !== 'ALL' || deptFilter !== 'ALL' || courseFilter !== 'ALL' || semesterFilter !== 'ALL' || enrolmentStatusFilter !== 'ALL') && (
            <button
              onClick={() => {
                setSearchTerm('')
                setStatusFilter('ALL')
                setDeptFilter('ALL')
                setCourseFilter('ALL')
                setSemesterFilter('ALL')
                setEnrolmentStatusFilter('ALL')
              }}
              className="text-[11px] font-semibold text-indigo-600 hover:text-indigo-800 underline cursor-pointer"
            >
              Reset Filters
            </button>
          )}
        </div>
      </div>

      {/* Tab Panels */}
      {isLoading ? (
        <div className="flex items-center justify-center py-16 text-xs text-slate-500 gap-2">
          <svg className="h-5 w-5 animate-spin text-indigo-600" fill="none" viewBox="0 0 24 24">
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
              {filteredDepartments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  {departments.length === 0
                    ? 'No departments configured yet. Click "+ Add Department" to create one.'
                    : 'No departments match your search or filter criteria.'}
                </div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 select-none">
                    <tr>
                      <th onClick={() => handleSort('code')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Code {sortField === 'code' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('name')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Department Name {sortField === 'name' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3">Description</th>
                      <th onClick={() => handleSort('status')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Status {sortField === 'status' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredDepartments.map((dept, idx) => (
                      <tr key={dept.id} className={idx % 2 === 1 ? 'bg-slate-50/40 hover:bg-slate-50' : 'hover:bg-slate-50/50'}>
                        <td className="px-4 py-3 font-semibold text-slate-900">{dept.code}</td>
                        <td className="px-4 py-3 text-slate-900 font-medium">{dept.name}</td>
                        <td className="px-4 py-3 text-slate-500 truncate max-w-xs">{dept.description || '—'}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${dept.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {dept.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() =>
                              setViewingDetails({
                                title: `Department: ${dept.code} — ${dept.name}`,
                                badge: { label: dept.active ? 'Active' : 'Inactive', color: dept.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700' },
                                fields: [
                                  { label: 'Department ID', value: dept.id },
                                  { label: 'Code', value: dept.code },
                                  { label: 'Name', value: dept.name },
                                  { label: 'Description', value: dept.description || 'No description provided' },
                                  { label: 'Status', value: dept.active ? 'Active (Operational)' : 'Inactive' },
                                  { label: 'Created At', value: dept.createdAt || 'System Seeding' },
                                  { label: 'Last Updated', value: dept.updatedAt || '—' },
                                ],
                              })
                            }
                            className="font-semibold text-slate-600 hover:text-slate-900 cursor-pointer"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => {
                              setEditingDept(dept)
                              setDeptForm({ code: dept.code, name: dept.name, description: dept.description || '' })
                              setShowDeptModal(true)
                            }}
                            className="font-semibold text-indigo-600 hover:text-indigo-800 cursor-pointer"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleToggleDepartmentStatus(dept)}
                            className={`font-semibold cursor-pointer ${dept.active ? 'text-amber-600 hover:text-amber-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {dept.active ? 'Deactivate' : 'Activate'}
                          </button>
                          <button
                            onClick={() =>
                              setDeleteConfirmation({
                                type: 'department',
                                id: dept.id,
                                code: dept.code,
                                name: dept.name,
                                parentOrContext: 'Department hierarchy',
                              })
                            }
                            className="font-semibold text-rose-600 hover:text-rose-800 cursor-pointer"
                          >
                            Delete
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
              {filteredCourses.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  {courses.length === 0
                    ? 'No courses configured yet. Click "+ Add Course" to create one.'
                    : 'No courses match your search or filter criteria.'}
                </div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 select-none">
                    <tr>
                      <th onClick={() => handleSort('code')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Code {sortField === 'code' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('name')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Course Name {sortField === 'name' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('department')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Department {sortField === 'department' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('status')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Status {sortField === 'status' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredCourses.map((course, idx) => (
                      <tr key={course.id} className={idx % 2 === 1 ? 'bg-slate-50/40 hover:bg-slate-50' : 'hover:bg-slate-50/50'}>
                        <td className="px-4 py-3 font-semibold text-slate-900">{course.code}</td>
                        <td className="px-4 py-3 text-slate-900 font-medium">{course.name}</td>
                        <td className="px-4 py-3 text-slate-600">
                          <span className="font-semibold text-slate-800">{course.departmentCode}</span> — {course.departmentName}
                        </td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${course.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {course.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() =>
                              setViewingDetails({
                                title: `Course: ${course.code} — ${course.name}`,
                                badge: { label: course.active ? 'Active' : 'Inactive', color: course.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700' },
                                fields: [
                                  { label: 'Course ID', value: course.id },
                                  { label: 'Degree Code', value: course.code },
                                  { label: 'Program Name', value: course.name },
                                  { label: 'Department', value: `${course.departmentCode} — ${course.departmentName}` },
                                  { label: 'Description', value: course.description || 'No description provided' },
                                  { label: 'Status', value: course.active ? 'Active' : 'Inactive' },
                                  { label: 'Created At', value: course.createdAt || '—' },
                                  { label: 'Last Updated', value: course.updatedAt || '—' },
                                ],
                              })
                            }
                            className="font-semibold text-slate-600 hover:text-slate-900 cursor-pointer"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => {
                              setEditingCourse(course)
                              setCourseForm({
                                code: course.code,
                                name: course.name,
                                description: course.description || '',
                                departmentId: course.departmentId,
                              })
                              setShowCourseModal(true)
                            }}
                            className="font-semibold text-indigo-600 hover:text-indigo-800 cursor-pointer"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleToggleCourseStatus(course)}
                            className={`font-semibold cursor-pointer ${course.active ? 'text-amber-600 hover:text-amber-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {course.active ? 'Deactivate' : 'Activate'}
                          </button>
                          <button
                            onClick={() =>
                              setDeleteConfirmation({
                                type: 'course',
                                id: course.id,
                                code: course.code,
                                name: course.name,
                                parentOrContext: `Department: ${course.departmentName}`,
                              })
                            }
                            className="font-semibold text-rose-600 hover:text-rose-800 cursor-pointer"
                          >
                            Delete
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
              {filteredSubjects.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  {subjects.length === 0
                    ? 'No subjects configured yet. Click "+ Add Subject" to create one.'
                    : 'No subjects match your search or filter criteria.'}
                </div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 select-none">
                    <tr>
                      <th onClick={() => handleSort('code')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Code {sortField === 'code' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('name')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Subject Name {sortField === 'name' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('course')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Course / Dept {sortField === 'course' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('credits')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Credits {sortField === 'credits' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('semester')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Semester {sortField === 'semester' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('status')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Status {sortField === 'status' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredSubjects.map((sub, idx) => (
                      <tr key={sub.id} className={idx % 2 === 1 ? 'bg-slate-50/40 hover:bg-slate-50' : 'hover:bg-slate-50/50'}>
                        <td className="px-4 py-3 font-semibold text-slate-900">{sub.code}</td>
                        <td className="px-4 py-3 text-slate-900 font-medium">{sub.name}</td>
                        <td className="px-4 py-3 text-slate-600">{sub.courseCode} ({sub.departmentCode})</td>
                        <td className="px-4 py-3 font-semibold text-slate-800">{sub.credits} Units</td>
                        <td className="px-4 py-3">Sem {sub.semester}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${sub.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {sub.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() =>
                              setViewingDetails({
                                title: `Subject: ${sub.code} — ${sub.name}`,
                                badge: { label: sub.active ? 'Active' : 'Inactive', color: sub.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700' },
                                fields: [
                                  { label: 'Subject ID', value: sub.id },
                                  { label: 'Code', value: sub.code },
                                  { label: 'Title', value: sub.name },
                                  { label: 'Degree Course', value: `${sub.courseCode} — ${sub.courseName}` },
                                  { label: 'Department', value: sub.departmentCode },
                                  { label: 'Academic Credits', value: `${sub.credits} Units` },
                                  { label: 'Curriculum Semester', value: `Semester ${sub.semester}` },
                                  { label: 'Description', value: sub.description || 'No description provided' },
                                  { label: 'Status', value: sub.active ? 'Active' : 'Inactive' },
                                  { label: 'Created At', value: sub.createdAt || '—' },
                                  { label: 'Last Updated', value: sub.updatedAt || '—' },
                                ],
                              })
                            }
                            className="font-semibold text-slate-600 hover:text-slate-900 cursor-pointer"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => {
                              setEditingSubject(sub)
                              setSubjectForm({
                                code: sub.code,
                                name: sub.name,
                                description: sub.description || '',
                                credits: sub.credits,
                                semester: sub.semester,
                                courseId: sub.courseId,
                              })
                              setShowSubjectModal(true)
                            }}
                            className="font-semibold text-indigo-600 hover:text-indigo-800 cursor-pointer"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleToggleSubjectStatus(sub)}
                            className={`font-semibold cursor-pointer ${sub.active ? 'text-amber-600 hover:text-amber-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {sub.active ? 'Deactivate' : 'Activate'}
                          </button>
                          <button
                            onClick={() =>
                              setDeleteConfirmation({
                                type: 'subject',
                                id: sub.id,
                                code: sub.code,
                                name: sub.name,
                                parentOrContext: `Course: ${sub.courseCode}`,
                              })
                            }
                            className="font-semibold text-rose-600 hover:text-rose-800 cursor-pointer"
                          >
                            Delete
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
              {filteredAssignments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  {assignments.length === 0
                    ? 'No lecturer assignments recorded yet. Click "+ Assign Lecturer" to create one.'
                    : 'No assignments match your search or filter criteria.'}
                </div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 select-none">
                    <tr>
                      <th onClick={() => handleSort('subject')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Subject {sortField === 'subject' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('lecturer')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Assigned Lecturer {sortField === 'lecturer' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('period')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Academic Period {sortField === 'period' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('status')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Status {sortField === 'status' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredAssignments.map((assign, idx) => (
                      <tr key={assign.id} className={idx % 2 === 1 ? 'bg-slate-50/40 hover:bg-slate-50' : 'hover:bg-slate-50/50'}>
                        <td className="px-4 py-3 font-semibold text-slate-900">
                          {assign.subjectCode} — {assign.subjectName}
                        </td>
                        <td className="px-4 py-3">
                          <p className="font-semibold text-slate-900">{assign.lecturerName}</p>
                          <p className="text-[11px] text-slate-500">{assign.lecturerIdentifier} • {assign.lecturerEmail}</p>
                        </td>
                        <td className="px-4 py-3">{assign.academicYear} • {assign.semester}</td>
                        <td className="px-4 py-3">
                          <span className={`inline-flex rounded-full px-2 py-0.5 text-[10px] font-semibold ${assign.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700'}`}>
                            {assign.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() =>
                              setViewingDetails({
                                title: `Lecturer Assignment: ${assign.subjectCode}`,
                                badge: { label: assign.active ? 'Active' : 'Inactive', color: assign.active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-200 text-slate-700' },
                                fields: [
                                  { label: 'Assignment ID', value: assign.id },
                                  { label: 'Subject', value: `${assign.subjectCode} — ${assign.subjectName}` },
                                  { label: 'Faculty Member', value: assign.lecturerName },
                                  { label: 'Staff ID', value: assign.lecturerIdentifier },
                                  { label: 'Email', value: assign.lecturerEmail },
                                  { label: 'Academic Year', value: assign.academicYear },
                                  { label: 'Semester', value: assign.semester },
                                  { label: 'Status', value: assign.active ? 'Active Teaching Assignment' : 'Inactive' },
                                  { label: 'Assigned At', value: assign.assignedAt || '—' },
                                ],
                              })
                            }
                            className="font-semibold text-slate-600 hover:text-slate-900 cursor-pointer"
                          >
                            Details
                          </button>
                          <button
                            onClick={() => handleToggleAssignmentStatus(assign)}
                            className={`font-semibold cursor-pointer ${assign.active ? 'text-amber-600 hover:text-amber-800' : 'text-emerald-600 hover:text-emerald-800'}`}
                          >
                            {assign.active ? 'Deactivate' : 'Activate'}
                          </button>
                          <button
                            onClick={() =>
                              setDeleteConfirmation({
                                type: 'assignment',
                                id: assign.id,
                                code: assign.subjectCode,
                                name: `${assign.lecturerName} (${assign.subjectCode})`,
                                parentOrContext: `Period: ${assign.academicYear} ${assign.semester}`,
                              })
                            }
                            className="font-semibold text-rose-600 hover:text-rose-800 cursor-pointer"
                          >
                            Delete
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
              {filteredEnrollments.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  {enrollments.length === 0
                    ? 'No student enrolments recorded yet. Click "+ Enrol Student" to create one.'
                    : 'No enrolments match your search or filter criteria.'}
                </div>
              ) : (
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 select-none">
                    <tr>
                      <th onClick={() => handleSort('student')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Student {sortField === 'student' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('subject')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Course / Subject {sortField === 'subject' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('period')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Period {sortField === 'period' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('credits')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Credits {sortField === 'credits' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th onClick={() => handleSort('status')} className="px-4 py-3 cursor-pointer hover:bg-slate-100">
                        Status {sortField === 'status' ? (sortAsc ? '▲' : '▼') : '↕'}
                      </th>
                      <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredEnrollments.map((enrol, idx) => (
                      <tr key={enrol.id} className={idx % 2 === 1 ? 'bg-slate-50/40 hover:bg-slate-50' : 'hover:bg-slate-50/50'}>
                        <td className="px-4 py-3">
                          <p className="font-semibold text-slate-900">{enrol.studentName}</p>
                          <p className="text-[11px] text-slate-500">{enrol.studentIdentifier} • {enrol.studentEmail}</p>
                        </td>
                        <td className="px-4 py-3">
                          <p className="font-medium text-slate-900">{enrol.subjectCode} — {enrol.subjectName}</p>
                          <p className="text-[11px] text-slate-500">{enrol.courseName}</p>
                        </td>
                        <td className="px-4 py-3">{enrol.academicYear} • {enrol.semester}</td>
                        <td className="px-4 py-3 font-semibold text-slate-800">{enrol.credits} Units</td>
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
                        <td className="px-4 py-3 text-right space-x-2">
                          <button
                            onClick={() =>
                              setViewingDetails({
                                title: `Student Enrolment: ${enrol.studentName}`,
                                badge: {
                                  label: enrol.status,
                                  color:
                                    enrol.status === 'ACTIVE'
                                      ? 'bg-emerald-100 text-emerald-800'
                                      : enrol.status === 'COMPLETED'
                                      ? 'bg-blue-100 text-blue-800'
                                      : 'bg-rose-100 text-rose-800',
                                },
                                fields: [
                                  { label: 'Enrolment ID', value: enrol.id },
                                  { label: 'Student Name', value: enrol.studentName },
                                  { label: 'Index / Student ID', value: enrol.studentIdentifier },
                                  { label: 'Student Email', value: enrol.studentEmail },
                                  { label: 'Subject', value: `${enrol.subjectCode} — ${enrol.subjectName}` },
                                  { label: 'Degree Course', value: enrol.courseName },
                                  { label: 'Credits', value: `${enrol.credits} Units` },
                                  { label: 'Academic Term', value: `${enrol.academicYear} • ${enrol.semester}` },
                                  { label: 'Status', value: enrol.status },
                                  { label: 'Enrolled At', value: enrol.enrolledAt || '—' },
                                ],
                              })
                            }
                            className="font-semibold text-slate-600 hover:text-slate-900 cursor-pointer"
                          >
                            Details
                          </button>
                          <select
                            value={enrol.status}
                            onChange={(e) => handleUpdateEnrollmentStatus(enrol.id, e.target.value as EnrollmentStatus)}
                            className="rounded border border-slate-300 bg-white px-2 py-1 text-xs text-slate-800 focus:outline-hidden focus:ring-1 focus:ring-indigo-600 cursor-pointer"
                          >
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="DROPPED">DROPPED</option>
                          </select>
                          <button
                            onClick={() =>
                              setDeleteConfirmation({
                                type: 'enrolment',
                                id: enrol.id,
                                code: enrol.studentIdentifier,
                                name: `${enrol.studentName} in ${enrol.subjectCode}`,
                                parentOrContext: `Subject: ${enrol.subjectName}`,
                              })
                            }
                            className="font-semibold text-rose-600 hover:text-rose-800 cursor-pointer"
                          >
                            Delete
                          </button>
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

      {/* RECORD DETAILS MODAL */}
      {viewingDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-lg rounded-xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex items-start justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="text-base font-bold text-slate-900">{viewingDetails.title}</h3>
                <p className="text-xs text-slate-500">Record metadata and entity attributes</p>
              </div>
              {viewingDetails.badge && (
                <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${viewingDetails.badge.color}`}>
                  {viewingDetails.badge.label}
                </span>
              )}
            </div>

            <div className="divide-y divide-slate-100 text-xs">
              {viewingDetails.fields.map((f, i) => (
                <div key={i} className="py-2 flex items-center justify-between">
                  <span className="font-semibold text-slate-500">{f.label}</span>
                  <span className="text-slate-900 font-medium text-right max-w-xs">{f.value !== undefined && f.value !== null ? String(f.value) : '—'}</span>
                </div>
              ))}
            </div>

            <div className="flex justify-end pt-2">
              <button
                type="button"
                onClick={() => setViewingDetails(null)}
                className="rounded-lg bg-slate-100 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-200 cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deleteConfirmation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl border border-rose-200 space-y-4">
            <div className="flex items-center gap-3 text-rose-600">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-rose-100">
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">Confirm Relational Safe Delete</h3>
                <p className="text-xs text-slate-500 capitalize">{deleteConfirmation.type} Deletion Gate</p>
              </div>
            </div>

            <div className="rounded-lg bg-rose-50/50 border border-rose-200 p-3.5 text-xs text-rose-900 space-y-2">
              <p className="font-semibold">
                Are you sure you want to delete <span className="font-bold underline">{deleteConfirmation.name}</span>?
              </p>
              <p className="text-[11px] text-rose-800 leading-relaxed">
                Relational integrity rule: If this record is linked to dependent records (such as courses under a department, subjects under a course, or marks/attendance under a student enrolment), deletion will be refused by the server. If dependent data exists, consider using <strong>Deactivate</strong> instead.
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                disabled={actionLoading}
                onClick={() => setDeleteConfirmation(null)}
                className="rounded-lg px-3.5 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 cursor-pointer disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={actionLoading}
                onClick={handleConfirmDelete}
                className="rounded-lg bg-rose-600 px-4 py-1.5 text-xs font-semibold text-white hover:bg-rose-700 cursor-pointer shadow-xs disabled:opacity-50 inline-flex items-center gap-1.5"
              >
                {actionLoading && (
                  <svg className="h-3.5 w-3.5 animate-spin text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                )}
                <span>Confirm Permanent Delete</span>
              </button>
            </div>
          </div>
        </div>
      )}

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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600 uppercase"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={deptForm.description}
                  onChange={(e) => setDeptForm({ ...deptForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="rounded bg-indigo-600 px-4 py-1.5 font-semibold text-white hover:bg-indigo-700 cursor-pointer shadow-xs"
                >
                  {editingDept ? 'Update Department' : 'Save Department'}
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
            <h3 className="text-base font-bold text-slate-900">{editingCourse ? 'Edit Course / Degree' : 'Create Course / Degree Program'}</h3>
            <form onSubmit={handleSaveCourse} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Department</label>
                <select
                  required
                  value={courseForm.departmentId}
                  onChange={(e) => setCourseForm({ ...courseForm, departmentId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600 uppercase"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={courseForm.description}
                  onChange={(e) => setCourseForm({ ...courseForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="rounded bg-indigo-600 px-4 py-1.5 font-semibold text-white hover:bg-indigo-700 cursor-pointer shadow-xs"
                >
                  {editingCourse ? 'Update Course' : 'Save Course'}
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
            <h3 className="text-base font-bold text-slate-900">{editingSubject ? 'Edit Academic Subject' : 'Create Academic Subject'}</h3>
            <form onSubmit={handleSaveSubject} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Associated Course</label>
                <select
                  required
                  value={subjectForm.courseId}
                  onChange={(e) => setSubjectForm({ ...subjectForm, courseId: Number(e.target.value) })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600 uppercase"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
                  />
                </div>
              </div>
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={subjectForm.description}
                  onChange={(e) => setSubjectForm({ ...subjectForm, description: e.target.value })}
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="rounded bg-indigo-600 px-4 py-1.5 font-semibold text-white hover:bg-indigo-700 cursor-pointer shadow-xs"
                >
                  {editingSubject ? 'Update Subject' : 'Save Subject'}
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Semester</label>
                  <input
                    type="text"
                    required
                    value={assignForm.semester}
                    onChange={(e) => setAssignForm({ ...assignForm, semester: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="rounded bg-indigo-600 px-4 py-1.5 font-semibold text-white hover:bg-indigo-700 cursor-pointer shadow-xs"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
                  />
                </div>
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Semester</label>
                  <input
                    type="text"
                    required
                    value={enrolForm.semester}
                    onChange={(e) => setEnrolForm({ ...enrolForm, semester: e.target.value })}
                    className="w-full rounded border border-slate-300 p-2 focus:ring-1 focus:ring-indigo-600"
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
                  className="rounded bg-indigo-600 px-4 py-1.5 font-semibold text-white hover:bg-indigo-700 cursor-pointer shadow-xs"
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
