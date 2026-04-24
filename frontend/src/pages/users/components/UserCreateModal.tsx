import { useEffect, useMemo } from 'react'
import { useForm } from 'react-hook-form'
import Modal from '../../../components/common/Modal'
import { useAssignableRoles } from '../../../hooks/useProfileData'
import { useCreateUser } from '../../../hooks/useAdminData'
import type { BlockItem, CenterItem, DistrictItem, StateItem } from '../../../hooks/useAdminData'

const centerLevelRoles = ['hr_manager', 'doctor', 'pharmacist', 'receptionist', 'staff', 'center_staff']

const getAssignmentScope = (role: string) => {
  const r = role.toLowerCase()
  if (r === 'state_manager') return 'state'
  if (r === 'district_manager') return 'district'
  if (r === 'block_manager') return 'block'
  if (centerLevelRoles.includes(r)) return 'center'
  return null
}

type FormValues = {
  username: string
  password: string
  email: string
  phone: string
  role: string
  assignedEntityId: string
}

type UserCreateModalProps = {
  isOpen: boolean
  onClose: () => void
  states: StateItem[]
  districts: DistrictItem[]
  blocks: BlockItem[]
  centers: CenterItem[]
}

/**
 * Modal form for creating a new user with backend-driven role dropdown.
 */
const UserCreateModal = ({ isOpen, onClose, states, districts, blocks, centers }: UserCreateModalProps) => {
  const { data: assignableRolesData } = useAssignableRoles()
  const createUser = useCreateUser()
  const assignableRoles = assignableRolesData?.roles ?? []

  const {
    register,
    handleSubmit,
    watch,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: { username: '', password: '', email: '', phone: '', role: '', assignedEntityId: '' },
  })

  const watchedRole = watch('role')
  const assignmentScope = getAssignmentScope(watchedRole)

  // Reset role to first assignable when list loads
  useEffect(() => {
    if (assignableRoles.length > 0 && !watchedRole) {
      setValue('role', assignableRoles[0])
    }
  }, [assignableRoles, watchedRole, setValue])

  const assignmentOptions = useMemo(() => {
    if (assignmentScope === 'state') return states.map((s) => ({ id: s.id, label: s.name }))
    if (assignmentScope === 'district') return districts.map((d) => ({ id: d.id, label: d.name }))
    if (assignmentScope === 'block') return blocks.map((b) => ({ id: b.id, label: b.name }))
    if (assignmentScope === 'center') return centers.map((c) => ({ id: c.id, label: c.name }))
    return []
  }, [assignmentScope, blocks, centers, districts, states])

  const onSubmit = async (values: FormValues) => {
    const assignedEntityId = values.assignedEntityId ? Number(values.assignedEntityId) : undefined
    const assignedPayload =
      assignmentScope === 'state'
        ? { assignedStateId: assignedEntityId }
        : assignmentScope === 'district'
          ? { assignedDistrictId: assignedEntityId }
          : assignmentScope === 'block'
            ? { assignedBlockId: assignedEntityId }
            : assignmentScope === 'center'
              ? { assignedCenterId: assignedEntityId }
              : {}

    await createUser.mutateAsync({
      username: values.username,
      password: values.password,
      email: values.email,
      phone: values.phone || undefined,
      role: values.role.toUpperCase(),
      ...assignedPayload,
    })

    reset()
    onClose()
  }

  return (
    <Modal title="Create New User" isOpen={isOpen} onClose={onClose}>
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Username *</label>
          <input
            {...register('username', { required: 'Username is required', minLength: { value: 3, message: 'Min 3 chars' } })}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          />
          {errors.username ? <p className="mt-1 text-xs text-red-600">{errors.username.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Password *</label>
          <input
            type="password"
            {...register('password', { required: 'Password is required', minLength: { value: 6, message: 'Min 6 chars' } })}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          />
          {errors.password ? <p className="mt-1 text-xs text-red-600">{errors.password.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Email *</label>
          <input
            type="email"
            {...register('email', { required: 'Email is required' })}
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

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Role *</label>
          <select
            {...register('role', { required: 'Role is required' })}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          >
            <option value="">Select role</option>
            {assignableRoles.map((role) => (
              <option key={role} value={role}>
                {role.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase())}
              </option>
            ))}
          </select>
          {errors.role ? <p className="mt-1 text-xs text-red-600">{errors.role.message}</p> : null}
        </div>

        {assignmentScope ? (
          <div>
            <label className="mb-1 block text-sm font-medium capitalize text-slate-700">
              Assigned {assignmentScope} *
            </label>
            <select
              {...register('assignedEntityId', { required: `Please select a ${assignmentScope}` })}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
            >
              <option value="">Select {assignmentScope}</option>
              {assignmentOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.assignedEntityId ? <p className="mt-1 text-xs text-red-600">{errors.assignedEntityId.message}</p> : null}
          </div>
        ) : null}

        {createUser.isError ? (
          <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700">
            Unable to create user. Please verify input and role permissions.
          </div>
        ) : null}

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
            Cancel
          </button>
          <button
            type="submit"
            disabled={createUser.isPending}
            className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-indigo-500 disabled:opacity-60"
          >
            {createUser.isPending ? 'Creating...' : 'Create User'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default UserCreateModal
