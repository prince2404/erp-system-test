import { useMemo, useState } from 'react'
import DataTable, { type Column } from '../../components/common/DataTable'
import EmptyState from '../../components/common/EmptyState'
import ErrorState from '../../components/common/ErrorState'
import Loader from '../../components/common/Loader'
import { useCenters } from '../../hooks/useAdminData'
import {
  useCreateFamily,
  useCreatePatient,
  useFamilies,
  useFamily,
  type Family,
  type FamilyMember,
} from '../../hooks/useClinicalData'
import AddPatientForm from './components/AddPatientForm'
import CreateFamilyForm from './components/CreateFamilyForm'

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

/**
 * Family and patient management page.
 * Access: reception workflow users through protected dashboard routes.
 */
const FamiliesPage = () => {
  const familiesQuery = useFamilies()
  const { data: centers = [] } = useCenters()
  const createFamily = useCreateFamily()
  const createPatient = useCreatePatient()
  const [selectedHealthCardNumber, setSelectedHealthCardNumber] = useState('')
  const [familyError, setFamilyError] = useState<string | null>(null)
  const [patientError, setPatientError] = useState<string | null>(null)

  const defaultHealthCard = useMemo(
    () => selectedHealthCardNumber || familiesQuery.data?.[0]?.healthCardNumber || '',
    [familiesQuery.data, selectedHealthCardNumber],
  )

  const familyDetailsQuery = useFamily(defaultHealthCard)

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-900">Family & Patient Management</h2>

      <CreateFamilyForm
        centers={centers}
        isSubmitting={createFamily.isPending}
        errorMessage={familyError}
        onCreate={async (payload) => {
          setFamilyError(null)
          try {
            await createFamily.mutateAsync(payload)
          } catch {
            setFamilyError('Unable to create family. Please verify details and try again.')
          }
        }}
      />

      {familiesQuery.isLoading ? <Loader message="Loading families..." /> : null}
      {familiesQuery.isError ? (
        <ErrorState
          message="Unable to load families."
          onRetry={() => {
            void familiesQuery.refetch()
          }}
        />
      ) : null}
      {!familiesQuery.isLoading && !familiesQuery.isError && (familiesQuery.data?.length ?? 0) === 0 ? (
        <EmptyState message="No families found yet." />
      ) : null}
      {!familiesQuery.isLoading && !familiesQuery.isError && (familiesQuery.data?.length ?? 0) > 0 ? (
        <DataTable columns={familyColumns} rows={familiesQuery.data ?? []} getRowKey={(family) => family.id} />
      ) : null}

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <div className="mb-4">
          <label className="mb-1 block text-sm font-medium text-slate-700">Select Family</label>
          <select
            value={defaultHealthCard}
            onChange={(event) => setSelectedHealthCardNumber(event.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm md:max-w-md"
          >
            {(familiesQuery.data ?? []).map((family) => (
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

            <AddPatientForm
              isSubmitting={createPatient.isPending}
              errorMessage={patientError}
              onCreate={async (payload) => {
                if (!defaultHealthCard) {
                  return
                }

                setPatientError(null)
                try {
                  await createPatient.mutateAsync({ healthCardNumber: defaultHealthCard, ...payload })
                } catch {
                  setPatientError('Unable to add patient. Please verify details and try again.')
                }
              }}
            />
          </div>
        ) : (
          <p className="text-sm text-slate-600">Select a family to view details and add patients.</p>
        )}
      </div>
    </div>
  )
}

export default FamiliesPage
