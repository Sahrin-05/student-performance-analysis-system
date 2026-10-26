import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import App from '../App';
import * as authService from '../services/authService';

vi.mock('../services/authService', async (importOriginal) => {
  const actual = await importOriginal<typeof authService>();
  return {
    ...actual,
    getStoredAuthSession: vi.fn(),
    clearAuthSession: vi.fn(),
    onUnauthorized: vi.fn(() => () => {}),
  };
});

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders public landing page when no authenticated session exists', () => {
    vi.mocked(authService.getStoredAuthSession).mockReturnValue(null);

    render(<App />);

    expect(screen.getByText('SPAS Portal')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Sign In$/i })).toBeInTheDocument();
  });

  it('navigates to Login page from public landing page', () => {
    vi.mocked(authService.getStoredAuthSession).mockReturnValue(null);

    render(<App />);

    fireEvent.click(screen.getByRole('button', { name: /^Sign In$/i }));

    expect(screen.getByLabelText(/Institutional Email or Student \/ Staff ID/i)).toBeInTheDocument();
  });

  it('renders Admin dashboard with System Audit Trail navigation when logged in as ADMIN', () => {
    const adminUser: authService.LoginResponseData = {
      id: 1,
      fullName: 'System Administrator',
      identifier: 'ADMIN001',
      email: 'admin@spas.edu',
      role: 'ADMIN',
      status: 'ACTIVE',
      token: 'jwt.admin.token',
      message: 'Admin login',
    };

    vi.mocked(authService.getStoredAuthSession).mockReturnValue(adminUser);

    render(<App />);

    expect(screen.getByText(/Administrator Management Center/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /System Audit Trail/i })).toBeInTheDocument();
  });

  it('renders Lecturer dashboard when logged in as LECTURER', () => {
    const lecturerUser: authService.LoginResponseData = {
      id: 2,
      fullName: 'Dr. Alan Turing',
      identifier: 'LEC001',
      email: 'alan@spas.edu',
      role: 'LECTURER',
      status: 'ACTIVE',
      token: 'jwt.lecturer.token',
      message: 'Lecturer login',
    };

    vi.mocked(authService.getStoredAuthSession).mockReturnValue(lecturerUser);

    render(<App />);

    expect(screen.getByText(/Faculty Instruction & Grading Portal/i)).toBeInTheDocument();
  });

  it('renders Student dashboard when logged in as STUDENT', () => {
    const studentUser: authService.LoginResponseData = {
      id: 3,
      fullName: 'Ada Lovelace',
      identifier: 'STU001',
      email: 'ada@spas.edu',
      role: 'STUDENT',
      status: 'ACTIVE',
      token: 'jwt.student.token',
      message: 'Student login',
    };

    vi.mocked(authService.getStoredAuthSession).mockReturnValue(studentUser);

    render(<App />);

    expect(screen.getByText(/Student Academic Progress Portal/i)).toBeInTheDocument();
  });
});
