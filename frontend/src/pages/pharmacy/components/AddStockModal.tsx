import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import Modal from '../../../components/common/Modal'
import type { Medicine } from '../../../hooks/usePharmacyData'
import { isPositiveInteger } from '../../../utils/validationUtils'

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
    .refine((value) => isPositiveInteger(value), 'Quantity must be greater than zero'),
  expiryDate: z.string().min(1, 'Expiry date is required'),
  cost: z
    .string()
    .trim()
    .min(1, 'Cost is required')
    .refine((value) => Number(value) > 0, 'Cost must be greater than zero'),
})

type BatchFormValues = z.infer<typeof batchSchema>

type AddStockModalProps = {
  isOpen: boolean
  onClose: () => void
  medicines: Medicine[]
  minExpiryDate: string
  isSubmitting: boolean
  onSubmitStock: (payload: {
    medicineId: number
    vendorId: number
    batchNumber: string
    quantityReceived: number
    expiryDate: string
    unitPrice: number
    sellingPrice: number
  }) => Promise<void>
}

/**
 * Modal form for creating stock batches.
 */
const AddStockModal = ({ isOpen, onClose, medicines, minExpiryDate, isSubmitting, onSubmitStock }: AddStockModalProps) => {
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
    if (values.expiryDate < minExpiryDate) {
      return
    }

    await onSubmitStock({
      medicineId: Number(values.medicineId),
      vendorId: Number(values.vendorId),
      batchNumber: values.batchNumber,
      quantityReceived: Number(values.quantityReceived),
      expiryDate: values.expiryDate,
      unitPrice: Number(values.cost),
      sellingPrice: Number(values.cost),
    })

    reset()
    onClose()
  }

  return (
    <Modal title="Add Stock Batch" isOpen={isOpen} onClose={onClose}>
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
          <input type="date" min={minExpiryDate} {...register('expiryDate')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
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
            disabled={isSubmitting}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? 'Saving...' : 'Save Stock Batch'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default AddStockModal
