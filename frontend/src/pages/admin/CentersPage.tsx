import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import Modal from '../../components/common/Modal'
import {
  useBlocks,
  useCenters,
  useCreateCenter,
  type CenterItem,
} from '../../hooks/useAdminData'
import { usePermission } from '../../hooks/usePermission'

const schema = z.object({
  name: z.string().trim().min(2, 'Center name is required'),
  type: z.enum(['CLINIC', 'HOSPITAL']),
  blockId: z.string().min(1, 'Please select a block'),
  centerCode: z.string().trim().min(3, 'Center code is required'),
  address: z.string().trim().min(3, 'Address is required'),
  contactNumber: z.string().trim().min(10, 'Contact number is required'),
})

type FormValues = z.infer<typeof schema>

const CentersPage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const { hasPermission } = usePermission()
  const { data: centers = [], isLoading } = useCenters()
  const { data: blocks = [] } = useBlocks()
  const createCenter = useCreateCenter()

  const columns: Column<CenterItem>[] = useMemo(
    () => [
      { key: 'name', header: 'Name', accessor: (center) => center.name },
      { key: 'type', header: 'Type', accessor: (center) => center.type },
      { key: 'code', header: 'Code', accessor: (center) => center.centerCode },
      {
        key: 'block',
        header: 'Block',
        accessor: (center) => blocks.find((block) => block.id === center.blockId)?.name ?? '-',
      },
      { key: 'contactNumber', header: 'Contact', accessor: (center) => center.contactNumber },
    ],
    [blocks],
  )

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { type: 'CLINIC' },
  })

  const onSubmit = async (values: FormValues) => {
    await createCenter.mutateAsync({
      name: values.name,
      centerCode: values.centerCode,
      type: values.type,
      blockId: Number(values.blockId),
      address: values.address,
      contactNumber: values.contactNumber,
    })
    reset({ type: 'CLINIC' })
    setIsModalOpen(false)
  }

  const canCreate = hasPermission('user:create')

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">Center Management</h2>
        {canCreate ? (
          <button
            type="button"
            onClick={() => setIsModalOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500"
          >
            Add Center
          </button>
        ) : null}
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading centers...</p> : <DataTable columns={columns} rows={centers} getRowKey={(center) => center.id} />}

      <Modal title="Create Center" isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Center Name</label>
            <input {...register('name')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Center Type</label>
            <select {...register('type')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="CLINIC">CLINIC</option>
              <option value="HOSPITAL">HOSPITAL</option>
            </select>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Block</label>
            <select {...register('blockId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select block</option>
              {blocks.map((block) => (
                <option key={block.id} value={block.id}>
                  {block.name}
                </option>
              ))}
            </select>
            {errors.blockId ? <p className="mt-1 text-xs text-red-600">{errors.blockId.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Center Code</label>
            <input
              {...register('centerCode')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
            {errors.centerCode ? <p className="mt-1 text-xs text-red-600">{errors.centerCode.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Address</label>
            <input {...register('address')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.address ? <p className="mt-1 text-xs text-red-600">{errors.address.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Contact Number</label>
            <input
              {...register('contactNumber')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
            {errors.contactNumber ? <p className="mt-1 text-xs text-red-600">{errors.contactNumber.message}</p> : null}
          </div>

          {createCenter.isError ? (
            <p className="text-xs text-red-600">Unable to create center. Please verify your access and input.</p>
          ) : null}

          <button
            type="submit"
            disabled={createCenter.isPending}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {createCenter.isPending ? 'Saving...' : 'Save Center'}
          </button>
        </form>
      </Modal>
    </div>
  )
}

export default CentersPage
