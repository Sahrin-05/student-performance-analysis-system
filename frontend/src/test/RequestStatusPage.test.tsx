import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import RequestStatusPage from '../RequestStatusPage';
import * as authService from '../services/authService';

vi.mock('../services/authService', async (importOriginal) => {
  const actual = await importOriginal<typeof authService>();
  return {
    ...actual,
    checkUserStatus: vi.fn(),
  };
});

describe('RequestStatusPage', () => {
  const onBackToDashboard = vi.fn();
  const onNavigateToLogin = vi.fn();
  const onNavigateToRegister = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders status inquiry form', () => {
    render(
      <RequestStatusPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToRegister={onNavigateToRegister}
      />
    );

    expect(screen.getByLabelText(/Student or Staff ID Number/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Institutional Email Address/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Inquire Verification Status/i })).toBeInTheDocument();
  });

  it('validates email format and required ID', async () => {
    render(
      <RequestStatusPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToRegister={onNavigateToRegister}
      />
    );

    fireEvent.change(screen.getByLabelText(/Institutional Email Address/i), { target: { value: 'not-an-email' } });
    fireEvent.click(screen.getByRole('button', { name: /Inquire Verification Status/i }));

    expect(await screen.findByText(/Please enter a valid institutional email address/i)).toBeInTheDocument();
    expect(screen.getByText(/Student or Staff ID is required/i)).toBeInTheDocument();
  });

  it('queries and displays pending status badge', async () => {
    vi.mocked(authService.checkUserStatus).mockResolvedValue({
      status: 'PENDING_APPROVAL',
      message: 'Your registration request is currently under review by academic administrators.',
    });

    render(
      <RequestStatusPage
        onBackToDashboard={onBackToDashboard}
        onNavigateToLogin={onNavigateToLogin}
        onNavigateToRegister={onNavigateToRegister}
      />
    );

    fireEvent.change(screen.getByLabelText(/Student or Staff ID Number/i), { target: { value: 'STU001' } });
    fireEvent.change(screen.getByLabelText(/Institutional Email Address/i), { target: { value: 'student@spas.edu' } });

    fireEvent.click(screen.getByRole('button', { name: /Inquire Verification Status/i }));

    await waitFor(() => {
      expect(authService.checkUserStatus).toHaveBeenCalledWith({
        identifier: 'STU001',
        institutionalEmail: 'student@spas.edu',
      });
    });

    expect(await screen.findByText(/PENDING APPROVAL/i)).toBeInTheDocument();
  });
});
