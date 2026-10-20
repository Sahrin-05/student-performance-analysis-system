import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService';

export interface Department {
  id: number;
  code: string;
  name: string;
  description?: string | null;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface DepartmentPayload {
  code: string;
  name: string;
  description?: string;
}

export interface Course {
  id: number;
  code: string;
  name: string;
  description?: string | null;
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CoursePayload {
  code: string;
  name: string;
  description?: string;
  departmentId: number;
}

export interface Subject {
  id: number;
  code: string;
  name: string;
  description?: string | null;
  credits: number;
  semester: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  departmentId: number;
  departmentCode: string;
  departmentName: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SubjectPayload {
  code: string;
  name: string;
  description?: string;
  credits: number;
  semester: number;
  courseId: number;
}

export interface LecturerAssignment {
  id: number;
  subjectId: number;
  subjectCode: string;
  subjectName: string;
  courseName: string;
  lecturerId: number;
  lecturerName: string;
  lecturerEmail: string;
  lecturerIdentifier: string;
  academicYear: string;
  semester: string;
  active: boolean;
  assignedAt?: string;
}

export interface AssignmentPayload {
  subjectId: number;
  lecturerId: number;
  academicYear: string;
  semester: string;
}

export type EnrollmentStatus = 'ACTIVE' | 'COMPLETED' | 'DROPPED';

export interface Enrollment {
  id: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  studentIdentifier: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  subjectId: number;
  subjectCode: string;
  subjectName: string;
  credits: number;
  academicYear: string;
  semester: string;
  status: EnrollmentStatus;
  enrolledAt?: string;
}

export interface EnrollmentPayload {
  studentId: number;
  courseId: number;
  subjectId: number;
  academicYear: string;
  semester: string;
}

export interface ActiveUserOption {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: 'STUDENT' | 'LECTURER' | 'ADMIN';
  status: 'ACTIVE';
}

async function handleResponse<T>(response: Response, defaultError: string): Promise<T> {
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    let errorMsg = data?.message || defaultError;
    if (data?.errors && typeof data.errors === 'object') {
      const fieldErrors = Object.values(data.errors).join(', ');
      if (fieldErrors) {
        errorMsg = `${errorMsg}: ${fieldErrors}`;
      }
    }
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    error.errors = data?.errors;
    error.errorCode = data?.errorCode;
    throw error;
  }

  return data as T;
}

// ==========================================
// ADMIN ACADEMIC APIS
// ==========================================

export async function fetchDepartments(activeOnly?: boolean): Promise<Department[]> {
  const query = activeOnly ? '?activeOnly=true' : '';
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/departments${query}`);
  return handleResponse<Department[]>(response, 'Failed to fetch departments.');
}

export async function createDepartment(payload: DepartmentPayload): Promise<Department> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/departments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Department>(response, 'Failed to create department.');
}

export async function updateDepartment(id: number, payload: DepartmentPayload): Promise<Department> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/departments/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Department>(response, 'Failed to update department.');
}

export async function setDepartmentStatus(id: number, active: boolean): Promise<Department> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/departments/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  });
  return handleResponse<Department>(response, 'Failed to update department status.');
}

export async function fetchCourses(departmentId?: number, activeOnly?: boolean): Promise<Course[]> {
  const params = new URLSearchParams();
  if (departmentId) params.append('departmentId', departmentId.toString());
  if (activeOnly) params.append('activeOnly', 'true');
  const qs = params.toString() ? `?${params.toString()}` : '';

  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/courses${qs}`);
  return handleResponse<Course[]>(response, 'Failed to fetch courses.');
}

export async function createCourse(payload: CoursePayload): Promise<Course> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/courses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Course>(response, 'Failed to create course.');
}

export async function updateCourse(id: number, payload: CoursePayload): Promise<Course> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/courses/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Course>(response, 'Failed to update course.');
}

export async function setCourseStatus(id: number, active: boolean): Promise<Course> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/courses/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  });
  return handleResponse<Course>(response, 'Failed to update course status.');
}

