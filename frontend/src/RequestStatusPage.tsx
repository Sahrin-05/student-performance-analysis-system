import React, { useState } from 'react'

interface RequestStatusPageProps {
  onBackToDashboard: () => void
  onNavigateToLogin: () => void
  onNavigateToRegister: () => void
}

export default function RequestStatusPage({
  onBackToDashboard,
  onNavigateToLogin,
  onNavigateToRegister,
}: RequestStatusPageProps) {
  const [email, setEmail] = useState('')
  const [identifier, setIdentifier] = useState('')
  const [hasChecked, setHasChecked] = useState(false)
  const [errors, setErrors] = useState<{ email?: string; identifier?: string }>({})

  const validate = (): boolean => {
    const newErrors: { email?: string; identifier?: string } = {}
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    if (!email.trim()) {
      newErrors.email = 'Institutional email is required.'
    } else if (!emailRegex.test(email.trim())) {
      newErrors.email = 'Please enter a valid email address.'
    }

    if (!identifier.trim()) {
      newErrors.identifier = 'Student or Staff ID is required.'
    } else if (identifier.trim().length < 3) {
      newErrors.identifier = 'Identifier must be at least 3 characters long.'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (validate()) {
      // Input is valid; show the integration notice without fabricating status results
      setHasChecked(true)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between text-slate-900 antialiased font-sans">
      {/* Top Bar with Brand & Navigation */}
      <header className="border-b border-slate-200 bg-white px-4 py-3 sm:px-8">
        <div className="mx-auto flex max-w-6xl items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-xs">
              SP
            </div>
            <div>
              <span className="text-sm font-semibold tracking-wide text-slate-900">SPAS Portal</span>
              <p className="text-[11px] text-slate-500">Academic Analytics</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onNavigateToLogin}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={onBackToDashboard}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
            >
              Dashboard
            </button>
          </div>
        </div>
      </header>

      {/* Main Request Status Container */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-md">
          {/* Card Wrapper */}
          <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-xs">
            {/* Header */}
            <div className="text-center mb-6">
              <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 border border-blue-200 mb-2">
                Application Tracking
              </span>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                Check Request Status
              </h2>
              <p className="mt-1 text-xs text-slate-600">
                Enter your submitted request credentials to check your account verification status.
              </p>
            </div>

            {/* Clear Integration Status Notice (shown after button click with valid input) */}
            {hasChecked && (
              <div className="mb-5 rounded-xl border border-blue-200 bg-blue-50/80 p-3.5 text-xs text-blue-900">
                <div className="flex items-start gap-2.5">
                  <svg className="h-5 w-5 shrink-0 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div className="leading-relaxed">
                    <span className="font-semibold text-blue-950">System Integration in Progress:</span>
                    <p className="mt-0.5 text-blue-800">
                      Live request status tracking will be connected once the Java Spring Boot backend and PostgreSQL database are implemented. No live request data is retrieved in this preview version.
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Request Status Form */}
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              {/* Institutional Email */}
              <div>
                <label htmlFor="status-email" className="block text-xs font-semibold text-slate-700 mb-1">
                  Institutional Email
                </label>
                <input
                  id="status-email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value)
                    setHasChecked(false)
                    if (errors.email) {
                      setErrors((prev) => ({ ...prev, email: undefined }))
                    }
                  }}
                  placeholder="e.g. user@student.university.edu"
                  className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                    errors.email
                      ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                      : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                  }`}
                />
                {errors.email && (
                  <p className="mt-1 text-xs text-rose-600 flex items-center gap-1">
                    <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                    {errors.email}
                  </p>
                )}
              </div>

              {/* Student or Staff ID */}
              <div>
                <label htmlFor="status-identifier" className="block text-xs font-semibold text-slate-700 mb-1">
                  Student or Staff ID Number
                </label>
                <input
                  id="status-identifier"
                  name="identifier"
                  type="text"
                  value={identifier}
                  onChange={(e) => {
                    setIdentifier(e.target.value)
                    setHasChecked(false)
                    if (errors.identifier) {
                      setErrors((prev) => ({ ...prev, identifier: undefined }))
                    }
                  }}
                  placeholder="e.g. STU-2026-4401 or STF-CS-1092"
                  className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
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

              {/* Submit Button */}
              <button
                type="submit"
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-blue-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 transition-colors mt-2"
              >
                Check Request Status
              </button>
            </form>

            {/* Helpful Links */}
            <div className="mt-6 border-t border-slate-100 pt-4 space-y-2 text-center text-xs text-slate-600">
              <div>
                Need to submit a new account request?{' '}
                <button
                  type="button"
                  onClick={onNavigateToRegister}
                  className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                >
                  Request an account
                </button>
              </div>
              <div>
                Already received your approval credentials?{' '}
                <button
                  type="button"
                  onClick={onNavigateToLogin}
                  className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                >
                  Sign in here
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 bg-white py-3 px-4 text-center text-xs text-slate-500">
        Student Performance Analysis System (SPAS) &copy; University Academic Portal
      </footer>
    </div>
  )
}
