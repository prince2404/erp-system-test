import { useAuth } from '../../../hooks/useAuth'

type Props = { profile: Record<string, unknown> | undefined }

/**
 * Section 5 — Permission Summary.
 * Read-only view of the user's permissions (role, scope, toggle state per target role).
 * Super Admin notes: the editable toggle panel lives in the user management page.
 */
const PermissionSummarySection = ({ profile }: Props) => {
  const { user } = useAuth()
  const role = user?.role ?? ''

  // Extract permission data from profile
  const toggles = Array.isArray(profile?.toggles) ? (profile.toggles as Array<Record<string, unknown>>) : []
  const scope = profile?.scope ?? profile?.assignedState ?? profile?.assignedDistrict ?? profile?.assignedBlock ?? profile?.assignedCenter ?? 'N/A'

  return (
    <div>
      <h2 className="text-lg font-bold text-slate-900">Permission Summary</h2>
      <p className="mb-6 text-sm text-slate-500">Overview of your current access level and permissions</p>

      <div className="mb-6 grid gap-4 sm:grid-cols-2">
        <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Your Role</p>
          <p className="mt-1 text-lg font-bold text-indigo-700">{role.replace(/_/g, ' ')}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Geographic Scope</p>
          <p className="mt-1 text-lg font-bold text-slate-800">{String(scope)}</p>
        </div>
      </div>

      {toggles.length > 0 ? (
        <div>
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Your Permissions per Managed Role</h3>
          <div className="overflow-hidden rounded-lg border border-slate-200">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50">
                  <th className="px-4 py-2.5 text-left font-semibold text-slate-600">Role</th>
                  <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Create</th>
                  <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Edit</th>
                  <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Delete</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {toggles.map((t) => (
                  <tr key={String(t.targetRole)}>
                    <td className="px-4 py-3 font-medium text-slate-800">
                      {String(t.targetRole).replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
                    </td>
                    {(['canCreate', 'canEdit', 'canDelete'] as const).map((field) => (
                      <td key={field} className="px-4 py-3 text-center">
                        <span className={`inline-flex h-6 w-6 items-center justify-center rounded-full text-xs font-bold ${
                          t[field] ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-400'
                        }`}>
                          {t[field] ? '✓' : '✕'}
                        </span>
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="rounded-lg border border-slate-200 bg-slate-50 p-6 text-center text-sm text-slate-500">
          {role === 'SUPER_ADMIN'
            ? 'Super Admin has full access — no toggle restrictions apply.'
            : 'No specific toggle permissions configured for your role.'}
        </div>
      )}

      <div className="mt-6 rounded-lg bg-blue-50 p-4 text-xs text-blue-700">
        💡 These permissions are controlled by your Super Admin. If a button or feature is not visible, it means you do not have that specific permission.
      </div>
    </div>
  )
}

export default PermissionSummarySection
