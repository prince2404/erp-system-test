import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import {
  useCreateFamily,
  useCreatePatient,
  useFamilies,
  useFamily,
  type Family,
  type FamilyMember,
} from '../../hooks/useClinicalData'
import { useCenters } from '../../hooks/useAdminData'

const familySchema = z.object({
  familyHeadName: z.string().trim().min(2, 'Head of family name is required'),
  centerId: z.string().min(1, 'Please select a center'),
})

const patientSchema = z.object({
  firstName: z.string().trim().min(2, 'First name is required'),
  lastName: z.string().trim().min(1, 'Last name is required'),
  dob: z.string().min(1, 'Date of birth is required'),
  gender: z.string().trim().min(1, 'Gender is required'),
  bloodGroup: z.string().trim().min(2, 'Blood group is required'),
})

type FamilyFormValues = z.infer<typeof familySchema>
type PatientFormValues = z.infer<typeof patientSchema>

const familyColumns: Column<Family>[] = [
  { key: 'head', header: 'Head of Family', accessor: (family) => family.familyHeadName },
  { key: 'card', header: 'Health Card', accessor: (family) => family.healthCardNumber },
  { key: 'wallet', header: 'Wallet Balance', accessor: (family) => `₹${family.walletBalance}` },
  { key: 'members', header: 'Patients', accessor: (family) => family.members.length },
]

const patientColumns: Column<FamilyMember>[] = [
  { key: 'name', header: 'Patient Name', accessor: (member) => `${member.firstName} ${member.lastName}` },
  { key: 'gender', header: 'Gender', accessor: (member) => member.gender },
  { key: 'dob', header: 'DOB', accessor: (member) => member.dob },
  { key: 'blood', header: 'Blood Group', accessor: (member) => member.bloodGroup },
]

