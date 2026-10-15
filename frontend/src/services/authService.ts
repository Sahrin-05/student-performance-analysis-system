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
  token: string;
  tokenType?: string;
}

const AUTH_STORAGE_KEY = 'spas_auth_session';

type UnauthorizedListener = () => void;
const unauthorizedListeners: Set<UnauthorizedListener> = new Set();

/**
 * Registers a listener to be notified when an HTTP 401 response occurs.
 * Returns an unregister cleanup function.
 */
export function onUnauthorized(listener: UnauthorizedListener): () => void {
  unauthorizedListeners.add(listener);
  return () => {
    unauthorizedListeners.delete(listener);
  };
}

function notifyUnauthorized(): void {
  clearAuthSession();
  unauthorizedListeners.forEach((listener) => {
    try {
      listener();
    } catch {
      // Ignore listener error
    }
  });
}

/**
 * Persists the authenticated session to localStorage (if rememberMe is true)
 * or sessionStorage (if rememberMe is false). Removes conflicting session from the other storage.
 */
export function saveAuthSession(data: LoginResponseData, rememberMe: boolean): void {
  const serialized = JSON.stringify(data);
  try {
    if (rememberMe) {
      localStorage.setItem(AUTH_STORAGE_KEY, serialized);
      sessionStorage.removeItem(AUTH_STORAGE_KEY);
    } else {
      sessionStorage.setItem(AUTH_STORAGE_KEY, serialized);
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }
  } catch {
    // Storage access may fail if disabled or quota exceeded
  }
}

/**
 * Retrieves and defensively validates the session from localStorage or sessionStorage.
 * If data is corrupted or missing essential fields, purges both stores and returns null.
 */
export function getStoredAuthSession(): LoginResponseData | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY) || sessionStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
      return null;
    }

    const parsed = JSON.parse(raw) as Partial<LoginResponseData>;
    if (
      typeof parsed === 'object' &&
      parsed !== null &&
      typeof parsed.id === 'number' &&
      typeof parsed.token === 'string' &&
      parsed.token.trim().length > 0 &&
      typeof parsed.email === 'string' &&
      parsed.email.trim().length > 0 &&
      (parsed.role === 'STUDENT' || parsed.role === 'LECTURER' || parsed.role === 'ADMIN') &&
      parsed.status === 'ACTIVE'
    ) {
      return parsed as LoginResponseData;
    }

    clearAuthSession();
    return null;
  } catch {
    clearAuthSession();
    return null;
  }
}

/**
 * Removes the stored authentication session from both localStorage and sessionStorage.
 */
export function clearAuthSession(): void {
  try {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
  } catch {
    // Ignore storage clear failures
  }
}

/**
 * Executes a fetch request with the Bearer JWT token attached.
 * If HTTP 401 is returned, clears the session and triggers unauthorized listeners.
 * Does NOT treat HTTP 403 as automatic logout (it represents forbidden access).
 */
export async function authenticatedFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const session = getStoredAuthSession();
  const headers = new Headers(options.headers || {});

  if (session?.token) {
    headers.set('Authorization', `Bearer ${session.token}`);
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    notifyUnauthorized();
  }

  return response;
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

