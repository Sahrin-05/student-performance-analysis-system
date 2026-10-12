import React, { useState } from 'react'

export type UserRole = 'Student' | 'Lecturer' | 'Admin'

interface LoginPageProps {
  onBackToDashboard: () => void
  onLoginSuccess?: (role: UserRole) => void
}

export default function LoginPage({ onBackToDashboard }: LoginPageProps) {
  const [selectedRole, setSelectedRole] = useState<UserRole>('Student')
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  // Validation state
  const [errors, setErrors] = useState<{ identifier?: string; password?: string }>({})
  const [submittedRole, setSubmittedRole] = useState<UserRole | null>(null)

  const roles: { id: UserRole; title: string; desc: string; badge: string }[] = [
    {
      id: 'Student',
      title: 'Student',
      desc: 'Access grades, progress & reports',
      badge: 'STU',
    },
    {
      id: 'Lecturer',
      title: 'Lecturer',
      desc: 'Enter marks & review class insights',
      badge: 'LEC',
    },
    {
      id: 'Admin',
      title: 'Admin',
      desc: 'Manage users, courses & system setup',
      badge: 'ADM',
    },
  ]

  const validate = (): boolean => {
    const newErrors: { identifier?: string; password?: string } = {}

    const trimmedIdentifier = identifier.trim()
    if (!trimmedIdentifier) {
      newErrors.identifier = 'Email or Username is required.'
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

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (validate()) {
      setSubmittedRole(selectedRole)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between text-slate-900 antialiased font-sans">
      {/* Top Bar with Brand & Back Navigation */}
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
          <button
            type="button"
            onClick={onBackToDashboard}
            className="inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-3.5 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Dashboard
          </button>
        </div>
      </header>

      {/* Main Login Container */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-md">
          {/* Card Wrapper */}
          <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-8 shadow-xs">
            {/* Header */}
            <div className="text-center mb-6">
              <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 border border-blue-200 mb-2">
                University Authentication
              </span>
              <h2 className="text-2xl font-bold tracking-tight text-slate-900">
                Sign in to SPAS
              </h2>
              <p className="mt-1 text-xs text-slate-600">
                Choose your role and enter your institutional credentials.
              </p>
            </div>

            {/* Client-Side Submission Banner */}
            {submittedRole && (
              <div className="mb-5 rounded-lg border border-emerald-200 bg-emerald-50 p-3.5 text-xs text-emerald-800">
                <div className="flex items-center gap-2 font-semibold">
                  <svg className="h-4 w-4 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Client Validation Passed
                </div>
                <p className="mt-1 text-emerald-700">
                  Form submitted for <strong className="font-semibold">{submittedRole}</strong> role. Ready for backend authentication integration.
                </p>
              </div>
            )}

            {/* Role Selection Tabs */}
            <div className="mb-6">
              <label className="block text-xs font-semibold text-slate-700 mb-2">
                Select Your Role
              </label>
              <div className="grid grid-cols-3 gap-2" role="radiogroup" aria-label="Account Role Selection">
                {roles.map((role) => {
                  const isSelected = selectedRole === role.id
                  return (
                    <button
                      key={role.id}
                      type="button"
                      role="radio"
                      aria-checked={isSelected}
                      onClick={() => {
                        setSelectedRole(role.id)
                        setSubmittedRole(null)
                      }}
                      className={`flex flex-col items-center justify-center rounded-xl p-2.5 text-center transition-all border ${
                        isSelected
                          ? 'border-blue-600 bg-blue-50/70 text-blue-900 shadow-xs ring-1 ring-blue-600'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:bg-slate-50'
                      }`}
                    >
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                          isSelected ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-500'
                        }`}
                      >
                        {role.badge}
                      </span>
                      <span className="mt-1 text-xs font-semibold">{role.title}</span>
                    </button>
                  )
                })}
              </div>
              <p className="mt-1.5 text-[11px] text-slate-500 text-center">
                {roles.find((r) => r.id === selectedRole)?.desc}
              </p>
            </div>

            {/* Login Form */}
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              {/* Identifier Input */}
              <div>
                <label
                  htmlFor="identifier"
                  className="block text-xs font-semibold text-slate-700 mb-1"
                >
                  {selectedRole === 'Student' ? 'Student ID or Email' : 'Institutional Email or Username'}
                </label>
                <div className="relative">
                  <input
                    id="identifier"
                    name="identifier"
                    type="text"
                    autoComplete="username"
                    value={identifier}
                    onChange={(e) => {
                      setIdentifier(e.target.value)
                      if (errors.identifier) {
                        setErrors((prev) => ({ ...prev, identifier: undefined }))
                      }
                    }}
                    placeholder={
                      selectedRole === 'Student'
                        ? 'e.g. stu202401 or student@uni.edu'
                        : selectedRole === 'Lecturer'
                        ? 'e.g. prof.smith or lecturer@uni.edu'
                        : 'e.g. admin or admin@uni.edu'
                    }
                    className={`w-full rounded-lg border px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-hidden transition-colors ${
                      errors.identifier
                        ? 'border-rose-400 bg-rose-50/30 focus:border-rose-600 focus:ring-1 focus:ring-rose-600'
                        : 'border-slate-300 bg-white focus:border-blue-600 focus:ring-1 focus:ring-blue-600'
                    }`}
                  />
                </div>
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
                  <a
                    href="#"
                    onClick={(e) => {
                      e.preventDefault()
                      alert('Password recovery will be connected to the institutional directory service in the backend.')
                    }}
                    className="text-[11px] font-medium text-blue-600 hover:text-blue-700"
                  >
                    Forgot password?
                  </a>
                </div>
                <div className="relative">
                  <input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value)
                      if (errors.password) {
                        setErrors((prev) => ({ ...prev, password: undefined }))
                      }
                    }}
                    placeholder="Enter your password"
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

              {/* Remember Me Checkbox */}
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                <label htmlFor="remember-me" className="ml-2 block text-xs text-slate-600">
                  Remember this device for 30 days
                </label>
              </div>

              {/* Login Button */}
              <button
                type="submit"
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-blue-700 focus:outline-hidden focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 transition-colors"
              >
                Sign In as {selectedRole}
              </button>
            </form>

            {/* Note on Security and Backend Development */}
            <div className="mt-6 rounded-lg bg-slate-50 border border-slate-200 p-3 text-[11px] text-slate-500 leading-relaxed">
              <span className="font-semibold text-slate-700">Notice:</span> Backend JWT authentication and role authorization will be implemented in the Spring Boot phase.
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
