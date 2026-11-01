import { useState, useEffect, useCallback } from 'react';
import {
  fetchAuditLogs,
  type AdminAuditLog,
  type AuditLogFilterParams,
  type PageResponse,
} from './services/adminService';

export default function AdminAuditTrailView() {
  const [logs, setLogs] = useState<AdminAuditLog[]>([]);
  const [pageInfo, setPageInfo] = useState<PageResponse<AdminAuditLog> | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize] = useState(15);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [actionFilter, setActionFilter] = useState('');
  const [entityTypeFilter, setEntityTypeFilter] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  // Debounce search input
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearch(searchTerm);
      setPage(0);
    }, 350);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  const loadAuditLogs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const filters: AuditLogFilterParams = {
        action: actionFilter || undefined,
        entityType: entityTypeFilter || undefined,
        search: debouncedSearch.trim() || undefined,
        page,
        size: pageSize,
      };

      const result = await fetchAuditLogs(filters);
      setPageInfo(result);
      setLogs(result.content || []);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load system audit trail.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [actionFilter, entityTypeFilter, debouncedSearch, page, pageSize]);

  useEffect(() => {
    queueMicrotask(() => {
      loadAuditLogs();
    });
  }, [loadAuditLogs]);

  const handleActionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setActionFilter(e.target.value);
    setPage(0);
  };

  const handleEntityTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setEntityTypeFilter(e.target.value);
    setPage(0);
  };

  const getActionBadgeClass = (action: string) => {
    switch (action) {
      case 'USER_APPROVED':
      case 'ALERT_RESOLVED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'USER_REJECTED':
      case 'USER_SUSPENDED':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'USER_STATUS_UPDATED':
      case 'ADMIN_OVERRIDE':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'ALERT_REVIEWED':
      case 'ALERT_ACTION_TAKEN':
      case 'ADMIN_UNLOCK':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      default:
        return 'bg-slate-50 text-slate-700 border-slate-200';
    }
  };

  const formatTimestamp = (isoString: string) => {
    if (!isoString) return '—';
    try {
      const date = new Date(isoString);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-slate-900">
              System Audit Trail
            </h1>
            <p className="mt-1 text-sm text-slate-500">
              Comprehensive immutable record of administrative actions, user account approvals, status changes, and override activities.
            </p>
          </div>
          <button
            onClick={loadAuditLogs}
            disabled={loading}
            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus:outline-hidden disabled:opacity-50 shadow-xs"
          >
            <svg
              className={`mr-2 h-4 w-4 ${loading ? 'animate-spin' : ''}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
            Refresh
          </button>
        </div>

        {/* Filter Bar */}
        <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <div>
            <label htmlFor="audit-search" className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Search Details or Actor
            </label>
            <input
              id="audit-search"
              type="text"
              placeholder="Search keyword, email, or identifier..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-800 placeholder-slate-400 focus:border-blue-500 focus:outline-hidden"
            />
          </div>

          <div>
            <label htmlFor="action-filter" className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Action Type
            </label>
            <select
              id="action-filter"
              value={actionFilter}
              onChange={handleActionChange}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-hidden"
            >
              <option value="">All Actions</option>
              <option value="USER_APPROVED">User Approved</option>
              <option value="USER_REJECTED">User Rejected</option>
              <option value="USER_STATUS_UPDATED">User Status Updated</option>
              <option value="ALERT_REVIEWED">Risk Alert Reviewed</option>
              <option value="ALERT_ACTION_TAKEN">Alert Action Recorded</option>
              <option value="ALERT_RESOLVED">Alert Resolved</option>
              <option value="ADMIN_UNLOCK">Results Unlocked</option>
              <option value="ADMIN_OVERRIDE">Mark Overridden</option>
            </select>
          </div>

          <div>
            <label htmlFor="entity-filter" className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">
              Target Entity
            </label>
            <select
              id="entity-filter"
              value={entityTypeFilter}
              onChange={handleEntityTypeChange}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-800 focus:border-blue-500 focus:outline-hidden"
            >
              <option value="">All Entity Types</option>
              <option value="USER">User Account</option>
              <option value="RISK_ALERT">Risk Alert</option>
              <option value="SUBJECT_RESULT_PUBLICATION">Subject Results</option>
              <option value="STUDENT_MARK">Student Mark</option>
            </select>
          </div>
        </div>
      </div>

      {/* Error alert */}
      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 flex items-center justify-between">
          <span>{error}</span>
          <button
            onClick={loadAuditLogs}
            className="text-xs font-semibold uppercase underline hover:text-red-900"
          >
            Retry
          </button>
        </div>
      )}

      {/* Audit Log Table */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-600">
            <thead className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-6 py-3.5">Timestamp</th>
                <th className="px-6 py-3.5">Action</th>
                <th className="px-6 py-3.5">Entity</th>
                <th className="px-6 py-3.5">Details</th>
                <th className="px-6 py-3.5">Performed By</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading && logs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-slate-400">
                    <div className="inline-flex items-center gap-2">
                      <div className="h-4 w-4 animate-spin rounded-full border-2 border-blue-600 border-t-transparent" />
                      Loading audit logs...
                    </div>
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-slate-400">
                    No audit records found matching the selected criteria.
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-xs text-slate-500 font-mono">
                      {formatTimestamp(log.performedAt)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex items-center rounded-lg border px-2.5 py-1 text-xs font-semibold ${getActionBadgeClass(
                          log.action
                        )}`}
                      >
                        {log.action}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="font-medium text-slate-800">{log.entityType}</div>
                      {log.entityIdentifier && (
                        <div className="text-xs text-slate-400 font-mono">
                          {log.entityIdentifier}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 max-w-md">
                      <p className="text-slate-700 text-xs sm:text-sm line-clamp-2">
                        {log.details}
                      </p>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="font-medium text-slate-800 text-xs">
                        {log.performedByName || 'Administrator'}
                      </div>
                      <div className="text-xs text-slate-400 font-mono">
                        {log.performedByEmail}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Server-Side Pagination Bar */}
        {pageInfo && pageInfo.totalPages > 0 && (
          <div className="border-t border-slate-200 bg-slate-50 px-6 py-3 flex items-center justify-between text-xs text-slate-600">
            <div>
              Showing{' '}
              <span className="font-semibold text-slate-800">
                {pageInfo.totalElements === 0 ? 0 : pageInfo.pageNumber * pageInfo.pageSize + 1}
              </span>{' '}
              to{' '}
              <span className="font-semibold text-slate-800">
                {Math.min((pageInfo.pageNumber + 1) * pageInfo.pageSize, pageInfo.totalElements)}
              </span>{' '}
              of <span className="font-semibold text-slate-800">{pageInfo.totalElements}</span> entries
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={pageInfo.first || loading}
                className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 font-medium hover:bg-slate-100 disabled:opacity-40"
              >
                Previous
              </button>
              <span className="px-2 font-medium">
                Page {pageInfo.pageNumber + 1} of {pageInfo.totalPages}
              </span>
              <button
                onClick={() => setPage((prev) => prev + 1)}
                disabled={pageInfo.last || loading}
                className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 font-medium hover:bg-slate-100 disabled:opacity-40"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
