import { useUserToggles, useUpdateToggle } from '../../../hooks/useProfileData'
import Loader from '../../../components/common/Loader'

type Props = {
  userId: number
}

/**
 * Toggle panel for Super Admin to control CREATE/EDIT/DELETE permissions per target role.
 * Each toggle auto-saves on change — no save button needed.
 */
const UserTogglePanel = ({ userId }: Props) => {
  const { data: toggles, isLoading, isError } = useUserToggles(userId)
  const updateToggle = useUpdateToggle()

  const handleToggle = (targetRole: string, field: 'canCreate' | 'canEdit' | 'canDelete', currentValue: boolean) => {
    updateToggle.mutate({
      userId,
      targetRole,
      [field]: !currentValue,
    })
  }

  if (isLoading) return <Loader message="Loading toggle settings..." />
  if (isError) return <div className="py-4 text-center text-sm text-red-600">Failed to load toggles.</div>
  if (!toggles || toggles.length === 0) {
    return <div className="py-4 text-center text-sm text-slate-500">No manageable roles for this user.</div>
  }

  return (
    <div className="space-y-3">
      <p className="text-xs text-slate-500">
        Toggle CREATE / EDIT / DELETE permissions for each role this user can manage. Changes take effect immediately.
      </p>

      <div className="overflow-hidden rounded-lg border border-slate-200">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 bg-slate-50">
              <th className="px-4 py-2.5 text-left font-semibold text-slate-600">Target Role</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Create</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Edit</th>
              <th className="px-4 py-2.5 text-center font-semibold text-slate-600">Delete</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {toggles.map((toggle) => (
              <tr key={toggle.targetRole} className="transition hover:bg-slate-50/50">
                <td className="px-4 py-3 font-medium text-slate-800">
                  {toggle.targetRole.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
                </td>
                {(['canCreate', 'canEdit', 'canDelete'] as const).map((field) => (
                  <td key={field} className="px-4 py-3 text-center">
                    <button
                      type="button"
                      onClick={() => handleToggle(toggle.targetRole, field, toggle[field])}
                      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 ${
                        toggle[field] ? 'bg-indigo-600' : 'bg-slate-200'
                      }`}
                      aria-label={`${field.replace('can', '')} toggle for ${toggle.targetRole}`}
                    >
                      <span
                        className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                          toggle[field] ? 'translate-x-5' : 'translate-x-0'
                        }`}
                      />
                    </button>
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {updateToggle.isError ? (
        <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700">Failed to update toggle. Check that you have Super Admin access.</div>
      ) : null}
    </div>
  )
}

export default UserTogglePanel
