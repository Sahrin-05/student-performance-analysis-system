import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  saveAuthSession,
  getStoredAuthSession,
  clearAuthSession,
  getAuthToken,
  onUnauthorized,
  authenticatedFetch,
  loginUser,
  type LoginResponseData,
} from '../services/authService';

describe('authService', () => {
  const mockUser: LoginResponseData = {
    id: 1,
    fullName: 'Jane Doe',
    identifier: 'STU001',
    email: 'jane@spas.edu',
    role: 'STUDENT',
    status: 'ACTIVE',
    token: 'jwt.token.here',
    message: 'Welcome back',
  };

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('saves auth session to localStorage when rememberMe is true', () => {
    saveAuthSession(mockUser, true);
    expect(localStorage.getItem('spas_auth_session')).toContain('jwt.token.here');
    expect(sessionStorage.getItem('spas_auth_session')).toBeNull();

    const stored = getStoredAuthSession();
    expect(stored).toEqual(mockUser);
    expect(getAuthToken()).toBe('jwt.token.here');
  });

  it('saves auth session to sessionStorage when rememberMe is false', () => {
    saveAuthSession(mockUser, false);
    expect(sessionStorage.getItem('spas_auth_session')).toContain('jwt.token.here');
    expect(localStorage.getItem('spas_auth_session')).toBeNull();

    const stored = getStoredAuthSession();
    expect(stored).toEqual(mockUser);
  });

  it('clears auth session from both storages', () => {
    saveAuthSession(mockUser, true);
    clearAuthSession();
    expect(getStoredAuthSession()).toBeNull();
    expect(getAuthToken()).toBeNull();
  });

  it('triggers onUnauthorized listener when HTTP 401 is received in authenticatedFetch', async () => {
    saveAuthSession(mockUser, true);

    const listener = vi.fn();
    const unsubscribe = onUnauthorized(listener);

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ message: 'Token expired' }), {
        status: 401,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    const res = await authenticatedFetch('/api/test');
    expect(res.status).toBe(401);
    expect(listener).toHaveBeenCalledTimes(1);
    expect(getStoredAuthSession()).toBeNull();

    unsubscribe();
  });

  it('attaches Authorization Bearer header when token exists', async () => {
    saveAuthSession(mockUser, true);

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    await authenticatedFetch('http://localhost:8080/api/test', {
      headers: { 'Custom-Header': 'value' },
    });

    expect(fetchSpy).toHaveBeenCalled();
    const headers = fetchSpy.mock.calls[0][1]?.headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer jwt.token.here');
    expect(headers.get('Custom-Header')).toBe('value');
  });

  it('loginUser sends credentials and returns LoginResponseData on success', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(mockUser), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    const result = await loginUser({
      identifierOrEmail: 'STU001',
      password: 'password123',
    });

    expect(result.token).toBe('jwt.token.here');
    expect(result.role).toBe('STUDENT');
  });
});
