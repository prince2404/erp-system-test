import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import type { CenterItem } from '../../../hooks/useAdminData'

const familySchema = z.object({
  familyHeadName: z.string().trim().min(2, 'Head of family name is required'),
  centerId: z.string().min(1, 'Please select a center'),
})

type FamilyFormValues = z.infer<typeof familySchema>

type CreateFamilyFormProps = {
  centers: CenterItem[]
  isSubmitting: boolean
  errorMessage: string | null
  onCreate: (payload: { familyHeadName: string; centerId: number }) => Promise<void>
}

/**
 * Form section for creating family records.
 */
const CreateFamilyForm = ({ centers, isSubmitting, errorMessage, onCreate }: CreateFamilyFormProps) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FamilyFormValues>({ resolver: zodResolver(familySchema) })

  const onSubmit = async (values: FamilyFormValues) => {
    await onCreate({ familyHeadName: values.familyHeadName, centerId: Number(values.centerId) })
    reset()
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h3 className="mb-3 text-base font-semibold text-slate-900">Create Family</h3>
      <form className="grid gap-3 md:grid-cols-3" onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Head of Family</label>
          <input {...register('familyHeadName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          {errors.familyHeadName ? <p className="mt-1 text-xs text-red-600">{errors.familyHeadName.message}</p> : null}
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Center</label>
          <select {...register('centerId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
            <option value="">Select center</option>
            {centers.map((center) => (
              <option key={center.id} value={center.id}>
                {center.name}
              </option>
            ))}
          </select>
          {errors.centerId ? <p className="mt-1 text-xs text-red-600">{errors.centerId.message}</p> : null}
        </div>
        <div className="flex items-end">
          <button
            type="submit"
            disabled={isSubmitting}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
          >
            {isSubmitting ? 'Saving...' : 'Create Family'}
          </button>
        </div>
        {errorMessage ? <p className="text-xs text-red-600 md:col-span-3">{errorMessage}</p> : null}
      </form>
    </div>
  )
}

export default CreateFamilyForm
