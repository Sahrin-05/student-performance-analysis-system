import React, { useState } from 'react'
import { loginUser, saveAuthSession, type LoginResponseData, type ApiErrorData } from './services/authService'

interface LoginPageProps {
  onBackToDashboard: () => void
  onNavigateToRegister?: () => void
  onNavigateToStatus?: () => void
  onLoginSuccess?: (user: LoginResponseData) => void
}

type ErrorCategory = 'pending_approval' | 'invalid_credentials' | 'connection_error' | 'general'

export default function LoginPage({
  onBackToDashboard,
  onNavigateToRegister,
  onNavigateToStatus,
  onLoginSuccess,
}: LoginPageProps) {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  // Submission & Feedback states
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const [errorCategory, setErrorCategory] = useState<ErrorCategory>('general')
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  // Validation state
  const [errors, setErrors] = useState<{ identifier?: string; password?: string }>({})

  const validate = (): boolean => {
    const newErrors: { identifier?: string; password?: string } = {}

    const trimmedIdentifier = identifier.trim()
    if (!trimmedIdentifier) {
      newErrors.identifier = 'Email address or Student/Staff ID is required.'
    } else if (trimmedIdentifier.length < 3) {
      newErrors.identifier = 'Identifier must be at least 3 characters long.'
    }

    if (!password) {
      newErrors.password = 'Password is required.'
    } else if (password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters long.'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setServerError(null)
    setSuccessMessage(null)

    if (!validate() || isSubmitting) {
      return
    }

    setIsSubmitting(true)

    try {
      const response = await loginUser({
        identifierOrEmail: identifier.trim(),
        password,
      })

      saveAuthSession(response, rememberMe)

      setSuccessMessage(`Welcome back, ${response.fullName}! Redirecting to your ${response.role.toLowerCase()} dashboard...`)
      setPassword('')

      if (onLoginSuccess) {
        setTimeout(() => {
          onLoginSuccess(response)
        }, 750)
      }
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      if (apiErr.errors) {
        setErrors((prev) => ({
          ...prev,
          identifier: apiErr.errors?.identifierOrEmail,
          password: apiErr.errors?.password,
        }))
      }

      const rawMsg = apiErr.message || 'An error occurred during authentication.'
      setServerError(rawMsg)

      if (
        apiErr.status === 403 ||
        rawMsg.toLowerCase().includes('pending administrator approval') ||
        rawMsg.toLowerCase().includes('pending')
      ) {
        setErrorCategory('pending_approval')
      } else if (
        apiErr.status === 401 ||
        rawMsg.toLowerCase().includes('invalid email/identifier or password') ||
        rawMsg.toLowerCase().includes('invalid')
      ) {
        setErrorCategory('invalid_credentials')
      } else if (
        rawMsg.toLowerCase().includes('unable to connect') ||
        rawMsg.toLowerCase().includes('failed to fetch')
      ) {
        setErrorCategory('connection_error')
      } else {
        setErrorCategory('general')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between text-slate-900 antialiased font-sans">
      {/* Top Header Bar */}
      <header className="border-b border-slate-200 bg-white px-4 py-3 sm:px-8">
        <div className="mx-auto flex max-w-6xl items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-xs">
              SP
            </div>
            <div>
              <span className="text-sm font-semibold tracking-wide text-slate-900">SPAS Portal</span>
              <p className="text-[11px] text-slate-500">Student Performance Analysis System</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onBackToDashboard}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3.5 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
            >
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Landing Page
            </button>
            {onNavigateToRegister && (
              <button
                type="button"
                onClick={onNavigateToRegister}
                className="inline-flex items-center gap-1.5 rounded-lg bg-blue-600 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-blue-700 transition-colors shadow-xs"
              >
                Request Account
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Sign-In Card Container */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-md">
          {/* Card Wrapper */}
          <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-xs">
            {/* Header */}
            <div className="text-center mb-6">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 border border-blue-200 mb-2.5">
                <span className="h-1.5 w-1.5 rounded-full bg-blue-600"></span>
                Institutional Authentication
              </span>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                Sign In to SPAS
              </h2>
              <p className="mt-1.5 text-xs text-slate-600 leading-relaxed">
                Enter your institutional email address or student/staff ID number to access your account.
              </p>
            </div>

            {/* Differentiated Error Feedback Banners */}
            {serverError && (
              <>
                {errorCategory === 'pending_approval' && (
                  <div className="mb-5 rounded-xl border border-amber-300 bg-amber-50 p-4 text-xs text-amber-900 shadow-xs">
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-amber-200/80 text-amber-800">
                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      </div>
                      <div className="flex-1 space-y-1">
                        <span className="font-semibold text-amber-950 block">Account Pending Approval</span>
                        <p className="text-amber-800 leading-relaxed">
                          Your account request has been received and is waiting for administrator review. You will be able to log in once your credentials are confirmed.
                        </p>
                        {onNavigateToStatus && (
                          <div className="pt-1.5">
                            <button
                              type="button"
                              onClick={onNavigateToStatus}
                              className="font-semibold text-blue-700 hover:text-blue-800 underline underline-offset-2 inline-flex items-center gap-1"
                            >
                              Check your registration status
                              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                              </svg>
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}

                {errorCategory === 'invalid_credentials' && (
                  <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs">
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-rose-200/80 text-rose-800">
                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <span className="font-semibold text-rose-950 block">Invalid Credentials</span>
                        <p className="mt-0.5 text-rose-700 leading-relaxed">
                          The email/identifier or password you entered does not match our records. Please verify and try again.
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                {errorCategory === 'connection_error' && (
                  <div className="mb-5 rounded-xl border border-red-300 bg-red-50 p-4 text-xs text-red-900 shadow-xs">
                    <div className="flex items-start gap-3">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-red-200 text-red-800">
                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <span className="font-semibold text-red-950 block">Backend Server Unreachable</span>
                        <p className="mt-0.5 text-red-800 leading-relaxed">
                          Unable to connect to the SPAS Spring Boot service on port 8080. Please ensure the backend is running.
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                {errorCategory === 'general' && (
                  <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800">
                    <span className="font-semibold text-rose-950 block">Authentication Notice</span>
                    <p className="mt-0.5 text-rose-700 leading-relaxed">{serverError}</p>
                  </div>
                )}
              </>
            )}

            {/* Success Message Alert */}
            {successMessage && (
              <div className="mb-5 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-xs text-emerald-800 shadow-xs">
                <div className="flex items-center gap-2.5">
                  <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-emerald-200 text-emerald-800">
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                  <div>
                    <span className="font-semibold text-emerald-950 block">Authentication Successful</span>
                    <p className="mt-0.5 text-emerald-700">{successMessage}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              {/* Identifier Input */}
              <div>
                <label
                  htmlFor="identifier"
                  className="block text-xs font-semibold text-slate-700 mb-1"
                >
                  Institutional Email or Student / Staff ID
                </label>
                <input
                  id="identifier"
                  name="identifier"
                  type="text"
                  autoComplete="username"
                  disabled={isSubmitting || Boolean(successMessage)}
                  value={identifier}
                  onChange={(e) => {
                    setIdentifier(e.target.value)
                    if (errors.identifier) {
                      setErrors((prev) => ({ ...prev, identifier: undefined }))
                    }
                  }}
                  placeholder="e.g. user@university.edu or STU-2026-001"
                  className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors disabled:bg-slate-100 disabled:cursor-not-allowed ${
                    errors.identifier
                      ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                      : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                  }`}
                />
                {errors.identifier && (
                  <p className="mt-1 text-xs text-rose-600 flex items-center gap-1">
                    <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {errors.identifier}
                  </p>
                )}
              </div>

              {/* Password Input */}
              <div>
                <div className="flex items-center justify-between mb-1">
                  <label
                    htmlFor="password"
                    className="block text-xs font-semibold text-slate-700"
                  >
                    Password
                  </label>
                  <span className="text-[11px] text-slate-400">
                    Min. 6 characters
                  </span>
                </div>
                <div className="relative">
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    disabled={isSubmitting || Boolean(successMessage)}
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value)
                      if (errors.password) {
                        setErrors((prev) => ({ ...prev, password: undefined }))
                      }
                    }}
                    placeholder="Enter your password"
                    className={`w-full rounded-lg border px-3.5 py-2.5 pr-10 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors disabled:bg-slate-100 disabled:cursor-not-allowed ${
                      errors.password
                        ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                        : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                    }`}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? (
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                      </svg>
                    ) : (
                      <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
                {errors.password && (
                  <p className="mt-1 text-xs text-rose-600 flex items-center gap-1">
                    <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {errors.password}
                  </p>
                )}
              </div>

              {/* Remember Me Checkbox */}
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  disabled={isSubmitting || Boolean(successMessage)}
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500 disabled:opacity-50"
                />
                <label htmlFor="remember-me" className="ml-2 block text-xs text-slate-600">
                  Remember this device for 30 days
                </label>
              </div>

              {/* Sign In Button */}
              <button
                type="submit"
                disabled={isSubmitting || Boolean(successMessage)}
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-blue-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {isSubmitting ? (
                  <>
                    <svg className="h-4 w-4 animate-spin text-white" viewBox="0 0 24 24" fill="none">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    <span>Authenticating...</span>
                  </>
                ) : (
                  'Sign In to SPAS'
                )}
              </button>
            </form>

            {/* Request Account Navigation */}
            {onNavigateToRegister && (
              <div className="mt-5 text-center text-xs text-slate-600 border-t border-slate-100 pt-4">
                Don't have an approved account yet?{' '}
                <button
                  type="button"
                  onClick={onNavigateToRegister}
                  className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                >
                  Request Student or Lecturer Account
                </button>
              </div>
            )}

            {/* Note on Security and Backend Development */}
            <div className="mt-6 rounded-lg bg-slate-50 border border-slate-200 p-3 text-[11px] text-slate-500 leading-relaxed">
              <span className="font-semibold text-slate-700">Campus Security Notice:</span> Access to student performance analytics is restricted to authenticated institutional users. Passwords are encrypted with BCrypt.
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-3 px-4 text-center text-xs text-slate-500">
        Student Performance Analysis System (SPAS) &copy; University Academic Analytics Portal
      </footer>
    </div>
  )
}
