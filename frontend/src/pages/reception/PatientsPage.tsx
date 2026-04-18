import DataTable, { type Column } from '../../components/common/DataTable'
import { usePatients } from '../../hooks/useClinicalData'

type PatientRow = {
  id: number
  firstName: string
  lastName: string
  dob: string
  gender: string
  bloodGroup: string
  familyHeadName: string
  familyHealthCardNumber: string
}

const columns: Column<PatientRow>[] = [
  { key: 'name', header: 'Patient Name', accessor: (patient) => `${patient.firstName} ${patient.lastName}` },
  { key: 'familyHead', header: 'Family Head', accessor: (patient) => patient.familyHeadName },
  { key: 'healthCard', header: 'Health Card', accessor: (patient) => patient.familyHealthCardNumber },
  { key: 'gender', header: 'Gender', accessor: (patient) => patient.gender },
  { key: 'dob', header: 'DOB', accessor: (patient) => patient.dob },
  { key: 'bloodGroup', header: 'Blood Group', accessor: (patient) => patient.bloodGroup },
]

const PatientsPage = () => {
  const { data: patients = [], isLoading } = usePatients()

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold text-slate-900">Patient Directory</h2>
      {isLoading ? (
        <p className="text-sm text-slate-600">Loading patients...</p>
      ) : (
        <DataTable columns={columns} rows={patients} getRowKey={(patient) => patient.id} />
      )}
    </div>
  )
}

export default PatientsPage
