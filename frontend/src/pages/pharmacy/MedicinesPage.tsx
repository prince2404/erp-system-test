import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import { useCreateMedicine, useMedicines, type Medicine } from '../../hooks/usePharmacyData'
import { usePermission } from '../../hooks/usePermission'

const schema = z.object({
  name: z.string().trim().min(2, 'Medicine name is required'),
  genericName: z.string().trim().min(2, 'Medicine type is required'),
  manufacturer: z.string().trim().min(2, 'Manufacturer is required'),
})

type FormValues = z.infer<typeof schema>

const columns: Column<Medicine>[] = [
  { key: 'name', header: 'Name', accessor: (medicine) => medicine.name },
  { key: 'manufacturer', header: 'Manufacturer', accessor: (medicine) => medicine.manufacturer },
  { key: 'price', header: 'Price', accessor: () => '—' },
  { key: 'type', header: 'Type', accessor: (medicine) => medicine.genericName },
]

const MedicinesPage = () => {
  const { hasPermission } = usePermission()
  const { data: medicines = [], isLoading } = useMedicines()
  const createMedicine = useCreateMedicine()
  const canAccessPharmacy = hasPermission('pharmacy:view')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', genericName: '', manufacturer: '' },
  })

  const onSubmit = async (values: FormValues) => {
    await createMedicine.mutateAsync(values)
    reset()
  }

  if (!canAccessPharmacy) {
    return <p className="text-sm text-rose-600">You do not have permission to access pharmacy catalog management.</p>
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-slate-900">Global Medicine Catalog</h2>
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading medicines...</p> : null}
      {!isLoading ? <DataTable columns={columns} rows={medicines} getRowKey={(medicine) => medicine.id} /> : null}

      <form onSubmit={handleSubmit(onSubmit)} className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 md:grid-cols-3">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Medicine Name</label>
          <input {...register('name')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Type</label>
          <input {...register('genericName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.genericName ? <p className="mt-1 text-xs text-red-600">{errors.genericName.message}</p> : null}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Manufacturer</label>
          <input {...register('manufacturer')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.manufacturer ? <p className="mt-1 text-xs text-red-600">{errors.manufacturer.message}</p> : null}
        </div>
        <div className="md:col-span-3">
          <button
            type="submit"
            disabled={createMedicine.isPending}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
          >
            {createMedicine.isPending ? 'Adding...' : 'Add Medicine'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default MedicinesPage
