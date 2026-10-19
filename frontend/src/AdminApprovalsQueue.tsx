import { useState, useEffect } from 'react'
import {
  fetchPendingUsers,
  approveUser,
  rejectUser,
  type PendingUser,
} from './services/adminService'
import type { ApiErrorData } from './services/authService'

interface AdminApprovalsQueueProps {
  onBackToOverview: () => void
}

export default function AdminApprovalsQueue({ onBackToOverview }: AdminApprovalsQueueProps) {
  const [pendingUsers, setPendingUsers] = useState<PendingUser[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null)
  const [actionType, setActionType] = useState<'approve' | 'reject' | null>(null)
  const [feedback, setFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null)
  const [rejectModalUser, setRejectModalUser] = useState<PendingUser | null>(null)

  const refreshQueue = async (isSilent = false) => {
    if (isSilent) setIsRefreshing(true)
    else setIsLoading(true)

    try {
      const data = await fetchPendingUsers()
      setPendingUsers(data)
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({
        type: 'error',
        message: apiErr.message || 'Failed to load pending user registrations.',
      })
    } finally {
      setIsLoading(false)
      setIsRefreshing(false)
    }
  }

  useEffect(() => {
    let ignore = false

    async function loadInitial() {
      try {
        const data = await fetchPendingUsers()
        if (!ignore) {
          setPendingUsers(data)
        }
      } catch (err: unknown) {
        if (!ignore) {
          const apiErr = err as Error & ApiErrorData
          setFeedback({
            type: 'error',
            message: apiErr.message || 'Failed to load pending user registrations.',
          })
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    loadInitial()
    return () => {
      ignore = true
    }
  }, [])

  const handleApprove = async (user: PendingUser) => {
    if (actionLoadingId !== null) return
    setActionLoadingId(user.id)
    setActionType('approve')
    setFeedback(null)

    try {
      const updated = await approveUser(user.id)
      setPendingUsers((prev) => prev.filter((u) => u.id !== user.id))
      setFeedback({
        type: 'success',
        message: `Account for ${updated.fullName} (${updated.identifier}) was successfully approved. The user can now sign in.`,
      })
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({
        type: 'error',
        message: apiErr.message || `Failed to approve ${user.fullName}.`,
      })
    } finally {
      setActionLoadingId(null)
      setActionType(null)
    }
  }

  const handleConfirmReject = async () => {
    if (!rejectModalUser || actionLoadingId !== null) return
    const userToReject = rejectModalUser
    setActionLoadingId(userToReject.id)
    setActionType('reject')
    setFeedback(null)

    try {
      const updated = await rejectUser(userToReject.id)
      setPendingUsers((prev) => prev.filter((u) => u.id !== userToReject.id))
      setRejectModalUser(null)
      setFeedback({
        type: 'success',
        message: `Account request for ${updated.fullName} (${updated.identifier}) has been rejected.`,
      })
    } catch (err: unknown) {
      const apiErr = err as Error & ApiErrorData
      setFeedback({
        type: 'error',
        message: apiErr.message || `Failed to reject ${userToReject.fullName}.`,
      })
    } finally {
      setActionLoadingId(null)
      setActionType(null)
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Header & Breadcrumb */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <button
              type="button"
              onClick={onBackToOverview}
              className="hover:text-blue-600 transition-colors inline-flex items-center gap-1 font-medium"
            >
              <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              Overview Dashboard
            </button>
            <span>/</span>
            <span className="text-slate-800 font-semibold">User Approvals Queue</span>
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Pending Registrations Approval Queue
          </h2>
          <p className="text-xs text-slate-600 mt-0.5">
            Review incoming student matriculation and faculty instructor onboarding requests before granting portal access.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            type="button"
            onClick={() => refreshQueue(true)}
            disabled={isLoading || isRefreshing || actionLoadingId !== null}
            className="inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors shadow-xs disabled:opacity-50"
          >
            <svg
              className={`h-4 w-4 text-slate-500 ${isRefreshing ? 'animate-spin' : ''}`}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            <span>{isRefreshing ? 'Refreshing...' : 'Refresh Queue'}</span>
          </button>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`flex items-start justify-between rounded-xl p-4 text-xs font-medium border shadow-xs transition-all ${
            feedback.type === 'success'
              ? 'bg-emerald-50 border-emerald-200 text-emerald-900'
              : 'bg-rose-50 border-rose-200 text-rose-900'
          }`}
        >
          <div className="flex items-center gap-2.5">
            {feedback.type === 'success' ? (
              <svg className="h-5 w-5 text-emerald-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            ) : (
              <svg className="h-5 w-5 text-rose-600 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            )}
            <span>{feedback.message}</span>
          </div>
          <button
            type="button"
            onClick={() => setFeedback(null)}
            className="text-slate-400 hover:text-slate-600 ml-4 font-bold"
            aria-label="Dismiss alert"
          >
            ✕
          </button>
        </div>
      )}

      {/* Main Content Area */}
      {isLoading ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-xs">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-blue-50 text-blue-600 mb-4 animate-spin">
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </div>
          <h3 className="text-base font-semibold text-slate-800">Loading Pending Registrations</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            Querying active onboarding requests from the SPAS database...
          </p>
        </div>
      ) : pendingUsers.length === 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-xs">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600 mb-4 border border-emerald-100">
            <svg className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h3 className="text-lg font-bold text-slate-900">All Caught Up!</h3>
          <p className="text-xs text-slate-600 mt-1.5 max-w-md mx-auto leading-relaxed">
            There are currently no student or lecturer onboarding applications waiting for administrator approval. New submissions will automatically appear here.
          </p>
          <div className="pt-5">
            <button
              type="button"
              onClick={() => refreshQueue(true)}
              className="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors shadow-xs"
            >
              Re-check Queue
            </button>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center justify-between text-xs text-slate-600 px-1">
            <span>
              Showing <strong className="text-slate-900 font-bold">{pendingUsers.length}</strong> pending{' '}
              {pendingUsers.length === 1 ? 'applicant' : 'applicants'}
            </span>
            <span className="text-slate-400">Status: PENDING_APPROVAL</span>
          </div>

          {/* Desktop Table View */}
          <div className="hidden md:block overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 bg-slate-50/75 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">
                  <th scope="col" className="py-3.5 px-6">Applicant</th>
                  <th scope="col" className="py-3.5 px-4">Identifier</th>
                  <th scope="col" className="py-3.5 px-4">Role</th>
                  <th scope="col" className="py-3.5 px-4">Status</th>
                  <th scope="col" className="py-3.5 px-6 text-right">Decision</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs text-slate-700">
                {pendingUsers.map((user) => {
                  const isOperating = actionLoadingId === user.id
                  return (
                    <tr key={user.id} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 font-bold text-slate-700 text-xs border border-slate-200">
                            {user.fullName
                              .split(' ')
                              .map((n) => n[0])
                              .slice(0, 2)
                              .join('')
                              .toUpperCase()}
                          </div>
                          <div>
                            <p className="font-semibold text-slate-900">{user.fullName}</p>
                            <p className="text-[11px] text-slate-500 font-mono">{user.email}</p>
                          </div>
                        </div>
                      </td>

                      <td className="py-4 px-4">
                        <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-1 font-mono text-[11px] font-medium text-slate-700 border border-slate-200">
                          {user.identifier}
                        </span>
                      </td>

                      <td className="py-4 px-4">
                        {user.role === 'STUDENT' ? (
                          <span className="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-[11px] font-semibold text-blue-700 border border-blue-200">
                            Student
                          </span>
                        ) : (
                          <span className="inline-flex items-center rounded-full bg-purple-50 px-2.5 py-0.5 text-[11px] font-semibold text-purple-700 border border-purple-200">
                            Lecturer
                          </span>
                        )}
                      </td>

                      <td className="py-4 px-4">
                        <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-2.5 py-0.5 text-[11px] font-semibold text-amber-700 border border-amber-200">
                          <span className="h-1.5 w-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                          Pending Review
                        </span>
                      </td>

                      <td className="py-4 px-6 text-right">
                        <div className="inline-flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => handleApprove(user)}
                            disabled={actionLoadingId !== null}
                            className="inline-flex items-center gap-1 rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700 transition-colors shadow-xs disabled:opacity-50"
                          >
                            {isOperating && actionType === 'approve' ? (
                              <>
                                <svg className="h-3.5 w-3.5 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                                <span>Approving...</span>
                              </>
                            ) : (
                              <>
                                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                </svg>
                                <span>Approve</span>
                              </>
                            )}
                          </button>

                          <button
                            type="button"
                            onClick={() => setRejectModalUser(user)}
                            disabled={actionLoadingId !== null}
                            className="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-white px-3 py-1.5 text-xs font-semibold text-rose-700 hover:bg-rose-50 transition-colors shadow-xs disabled:opacity-50"
                          >
                            <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            <span>Reject</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>

          {/* Mobile Card View */}
          <div className="grid grid-cols-1 gap-3 md:hidden">
            {pendingUsers.map((user) => {
              const isOperating = actionLoadingId === user.id
              return (
                <div
                  key={user.id}
                  className="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs space-y-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="font-semibold text-slate-900 text-sm">{user.fullName}</h4>
                      <p className="text-xs text-slate-500 font-mono mt-0.5">{user.email}</p>
                    </div>
                    {user.role === 'STUDENT' ? (
                      <span className="inline-flex items-center rounded-full bg-blue-50 px-2 py-0.5 text-[10px] font-semibold text-blue-700 border border-blue-200">
                        Student
                      </span>
                    ) : (
                      <span className="inline-flex items-center rounded-full bg-purple-50 px-2 py-0.5 text-[10px] font-semibold text-purple-700 border border-purple-200">
                        Lecturer
                      </span>
                    )}
                  </div>

                  <div className="flex items-center justify-between border-t border-slate-100 pt-2.5 text-xs">
                    <span className="text-slate-500">Identifier:</span>
                    <span className="font-mono font-medium text-slate-800">{user.identifier}</span>
                  </div>

                  <div className="flex items-center justify-between border-t border-slate-100 pt-2.5 text-xs">
                    <span className="text-slate-500">Status:</span>
                    <span className="font-semibold text-amber-700">Pending Review</span>
                  </div>

                  <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100">
                    <button
                      type="button"
                      onClick={() => handleApprove(user)}
                      disabled={actionLoadingId !== null}
                      className="w-full inline-flex items-center justify-center gap-1.5 rounded-lg bg-emerald-600 py-2 text-xs font-semibold text-white hover:bg-emerald-700 transition-colors shadow-xs disabled:opacity-50"
                    >
                      {isOperating && actionType === 'approve' ? 'Approving...' : 'Approve'}
                    </button>
                    <button
                      type="button"
                      onClick={() => setRejectModalUser(user)}
                      disabled={actionLoadingId !== null}
                      className="w-full inline-flex items-center justify-center gap-1.5 rounded-lg border border-rose-200 bg-white py-2 text-xs font-semibold text-rose-700 hover:bg-rose-50 transition-colors shadow-xs disabled:opacity-50"
                    >
                      Reject
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}

      {/* Confirmation Dialog for Rejection */}
      {rejectModalUser && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4"
          aria-modal="true"
          role="dialog"
        >
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-rose-100 text-rose-600">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">Reject Registration Request?</h3>
                <p className="text-xs text-slate-500">This action will deny portal access.</p>
              </div>
            </div>

            <p className="text-xs text-slate-600 leading-relaxed">
              Are you sure you want to reject the application for{' '}
              <strong className="text-slate-900 font-semibold">{rejectModalUser.fullName}</strong> (
              <span className="font-mono text-slate-700">{rejectModalUser.identifier}</span> • {rejectModalUser.email}
              )? Their account status will transition to <span className="font-semibold text-rose-700">REJECTED</span> and they will not be able to log in.
            </p>

            <div className="flex items-center justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={() => setRejectModalUser(null)}
                disabled={actionLoadingId !== null}
                className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors shadow-xs disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmReject}
                disabled={actionLoadingId !== null}
                className="inline-flex items-center gap-1.5 rounded-lg bg-rose-600 px-4 py-2 text-xs font-semibold text-white hover:bg-rose-700 transition-colors shadow-xs disabled:opacity-50"
              >
                {actionLoadingId === rejectModalUser.id && actionType === 'reject' ? (
                  <>
                    <svg className="h-3.5 w-3.5 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    <span>Rejecting...</span>
                  </>
                ) : (
                  <span>Confirm Rejection</span>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
