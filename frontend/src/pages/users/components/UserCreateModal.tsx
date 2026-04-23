import { useMemo } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import Modal from '../../../components/common/Modal'
import { ROLE_ID_BY_NAME, ROLE_NAMES, type RoleName } from '../../../constants/roles'
import type { BlockItem, CenterItem, DistrictItem, StateItem } from '../../../hooks/useAdminData'

const centerLevelRoles: RoleName[] = ['HR_MANAGER', 'DOCTOR', 'PHARMACIST', 'RECEPTIONIST', 'STAFF']

/**
 * Calculates assignment scope based on selected role.
 */
const getAssignmentScope = (role: RoleName | '') => {
  if (role === 'STATE_MANAGER') return 'state'
  if (role === 'DISTRICT_MANAGER') return 'district'
  if (role === 'BLOCK_MANAGER') return 'block'
  if (centerLevelRoles.includes(role as RoleName)) return 'center'
  return null
}

const schema = z
  .object({
    username: z.string().trim().min(3, 'Username is required'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    email: z.string().email('Valid email is required'),
    phone: z.string().trim().optional(),
    role: z.enum(ROLE_NAMES),
    assignedEntityId: z.string().optional(),
  })
  .superRefine((values, context) => {
    const scope = getAssignmentScope(values.role)
    if (scope && !values.assignedEntityId) {
      context.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['assignedEntityId'],
        message: `Please select ${scope} assignment`,
      })
    }
  })

type FormValues = z.infer<typeof schema>

type UserCreateModalProps = {
  isOpen: boolean
  onClose: () => void
  onCreate: (payload: {
    username: string
    password: string
    email: string
    phone?: string
    roleId: number
    assignedStateId?: number
    assignedDistrictId?: number
    assignedBlockId?: number
    assignedCenterId?: number
  }) => Promise<void>
  states: StateItem[]
  districts: DistrictItem[]
  blocks: BlockItem[]
  centers: CenterItem[]
  isSubmitting: boolean
  hasError: boolean
}

/**
 * Modal form for creating a new user with scope-based assignment.
 */
const UserCreateModal = ({
  isOpen,
  onClose,
  onCreate,
  states,
  districts,
  blocks,
  centers,
  isSubmitting,
  hasError,
}: UserCreateModalProps) => {
  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'STATE_MANAGER' },
  })

  const watchedRole = watch('role')
  const assignmentScope = getAssignmentScope(watchedRole)

  const assignmentOptions = useMemo(() => {
    if (assignmentScope === 'state') return states.map((state) => ({ id: state.id, label: state.name }))
    if (assignmentScope === 'district') return districts.map((district) => ({ id: district.id, label: district.name }))
    if (assignmentScope === 'block') return blocks.map((block) => ({ id: block.id, label: block.name }))
    if (assignmentScope === 'center') return centers.map((center) => ({ id: center.id, label: center.name }))
    return []
  }, [assignmentScope, blocks, centers, districts, states])

  const onSubmit = async (values: FormValues) => {
    const roleId = ROLE_ID_BY_NAME[values.role]
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

    await onCreate({
      username: values.username,
      password: values.password,
      email: values.email,
      phone: values.phone,
      roleId,
      ...assignedPayload,
    })

    reset({ role: 'STATE_MANAGER' })
    onClose()
  }

  return (
    <Modal title="Create User" isOpen={isOpen} onClose={onClose}>
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Username</label>
          <input {...register('username')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.username ? <p className="mt-1 text-xs text-red-600">{errors.username.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
          <input type="password" {...register('password')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.password ? <p className="mt-1 text-xs text-red-600">{errors.password.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
          <input type="email" {...register('email')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.email ? <p className="mt-1 text-xs text-red-600">{errors.email.message}</p> : null}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Phone</label>
          <input {...register('phone')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
          <select {...register('role')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
            {ROLE_NAMES.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
        </div>

        {assignmentScope ? (
          <div>
            <label className="mb-1 block text-sm font-medium capitalize text-slate-700">Assigned {assignmentScope}</label>
            <select {...register('assignedEntityId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
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

        {hasError ? <p className="text-xs text-red-600">Unable to create user. Please verify input and role permissions.</p> : null}

        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
        >
          {isSubmitting ? 'Saving...' : 'Save User'}
        </button>
      </form>
    </Modal>
  )
}

export default UserCreateModal
