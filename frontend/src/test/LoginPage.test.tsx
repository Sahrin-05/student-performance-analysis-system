import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginPage from '../LoginPage';
import * as authService from '../services/authService';

vi.mock('../services/authService', async (importOriginal) => {
  const actual = await importOriginal<typeof authService>();
  return {
    ...actual,
    loginUser: vi.fn(),
    saveAuthSession: vi.fn(),
  };
});

describe('LoginPage', () => {
  const onBackToDashboard = vi.fn();
  const onNavigateToRegister = vi.fn();
  const onNavigateToStatus = vi.fn();
  const onLoginSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders login form with inputs and buttons', () => {
    render(
      <LoginPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToRegister={onNavigateToRegister}
        onNavigateToStatus={onNavigateToStatus}
        onLoginSuccess={onLoginSuccess}
      />
    );

    expect(screen.getByLabelText(/Institutional Email or Student \/ Staff ID/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Sign In to SPAS/i })).toBeInTheDocument();
  });

  it('validates required fields on empty submit', async () => {
    render(
      <LoginPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToRegister={onNavigateToRegister}
        onNavigateToStatus={onNavigateToStatus}
        onLoginSuccess={onLoginSuccess}
      />
    );

    fireEvent.click(screen.getByRole('button', { name: /Sign In to SPAS/i }));

    expect(await screen.findByText(/Email address or Student\/Staff ID is required/i)).toBeInTheDocument();
    expect(screen.getByText(/Password is required/i)).toBeInTheDocument();
    expect(authService.loginUser).not.toHaveBeenCalled();
  });

  it('submits form with valid credentials and triggers onLoginSuccess', async () => {
    const mockUser: authService.LoginResponseData = {
      id: 10,
      fullName: 'Alice Smith',
      identifier: 'STU100',
      email: 'alice@spas.edu',
      role: 'STUDENT',
      status: 'ACTIVE',
      token: 'jwt.sample.token',
      message: 'Authentication successful',
    };

    vi.mocked(authService.loginUser).mockResolvedValue(mockUser);

    render(
      <LoginPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToRegister={onNavigateToRegister}
        onNavigateToStatus={onNavigateToStatus}
        onLoginSuccess={onLoginSuccess}
      />
    );

    fireEvent.change(screen.getByLabelText(/Institutional Email or Student \/ Staff ID/i), {
      target: { value: 'alice@spas.edu' },
    });
    fireEvent.change(screen.getByLabelText(/^Password/i), {
      target: { value: 'securePass123' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Sign In to SPAS/i }));

    await waitFor(() => {
      expect(authService.loginUser).toHaveBeenCalledWith({
        identifierOrEmail: 'alice@spas.edu',
        password: 'securePass123',
      });
    });

    expect(authService.saveAuthSession).toHaveBeenCalledWith(mockUser, false);
    expect(await screen.findByText(/Welcome back, Alice Smith!/i)).toBeInTheDocument();
  });

  it('displays server error on authentication failure', async () => {
    vi.mocked(authService.loginUser).mockRejectedValue(
      new Error('Invalid institutional identifier or password.')
    );

    render(
      <LoginPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToRegister={onNavigateToRegister}
        onNavigateToStatus={onNavigateToStatus}
        onLoginSuccess={onLoginSuccess}
      />
    );

    fireEvent.change(screen.getByLabelText(/Institutional Email or Student \/ Staff ID/i), {
      target: { value: 'bad@spas.edu' },
    });
    fireEvent.change(screen.getByLabelText(/^Password/i), {
      target: { value: 'wrongPassword' },
    });

    fireEvent.click(screen.getByRole('button', { name: /Sign In to SPAS/i }));

    expect(await screen.findByText(/Invalid Credentials/i)).toBeInTheDocument();
  });
});
