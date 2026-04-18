import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import Modal from '../../components/common/Modal'
import { useCenters, useCurrentUser } from '../../hooks/useAdminData'
import { useAddStock, useInventory, useMedicines } from '../../hooks/usePharmacyData'

const toLocalIsoDate = (date: Date) => {
  const timezoneOffsetMs = date.getTimezoneOffset() * 60 * 1000
  return new Date(date.getTime() - timezoneOffsetMs).toISOString().slice(0, 10)
}

const TODAY_LOCAL_ISO_DATE = toLocalIsoDate(new Date())

const batchSchema = z.object({
  medicineId: z.string().min(1, 'Medicine is required'),
  vendorId: z.string().min(1, 'Vendor ID is required'),
  batchNumber: z
    .string()
    .trim()
    .regex(/^[A-Z0-9-]{4,40}$/i, 'Batch number must be 4-40 chars (letters, numbers, dash)'),
  quantityReceived: z
    .string()
    .trim()
    .min(1, 'Quantity is required')
    .refine((value) => Number.isInteger(Number(value)) && Number(value) > 0, 'Quantity must be greater than zero'),
  expiryDate: z
    .string()
    .min(1, 'Expiry date is required')
    .refine((value) => value >= TODAY_LOCAL_ISO_DATE, 'Expiry date cannot be in the past'),
  cost: z
    .string()
    .trim()
    .min(1, 'Cost is required')
    .refine((value) => Number(value) > 0, 'Cost must be greater than zero'),
})

type BatchFormValues = z.infer<typeof batchSchema>

const InventoryPage = () => {
  const { data: currentUser } = useCurrentUser()
  const { data: centers = [] } = useCenters()
  const { data: medicines = [] } = useMedicines()
  const canAccessPharmacy = currentUser?.role === 'PHARMACIST' || currentUser?.role === 'SUPER_ADMIN'

  const defaultCenterId = useMemo(
    () => currentUser?.assignedCenterId ?? centers[0]?.id ?? null,
    [centers, currentUser?.assignedCenterId],
  )
  const [selectedCenterId, setSelectedCenterId] = useState<number | null>(null)
  const [isAddStockOpen, setIsAddStockOpen] = useState(false)
  const activeCenterId = selectedCenterId ?? defaultCenterId
  const { data: batches = [], isLoading } = useInventory(activeCenterId)
  const addStock = useAddStock()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BatchFormValues>({
    resolver: zodResolver(batchSchema),
    defaultValues: {
      medicineId: '',
      vendorId: '',
      batchNumber: '',
      quantityReceived: '1',
      expiryDate: '',
      cost: '',
    },
  })

  const onSubmit = async (values: BatchFormValues) => {
    if (!activeCenterId) {
      return
    }

    await addStock.mutateAsync({
      medicineId: Number(values.medicineId),
      vendorId: Number(values.vendorId),
      centerId: activeCenterId,
      batchNumber: values.batchNumber,
      quantityReceived: Number(values.quantityReceived),
      expiryDate: values.expiryDate,
      unitPrice: Number(values.cost),
      sellingPrice: Number(values.cost),
    })

    reset()
    setIsAddStockOpen(false)
  }

  if (!canAccessPharmacy) {
    return <p className="text-sm text-rose-600">You do not have permission to access inventory management.</p>
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold text-slate-900">Center Stock Management</h2>
        <div className="flex flex-wrap items-center gap-2">
          <label className="text-sm text-slate-700">
            Center
            <select
              value={activeCenterId ?? ''}
              onChange={(event) => setSelectedCenterId(event.target.value ? Number(event.target.value) : null)}
              className="ml-2 rounded-md border border-slate-300 px-2 py-1 text-sm"
              disabled={Boolean(currentUser?.assignedCenterId)}
            >
              {centers.map((center) => (
                <option key={center.id} value={center.id}>
                  {center.name}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            onClick={() => setIsAddStockOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white"
          >
            Add Stock
          </button>
        </div>
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading inventory batches...</p> : null}

      {!isLoading ? (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Medicine Name</th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Batch Number</th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Quantity</th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Expiry Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {batches.length > 0 ? (
                batches.map((batch) => {
                  const isExpired = batch.expiryDate < TODAY_LOCAL_ISO_DATE
                  const isLowStock = batch.quantityAvailable < 50
                  const rowClassName = isExpired
                    ? 'bg-rose-50'
                    : isLowStock
                      ? 'bg-amber-50'
                      : 'hover:bg-slate-50'

                  return (
                    <tr key={batch.id} className={rowClassName}>
                      <td className="px-4 py-3 text-slate-700">{batch.medicineName}</td>
                      <td className="px-4 py-3 text-slate-700">{batch.batchNumber}</td>
                      <td className="px-4 py-3 text-slate-700">{batch.quantityAvailable}</td>
                      <td className="px-4 py-3 text-slate-700">{batch.expiryDate}</td>
                    </tr>
                  )
                })
              ) : (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-slate-500">
                    No inventory batches found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      ) : null}

      <p className="text-xs text-slate-600">
        Highlight legend: <span className="font-medium text-amber-700">low stock</span> (qty &lt; 50),{' '}
        <span className="font-medium text-rose-700">expired</span>.
      </p>

      <Modal title="Add Stock Batch" isOpen={isAddStockOpen} onClose={() => setIsAddStockOpen(false)}>
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-3 md:grid-cols-2">
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Medicine</label>
            <select {...register('medicineId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select medicine</option>
              {medicines.map((medicine) => (
                <option key={medicine.id} value={medicine.id}>
                  {medicine.name}
                </option>
              ))}
            </select>
            {errors.medicineId ? <p className="mt-1 text-xs text-red-600">{errors.medicineId.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Batch Number</label>
            <input {...register('batchNumber')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.batchNumber ? <p className="mt-1 text-xs text-red-600">{errors.batchNumber.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Quantity</label>
            <input type="number" min={1} {...register('quantityReceived')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.quantityReceived ? <p className="mt-1 text-xs text-red-600">{errors.quantityReceived.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Expiry Date</label>
            <input type="date" {...register('expiryDate')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.expiryDate ? <p className="mt-1 text-xs text-red-600">{errors.expiryDate.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Cost</label>
            <input type="number" step="0.01" min={0.01} {...register('cost')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.cost ? <p className="mt-1 text-xs text-red-600">{errors.cost.message}</p> : null}
          </div>

          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Vendor ID</label>
            <input type="number" min={1} {...register('vendorId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.vendorId ? <p className="mt-1 text-xs text-red-600">{errors.vendorId.message}</p> : null}
          </div>

          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={addStock.isPending}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
            >
              {addStock.isPending ? 'Saving...' : 'Save Stock Batch'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

export default InventoryPage
