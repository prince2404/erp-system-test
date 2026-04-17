import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import Modal from '../../components/common/Modal'
import { useCreateState, useCurrentUser, useStates, type StateItem } from '../../hooks/useAdminData'
import { canCreateState } from '../../lib/rbac'

const schema = z.object({
  name: z.string().trim().min(2, 'State name is required'),
  code: z.string().trim().min(2, 'State code is required').max(10, 'State code is too long'),
})

type FormValues = z.infer<typeof schema>

const columns: Column<StateItem>[] = [
  { key: 'name', header: 'Name', accessor: (state) => state.name },
  { key: 'code', header: 'Code', accessor: (state) => state.code },
]

const StatesPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { data: states = [], isLoading } = useStates()
  const { data: currentUser } = useCurrentUser()
  const createState = useCreateState()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (values: FormValues) => {
    await createState.mutateAsync(values)
    reset()
    setIsModalOpen(false)
  }

  const canCreate = canCreateState(currentUser?.role)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">State Management</h2>
        {canCreate ? (
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            Add State
          </button>
        ) : null}
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading states...</p> : <DataTable columns={columns} rows={states} />}

      <Modal title="Create State" isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">State Name</label>
            <input {...register('name')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">State Code</label>
            <input {...register('code')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.code ? <p className="mt-1 text-xs text-red-600">{errors.code.message}</p> : null}
          </div>

          {createState.isError ? (
            <p className="text-xs text-red-600">Unable to create state. Please verify your access and input.</p>
          ) : null}

          <button
            type="submit"
            disabled={createState.isPending}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {createState.isPending ? 'Saving...' : 'Save State'}
          </button>
        </form>
      </Modal>
    </div>
  )
}

export default StatesPage
