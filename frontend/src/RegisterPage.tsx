import React, { useState } from 'react'
import { registerUser, type RegisterResponseData, type ApiErrorData } from './services/authService'

export type AccountType = 'Student' | 'Lecturer'

interface RegisterPageProps {
  onBackToDashboard: () => void
  onNavigateToLogin: () => void
  onNavigateToStatus?: () => void
}

export default function RegisterPage({
  onBackToDashboard,
  onNavigateToLogin,
  onNavigateToStatus,
}: RegisterPageProps) {
  const [accountType, setAccountType] = useState<AccountType>('Student')
  const [fullName, setFullName] = useState('')
  const [identifier, setIdentifier] = useState('') // Student ID or Staff ID
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  // Submission & Response states
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)
  const [successResponse, setSuccessResponse] = useState<RegisterResponseData | null>(null)

  // Validation state
  const [errors, setErrors] = useState<{
    fullName?: string
    identifier?: string
    email?: string
    password?: string
    confirmPassword?: string
  }>({})

  const validate = (): boolean => {
    const newErrors: {
      fullName?: string
      identifier?: string
      email?: string
      password?: string
      confirmPassword?: string
    } = {}

    // Full Name
    if (!fullName.trim()) {
      newErrors.fullName = 'Full Name is required.'
    } else if (fullName.trim().length < 3) {
      newErrors.fullName = 'Full Name must be at least 3 characters long.'
    }

    // ID (Student ID or Staff ID)
    const idLabel = accountType === 'Student' ? 'Student ID' : 'Staff ID'
    if (!identifier.trim()) {
      newErrors.identifier = `${idLabel} is required.`
    } else if (identifier.trim().length < 3) {
      newErrors.identifier = `${idLabel} must be at least 3 characters long.`
    }

    // Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!email.trim()) {
      newErrors.email = 'Institutional email is required.'
    } else if (!emailRegex.test(email.trim())) {
      newErrors.email = 'Please enter a valid institutional email address (e.g. user@university.edu).'
    }

    // Password
    if (!password) {
      newErrors.password = 'Password is required.'
    } else if (password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters long.'
    }

    // Confirm Password
    if (!confirmPassword) {
      newErrors.confirmPassword = 'Confirming your password is required.'
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match.'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setServerError(null)

    if (!validate()) {
      return
    }

    setIsSubmitting(true)

    try {
      const response = await registerUser({
        fullName: fullName.trim(),
        identifier: identifier.trim(),
        email: email.trim().toLowerCase(),
        password,
        role: accountType === 'Student' ? 'STUDENT' : 'LECTURER',
      })

      // Successful submission
      setSuccessResponse(response)
      setPassword('')
      setConfirmPassword('')
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      if (apiErr.errors) {
        setErrors((prev) => ({ ...prev, ...apiErr.errors }))
      }
      setServerError(apiErr.message || 'An unexpected error occurred during registration. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleResetForm = () => {
    setSuccessResponse(null)
    setServerError(null)
    setFullName('')
    setIdentifier('')
    setEmail('')
    setPassword('')
    setConfirmPassword('')
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

      {/* Main Registration Container */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-lg">
          {/* Card Wrapper */}
          <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-xs">
            {/* SUCCESS STATE VIEW */}
            {successResponse ? (
              <div className="text-center py-2 space-y-5">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 shadow-xs">
                  <svg className="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>

                <div>
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3 py-1 text-xs font-semibold text-amber-800 border border-amber-200 mb-2">
                    <span className="h-2 w-2 rounded-full bg-amber-500 animate-pulse"></span>
                    Status: PENDING APPROVAL
                  </span>
                  <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                    Registration Request Received
                  </h2>
                  <p className="mt-2 text-xs text-slate-600 max-w-md mx-auto leading-relaxed">
                    Your institutional account request has been successfully submitted and saved to PostgreSQL. An administrator will review your credentials before access is granted.
                  </p>
                </div>

                {/* Account Summary Card */}
                <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-left text-xs space-y-2.5">
                  <div className="flex justify-between border-b border-slate-200 pb-2">
                    <span className="text-slate-500">Applicant Name:</span>
                    <span className="font-semibold text-slate-900">{successResponse.fullName}</span>
                  </div>
                  <div className="flex justify-between border-b border-slate-200 pb-2">
                    <span className="text-slate-500">
                      {successResponse.role === 'STUDENT' ? 'Student ID:' : 'Staff ID:'}
                    </span>
                    <span className="font-semibold text-slate-900">{successResponse.identifier}</span>
                  </div>
                  <div className="flex justify-between border-b border-slate-200 pb-2">
                    <span className="text-slate-500">Institutional Email:</span>
                    <span className="font-semibold text-slate-900">{successResponse.email}</span>
                  </div>
                  <div className="flex justify-between border-b border-slate-200 pb-2">
                    <span className="text-slate-500">Requested Role:</span>
                    <span className="inline-flex items-center rounded-md bg-blue-100 px-2 py-0.5 text-[11px] font-semibold text-blue-800">
                      {successResponse.role}
                    </span>
                  </div>
                  <div className="flex justify-between pt-0.5 items-center">
                    <span className="text-slate-500">Account Status:</span>
                    <span className="font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
                      PENDING_APPROVAL
                    </span>
                  </div>
                </div>

                {/* Security and Approval Guidance */}
                <div className="rounded-xl bg-blue-50/70 border border-blue-200 p-3.5 text-[11px] text-blue-900 leading-relaxed text-left flex items-start gap-2.5">
                  <svg className="h-5 w-5 text-blue-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <div>
                    <span className="font-semibold">Academic Integrity Notice:</span> To protect student records and academic data, new accounts remain inactive until approved by university administration. You cannot log in with this account until approval is completed.
                  </div>
                </div>

                {/* Navigation CTA Buttons */}
                <div className="flex flex-col sm:flex-row gap-2 pt-2">
                  <button
                    type="button"
                    onClick={onNavigateToLogin}
                    className="flex-1 rounded-lg bg-blue-600 px-4 py-2.5 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors"
                  >
                    Go to Sign In
                  </button>
                  {onNavigateToStatus && (
                    <button
                      type="button"
                      onClick={onNavigateToStatus}
                      className="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
                    >
                      Check Status Guidance
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={handleResetForm}
                    className="rounded-lg border border-slate-200 px-3 py-2.5 text-xs text-slate-600 hover:bg-slate-100 transition-colors"
                  >
                    New Request
                  </button>
                </div>
              </div>
            ) : (
              <>
                {/* Form Header */}
                <div className="text-center mb-6">
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 border border-blue-200 mb-2.5">
                    <span className="h-1.5 w-1.5 rounded-full bg-blue-600"></span>
                    Account Onboarding
                  </span>
                  <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                    Request an Account
                  </h2>
                  <p className="mt-1.5 text-xs text-slate-600 max-w-sm mx-auto leading-relaxed">
                    Submit your academic credentials to request access. Accounts are verified and activated by system administrators.
                  </p>
                </div>

                {/* Server Error Alert */}
                {serverError && (
                  <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800 shadow-xs">
                    <div className="flex items-start gap-2.5">
                      <svg className="h-5 w-5 text-rose-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div>
                        <span className="font-semibold text-rose-950 block">Registration Error</span>
                        <p className="mt-0.5 text-rose-700 leading-relaxed">{serverError}</p>
                      </div>
                    </div>
                  </div>
                )}

                {/* Approval Notice Callout */}
                <div className="mb-6 rounded-xl border border-amber-200 bg-amber-50/80 p-3.5 text-xs text-amber-900">
                  <div className="flex items-start gap-2.5">
                    <svg className="h-5 w-5 shrink-0 text-amber-600 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <div className="leading-relaxed">
                      <span className="font-semibold text-amber-950">Administrative Verification Required:</span>
                      <p className="mt-0.5 text-amber-800">
                        New registrations enter the administrator review queue. Accounts cannot self-activate or self-assign the Administrator role.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Account Type Selector (Student vs Lecturer) */}
                <div className="mb-6">
                  <label className="block text-xs font-semibold text-slate-700 mb-2">
                    Select Your Institutional Role
                  </label>
                  <div className="grid grid-cols-2 gap-3" role="radiogroup" aria-label="Requested Account Type">
                    <button
                      type="button"
                      role="radio"
                      aria-checked={accountType === 'Student'}
                      disabled={isSubmitting}
                      onClick={() => {
                        setAccountType('Student')
                        setErrors({})
                        setServerError(null)
                      }}
                      className={`flex items-center gap-3 rounded-xl border p-3.5 text-left transition-all ${
                        accountType === 'Student'
                          ? 'border-blue-600 bg-blue-50/70 text-blue-900 shadow-xs ring-1 ring-blue-600'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50'
                      }`}
                    >
                      <div
                        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-xs font-bold ${
                          accountType === 'Student' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        STU
                      </div>
                      <div>
                        <div className="text-xs font-semibold">Student</div>
                        <div className="text-[11px] text-slate-500">Enrolled scholar</div>
                      </div>
                    </button>

                    <button
                      type="button"
                      role="radio"
                      aria-checked={accountType === 'Lecturer'}
                      disabled={isSubmitting}
                      onClick={() => {
                        setAccountType('Lecturer')
                        setErrors({})
                        setServerError(null)
                      }}
                      className={`flex items-center gap-3 rounded-xl border p-3.5 text-left transition-all ${
                        accountType === 'Lecturer'
                          ? 'border-blue-600 bg-blue-50/70 text-blue-900 shadow-xs ring-1 ring-blue-600'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50'
                      }`}
                    >
                      <div
                        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-xs font-bold ${
                          accountType === 'Lecturer' ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        LEC
                      </div>
                      <div>
                        <div className="text-xs font-semibold">Lecturer / Faculty</div>
                        <div className="text-[11px] text-slate-500">Academic instructor</div>
                      </div>
                    </button>
                  </div>
                </div>

                {/* Registration Form */}
                <form onSubmit={handleSubmit} noValidate className="space-y-4">
                  {/* Full Name */}
                  <div>
                    <label htmlFor="fullName" className="block text-xs font-semibold text-slate-700 mb-1">
                      Full Legal Name
                    </label>
                    <input
                      id="fullName"
                      name="fullName"
                      type="text"
                      autoComplete="name"
                      disabled={isSubmitting}
                      value={fullName}
                      onChange={(e) => {
                        setFullName(e.target.value)
                        if (errors.fullName) {
                          setErrors((prev) => ({ ...prev, fullName: undefined }))
                        }
                      }}
                      placeholder="e.g. Jane Doe"
                      className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                        errors.fullName
                          ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                          : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                      }`}
                    />
                    {errors.fullName && (
                      <p className="mt-1 text-xs text-rose-600 flex items-center gap-1">
                        <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                        </svg>
                        {errors.fullName}
                      </p>
                    )}
                  </div>

                  {/* Student ID / Staff ID */}
                  <div>
                    <label htmlFor="identifier" className="block text-xs font-semibold text-slate-700 mb-1">
                      {accountType === 'Student' ? 'Student ID Number' : 'Staff ID Number'}
                    </label>
                    <input
                      id="identifier"
                      name="identifier"
                      type="text"
                      disabled={isSubmitting}
                      value={identifier}
                      onChange={(e) => {
                        setIdentifier(e.target.value)
                        if (errors.identifier) {
                          setErrors((prev) => ({ ...prev, identifier: undefined }))
                        }
                      }}
                      placeholder={
                        accountType === 'Student'
                          ? 'e.g. STU-2026-001'
                          : 'e.g. STF-CS-001'
                      }
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

                  {/* Institutional Email */}
                  <div>
                    <label htmlFor="email" className="block text-xs font-semibold text-slate-700 mb-1">
                      Institutional Email Address
                    </label>
                    <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      disabled={isSubmitting}
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value)
                        if (errors.email) {
                          setErrors((prev) => ({ ...prev, email: undefined }))
                        }
                      }}
                      placeholder={
                        accountType === 'Student'
                          ? 'e.g. student@university.edu'
                          : 'e.g. lecturer@university.edu'
                      }
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

                  {/* Password */}
                  <div>
                    <div className="flex items-center justify-between mb-1">
                      <label htmlFor="password" className="block text-xs font-semibold text-slate-700">
                        Password
                      </label>
                      <span className="text-[11px] text-slate-400">
                        Min. 8 characters
                      </span>
                    </div>
                    <div className="relative">
                      <input
                        id="password"
                        name="password"
                        type={showPassword ? 'text' : 'password'}
                        autoComplete="new-password"
                        disabled={isSubmitting}
                        value={password}
                        onChange={(e) => {
                          setPassword(e.target.value)
                          if (errors.password) {
                            setErrors((prev) => ({ ...prev, password: undefined }))
                          }
                        }}
                        placeholder="Create a strong password"
                        className={`w-full rounded-lg border px-3.5 py-2.5 pr-10 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
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

                  {/* Confirm Password */}
                  <div>
                    <label htmlFor="confirmPassword" className="block text-xs font-semibold text-slate-700 mb-1">
                      Confirm Password
                    </label>
                    <div className="relative">
                      <input
                        id="confirmPassword"
                        name="confirmPassword"
                        type={showConfirmPassword ? 'text' : 'password'}
                        autoComplete="new-password"
                        disabled={isSubmitting}
                        value={confirmPassword}
                        onChange={(e) => {
                          setConfirmPassword(e.target.value)
                          if (errors.confirmPassword) {
                            setErrors((prev) => ({ ...prev, confirmPassword: undefined }))
                          }
                        }}
                        placeholder="Re-enter password"
                        className={`w-full rounded-lg border px-3.5 py-2.5 pr-10 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                          errors.confirmPassword
                            ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                            : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                        }`}
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute inset-y-0 right-0 flex items-center pr-3 text-slate-400 hover:text-slate-600"
                        aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                      >
                        {showConfirmPassword ? (
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
                    {errors.confirmPassword && (
                      <p className="mt-1 text-xs text-rose-600 flex items-center gap-1">
                        <svg className="h-3.5 w-3.5 shrink-0" fill="currentColor" viewBox="0 0 20 20">
                          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                        </svg>
                        {errors.confirmPassword}
                      </p>
                    )}
                  </div>

                  {/* Submit Button with Loading State */}
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className={`w-full rounded-lg px-4 py-2.5 text-sm font-semibold text-white shadow-xs focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 transition-all mt-2 flex items-center justify-center gap-2 ${
                      isSubmitting
                        ? 'bg-blue-400 cursor-not-allowed'
                        : 'bg-blue-600 hover:bg-blue-700 active:bg-blue-800'
                    }`}
                  >
                    {isSubmitting ? (
                      <>
                        <svg className="h-4 w-4 animate-spin text-white" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                        </svg>
                        Submitting {accountType} Request...
                      </>
                    ) : (
                      `Submit ${accountType} Registration`
                    )}
                  </button>
                </form>

                {/* Bottom Navigation Links */}
                <div className="mt-6 text-center text-xs text-slate-600 border-t border-slate-100 pt-4 space-y-2">
                  <div>
                    Already have an approved account?{' '}
                    <button
                      type="button"
                      onClick={onNavigateToLogin}
                      className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                    >
                      Sign in here
                    </button>
                  </div>
                  {onNavigateToStatus && (
                    <div>
                      Want to check how verification works?{' '}
                      <button
                        type="button"
                        onClick={onNavigateToStatus}
                        className="font-semibold text-blue-600 hover:text-blue-700 underline-offset-2 hover:underline"
                      >
                        View approval status guidance
                      </button>
                    </div>
                  )}
                </div>
              </>
            )}
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
