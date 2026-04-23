import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'

const patientSchema = z.object({
  firstName: z.string().trim().min(2, 'First name is required'),
  lastName: z.string().trim().min(1, 'Last name is required'),
  dob: z.string().min(1, 'Date of birth is required'),
  gender: z.string().trim().min(1, 'Gender is required'),
  bloodGroup: z.string().trim().min(2, 'Blood group is required'),
})

type PatientFormValues = z.infer<typeof patientSchema>

type AddPatientFormProps = {
  isSubmitting: boolean
  errorMessage: string | null
  onCreate: (payload: { firstName: string; lastName: string; dob: string; gender: string; bloodGroup: string }) => Promise<void>
}

/**
 * Form section for adding a patient/member to selected family.
 */
const AddPatientForm = ({ isSubmitting, errorMessage, onCreate }: AddPatientFormProps) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PatientFormValues>({ resolver: zodResolver(patientSchema) })

  const onSubmit = async (values: PatientFormValues) => {
    await onCreate(values)
    reset()
  }

  return (
    <form className="grid gap-3 md:grid-cols-3" onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">First Name</label>
        <input {...register('firstName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        {errors.firstName ? <p className="mt-1 text-xs text-red-600">{errors.firstName.message}</p> : null}
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Last Name</label>
        <input {...register('lastName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        {errors.lastName ? <p className="mt-1 text-xs text-red-600">{errors.lastName.message}</p> : null}
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Date of Birth</label>
        <input type="date" {...register('dob')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        {errors.dob ? <p className="mt-1 text-xs text-red-600">{errors.dob.message}</p> : null}
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Gender</label>
        <input {...register('gender')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        {errors.gender ? <p className="mt-1 text-xs text-red-600">{errors.gender.message}</p> : null}
      </div>
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Blood Group</label>
        <input {...register('bloodGroup')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        {errors.bloodGroup ? <p className="mt-1 text-xs text-red-600">{errors.bloodGroup.message}</p> : null}
      </div>
      <div className="flex items-end">
        <button
          type="submit"
          disabled={isSubmitting}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
        >
          {isSubmitting ? 'Saving...' : 'Add Patient'}
        </button>
      </div>
      {errorMessage ? <p className="text-xs text-red-600 md:col-span-3">{errorMessage}</p> : null}
    </form>
  )
}

export default AddPatientForm
