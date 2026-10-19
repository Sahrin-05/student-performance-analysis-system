import { authenticatedFetch, API_BASE_URL, type ApiErrorData } from './authService';

export interface PendingUser {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: 'STUDENT' | 'LECTURER' | 'ADMIN';
  status: 'PENDING_APPROVAL' | 'ACTIVE' | 'REJECTED';
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