const FamiliesPage = () => {
  const { data: families = [], isLoading } = useFamilies()
  const { data: centers = [] } = useCenters()
  const createFamily = useCreateFamily()
  const createPatient = useCreatePatient()
  const [selectedHealthCardNumber, setSelectedHealthCardNumber] = useState('')
  const [familyError, setFamilyError] = useState<string | null>(null)
  const [patientError, setPatientError] = useState<string | null>(null)

  const {
    register: registerFamily,
    handleSubmit: handleSubmitFamily,
    reset: resetFamilyForm,
    formState: { errors: familyErrors },
  } = useForm<FamilyFormValues>({ resolver: zodResolver(familySchema) })

  const {
    register: registerPatient,
    handleSubmit: handleSubmitPatient,
    reset: resetPatientForm,
    formState: { errors: patientErrors },
  } = useForm<PatientFormValues>({ resolver: zodResolver(patientSchema) })

  const defaultHealthCard = useMemo(
    () => selectedHealthCardNumber || families[0]?.healthCardNumber || '',
    [families, selectedHealthCardNumber],
  )

  const familyDetailsQuery = useFamily(defaultHealthCard)

  const onCreateFamily = async (values: FamilyFormValues) => {
    setFamilyError(null)
    try {
      await createFamily.mutateAsync({
        familyHeadName: values.familyHeadName,
        centerId: Number(values.centerId),
      })
      resetFamilyForm()
    } catch {
      setFamilyError('Unable to create family. Please verify details and try again.')
    }
  }

  const onCreatePatient = async (values: PatientFormValues) => {
    if (!defaultHealthCard) {
      return
    }

    setPatientError(null)
    try {
      await createPatient.mutateAsync({
        healthCardNumber: defaultHealthCard,
        firstName: values.firstName,
        lastName: values.lastName,
        dob: values.dob,
        gender: values.gender,
        bloodGroup: values.bloodGroup,
      })
      resetPatientForm()
    } catch {
      setPatientError('Unable to add patient. Please verify details and try again.')
    }
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-900">Family & Patient Management</h2>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <h3 className="mb-3 text-base font-semibold text-slate-900">Create Family</h3>
        <form className="grid gap-3 md:grid-cols-3" onSubmit={handleSubmitFamily(onCreateFamily)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Head of Family</label>
            <input {...registerFamily('familyHeadName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {familyErrors.familyHeadName ? <p className="mt-1 text-xs text-red-600">{familyErrors.familyHeadName.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Center</label>
            <select {...registerFamily('centerId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select center</option>
              {centers.map((center) => (
                <option key={center.id} value={center.id}>
                  {center.name}
                </option>
              ))}
            </select>
            {familyErrors.centerId ? <p className="mt-1 text-xs text-red-600">{familyErrors.centerId.message}</p> : null}
          </div>
          <div className="flex items-end">
            <button
              type="submit"
              disabled={createFamily.isPending}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
            >
              {createFamily.isPending ? 'Saving...' : 'Create Family'}
            </button>
          </div>
          {familyError ? <p className="text-xs text-red-600 md:col-span-3">{familyError}</p> : null}
        </form>
      </div>

      <div>
        {isLoading ? <p className="text-sm text-slate-600">Loading families...</p> : <DataTable columns={familyColumns} rows={families} getRowKey={(family) => family.id} />}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <div className="mb-4">
          <label className="mb-1 block text-sm font-medium text-slate-700">Select Family</label>
          <select
            value={defaultHealthCard}
            onChange={(event) => setSelectedHealthCardNumber(event.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm md:max-w-md"
          >
            {families.map((family) => (
              <option key={family.id} value={family.healthCardNumber}>
                {family.familyHeadName} ({family.healthCardNumber})
              </option>
            ))}
          </select>
        </div>

        {familyDetailsQuery.data ? (
          <div className="space-y-4">
            <div className="rounded-md bg-slate-50 p-3 text-sm text-slate-700">
              <p>
                <span className="font-medium">Family Head:</span> {familyDetailsQuery.data.familyHeadName}
              </p>
              <p>
                <span className="font-medium">Health Card:</span> {familyDetailsQuery.data.healthCardNumber}
              </p>
              <p>
                <span className="font-medium">Wallet:</span> ₹{familyDetailsQuery.data.walletBalance}
              </p>
            </div>

            <DataTable
              columns={patientColumns}
              rows={familyDetailsQuery.data.members}
              getRowKey={(member) => member.id}
              emptyText="No patients added for this family yet."
            />

            <form className="grid gap-3 md:grid-cols-3" onSubmit={handleSubmitPatient(onCreatePatient)}>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">First Name</label>
                <input {...registerPatient('firstName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
                {patientErrors.firstName ? <p className="mt-1 text-xs text-red-600">{patientErrors.firstName.message}</p> : null}
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Last Name</label>
                <input {...registerPatient('lastName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
                {patientErrors.lastName ? <p className="mt-1 text-xs text-red-600">{patientErrors.lastName.message}</p> : null}
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Date of Birth</label>
                <input type="date" {...registerPatient('dob')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
                {patientErrors.dob ? <p className="mt-1 text-xs text-red-600">{patientErrors.dob.message}</p> : null}
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Gender</label>
                <input {...registerPatient('gender')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
                {patientErrors.gender ? <p className="mt-1 text-xs text-red-600">{patientErrors.gender.message}</p> : null}
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Blood Group</label>
                <input {...registerPatient('bloodGroup')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
                {patientErrors.bloodGroup ? <p className="mt-1 text-xs text-red-600">{patientErrors.bloodGroup.message}</p> : null}
              </div>
              <div className="flex items-end">
                <button
                  type="submit"
                  disabled={createPatient.isPending}
                  className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                >
                  {createPatient.isPending ? 'Saving...' : 'Add Patient'}
                </button>
              </div>
              {patientError ? <p className="text-xs text-red-600 md:col-span-3">{patientError}</p> : null}
            </form>
          </div>
        ) : (
          <p className="text-sm text-slate-600">Select a family to view details and add patients.</p>
        )}
      </div>
    </div>
  )
}

export default FamiliesPage
