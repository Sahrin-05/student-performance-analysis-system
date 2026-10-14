export type AllowedRole = 'STUDENT' | 'LECTURER';

export interface RegisterPayload {
  fullName: string;
  identifier: string;
  email: string;
  password: string;
  role: AllowedRole;
}

export interface RegisterResponseData {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: 'STUDENT' | 'LECTURER' | 'ADMIN';
  status: 'PENDING_APPROVAL' | 'ACTIVE';
  message: string;
}

export interface ApiErrorData {
  status?: number;
  message: string;
  errors?: Record<string, string>;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Sends an account registration request to the Spring Boot backend.
 * Uses native fetch without external dependencies.
 */
export async function registerUser(payload: RegisterPayload): Promise<RegisterResponseData> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Registration request could not be completed.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    error.errors = data?.errors;
    throw error;
  }

  return data as RegisterResponseData;
}

export interface LoginPayload {
  identifierOrEmail: string;
  password: string;
}

export interface LoginResponseData {
  id: number;
  fullName: string;
  identifier: string;
  email: string;
  role: 'STUDENT' | 'LECTURER' | 'ADMIN';
  status: 'PENDING_APPROVAL' | 'ACTIVE';
  message: string;
}

/**
 * Sends a login request to the Spring Boot backend.
 * Uses native fetch without external dependencies.
 */
export async function loginUser(payload: LoginPayload): Promise<LoginResponseData> {
  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(
      'Unable to connect to the backend server. Please verify that the Spring Boot service is running on port 8080.'
    );
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || 'Login request could not be completed.';
    const error = new Error(errorMsg) as Error & ApiErrorData;
    error.status = response.status;
    error.errors = data?.errors;
    throw error;
  }

  return data as LoginResponseData;
}

