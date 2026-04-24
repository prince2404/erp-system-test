import { useState } from 'react'
import Loader from '../../components/common/Loader'
import ErrorState from '../../components/common/ErrorState'
import EmptyState from '../../components/common/EmptyState'
import Modal from '../../components/common/Modal'
import { usePermission } from '../../hooks/usePermission'
import { useAuth } from '../../hooks/useAuth'
import {
  useBlocks,
  useCenters,
  useDistricts,
  useStates,
  useUsers,
  type UserSummary,
} from '../../hooks/useAdminData'
import { useDeactivateUser, useReactivateUser } from '../../hooks/useProfileData'
import UserCreateModal from './components/UserCreateModal'
import UserEditModal from './components/UserEditModal'
import UserTogglePanel from './components/UserTogglePanel'

/**
 * User management page with list, create, edit, deactivate/reactivate, and toggle panel.
 */
const UsersPage = () => {
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [editUser, setEditUser] = useState<UserSummary | null>(null)
  const [toggleUser, setToggleUser] = useState<UserSummary | null>(null)
  const { hasPermission } = usePermission()
  const { user } = useAuth()
  const usersQuery = useUsers()
  const { data: states = [] } = useStates()
  const { data: districts = [] } = useDistricts()
  const { data: blocks = [] } = useBlocks()
  const { data: centers = [] } = useCenters()
  const deactivate = useDeactivateUser()
  const reactivate = useReactivateUser()

  const isSuperAdmin = user?.role === 'SUPER_ADMIN'
  const isAdminOrAbove = user?.role === 'SUPER_ADMIN' || user?.role === 'ADMIN'

  if (!hasPermission('user:view')) {
    return <ErrorState message="You do not have permission to view user management." />
  }

  const handleDeactivate = async (u: UserSummary) => {
    if (!confirm(`Deactivate ${u.username}? They will be immediately logged out.`)) return
    await deactivate.mutateAsync(u.id)
  }

  const handleReactivate = async (u: UserSummary) => {
    if (!confirm(`Reactivate ${u.username}? They will be able to log in again.`)) return
    await reactivate.mutateAsync(u.id)
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900">User Management</h2>
          <p className="mt-1 text-sm text-slate-500">Manage all platform users with role-based access control</p>
        </div>
        {hasPermission('user:create') ? (
          <button
            type="button"
            onClick={() => setIsCreateOpen(true)}
            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
          >
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" /></svg>
            Add User
          </button>
        ) : null}
      </div>

      {/* Content */}
      {usersQuery.isLoading ? <Loader message="Loading users..." /> : null}
      {usersQuery.isError ? (
        <ErrorState message="Unable to load users." onRetry={() => { void usersQuery.refetch() }} />
      ) : null}
      {!usersQuery.isLoading && !usersQuery.isError && (usersQuery.data?.length ?? 0) === 0 ? (
        <EmptyState message="No users found." />
      ) : null}

      {!usersQuery.isLoading && !usersQuery.isError && (usersQuery.data?.length ?? 0) > 0 ? (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-100 bg-slate-50">
                <th className="px-4 py-3 font-semibold text-slate-600">Username</th>
                <th className="px-4 py-3 font-semibold text-slate-600">Email</th>
                <th className="px-4 py-3 font-semibold text-slate-600">Phone</th>
                <th className="px-4 py-3 font-semibold text-slate-600">Role</th>
                <th className="px-4 py-3 font-semibold text-slate-600">Status</th>
                <th className="px-4 py-3 font-semibold text-slate-600 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {(usersQuery.data ?? []).map((u) => (
                <tr key={u.id} className="transition hover:bg-slate-50/70">
                  <td className="px-4 py-3 font-medium text-slate-900">{u.username}</td>
                  <td className="px-4 py-3 text-slate-600">{u.email}</td>
                  <td className="px-4 py-3 text-slate-600">{u.phone ?? '-'}</td>
                  <td className="px-4 py-3">
                    <span className="inline-flex rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-semibold text-indigo-700">
                      {u.role?.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      u.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700'
                        : 'bg-red-50 text-red-700'
                    }`}>
                      {u.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex items-center justify-end gap-1">
                      {isAdminOrAbove ? (
                        <button
                          type="button"
                          onClick={() => setEditUser(u)}
                          className="rounded-md px-2.5 py-1.5 text-xs font-medium text-indigo-600 transition hover:bg-indigo-50"
                        >
                          Edit
                        </button>
                      ) : null}
                      {isSuperAdmin ? (
                        <button
                          type="button"
                          onClick={() => setToggleUser(u)}
                          className="rounded-md px-2.5 py-1.5 text-xs font-medium text-violet-600 transition hover:bg-violet-50"
                        >
                          Toggles
                        </button>
                      ) : null}
                      {u.status === 'ACTIVE' ? (
                        <button
                          type="button"
                          onClick={() => { void handleDeactivate(u) }}
                          className="rounded-md px-2.5 py-1.5 text-xs font-medium text-red-600 transition hover:bg-red-50"
                        >
                          Deactivate
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => { void handleReactivate(u) }}
                          className="rounded-md px-2.5 py-1.5 text-xs font-medium text-emerald-600 transition hover:bg-emerald-50"
                        >
                          Reactivate
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      {/* Create User Modal */}
      <UserCreateModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        states={states}
        districts={districts}
        blocks={blocks}
        centers={centers}
      />

      {/* Edit User Modal */}
      {editUser ? (
        <UserEditModal
          user={editUser}
          isAdmin={isAdminOrAbove}
          onClose={() => setEditUser(null)}
          states={states}
          districts={districts}
          blocks={blocks}
          centers={centers}
        />
      ) : null}

      {/* Toggle Panel Modal */}
      {toggleUser ? (
        <Modal title={`Permission Toggles — ${toggleUser.username}`} isOpen onClose={() => setToggleUser(null)}>
          <UserTogglePanel userId={toggleUser.id} />
        </Modal>
      ) : null}
    </div>
  )
}

export default UsersPage
