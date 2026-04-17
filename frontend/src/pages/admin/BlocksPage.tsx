import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import Modal from '../../components/common/Modal'
import {
  useBlocks,
  useCreateBlock,
  useCurrentUser,
  useDistricts,
  type BlockItem,
} from '../../hooks/useAdminData'
import { canCreateBlock } from '../../lib/rbac'

const schema = z.object({
  name: z.string().trim().min(2, 'Block name is required'),
  districtId: z.string().min(1, 'Please select a district'),
})

type FormValues = z.infer<typeof schema>

const BlocksPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { data: currentUser } = useCurrentUser()
  const { data: blocks = [], isLoading } = useBlocks()
  const { data: districts = [] } = useDistricts()
  const createBlock = useCreateBlock()

  const columns: Column<BlockItem>[] = useMemo(
    () => [
      { key: 'name', header: 'Name', accessor: (block) => block.name },
      {
        key: 'district',
        header: 'District',
        accessor: (block) => districts.find((district) => district.id === block.districtId)?.name ?? '-',
      },
    ],
    [districts],
  )

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (values: FormValues) => {
    await createBlock.mutateAsync({
      name: values.name,
      districtId: Number(values.districtId),
    })
    reset()
    setIsModalOpen(false)
  }

  const canCreate = canCreateBlock(currentUser?.role)

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">Block Management</h2>
        {canCreate ? (
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            Add Block
          </button>
        ) : null}
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading blocks...</p> : <DataTable columns={columns} rows={blocks} />}

      <Modal title="Create Block" isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Block Name</label>
            <input {...register('name')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">District</label>
            <select
              {...register('districtId')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">Select district</option>
              {districts.map((district) => (
                <option key={district.id} value={district.id}>
                  {district.name}
                </option>
              ))}
            </select>
            {errors.districtId ? <p className="mt-1 text-xs text-red-600">{errors.districtId.message}</p> : null}
          </div>

          {createBlock.isError ? (
            <p className="text-xs text-red-600">Unable to create block. Please verify your access and input.</p>
          ) : null}

          <button
            type="submit"
            disabled={createBlock.isPending}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {createBlock.isPending ? 'Saving...' : 'Save Block'}
          </button>
        </form>
      </Modal>
    </div>
  )
}

export default BlocksPage
