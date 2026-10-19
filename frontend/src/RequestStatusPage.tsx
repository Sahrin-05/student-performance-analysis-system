import React, { useState, useEffect, useRef } from 'react'
import { checkUserStatus, type StatusCheckResponseData, type ApiErrorData } from './services/authService'

interface RequestStatusPageProps {
  onBackToDashboard: () => void
  onNavigateToLogin: () => void
  onNavigateToRegister: () => void
  initialIdentifier?: string
  initialEmail?: string
}

export default function RequestStatusPage({
  onBackToDashboard,
  onNavigateToLogin,
  onNavigateToRegister,
  initialIdentifier,
  initialEmail,
}: RequestStatusPageProps) {
  const [email, setEmail] = useState(initialEmail || '')
  const [identifier, setIdentifier] = useState(initialIdentifier || '')
  const [statusResult, setStatusResult] = useState<StatusCheckResponseData | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [queryError, setQueryError] = useState<string | null>(null)
  const [errors, setErrors] = useState<{ email?: string; identifier?: string }>({})

  const isCheckingRef = useRef(false)
  const isMountedRef = useRef(true)

  useEffect(() => {
    isMountedRef.current = true
    return () => {
      isMountedRef.current = false
    }
  }, [])

  // Automatically query status if prefilled credentials were provided from registration
  useEffect(() => {
    if (initialIdentifier && initialEmail) {
      performStatusCheck(initialIdentifier, initialEmail)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialIdentifier, initialEmail])

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

  const performStatusCheck = async (
    idToCheck: string,
    emailToCheck: string,
    isRefresh = false,
    isBackground = false
  ) => {
    if (isCheckingRef.current) return
    isCheckingRef.current = true

    if (!isBackground) {
      if (isRefresh) {
        setIsRefreshing(true)
      } else {
        setIsLoading(true)
      }
      setQueryError(null)
    }

    try {
      const response = await checkUserStatus({
        identifier: idToCheck.trim(),
        institutionalEmail: emailToCheck.trim().toLowerCase(),
      })
      if (isMountedRef.current) {
        setStatusResult(response)
        setQueryError(null)
      }
    } catch (err: unknown) {
      if (isMountedRef.current) {
        const apiErr = err as Error & ApiErrorData
        if (!isRefresh && !isBackground) {
          setStatusResult(null)
        }
        if (!isBackground) {
          setQueryError(
            apiErr.message || 'Unable to check verification status. Please check your connection and try again.'
          )
        }
      }
    } finally {
      isCheckingRef.current = false
      if (isMountedRef.current) {
        setIsLoading(false)
        setIsRefreshing(false)
      }
    }
  }

  const performStatusCheckRef = useRef(performStatusCheck)
  useEffect(() => {
    performStatusCheckRef.current = performStatusCheck
  })

  // Automatically poll every 5 seconds while account is PENDING_APPROVAL
  useEffect(() => {
    if (statusResult?.status !== 'PENDING_APPROVAL' || !identifier.trim() || !email.trim()) {
      return
    }

    const pollInterval = setInterval(() => {
      if (identifier.trim() && email.trim() && !isCheckingRef.current) {
        performStatusCheckRef.current(identifier, email, true, true)
      }
    }, 5000)

    return () => {
      clearInterval(pollInterval)
    }
  }, [statusResult?.status, identifier, email])

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (validate()) {
      performStatusCheck(identifier, email)
    }
  }

  const handleRefresh = () => {
    if (identifier.trim() && email.trim()) {
      performStatusCheck(identifier, email, true)
    }
  }

  const handleResetInquiry = () => {
    setStatusResult(null)
    setQueryError(null)
    setErrors({})
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
                Registration Status
              </h2>
              <p className="mt-1.5 text-xs text-slate-600 leading-relaxed">
                Check whether your student or lecturer registration request has been reviewed and approved by administrators.
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
                      Your registration is recorded securely with password hashing and assigned <code className="text-amber-800 bg-amber-50 px-1 py-0.5 rounded border border-amber-200 text-[10px]">PENDING_APPROVAL</code> status.
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
                      Campus administrators verify your university enrollment or staff appointment in the Admin Approvals Queue.
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
                      Once approved, status transitions to <code className="text-emerald-800 bg-emerald-50 px-1 py-0.5 rounded border border-emerald-200 text-[10px]">ACTIVE</code>, allowing portal login with your credentials.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Error Feedback Banner */}
            {queryError && (
              <div className="mb-5 rounded-xl border border-rose-300 bg-rose-50 p-4 text-xs text-rose-900 shadow-xs">
                <div className="flex items-start gap-2.5">
                  <svg className="h-5 w-5 text-rose-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div className="flex-1">
                    <span className="font-semibold text-rose-950 block">Status Inquiry Notice</span>
                    <p className="mt-0.5 text-rose-800 leading-relaxed">{queryError}</p>
                    <div className="mt-2.5 flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => performStatusCheck(identifier, email)}
                        className="rounded-md bg-rose-600 px-2.5 py-1 text-[11px] font-semibold text-white hover:bg-rose-700 transition-colors"
                      >
                        Try Again
                      </button>
                      <button
                        type="button"
                        onClick={handleResetInquiry}
                        className="rounded-md border border-rose-300 bg-white px-2.5 py-1 text-[11px] font-medium text-rose-800 hover:bg-rose-50 transition-colors"
                      >
                        Edit Details
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Live Inquiry Result Banner (Connected to Real API) */}
            {statusResult && (
              <div className="mb-6 space-y-3">
                {statusResult.status === 'PENDING_APPROVAL' && (
                  <div className="rounded-xl border border-amber-300 bg-amber-50/90 p-4 text-xs text-amber-900 shadow-xs space-y-2.5">
                    <div className="flex items-center justify-between border-b border-amber-200/80 pb-2">
                      <span className="font-semibold text-amber-950 flex items-center gap-1.5">
                        <span className="h-2 w-2 rounded-full bg-amber-500 animate-pulse"></span>
                        Verification Status:
                      </span>
                      <span className="rounded-full bg-amber-200/80 px-2.5 py-0.5 text-[11px] font-bold text-amber-950 border border-amber-300">
                        PENDING APPROVAL
                      </span>
                    </div>
                    <div className="text-[12px] text-amber-900 leading-relaxed">
                      <p>
                        Application for <strong>{identifier}</strong> ({email}) is actively recorded and awaiting administrator review.
                      </p>
                      <p className="mt-1 text-amber-800 text-[11px]">
                        {statusResult.message}
                      </p>
                    </div>
                    <div className="pt-2 border-t border-amber-200/60 flex items-center justify-between">
                      <button
                        type="button"
                        onClick={handleRefresh}
                        disabled={isRefreshing}
                        className="inline-flex items-center gap-1.5 rounded-lg bg-amber-600 px-3 py-1.5 text-xs font-semibold text-white shadow-xs hover:bg-amber-700 disabled:opacity-50 transition-colors"
                      >
                        <svg
                          className={`h-3.5 w-3.5 ${isRefreshing ? 'animate-spin' : ''}`}
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        {isRefreshing ? 'Refreshing...' : 'Refresh Status'}
                      </button>
                      <button
                        type="button"
                        onClick={handleResetInquiry}
                        className="text-xs text-amber-800 hover:text-amber-950 underline underline-offset-2 font-medium"
                      >
                        Check another account
                      </button>
                    </div>
                  </div>
                )}

                {statusResult.status === 'ACTIVE' && (
                  <div className="rounded-xl border border-emerald-300 bg-emerald-50/90 p-4 text-xs text-emerald-900 shadow-xs space-y-2.5">
                    <div className="flex items-center justify-between border-b border-emerald-200/80 pb-2">
                      <span className="font-semibold text-emerald-950 flex items-center gap-1.5">
                        <span className="h-2 w-2 rounded-full bg-emerald-500"></span>
                        Verification Status:
                      </span>
                      <span className="rounded-full bg-emerald-200/90 px-2.5 py-0.5 text-[11px] font-bold text-emerald-950 border border-emerald-300">
                        APPROVED &amp; ACTIVE
                      </span>
                    </div>
                    <div className="text-[12px] text-emerald-900 leading-relaxed">
                      <p>
                        Account <strong>{identifier}</strong> ({email}) has been verified and activated!
                      </p>
                      <p className="mt-1 text-emerald-800 text-[11px]">
                        {statusResult.message}
                      </p>
                    </div>
                    <div className="pt-2 border-t border-emerald-200/60 flex items-center justify-between">
                      <button
                        type="button"
                        onClick={onNavigateToLogin}
                        className="inline-flex items-center gap-1.5 rounded-lg bg-emerald-600 px-3.5 py-1.5 text-xs font-semibold text-white shadow-xs hover:bg-emerald-700 transition-colors"
                      >
                        Proceed to Sign In
                        <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                        </svg>
                      </button>
                      <button
                        type="button"
                        onClick={handleResetInquiry}
                        className="text-xs text-emerald-800 hover:text-emerald-950 underline underline-offset-2 font-medium"
                      >
                        Check another account
                      </button>
                    </div>
                  </div>
                )}

                {statusResult.status === 'REJECTED' && (
                  <div className="rounded-xl border border-rose-300 bg-rose-50/90 p-4 text-xs text-rose-900 shadow-xs space-y-2.5">
                    <div className="flex items-center justify-between border-b border-rose-200/80 pb-2">
                      <span className="font-semibold text-rose-950 flex items-center gap-1.5">
                        <span className="h-2 w-2 rounded-full bg-rose-500"></span>
                        Verification Status:
                      </span>
                      <span className="rounded-full bg-rose-200/90 px-2.5 py-0.5 text-[11px] font-bold text-rose-950 border border-rose-300">
                        REQUEST REJECTED
                      </span>
                    </div>
                    <div className="text-[12px] text-rose-900 leading-relaxed">
                      <p>
                        The registration request for <strong>{identifier}</strong> was reviewed and declined by university administration.
                      </p>
                      <p className="mt-1 text-rose-800 text-[11px]">
                        {statusResult.message}
                      </p>
                    </div>
                    <div className="pt-2 border-t border-rose-200/60 flex items-center justify-between">
                      <button
                        type="button"
                        onClick={onNavigateToRegister}
                        className="inline-flex items-center gap-1.5 rounded-lg bg-rose-600 px-3.5 py-1.5 text-xs font-semibold text-white shadow-xs hover:bg-rose-700 transition-colors"
                      >
                        Submit New Request
                      </button>
                      <button
                        type="button"
                        onClick={handleResetInquiry}
                        className="text-xs text-rose-800 hover:text-rose-950 underline underline-offset-2 font-medium"
                      >
                        Check another account
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Status Inquiry Form (only shown or editable when not viewing active result) */}
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
                  disabled={isLoading || isRefreshing}
                  onChange={(e) => {
                    setIdentifier(e.target.value)
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
                  disabled={isLoading || isRefreshing}
                  onChange={(e) => {
                    setEmail(e.target.value)
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
                disabled={isLoading || isRefreshing}
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-blue-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-60 transition-colors mt-2 inline-flex items-center justify-center gap-2"
              >
                {isLoading ? (
                  <>
                    <svg className="h-4 w-4 animate-spin text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span>Verifying with Server...</span>
                  </>
                ) : (
                  <span>Inquire Verification Status</span>
                )}
              </button>
            </form>

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
