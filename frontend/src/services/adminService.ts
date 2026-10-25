import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService';

export type UserRole = 'STUDENT' | 'LECTURER' | 'ADMIN';
export type UserStatus = 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED' | 'SUSPENDED';

export interface UserAccount {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  message?: string | null;
}

export interface UserFilterParams {
  role?: UserRole | '';
  status?: UserStatus | '';
  search?: string;
}

export interface PendingUser {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  message?: string | null;
}

/**
 * Fetches all user registration requests awaiting administrator approval.
 * Requires ADMIN role.
 */
export async function fetchPendingUsers(): Promise<PendingUser[]> {
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users/pending`);
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg =
      data?.message ||
      (response.status === 403
        ? 'Access denied. You do not have administrator permissions to view pending users.'
        : 'Failed to retrieve pending users.');
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return (Array.isArray(data) ? data : []) as PendingUser[];
}

/**
 * Approves a user's pending registration request, transitioning their status to ACTIVE.
 * Requires ADMIN role.
 */
export async function approveUser(id: number): Promise<PendingUser> {
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users/${id}/approve`, {
      method: 'PUT',
    });
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Failed to approve user account.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return data as PendingUser;
}

/**
 * Rejects a user's pending registration request, transitioning their status to REJECTED.
 * Requires ADMIN role.
 */
export async function rejectUser(id: number): Promise<PendingUser> {
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users/${id}/reject`, {
      method: 'PUT',
    });
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Failed to reject user account.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return data as PendingUser;
}

/**
 * Fetches all user accounts across the institution with optional filters.
 * Requires ADMIN role.
 */
export async function fetchAllUsers(filters?: UserFilterParams): Promise<UserAccount[]> {
  const params = new URLSearchParams();
  if (filters?.role) params.append('role', filters.role);
  if (filters?.status) params.append('status', filters.status);
  if (filters?.search && filters.search.trim()) params.append('search', filters.search.trim());

  const query = params.toString() ? `?${params.toString()}` : '';
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users${query}`);
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Failed to retrieve user accounts.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return (Array.isArray(data) ? data : []) as UserAccount[];
}

/**
 * Fetches single user account details by ID.
 * Requires ADMIN role.
 */
export async function fetchUserById(id: number): Promise<UserAccount> {
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users/${id}`);
  } catch {
    throw new Error('Unable to connect to the backend server.');
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Failed to retrieve user details.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return data as UserAccount;
}

/**
 * Updates a user account's status (e.g. SUSPENDED, ACTIVE, REJECTED).
 * Requires ADMIN role.
 */
export async function updateUserStatus(
  id: number,
  status: UserStatus,
  reason?: string
): Promise<UserAccount> {
  let response: Response;

  try {
    response = await authenticatedFetch(`${API_BASE_URL}/admin/users/${id}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status, reason }),
    });
  } catch {
    throw new Error('Unable to connect to the backend server.');
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Failed to update user status.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    throw error;
  }

  return data as UserAccount;
}
