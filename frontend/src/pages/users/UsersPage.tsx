import { useState } from 'react'
import DataTable, { type Column } from '../../components/common/DataTable'
import EmptyState from '../../components/common/EmptyState'
import ErrorState from '../../components/common/ErrorState'
import Loader from '../../components/common/Loader'
import {
  useBlocks,
  useCenters,
  useCreateUser,
  useDistricts,
  useStates,
  useUsers,
  type UserSummary,
} from '../../hooks/useAdminData'
import { usePermission } from '../../hooks/usePermission'
import UserCreateModal from './components/UserCreateModal'

const columns: Column<UserSummary>[] = [
  { key: 'username', header: 'Username', accessor: (user) => user.username },
  { key: 'email', header: 'Email', accessor: (user) => user.email },
  { key: 'phone', header: 'Phone', accessor: (user) => user.phone ?? '-' },
  { key: 'role', header: 'Role', accessor: (user) => user.role },
]

/**
 * User management page with list and create-user workflow.
 * Access: requires `user:view` and `user:create` permissions.
 */
const UsersPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { hasPermission } = usePermission()
  const usersQuery = useUsers()
  const { data: states = [] } = useStates()
  const { data: districts = [] } = useDistricts()
  const { data: blocks = [] } = useBlocks()
  const { data: centers = [] } = useCenters()
  const createUser = useCreateUser()

  if (!hasPermission('user:view')) {
    return <ErrorState message="You do not have permission to view user management." />
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">User Management</h2>
        {hasPermission('user:create') ? (
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            Add User
          </button>
        ) : null}
      </div>

      {usersQuery.isLoading ? <Loader message="Loading users..." /> : null}
      {usersQuery.isError ? (
        <ErrorState
          message="Unable to load users."
          onRetry={() => {
            void usersQuery.refetch()
          }}
        />
      ) : null}
      {!usersQuery.isLoading && !usersQuery.isError && (usersQuery.data?.length ?? 0) === 0 ? (
        <EmptyState message="No users found." />
      ) : null}
      {!usersQuery.isLoading && !usersQuery.isError && (usersQuery.data?.length ?? 0) > 0 ? (
        <DataTable columns={columns} rows={usersQuery.data ?? []} getRowKey={(user) => user.id} />
      ) : null}

      <UserCreateModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onCreate={async (payload) => {
          await createUser.mutateAsync(payload)
        }}
        states={states}
        districts={districts}
        blocks={blocks}
        centers={centers}
        isSubmitting={createUser.isPending}
        hasError={createUser.isError}
      />
    </div>
  )
}

export default UsersPage
