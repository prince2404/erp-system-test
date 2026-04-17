import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import Modal from '../../components/common/Modal'
import {
  useCreateDistrict,
  useCurrentUser,
  useDistricts,
  useStates,
  type DistrictItem,
} from '../../hooks/useAdminData'
import { canCreateDistrict } from '../../lib/rbac'

const schema = z.object({
  name: z.string().trim().min(2, 'District name is required'),
  stateId: z.string().min(1, 'Please select a state'),
})

type FormValues = z.infer<typeof schema>

const DistrictsPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { data: currentUser } = useCurrentUser()
  const { data: districts = [], isLoading } = useDistricts()
  const { data: states = [] } = useStates()
  const createDistrict = useCreateDistrict()

  const columns: Column<DistrictItem>[] = useMemo(
    () => [
      { key: 'name', header: 'Name', accessor: (district) => district.name },
      {
        key: 'state',
        header: 'State',
        accessor: (district) => states.find((state) => state.id === district.stateId)?.name ?? '-',
      },
    ],
    [states],
  )

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (values: FormValues) => {
    await createDistrict.mutateAsync({
      name: values.name,
      stateId: Number(values.stateId),
    })
    reset()
    setIsModalOpen(false)
  }

  const canCreate = canCreateDistrict(currentUser?.role)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">District Management</h2>
        {canCreate ? (
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            Add District
          </button>
        ) : null}
      </div>

      {isLoading ? (
        <p className="text-sm text-slate-600">Loading districts...</p>
      ) : (
        <DataTable columns={columns} rows={districts} getRowKey={(district) => district.id} />
      )}

      <Modal title="Create District" isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">District Name</label>
            <input {...register('name')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">State</label>
            <select {...register('stateId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select state</option>
              {states.map((state) => (
                <option key={state.id} value={state.id}>
                  {state.name}
                </option>
              ))}
            </select>
            {errors.stateId ? <p className="mt-1 text-xs text-red-600">{errors.stateId.message}</p> : null}
          </div>

          {createDistrict.isError ? (
            <p className="text-xs text-red-600">Unable to create district. Please verify your access and input.</p>
          ) : null}

          <button
            type="submit"
            disabled={createDistrict.isPending}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {createDistrict.isPending ? 'Saving...' : 'Save District'}
          </button>
        </form>
      </Modal>
    </div>
  )
}

export default DistrictsPage
