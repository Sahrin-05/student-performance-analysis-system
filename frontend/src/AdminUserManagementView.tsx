import { useState, useEffect, useCallback } from 'react'
import {
  fetchAllUsers,
  updateUserStatus,
  fetchPendingUsers,
  type UserAccount,
  type UserRole,
  type UserStatus,
  type PendingUser,
} from './services/adminService'
import type { ApiErrorData } from './services/authService'
import AdminApprovalsQueue from './AdminApprovalsQueue'

interface AdminUserManagementViewProps {
  onBackToOverview?: () => void
}

export default function AdminUserManagementView({ onBackToOverview }: AdminUserManagementViewProps) {
  const [activeTab, setActiveTab] = useState<'directory' | 'approvals'>('directory')

  // User Directory State
  const [users, setUsers] = useState<UserAccount[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [roleFilter, setRoleFilter] = useState<UserRole | ''>('')
  const [statusFilter, setStatusFilter] = useState<UserStatus | ''>('')
  const [searchQuery, setSearchQuery] = useState('')
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null)

  // Pending Count for badge
  const [pendingCount, setPendingCount] = useState<number>(0)

  // Modal State for Status Transitions (Suspend / Reactivate / Reinstate)
  const [statusModalUser, setStatusModalUser] = useState<UserAccount | null>(null)
  const [targetStatus, setTargetStatus] = useState<UserStatus | null>(null)
  const [statusReason, setStatusReason] = useState('')
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false)
  const [modalError, setModalError] = useState<string | null>(null)

  const loadUsers = useCallback(async (isSilent = false) => {
    if (isSilent) setIsRefreshing(true)
    else setIsLoading(true)
    setFeedback(null)

    try {
      const data = await fetchAllUsers({
        role: roleFilter,
        status: statusFilter,
        search: searchQuery,
      })
      setUsers(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({
        type: 'error',
        message: apiErr.message || 'Failed to load user directory.',
      })
    } finally {
      setIsLoading(false)
      setIsRefreshing(false)
    }
  }, [roleFilter, statusFilter, searchQuery])

  // Fetch pending count periodically or on tab switch
  const loadPendingCount = useCallback(async () => {
    try {
      const pending: PendingUser[] = await fetchPendingUsers()
      setPendingCount(pending.length)
    } catch {
      // silently ignore error on secondary count fetch
    }
  }, [])

  useEffect(() => {
    queueMicrotask(() => {
      loadUsers()
      loadPendingCount()
    })
  }, [loadUsers, loadPendingCount])

  // Quick stats computed from current users or whole state
  const totalUsers = users.length
  const activeStudents = users.filter((u) => u.role === 'STUDENT' && u.status === 'ACTIVE').length
  const activeLecturers = users.filter((u) => u.role === 'LECTURER' && u.status === 'ACTIVE').length
  const suspendedUsers = users.filter((u) => u.status === 'SUSPENDED').length
  const rejectedUsers = users.filter((u) => u.status === 'REJECTED').length

  const openStatusModal = (user: UserAccount, newStatus: UserStatus) => {
    setStatusModalUser(user)
    setTargetStatus(newStatus)
    setStatusReason('')
    setModalError(null)
  }

  const closeStatusModal = () => {
    setStatusModalUser(null)
    setTargetStatus(null)
    setStatusReason('')
    setModalError(null)
  }

  const handleConfirmStatusChange = async () => {
    if (!statusModalUser || !targetStatus) return

    setIsUpdatingStatus(true)
    setModalError(null)

    try {
      const updated = await updateUserStatus(
        statusModalUser.id,
        targetStatus,
        statusReason.trim() || undefined
      )

      setUsers((prev) =>
        prev.map((u) => (u.id === updated.id ? { ...u, status: updated.status } : u))
      )

      const actionLabel =
        targetStatus === 'SUSPENDED'
          ? 'suspended'
          : targetStatus === 'ACTIVE'
          ? 'reactivated'
          : 'updated'

      setFeedback({
        type: 'success',
        message: `Account for ${updated.fullName} (${updated.identifier}) was successfully ${actionLabel}.`,
      })

      closeStatusModal()
      loadPendingCount()
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setModalError(apiErr.message || 'Failed to update account status.')
    } finally {
      setIsUpdatingStatus(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Banner & Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white border border-slate-200 rounded-2xl p-6 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center rounded-full bg-blue-100 px-3 py-1 text-xs font-semibold text-blue-800 border border-blue-200">
              Identity & Access Management
            </span>
            {pendingCount > 0 && (
              <span className="inline-flex items-center rounded-full bg-amber-100 px-2.5 py-0.5 text-xs font-bold text-amber-800 border border-amber-300 animate-pulse">
                {pendingCount} Pending Approvals
              </span>
            )}
          </div>
          <h2 className="text-xl sm:text-2xl font-bold text-slate-900 mt-2">
            User Accounts & Directory Management
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Oversee institutional identities, approve incoming applicant registrations, and manage account statuses.
          </p>
        </div>

        {onBackToOverview && (
          <button
            type="button"
            onClick={onBackToOverview}
            className="inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 transition-colors shadow-xs self-start sm:self-auto cursor-pointer"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            <span>Back to Overview</span>
          </button>
        )}
      </div>

      {/* KPI Stats Row */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
        <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-xs">
          <p className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Total Users</p>
          <p className="text-2xl font-bold text-slate-900 mt-1">{totalUsers}</p>
          <p className="text-[11px] text-slate-400 mt-0.5">Across all roles</p>
        </div>

        <div className="rounded-xl border border-blue-200 bg-blue-50/40 p-4 shadow-xs">
          <p className="text-[11px] font-semibold text-blue-700 uppercase tracking-wider">Active Students</p>
          <p className="text-2xl font-bold text-blue-900 mt-1">{activeStudents}</p>
          <p className="text-[11px] text-blue-600 mt-0.5">Enrolment eligible</p>
        </div>

        <div className="rounded-xl border border-emerald-200 bg-emerald-50/40 p-4 shadow-xs">
          <p className="text-[11px] font-semibold text-emerald-700 uppercase tracking-wider">Active Faculty</p>
          <p className="text-2xl font-bold text-emerald-900 mt-1">{activeLecturers}</p>
          <p className="text-[11px] text-emerald-600 mt-0.5">Teaching faculty</p>
        </div>

        <div className="rounded-xl border border-amber-200 bg-amber-50/40 p-4 shadow-xs">
          <p className="text-[11px] font-semibold text-amber-700 uppercase tracking-wider">Pending Approvals</p>
          <p className="text-2xl font-bold text-amber-900 mt-1">{pendingCount}</p>
          <p className="text-[11px] text-amber-600 mt-0.5">Awaiting review</p>
        </div>

        <div className="rounded-xl border border-rose-200 bg-rose-50/40 p-4 shadow-xs col-span-2 sm:col-span-1">
          <p className="text-[11px] font-semibold text-rose-700 uppercase tracking-wider">Suspended / Inactive</p>
          <p className="text-2xl font-bold text-rose-900 mt-1">{suspendedUsers + rejectedUsers}</p>
          <p className="text-[11px] text-rose-600 mt-0.5">
            {suspendedUsers} suspended • {rejectedUsers} rejected
          </p>
        </div>
      </div>

      {/* Main Tab Controls */}
      <div className="flex border-b border-slate-200">
        <button
          type="button"
          onClick={() => setActiveTab('directory')}
          className={`flex items-center gap-2 py-3 px-5 text-xs font-semibold border-b-2 transition-colors cursor-pointer ${
            activeTab === 'directory'
              ? 'border-blue-600 text-blue-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          <span>User Directory & Status Control</span>
          <span className="rounded-full bg-slate-100 text-slate-600 px-2 py-0.5 text-[10px]">
            {totalUsers}
          </span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('approvals')}
          className={`flex items-center gap-2 py-3 px-5 text-xs font-semibold border-b-2 transition-colors cursor-pointer ${
            activeTab === 'approvals'
              ? 'border-amber-600 text-amber-700'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Pending Approvals Queue</span>
          {pendingCount > 0 && (
            <span className="rounded-full bg-amber-500 text-white px-2 py-0.5 text-[10px] font-bold">
              {pendingCount}
            </span>
          )}
        </button>
      </div>

      {/* TAB 1: PENDING APPROVALS QUEUE */}
      {activeTab === 'approvals' && (
        <div className="space-y-4">
          <div className="rounded-lg bg-amber-50 border border-amber-200 p-3 text-xs text-amber-900 flex items-center justify-between">
            <span>
              Reviewing applicant queue. Approved applicants are immediately transitioned to <strong>ACTIVE</strong> and can access their respective portals.
            </span>
            <button
              type="button"
              onClick={() => {
                loadUsers(true)
                loadPendingCount()
              }}
              className="text-xs font-semibold text-amber-700 hover:text-amber-900 underline cursor-pointer"
            >
              Refresh
            </button>
          </div>
          <AdminApprovalsQueue
            onBackToOverview={() => setActiveTab('directory')}
          />
        </div>
      )}

      {/* TAB 2: USER DIRECTORY & STATUS MANAGEMENT */}
      {activeTab === 'directory' && (
        <div className="space-y-5">
          {/* Feedback Alert */}
          {feedback && (
            <div
              className={`rounded-xl p-4 text-xs font-medium flex items-center justify-between border ${
                feedback.type === 'success'
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                  : 'bg-rose-50 border-rose-200 text-rose-800'
              }`}
            >
              <span>{feedback.message}</span>
              <button
                type="button"
                onClick={() => setFeedback(null)}
                className="text-slate-400 hover:text-slate-700 cursor-pointer"
              >
                ✕
              </button>
            </div>
          )}

          {/* Filter and Search Bar */}
          <div className="flex flex-col md:flex-row md:items-center gap-3 bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
            <div className="flex-1 relative">
              <svg className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder="Search by full name, ID, or institutional email..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full rounded-lg border border-slate-300 pl-9 pr-3 py-2 text-xs text-slate-900 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"
              />
            </div>

            <div className="flex flex-wrap items-center gap-2.5">
              <select
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value as UserRole | '')}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-700 outline-none focus:border-blue-500"
              >
                <option value="">All Roles</option>
                <option value="STUDENT">Students</option>
                <option value="LECTURER">Lecturers</option>
                <option value="ADMIN">Administrators</option>
              </select>

              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as UserStatus | '')}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-xs text-slate-700 outline-none focus:border-blue-500"
              >
                <option value="">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="PENDING_APPROVAL">Pending Approval</option>
                <option value="SUSPENDED">Suspended</option>
                <option value="REJECTED">Rejected</option>
              </select>

              <button
                type="button"
                onClick={() => loadUsers(true)}
                disabled={isRefreshing}
                className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition shadow-xs cursor-pointer disabled:opacity-50"
              >
                <svg
                  className={`h-3.5 w-3.5 text-slate-500 ${isRefreshing ? 'animate-spin' : ''}`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                <span>{isRefreshing ? 'Refreshing...' : 'Refresh'}</span>
              </button>
            </div>
          </div>

          {/* User Table */}
          {isLoading ? (
            <div className="flex flex-col items-center justify-center p-12 space-y-3 bg-white rounded-xl border border-slate-200">
              <div className="w-8 h-8 border-3 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
              <p className="text-xs font-medium text-slate-500">Loading institutional user accounts...</p>
            </div>
          ) : users.length === 0 ? (
            <div className="rounded-xl border border-slate-200 bg-white p-12 text-center space-y-3">
              <div className="mx-auto w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center text-slate-400">
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h4 className="text-sm font-bold text-slate-800">No User Accounts Found</h4>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">
                No users match your search criteria. Try modifying your search query or removing role and status filters.
              </p>
              {(roleFilter || statusFilter || searchQuery) && (
                <button
                  type="button"
                  onClick={() => {
                    setRoleFilter('')
                    setStatusFilter('')
                    setSearchQuery('')
                  }}
                  className="px-3.5 py-1.5 rounded-lg bg-blue-600 text-white text-xs font-semibold hover:bg-blue-700 transition cursor-pointer"
                >
                  Clear Filters
                </button>
              )}
            </div>
          ) : (
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xs">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50/80 border-b border-slate-200 text-slate-600 uppercase font-semibold text-[10px] tracking-wider">
                    <tr>
                      <th className="py-3 px-4">User</th>
                      <th className="py-3 px-4">Identifier</th>
                      <th className="py-3 px-4">Role</th>
                      <th className="py-3 px-4">Account Status</th>
                      <th className="py-3 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {users.map((user) => {
                      const isSelfOrAdmin = user.role === 'ADMIN'

                      return (
                        <tr key={user.id} className="hover:bg-slate-50/60 transition-colors">
                          <td className="py-3.5 px-4">
                            <div className="flex items-center gap-3">
                              <div
                                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold ${
                                  user.role === 'ADMIN'
                                    ? 'bg-purple-100 text-purple-700'
                                    : user.role === 'LECTURER'
                                    ? 'bg-emerald-100 text-emerald-700'
                                    : 'bg-blue-100 text-blue-700'
                                }`}
                              >
                                {user.fullName
                                  .split(' ')
                                  .map((n) => n[0])
                                  .slice(0, 2)
                                  .join('')
                                  .toUpperCase()}
                              </div>
                              <div className="truncate">
                                <p className="font-semibold text-slate-900">{user.fullName}</p>
                                <p className="text-[11px] text-slate-500">{user.email}</p>
                              </div>
                            </div>
                          </td>

                          <td className="py-3.5 px-4 font-mono font-medium text-slate-700">
                            {user.identifier}
                          </td>

                          <td className="py-3.5 px-4">
                            <span
                              className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-[10px] font-semibold border ${
                                user.role === 'ADMIN'
                                  ? 'bg-purple-50 text-purple-700 border-purple-200'
                                  : user.role === 'LECTURER'
                                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                                  : 'bg-blue-50 text-blue-700 border-blue-200'
                              }`}
                            >
                              {user.role}
                            </span>
                          </td>

                          <td className="py-3.5 px-4">
                            <span
                              className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[10px] font-semibold border ${
                                user.status === 'ACTIVE'
                                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                                  : user.status === 'PENDING_APPROVAL'
                                  ? 'bg-amber-50 text-amber-800 border-amber-200'
                                  : user.status === 'SUSPENDED'
                                  ? 'bg-rose-50 text-rose-700 border-rose-200'
                                  : 'bg-slate-100 text-slate-600 border-slate-200'
                              }`}
                            >
                              <span
                                className={`h-1.5 w-1.5 rounded-full ${
                                  user.status === 'ACTIVE'
                                    ? 'bg-emerald-500'
                                    : user.status === 'PENDING_APPROVAL'
                                    ? 'bg-amber-500'
                                    : user.status === 'SUSPENDED'
                                    ? 'bg-rose-500'
                                    : 'bg-slate-400'
                                }`}
                              ></span>
                              {user.status === 'PENDING_APPROVAL'
                                ? 'PENDING'
                                : user.status}
                            </span>
                          </td>

                          <td className="py-3.5 px-4 text-right">
                            {isSelfOrAdmin ? (
                              <span className="text-[11px] text-slate-400 font-medium italic">
                                Protected Admin
                              </span>
                            ) : user.status === 'ACTIVE' ? (
                              <button
                                type="button"
                                onClick={() => openStatusModal(user, 'SUSPENDED')}
                                className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50/50 hover:bg-rose-100 text-rose-700 px-2.5 py-1 text-xs font-semibold transition cursor-pointer"
                              >
                                <span>Suspend</span>
                              </button>
                            ) : user.status === 'SUSPENDED' ? (
                              <button
                                type="button"
                                onClick={() => openStatusModal(user, 'ACTIVE')}
                                className="inline-flex items-center gap-1 rounded-lg border border-emerald-200 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 px-2.5 py-1 text-xs font-semibold transition cursor-pointer"
                              >
                                <span>Reactivate</span>
                              </button>
                            ) : user.status === 'REJECTED' ? (
                              <button
                                type="button"
                                onClick={() => openStatusModal(user, 'ACTIVE')}
                                className="inline-flex items-center gap-1 rounded-lg border border-blue-200 bg-blue-50 hover:bg-blue-100 text-blue-700 px-2.5 py-1 text-xs font-semibold transition cursor-pointer"
                              >
                                <span>Reinstate</span>
                              </button>
                            ) : (
                              <button
                                type="button"
                                onClick={() => setActiveTab('approvals')}
                                className="inline-flex items-center gap-1 rounded-lg border border-amber-200 bg-amber-50 hover:bg-amber-100 text-amber-800 px-2.5 py-1 text-xs font-semibold transition cursor-pointer"
                              >
                                <span>Review Queue →</span>
                              </button>
                            )}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Confirmation Modal for Status Updates (Suspend / Reactivate / Reinstate) */}
      {statusModalUser && targetStatus && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-slate-200 space-y-4 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <span
                  className={`h-3 w-3 rounded-full ${
                    targetStatus === 'SUSPENDED'
                      ? 'bg-rose-500'
                      : targetStatus === 'ACTIVE'
                      ? 'bg-emerald-500'
                      : 'bg-blue-500'
                  }`}
                ></span>
                <h3 className="text-sm font-bold text-slate-900">
                  {targetStatus === 'SUSPENDED'
                    ? 'Confirm Account Suspension'
                    : targetStatus === 'ACTIVE' && statusModalUser.status === 'SUSPENDED'
                    ? 'Confirm Account Reactivation'
                    : 'Confirm Account Reinstatement'}
                </h3>
              </div>
              <button
                type="button"
                onClick={closeStatusModal}
                disabled={isUpdatingStatus}
                className="text-slate-400 hover:text-slate-600 cursor-pointer"
              >
                ✕
              </button>
            </div>

            {modalError && (
              <div className="rounded-lg bg-rose-50 border border-rose-200 p-3 text-xs text-rose-800">
                {modalError}
              </div>
            )}

            <div className="space-y-2 text-xs text-slate-600 leading-relaxed">
              <p>
                You are about to change the status of:
              </p>
              <div className="rounded-lg bg-slate-50 border border-slate-200 p-3 space-y-1">
                <p className="font-bold text-slate-900">{statusModalUser.fullName}</p>
                <p className="text-slate-500">ID: {statusModalUser.identifier} • {statusModalUser.email}</p>
                <p className="text-slate-500">
                  Role: <span className="font-semibold text-slate-700">{statusModalUser.role}</span> •
                  Current Status: <span className="font-semibold text-slate-700">{statusModalUser.status}</span>
                </p>
              </div>

              {targetStatus === 'SUSPENDED' && (
                <p className="text-rose-700 font-medium">
                  Suspending this account will immediately revoke all active JWT authentication sessions. The user will be unable to log in until reactivated.
                </p>
              )}

              {targetStatus === 'ACTIVE' && (
                <p className="text-emerald-700 font-medium">
                  Reactivating this account will restore immediate portal access and allow the user to authenticate normally.
                </p>
              )}
            </div>

            <div className="space-y-1">
              <label htmlFor="status-reason" className="block text-xs font-semibold text-slate-700">
                Administrative Reason / Remarks (Optional)
              </label>
              <input
                id="status-reason"
                type="text"
                placeholder={
                  targetStatus === 'SUSPENDED'
                    ? 'e.g., Disciplinary probation, fee arrears, academic review'
                    : 'e.g., Clearance completed, suspension period expired'
                }
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-xs text-slate-900 focus:border-blue-500 outline-none"
              />
            </div>

            <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-slate-100">
              <button
                type="button"
                onClick={closeStatusModal}
                disabled={isUpdatingStatus}
                className="px-3.5 py-2 rounded-lg border border-slate-300 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition cursor-pointer disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmStatusChange}
                disabled={isUpdatingStatus}
                className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold text-white transition shadow-xs cursor-pointer disabled:opacity-50 ${
                  targetStatus === 'SUSPENDED'
                    ? 'bg-rose-600 hover:bg-rose-700'
                    : 'bg-emerald-600 hover:bg-emerald-700'
                }`}
              >
                {isUpdatingStatus && (
                  <div className="h-3 w-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                )}
                <span>
                  {isUpdatingStatus
                    ? 'Updating...'
                    : targetStatus === 'SUSPENDED'
                    ? 'Confirm Suspension'
                    : targetStatus === 'ACTIVE' && statusModalUser.status === 'SUSPENDED'
                    ? 'Confirm Reactivation'
                    : 'Confirm Reinstatement'}
                </span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
