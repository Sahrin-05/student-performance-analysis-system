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
      newErrors.email = 'Please enter a valid institutional email address.'
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
      setHasChecked(true)
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
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
            >
              Landing Page
            </button>
            <button
              type="button"
              onClick={onNavigateToLogin}
              className="inline-flex items-center gap-1.5 rounded-lg bg-blue-600 px-3.5 py-1.5 text-xs font-medium text-white hover:bg-blue-700 transition-colors shadow-xs"
            >
              Sign In
            </button>
          </div>
        </div>
      </header>

      {/* Main Request Status Container */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-lg space-y-6">
          {/* Card Wrapper */}
          <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-xs">
            {/* Header */}
            <div className="text-center mb-6">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 border border-blue-200 mb-2.5">
                <span className="h-1.5 w-1.5 rounded-full bg-blue-600"></span>
                Account Verification Lifecycle
              </span>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                Registration Status Guidance
              </h2>
              <p className="mt-1.5 text-xs text-slate-600 leading-relaxed">
                Understand how new Student and Lecturer accounts are verified and activated by system administrators.
              </p>
            </div>

            {/* 3-Step Verification Timeline */}
            <div className="mb-6 rounded-xl border border-slate-200 bg-slate-50 p-4 space-y-3.5">
              <span className="text-xs font-semibold text-slate-800 uppercase tracking-wider block">
                Verification Workflow
              </span>
              
              <div className="space-y-3 text-xs">
                <div className="flex items-start gap-3">
                  <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-emerald-700 font-bold text-[11px]">
                    1
                  </div>
                  <div>
                    <span className="font-semibold text-slate-900">Submission Received</span>
                    <p className="text-slate-600 text-[11px] mt-0.5 leading-relaxed">
                      Your registration is recorded in the PostgreSQL database with BCrypt password hashing and assigned <code className="text-amber-800 bg-amber-50 px-1 py-0.5 rounded border border-amber-200 text-[10px]">PENDING_APPROVAL</code> status.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-700 font-bold text-[11px]">
                    2
                  </div>
                  <div>
                    <span className="font-semibold text-slate-900">Administrative Audit</span>
                    <p className="text-slate-600 text-[11px] mt-0.5 leading-relaxed">
                      Campus administrators verify your university enrollment or staff appointment from the Admin Approval Queue.
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-slate-200 text-slate-700 font-bold text-[11px]">
                    3
                  </div>
                  <div>
                    <span className="font-semibold text-slate-900">Account Activation</span>
                    <p className="text-slate-600 text-[11px] mt-0.5 leading-relaxed">
                      Once approved, status transitions to <code className="text-emerald-800 bg-emerald-50 px-1 py-0.5 rounded border border-emerald-200 text-[10px]">ACTIVE</code>, allowing you to log in and access your role-based academic dashboard.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Status Inquiry Form */}
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
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
                  placeholder="e.g. STU-2026-001 or STF-CS-001"
                  className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                    errors.identifier
                      ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                      : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                  }`}
                />
                {errors.identifier && (
                  <p className="mt-1 text-xs text-rose-600">{errors.identifier}</p>
                )}
              </div>

              <div>
                <label htmlFor="status-email" className="block text-xs font-semibold text-slate-700 mb-1">
                  Institutional Email Address
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
                  placeholder="e.g. applicant@university.edu"
                  className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                    errors.email
                      ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                      : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                  }`}
                />
                {errors.email && (
                  <p className="mt-1 text-xs text-rose-600">{errors.email}</p>
                )}
              </div>

              <button
                type="submit"
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-blue-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 transition-colors mt-2"
              >
                Inquire Verification Status
              </button>
            </form>

            {/* Simulated Live Inquiry Result */}
            {hasChecked && (
              <div className="mt-5 rounded-xl border border-amber-300 bg-amber-50/80 p-4 text-xs text-amber-900 shadow-xs space-y-2">
                <div className="flex items-center justify-between border-b border-amber-200/80 pb-2">
                  <span className="font-semibold text-amber-950">Inquiry Result:</span>
                  <span className="rounded-full bg-amber-200/80 px-2 py-0.5 text-[10px] font-bold text-amber-900">
                    PENDING APPROVAL
                  </span>
                </div>
                <div className="text-[11px] text-amber-800 leading-relaxed">
                  <p>
                    Submitted request for <strong>{identifier}</strong> ({email}) is actively recorded in the system.
                  </p>
                  <p className="mt-1">
                    An administrator must approve this account from the Admin Approval Queue before sign-in can proceed. If you require immediate activation for semester onboarding, please contact your academic administrator.
                  </p>
                </div>
              </div>
            )}

            {/* Helpful Links */}
            <div className="mt-6 border-t border-slate-100 pt-4 space-y-2 text-center text-xs text-slate-600">
              <div>
                Need to submit a fresh account request?{' '}
                <button
                  type="button"
                  onClick={onNavigateToRegister}
                  className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                >
                  Request Student or Lecturer Account
                </button>
              </div>
              <div>
                Already received your administrator approval?{' '}
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
        Student Performance Analysis System (SPAS) &copy; University Academic Analytics Portal
      </footer>
    </div>
  )
}
