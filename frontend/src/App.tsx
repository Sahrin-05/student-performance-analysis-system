import { useState, useEffect } from 'react'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'
import RequestStatusPage from './RequestStatusPage'
import AdminApprovalsQueue from './AdminApprovalsQueue'
import {
  getStoredAuthSession,
  clearAuthSession,
  onUnauthorized,
  type LoginResponseData,
} from './services/authService'

export default function App() {
  const [currentPage, setCurrentPage] = useState<'dashboard' | 'login' | 'register' | 'status'>('dashboard')
  const [currentUser, setCurrentUser] = useState<LoginResponseData | null>(() => getStoredAuthSession())
  const [adminView, setAdminView] = useState<'overview' | 'approvals'>('overview')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [statusPrefill, setStatusPrefill] = useState<{ identifier: string; email: string } | null>(null)

  // Listen for 401 Unauthorized events from authenticatedFetch
  useEffect(() => {
    const unsubscribe = onUnauthorized(() => {
      setCurrentUser(null)
      setAdminView('overview')
      setCurrentPage('login')
    })
    return unsubscribe
  }, [])

  const handleSignOut = () => {
    clearAuthSession()
    setCurrentUser(null)
    setAdminView('overview')
    setSidebarOpen(false)
    setCurrentPage('dashboard')
  }

  // 1. Login View
  if (currentPage === 'login') {
    return (
      <LoginPage
        onBackToDashboard={() => setCurrentPage('dashboard')}
        onNavigateToRegister={() => setCurrentPage('register')}
        onNavigateToStatus={() => {
          setStatusPrefill(null)
          setCurrentPage('status')
        }}
        onLoginSuccess={(user) => {
          setCurrentUser(user)
          setAdminView('overview')
          setCurrentPage('dashboard')
        }}
      />
    )
  }

  // 2. Registration View
  if (currentPage === 'register') {
    return (
      <RegisterPage
        onBackToDashboard={() => setCurrentPage('dashboard')}
        onNavigateToLogin={() => setCurrentPage('login')}
        onNavigateToStatus={(id, em) => {
          if (id && em) {
            setStatusPrefill({ identifier: id, email: em })
          } else {
            setStatusPrefill(null)
          }
          setCurrentPage('status')
        }}
      />
    )
  }

  // 3. Status Guidance View
  if (currentPage === 'status') {
    return (
      <RequestStatusPage
        onBackToDashboard={() => setCurrentPage('dashboard')}
        onNavigateToLogin={() => setCurrentPage('login')}
        onNavigateToRegister={() => setCurrentPage('register')}
        initialIdentifier={statusPrefill?.identifier}
        initialEmail={statusPrefill?.email}
      />
    )
  }

  // 4. Unauthenticated Public Landing Page View
  if (!currentUser) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col justify-between text-slate-900 antialiased font-sans">
        {/* Public Header Navigation */}
        <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur-xs px-4 py-3 sm:px-8">
          <div className="mx-auto flex max-w-6xl items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-white font-bold shadow-xs">
                SP
              </div>
              <div>
                <h1 className="text-base font-bold tracking-tight text-slate-900">SPAS Portal</h1>
                <p className="text-[11px] text-slate-500">Student Performance Analysis System</p>
              </div>
            </div>

            <div className="flex items-center gap-2.5 sm:gap-3">
              <button
                type="button"
                onClick={() => setCurrentPage('status')}
                className="hidden sm:inline-flex text-xs font-medium text-slate-600 hover:text-slate-900 px-3 py-1.5 transition-colors"
              >
                Track Status
              </button>
              <button
                type="button"
                onClick={() => setCurrentPage('login')}
                className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
              >
                Sign In
              </button>
              <button
                type="button"
                onClick={() => setCurrentPage('register')}
                className="inline-flex items-center gap-1.5 rounded-lg bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors"
              >
                Request Account
              </button>
            </div>
          </div>
        </header>

        {/* Hero Section */}
        <main className="flex-1">
          <section className="border-b border-slate-200 bg-gradient-to-b from-white to-slate-50 px-4 py-16 sm:px-8 sm:py-24">
            <div className="mx-auto max-w-4xl text-center space-y-6">
              <div className="inline-flex items-center gap-2 rounded-full bg-blue-50 px-3.5 py-1 text-xs font-semibold text-blue-700 border border-blue-200">
                <span className="h-1.5 w-1.5 rounded-full bg-blue-600 animate-pulse"></span>
                University Academic Analytics & Performance Platform
              </div>

              <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 sm:text-5xl sm:leading-tight">
                Empowering Academic Success Through{' '}
                <span className="text-blue-600">Performance Analytics</span>
              </h2>

              <p className="mx-auto max-w-2xl text-sm leading-relaxed text-slate-600 sm:text-base">
                A unified university portal designed to monitor course outcomes, analyze student assessments, and deliver early intervention alerts for at-risk students across faculties.
              </p>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setCurrentPage('login')}
                  className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl bg-blue-600 px-6 py-3.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 transition-all"
                >
                  Sign In to Portal
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                  </svg>
                </button>
                <button
                  type="button"
                  onClick={() => setCurrentPage('register')}
                  className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-slate-300 bg-white px-6 py-3.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-all shadow-xs"
                >
                  Request Student or Lecturer Account
                </button>
              </div>

              {/* Status Tracking Link */}
              <div className="pt-2">
                <button
                  type="button"
                  onClick={() => setCurrentPage('status')}
                  className="text-xs font-medium text-slate-500 hover:text-blue-600 transition-colors inline-flex items-center gap-1"
                >
                  Already submitted an onboarding request?
                  <span className="underline underline-offset-2 font-semibold">Check verification status</span>
                </button>
              </div>
            </div>
          </section>

          {/* Key Capabilities Section */}
          <section className="px-4 py-16 sm:px-8 max-w-6xl mx-auto">
            <div className="text-center mb-12">
              <span className="text-xs font-semibold uppercase tracking-wider text-blue-600">Core Capabilities</span>
              <h3 className="text-2xl font-bold text-slate-900 mt-1">Data-Driven Academic Management</h3>
              <p className="text-xs text-slate-600 max-w-lg mx-auto mt-2">
                Structured metrics and role-based workflows built for students, lecturers, and academic administrators.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-slate-300 transition-all">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-100 text-blue-600 mb-4">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <h4 className="text-base font-semibold text-slate-900">Academic Progress Tracking</h4>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Continuous assessment recording, midterm and final examination evaluations with automated GPA calculation and progress indicators.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-slate-300 transition-all">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-emerald-100 text-emerald-600 mb-4">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 8v8m-4-5v5m-4-2v2m-2 4h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <h4 className="text-base font-semibold text-slate-900">Performance Analytics</h4>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Visual grade distribution curves and trend lines highlighting semester-over-semester growth and faculty-level comparisons.
                </p>
              </div>

              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-slate-300 transition-all">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-100 text-amber-700 mb-4">
                  <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                </div>
                <h4 className="text-base font-semibold text-slate-900">Early Intervention Support</h4>
                <p className="mt-2 text-xs leading-relaxed text-slate-600">
                  Algorithmic alerts highlighting students at academic risk to enable proactive advisory support before final grading periods.
                </p>
              </div>
            </div>
          </section>

          {/* Role Portals Overview Section */}
          <section className="bg-white border-t border-b border-slate-200 px-4 py-16 sm:px-8">
            <div className="max-w-6xl mx-auto space-y-8">
              <div className="text-center">
                <span className="text-xs font-semibold uppercase tracking-wider text-blue-600">Role-Based Access</span>
                <h3 className="text-2xl font-bold text-slate-900 mt-1">Tailored for Every Academic Stakeholder</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="rounded-xl border border-slate-200 bg-slate-50 p-5 space-y-2">
                  <span className="inline-flex items-center rounded-md bg-blue-100 px-2 py-0.5 text-xs font-semibold text-blue-800">
                    Students
                  </span>
                  <h4 className="text-sm font-semibold text-slate-900">Personal Academic Portal</h4>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    View semester marks, evaluate your cumulative GPA, and receive timely notifications on coursework milestones.
                  </p>
                </div>

                <div className="rounded-xl border border-slate-200 bg-slate-50 p-5 space-y-2">
                  <span className="inline-flex items-center rounded-md bg-emerald-100 px-2 py-0.5 text-xs font-semibold text-emerald-800">
                    Lecturers & Faculty
                  </span>
                  <h4 className="text-sm font-semibold text-slate-900">Instruction & Grading Suite</h4>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Record continuous assessments, monitor class performance distributions, and identify students who require academic support.
                  </p>
                </div>

                <div className="rounded-xl border border-slate-200 bg-slate-50 p-5 space-y-2">
                  <span className="inline-flex items-center rounded-md bg-purple-100 px-2 py-0.5 text-xs font-semibold text-purple-800">
                    Administrators
                  </span>
                  <h4 className="text-sm font-semibold text-slate-900">Administrative Governance</h4>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    Review and approve incoming student and lecturer registration requests, verify institutional credentials, and maintain system integrity.
                  </p>
                </div>
              </div>
            </div>
          </section>
        </main>

        {/* Footer */}
        <footer className="border-t border-slate-200 bg-white py-6 px-4 sm:px-8 text-center text-xs text-slate-500 space-y-1">
          <p className="font-medium text-slate-700">Student Performance Analysis System (SPAS)</p>
          <p>University Academic Progress & Analytics Platform &copy; 2026</p>
        </footer>
      </div>
    )
  }

  // 5. Authenticated Role-Based Dashboard View (Student, Lecturer, Admin)
  const navLinks = [
    {
      name: 'Overview Dashboard',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
      ),
      active: currentUser.role === 'ADMIN' ? adminView === 'overview' : true,
      onClick: currentUser.role === 'ADMIN' ? () => {
        setAdminView('overview')
        setSidebarOpen(false)
      } : undefined,
    },
    {
      name: currentUser.role === 'ADMIN' ? 'User Approvals Queue' : 'Courses & Subjects',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
      ),
      active: currentUser.role === 'ADMIN' ? adminView === 'approvals' : false,
      onClick: currentUser.role === 'ADMIN' ? () => {
        setAdminView('approvals')
        setSidebarOpen(false)
      } : undefined,
    },
    {
      name: currentUser.role === 'ADMIN' ? 'System Audit Trail' : 'Grades & Marks',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
        </svg>
      ),
      active: false,
    },
    {
      name: 'Analytics & Reports',
      icon: (
        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      ),
      active: false,
    },
  ]

  return (
    <div className="flex h-screen bg-slate-50 text-slate-900 antialiased font-sans overflow-hidden">
      {/* Mobile Sidebar Backdrop */}
      {sidebarOpen && (
        <div
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs lg:hidden"
          aria-hidden="true"
        />
      )}

      {/* Sidebar Navigation */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 flex w-64 flex-col bg-slate-900 text-slate-100 transition-transform duration-200 ease-in-out lg:static lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-0 max-lg:-translate-x-full'
        }`}
      >
        {/* Brand Area */}
        <div className="flex h-16 items-center justify-between px-6 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white font-bold shadow-xs">
              SP
            </div>
            <div>
              <h1 className="text-sm font-semibold tracking-wide text-white">SPAS Portal</h1>
              <p className="text-[11px] text-slate-400">Academic Analytics</p>
            </div>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="rounded p-1 text-slate-400 hover:text-white lg:hidden"
            aria-label="Close sidebar"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Links */}
        <nav className="flex-1 space-y-1.5 px-3 py-4 overflow-y-auto">
          <div className="px-3 pb-2 text-[11px] font-semibold tracking-wider text-slate-400 uppercase">
            {currentUser.role} Navigation
          </div>
          {navLinks.map((item) => {
            const isClickable = Boolean(item.onClick)
            return isClickable ? (
              <button
                key={item.name}
                type="button"
                onClick={item.onClick}
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors text-left cursor-pointer ${
                  item.active
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                }`}
              >
                {item.icon}
                <span>{item.name}</span>
              </button>
            ) : (
              <div
                key={item.name}
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors text-left ${
                  item.active
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-slate-500 opacity-60 cursor-default'
                }`}
              >
                {item.icon}
                <span>{item.name}</span>
              </div>
            )
          })}

          {/* Sign Out Link in Sidebar */}
          <div className="pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={handleSignOut}
              className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-rose-300 hover:bg-slate-800 hover:text-rose-200 transition-colors text-left"
            >
              <svg className="h-5 w-5 text-rose-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span>Sign Out ({currentUser.fullName.split(' ')[0]})</span>
            </button>
          </div>
        </nav>

        {/* Footer Role Indicator */}
        <div className="border-t border-slate-800 p-4">
          <div className="flex items-center gap-3 rounded-lg bg-slate-800/60 p-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-700 text-xs font-semibold text-slate-200">
              {currentUser.fullName
                .split(' ')
                .map((n) => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase()}
            </div>
            <div className="truncate">
              <p className="text-xs font-semibold text-slate-200">{currentUser.fullName}</p>
              <p className="text-[11px] text-emerald-400">
                ● Role: {currentUser.role}
              </p>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Top Header */}
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setSidebarOpen(true)}
              className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
              aria-label="Open sidebar"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <div>
              <h2 className="text-lg font-semibold text-slate-900">
                {currentUser.role === 'ADMIN' && (adminView === 'approvals' ? 'User Approvals & Account Authorization' : 'Administrator Management Center')}
                {currentUser.role === 'LECTURER' && 'Faculty Instruction & Grading Portal'}
                {currentUser.role === 'STUDENT' && 'Student Academic Progress Portal'}
              </h2>
              <p className="hidden text-xs text-slate-500 sm:block">
                Authenticated User: {currentUser.identifier} ({currentUser.email})
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="hidden text-right md:block">
              <p className="text-xs font-semibold text-slate-800">{currentUser.fullName}</p>
              <span className="inline-flex items-center rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold text-emerald-700 border border-emerald-200">
                {currentUser.role} • Active
              </span>
            </div>
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-600 text-sm font-semibold text-white shadow-xs">
              {currentUser.fullName
                .split(' ')
                .map((n) => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase()}
            </div>
            <button
              type="button"
              onClick={handleSignOut}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors shadow-xs"
            >
              Sign Out
            </button>
          </div>
        </header>

        {/* Role-Specific Body Canvas */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div className="mx-auto max-w-5xl space-y-6">
            {/* ADMIN DASHBOARD VIEW */}
            {currentUser.role === 'ADMIN' && (
              adminView === 'approvals' ? (
                <AdminApprovalsQueue onBackToOverview={() => setAdminView('overview')} />
              ) : (
                <>
                  <section className="rounded-2xl border border-purple-200 bg-purple-50/50 p-6 sm:p-8 shadow-xs">
                    <div className="max-w-3xl space-y-2">
                      <span className="inline-flex items-center rounded-full bg-purple-100 px-3 py-1 text-xs font-semibold text-purple-800 border border-purple-200">
                        System Governance
                      </span>
                      <h3 className="text-2xl font-bold tracking-tight text-slate-900">
                        Welcome, Administrator {currentUser.fullName}
                      </h3>
                      <p className="text-xs leading-relaxed text-slate-600">
                        Manage system users, review applicant onboarding requests, and oversee academic analytics security.
                      </p>
                    </div>
                  </section>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                      <span className="text-xs text-slate-500 font-medium">PostgreSQL Database</span>
                      <h4 className="text-lg font-bold text-slate-900 mt-1">Connected</h4>
                      <p className="text-[11px] text-emerald-600 mt-1">● spas_db Active</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                      <span className="text-xs text-slate-500 font-medium">Approval Workflow</span>
                      <h4 className="text-lg font-bold text-slate-900 mt-1">Queue Ready</h4>
                      <p className="text-[11px] text-emerald-600 mt-1">● Live Backend API Connected</p>
                    </div>

                    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                      <span className="text-xs text-slate-500 font-medium">Security Policy</span>
                      <h4 className="text-lg font-bold text-slate-900 mt-1">Stateless</h4>
                      <p className="text-[11px] text-slate-600 mt-1">BCrypt Encryption</p>
                    </div>
                  </div>

                  {/* Admin Approval Section */}
                  <section className="rounded-xl border border-blue-200 bg-white p-6 shadow-xs space-y-4">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-100 pb-3">
                      <div>
                        <h4 className="text-sm font-semibold text-slate-900">Pending Registrations Approval Queue</h4>
                        <p className="text-xs text-slate-500">Student and Lecturer applicants awaiting administrative authorization</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setAdminView('approvals')}
                        className="inline-flex items-center justify-center gap-1.5 rounded-lg bg-blue-600 px-3.5 py-2 text-xs font-semibold text-white shadow-xs hover:bg-blue-700 transition-colors cursor-pointer"
                      >
                        <span>Open Approvals Queue</span>
                        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                        </svg>
                      </button>
                    </div>

                    <div className="rounded-lg bg-slate-50 border border-slate-200 p-4 text-xs text-slate-600 space-y-2 leading-relaxed">
                      <p>
                        Review incoming student and lecturer registration requests. Authorize accounts to allow sign-in, or reject unverified applicants with a permanent rejection record.
                      </p>
                    </div>
                  </section>
                </>
              )
            )}

            {/* LECTURER DASHBOARD VIEW */}
            {currentUser.role === 'LECTURER' && (
              <>
                <section className="rounded-2xl border border-emerald-200 bg-emerald-50/50 p-6 sm:p-8 shadow-xs">
                  <div className="max-w-3xl space-y-2">
                    <span className="inline-flex items-center rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-800 border border-emerald-200">
                      Faculty Portal
                    </span>
                    <h3 className="text-2xl font-bold tracking-tight text-slate-900">
                      Welcome, Instructor {currentUser.fullName}
                    </h3>
                    <p className="text-xs leading-relaxed text-slate-600">
                      Staff ID: <span className="font-semibold text-slate-800">{currentUser.identifier}</span> • Monitor enrolled classes and record continuous assessment metrics.
                    </p>
                  </div>
                </section>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Assigned Courses</span>
                    <h4 className="text-lg font-bold text-slate-900 mt-1">Semester Roster</h4>
                    <p className="text-[11px] text-slate-500 mt-1">Module in development</p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Student Assessments</span>
                    <h4 className="text-lg font-bold text-slate-900 mt-1">Continuous Grading</h4>
                    <p className="text-[11px] text-slate-500 mt-1">Module in development</p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Account Status</span>
                    <h4 className="text-lg font-bold text-emerald-600 mt-1">Verified & Active</h4>
                    <p className="text-[11px] text-slate-500 mt-1">Authenticated via Spring Security</p>
                  </div>
                </div>

                <div className="rounded-xl bg-blue-50/70 border border-blue-200 p-4 text-xs text-blue-900 leading-relaxed">
                  <span className="font-semibold">Academic Modules Notice:</span> Course assignment, class rosters, and assessment mark entry will be connected in upcoming functional milestones. Your lecturer account has been authenticated.
                </div>
              </>
            )}

            {/* STUDENT DASHBOARD VIEW */}
            {currentUser.role === 'STUDENT' && (
              <>
                <section className="rounded-2xl border border-blue-200 bg-blue-50/50 p-6 sm:p-8 shadow-xs">
                  <div className="max-w-3xl space-y-2">
                    <span className="inline-flex items-center rounded-full bg-blue-100 px-3 py-1 text-xs font-semibold text-blue-800 border border-blue-200">
                      Student Academic Standing
                    </span>
                    <h3 className="text-2xl font-bold tracking-tight text-slate-900">
                      Welcome, {currentUser.fullName}
                    </h3>
                    <p className="text-xs leading-relaxed text-slate-600">
                      Student ID: <span className="font-semibold text-slate-800">{currentUser.identifier}</span> • Track your academic progress, continuous assessments, and GPA trends.
                    </p>
                  </div>
                </section>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Academic Status</span>
                    <h4 className="text-lg font-bold text-emerald-600 mt-1">Good Standing</h4>
                    <p className="text-[11px] text-slate-500 mt-1">Active Matriculation</p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Cumulative GPA</span>
                    <h4 className="text-lg font-bold text-slate-900 mt-1">Term 1</h4>
                    <p className="text-[11px] text-slate-500 mt-1">Awaiting grade posting</p>
                  </div>

                  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-xs">
                    <span className="text-xs text-slate-500 font-medium">Intervention Alert</span>
                    <h4 className="text-lg font-bold text-emerald-600 mt-1">Clear</h4>
                    <p className="text-[11px] text-slate-500 mt-1">No at-risk indicators</p>
                  </div>
                </div>

                <div className="rounded-xl bg-blue-50/70 border border-blue-200 p-4 text-xs text-blue-900 leading-relaxed">
                  <span className="font-semibold">Student Portal Notice:</span> Course syllabus enrollment and individual grade sheets are scheduled for upcoming academic releases. Your student account has been successfully verified and activated.
                </div>
              </>
            )}
          </div>
        </main>
      </div>
    </div>
  )
}
