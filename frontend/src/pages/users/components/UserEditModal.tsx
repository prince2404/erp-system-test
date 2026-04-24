import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import Modal from '../../../components/common/Modal'
import { useEditableForm, useSafeEditUser, useDangerousEditUser, useAssignableRoles } from '../../../hooks/useProfileData'
import type { UserSummary, StateItem, DistrictItem, BlockItem, CenterItem } from '../../../hooks/useAdminData'

type Props = {
  user: UserSummary
  isAdmin: boolean
  onClose: () => void
  states: StateItem[]
  districts: DistrictItem[]
  blocks: BlockItem[]
  centers: CenterItem[]
}

type FormValues = {
  username: string
  email: string
  phone: string
  role: string
  assignedStateId: string
  assignedDistrictId: string
  assignedBlockId: string
  assignedCenterId: string
}

/**
 * Modal for editing user details. Safe fields for all editors,
 * dangerous fields (role, scope) only for Admin/Super Admin.
 */
const UserEditModal = ({ user, isAdmin, onClose, states, districts, blocks, centers }: Props) => {
  const { data: editable, isLoading } = useEditableForm(user.id)
  const { data: assignableRolesData } = useAssignableRoles()
  const safeEdit = useSafeEditUser()
  const dangerousEdit = useDangerousEditUser()

  const assignableRoles = assignableRolesData?.roles ?? []
  const hasDangerousFields = isAdmin && editable && 'role' in editable

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormValues>()

  useEffect(() => {
    if (editable) {
      reset({
        username: String(editable.username ?? ''),
        email: String(editable.email ?? ''),
        phone: String(editable.phone ?? ''),
        role: String(editable.role ?? ''),
        assignedStateId: editable.assignedStateId != null ? String(editable.assignedStateId) : '',
        assignedDistrictId: editable.assignedDistrictId != null ? String(editable.assignedDistrictId) : '',
        assignedBlockId: editable.assignedBlockId != null ? String(editable.assignedBlockId) : '',
        assignedCenterId: editable.assignedCenterId != null ? String(editable.assignedCenterId) : '',
      })
    }
  }, [editable, reset])

  const onSubmit = async (values: FormValues) => {
    // Safe fields first
    await safeEdit.mutateAsync({
      id: user.id,
      payload: { username: values.username, email: values.email, phone: values.phone },
    })

    // Dangerous fields if available
    if (hasDangerousFields) {
      const dangerousPayload: Record<string, unknown> = {}
      if (values.role) dangerousPayload.role = values.role.toUpperCase()
      if (values.assignedStateId) dangerousPayload.assignedStateId = Number(values.assignedStateId)
      if (values.assignedDistrictId) dangerousPayload.assignedDistrictId = Number(values.assignedDistrictId)
      if (values.assignedBlockId) dangerousPayload.assignedBlockId = Number(values.assignedBlockId)
      if (values.assignedCenterId) dangerousPayload.assignedCenterId = Number(values.assignedCenterId)
      if (Object.keys(dangerousPayload).length > 0) {
        await dangerousEdit.mutateAsync({ id: user.id, payload: dangerousPayload })
      }
    }

    onClose()
  }

  const isSubmitting = safeEdit.isPending || dangerousEdit.isPending

  return (
    <Modal title={`Edit User — ${user.username}`} isOpen onClose={onClose}>
      {isLoading ? (
        <div className="py-8 text-center text-sm text-slate-500">Loading editable fields...</div>
      ) : (
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Username</label>
            <input
              {...register('username', { required: 'Required' })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            />
            {errors.username ? <p className="mt-1 text-xs text-red-600">{errors.username.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
            <input
              type="email"
              {...register('email', { required: 'Required' })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            />
            {errors.email ? <p className="mt-1 text-xs text-red-600">{errors.email.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Phone</label>
            <input
              {...register('phone')}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          {/* Dangerous fields — only for Admin/SuperAdmin */}
          {hasDangerousFields ? (
            <>
              <hr className="border-slate-200" />
              <p className="text-xs font-semibold uppercase tracking-wide text-amber-600">⚠ Privileged Fields</p>

              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
                <select
                  {...register('role')}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="">Keep current</option>
                  {assignableRoles.map((r) => (
                    <option key={r} value={r}>
                      {r.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">State</label>
                  <select {...register('assignedStateId')} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                    <option value="">None</option>
                    {states.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">District</label>
                  <select {...register('assignedDistrictId')} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                    <option value="">None</option>
                    {districts.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Block</label>
                  <select {...register('assignedBlockId')} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                    <option value="">None</option>
                    {blocks.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Center</label>
                  <select {...register('assignedCenterId')} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                    <option value="">None</option>
                    {centers.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              </div>
            </>
          ) : null}

          {(safeEdit.isError || dangerousEdit.isError) ? (
            <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700">Failed to update user. Check permissions.</div>
          ) : null}

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-500 disabled:opacity-60"
            >
              {isSubmitting ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  )
}

export default UserEditModal
