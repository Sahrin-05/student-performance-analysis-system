import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import RegisterPage from '../RegisterPage';
import * as authService from '../services/authService';

vi.mock('../services/authService', async (importOriginal) => {
  const actual = await importOriginal<typeof authService>();
  return {
    ...actual,
    registerUser: vi.fn(),
  };
});

describe('RegisterPage', () => {
  const onBackToDashboard = vi.fn();
  const onNavigateToLogin = vi.fn();
  const onNavigateToStatus = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders registration form with role selection tabs', () => {
    render(
      <RegisterPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToStatus={onNavigateToStatus}
      />
    );

    const radios = screen.getAllByRole('radio');
    expect(radios.length).toBe(2);
    expect(screen.getByLabelText(/Full Legal Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Student ID Number/i)).toBeInTheDocument();
  });

  it('switches to Lecturer account type and updates identifier label', () => {
    render(
      <RegisterPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToStatus={onNavigateToStatus}
      />
    );

    const radios = screen.getAllByRole('radio');
    fireEvent.click(radios[1]); // Lecturer
    expect(screen.getByLabelText(/Staff ID Number/i)).toBeInTheDocument();
  });

  it('validates fields on empty submission', async () => {
    render(
      <RegisterPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToStatus={onNavigateToStatus}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /Submit .* Registration/i }));

    expect(await screen.findByText(/Full Name is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Student ID is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Institutional Email is required/i)).toBeInTheDocument();
    expect(authService.registerUser).not.toHaveBeenCalled();
  });

  it('submits valid registration request successfully', async () => {
    const mockResponse: authService.RegisterResponseData = {
      id: 25,
      fullName: 'Bob Johnson',
      identifier: 'STU200',
      email: 'bob@spas.edu',
      role: 'STUDENT',
      status: 'PENDING_APPROVAL',
      message: 'Registration request submitted successfully.',
    };

    vi.mocked(authService.registerUser).mockResolvedValue(mockResponse);

    render(
      <RegisterPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToStatus={onNavigateToStatus}
      />
    );

    fireEvent.change(screen.getByLabelText(/Full Legal Name/i), { target: { value: 'Bob Johnson' } });
    fireEvent.change(screen.getByLabelText(/Student ID Number/i), { target: { value: 'STU200' } });
    fireEvent.change(screen.getByLabelText(/Institutional Email Address/i), { target: { value: 'bob@spas.edu' } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'securePass123' } });
    fireEvent.change(screen.getByLabelText(/Confirm Password/i), { target: { value: 'securePass123' } });

    fireEvent.click(screen.getByRole('button', { name: /Submit .* Registration/i }));

    await waitFor(() => {
      expect(authService.registerUser).toHaveBeenCalledWith({
        fullName: 'Bob Johnson',
        identifier: 'STU200',
        email: 'bob@spas.edu',
        password: 'securePass123',
        role: 'STUDENT',
      });
    });

    expect(await screen.findByText(/Registration Request Received/i)).toBeInTheDocument();
  });
});