export async function fetchSubjects(courseId?: number, departmentId?: number, activeOnly?: boolean): Promise<Subject[]> {
  const params = new URLSearchParams();
  if (courseId) params.append('courseId', courseId.toString());
  if (departmentId) params.append('departmentId', departmentId.toString());
  if (activeOnly) params.append('activeOnly', 'true');
  const qs = params.toString() ? `?${params.toString()}` : '';

  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/subjects${qs}`);
  return handleResponse<Subject[]>(response, 'Failed to fetch subjects.');
}

export async function createSubject(payload: SubjectPayload): Promise<Subject> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/subjects`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Subject>(response, 'Failed to create subject.');
}

export async function updateSubject(id: number, payload: SubjectPayload): Promise<Subject> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/subjects/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Subject>(response, 'Failed to update subject.');
}

export async function setSubjectStatus(id: number, active: boolean): Promise<Subject> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/subjects/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  });
  return handleResponse<Subject>(response, 'Failed to update subject status.');
}

export async function fetchAssignments(subjectId?: number, lecturerId?: number): Promise<LecturerAssignment[]> {
  const params = new URLSearchParams();
  if (subjectId) params.append('subjectId', subjectId.toString());
  if (lecturerId) params.append('lecturerId', lecturerId.toString());
  const qs = params.toString() ? `?${params.toString()}` : '';

  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/lecturer-assignments${qs}`);
  return handleResponse<LecturerAssignment[]>(response, 'Failed to fetch lecturer assignments.');
}

export async function assignLecturer(payload: AssignmentPayload): Promise<LecturerAssignment> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/lecturer-assignments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<LecturerAssignment>(response, 'Failed to assign lecturer.');
}

export async function setAssignmentStatus(id: number, active: boolean): Promise<LecturerAssignment> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/lecturer-assignments/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ active }),
  });
  return handleResponse<LecturerAssignment>(response, 'Failed to update assignment status.');
}

export async function fetchEnrollments(courseId?: number, subjectId?: number, studentId?: number): Promise<Enrollment[]> {
  const params = new URLSearchParams();
  if (courseId) params.append('courseId', courseId.toString());
  if (subjectId) params.append('subjectId', subjectId.toString());
  if (studentId) params.append('studentId', studentId.toString());
  const qs = params.toString() ? `?${params.toString()}` : '';

  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/enrolments${qs}`);
  return handleResponse<Enrollment[]>(response, 'Failed to fetch student enrolments.');
}

export async function enrolStudent(payload: EnrollmentPayload): Promise<Enrollment> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/enrolments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse<Enrollment>(response, 'Failed to enrol student.');
}

export async function updateEnrollmentStatus(id: number, status: EnrollmentStatus): Promise<Enrollment> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/enrolments/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
  return handleResponse<Enrollment>(response, 'Failed to update enrolment status.');
}

export async function fetchActiveLecturers(): Promise<ActiveUserOption[]> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/active-lecturers`);
  return handleResponse<ActiveUserOption[]>(response, 'Failed to fetch active lecturers.');
}

export async function fetchActiveStudents(): Promise<ActiveUserOption[]> {
  const response = await authenticatedFetch(`${API_BASE_URL}/admin/academic/active-students`);
  return handleResponse<ActiveUserOption[]>(response, 'Failed to fetch active students.');
}

// ==========================================
// LECTURER PORTAL APIS
// ==========================================

export async function fetchLecturerMySubjects(): Promise<Subject[]> {
  const response = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/my-subjects`);
  return handleResponse<Subject[]>(response, 'Failed to fetch assigned subjects.');
}

export async function fetchLecturerSubjectStudents(subjectId: number): Promise<Enrollment[]> {
  const response = await authenticatedFetch(`${API_BASE_URL}/academic/lecturer/subjects/${subjectId}/students`);
  return handleResponse<Enrollment[]>(response, 'Failed to fetch enrolled students for this subject.');
}

// ==========================================
// STUDENT PORTAL APIS
// ==========================================

export async function fetchStudentMyEnrolments(): Promise<Enrollment[]> {
  const response = await authenticatedFetch(`${API_BASE_URL}/academic/student/my-enrolments`);
  return handleResponse<Enrollment[]>(response, 'Failed to fetch my enrolled subjects.');
}
