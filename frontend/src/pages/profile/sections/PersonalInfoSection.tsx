import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useUpdatePersonal } from '../../../hooks/useProfileData'

type Props = {
  profile: Record<string, unknown> | undefined
}

type FormValues = {
  fullName: string
  dateOfBirth: string
  gender: string
  phone: string
  email: string
  addressStreet: string
  addressCity: string
  addressDistrict: string
  addressState: string
  addressPincode: string
  emergencyName: string
  emergencyPhone: string
}

/**
 * Section 1 — Personal Information.
 * Users can view and edit their own personal details.
 */
const PersonalInfoSection = ({ profile }: Props) => {
  const [isEditing, setIsEditing] = useState(false)
  const updatePersonal = useUpdatePersonal()

  const { register, handleSubmit, reset } = useForm<FormValues>({
    defaultValues: {
      fullName: String(profile?.fullName ?? ''),
      dateOfBirth: String(profile?.dateOfBirth ?? ''),
      gender: String(profile?.gender ?? ''),
      phone: String(profile?.phone ?? ''),
      email: String(profile?.email ?? ''),
      addressStreet: String(profile?.addressStreet ?? ''),
      addressCity: String(profile?.addressCity ?? ''),
      addressDistrict: String(profile?.addressDistrict ?? ''),
      addressState: String(profile?.addressState ?? ''),
      addressPincode: String(profile?.addressPincode ?? ''),
      emergencyName: String(profile?.emergencyName ?? ''),
      emergencyPhone: String(profile?.emergencyPhone ?? ''),
    },
  })

  const onSubmit = async (values: FormValues) => {
    await updatePersonal.mutateAsync(values as unknown as Record<string, unknown>)
    setIsEditing(false)
  }

  const handleCancel = () => {
    reset()
    setIsEditing(false)
  }

  const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 disabled:bg-slate-50 disabled:text-slate-500'
  const labelCls = 'mb-1 block text-xs font-semibold uppercase tracking-wide text-slate-500'

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold text-slate-900">Personal Information</h2>
          <p className="text-sm text-slate-500">Your basic profile details</p>
        </div>
        {!isEditing ? (
          <button
            type="button"
            onClick={() => setIsEditing(true)}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-indigo-500"
          >
            Edit
          </button>
        ) : null}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <div>
            <label className={labelCls}>Full Name</label>
            <input {...register('fullName')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Date of Birth</label>
            <input type="date" {...register('dateOfBirth')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Gender</label>
            <select {...register('gender')} disabled={!isEditing} className={inputCls}>
              <option value="">Select</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
              <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
            </select>
          </div>
          <div>
            <label className={labelCls}>Phone</label>
            <input {...register('phone')} disabled={!isEditing} className={inputCls} />
          </div>
          <div className="sm:col-span-2">
            <label className={labelCls}>Email</label>
            <input type="email" {...register('email')} disabled={!isEditing} className={inputCls} />
          </div>
        </div>

        <hr className="border-slate-200" />
        <h3 className="text-sm font-semibold text-slate-700">Address</h3>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className={labelCls}>Street</label>
            <input {...register('addressStreet')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>City</label>
            <input {...register('addressCity')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>District</label>
            <input {...register('addressDistrict')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>State</label>
            <input {...register('addressState')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Pincode</label>
            <input {...register('addressPincode')} disabled={!isEditing} className={inputCls} />
          </div>
        </div>

        <hr className="border-slate-200" />
        <h3 className="text-sm font-semibold text-slate-700">Emergency Contact</h3>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className={labelCls}>Name</label>
            <input {...register('emergencyName')} disabled={!isEditing} className={inputCls} />
          </div>
          <div>
            <label className={labelCls}>Phone</label>
            <input {...register('emergencyPhone')} disabled={!isEditing} className={inputCls} />
          </div>
        </div>

        {updatePersonal.isError ? (
          <div className="rounded-lg bg-red-50 p-3 text-xs text-red-700">Failed to update. Please try again.</div>
        ) : null}

        {isEditing ? (
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={handleCancel} className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
              Cancel
            </button>
            <button
              type="submit"
              disabled={updatePersonal.isPending}
              className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {updatePersonal.isPending ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        ) : null}
      </form>
    </div>
  )
}

export default PersonalInfoSection
