import { Link } from 'react-router-dom'
import DataTable, { type Column } from '../../components/common/DataTable'
import { useCurrentUser, useUsers } from '../../hooks/useAdminData'
import { useAppointments, type Appointment } from '../../hooks/useClinicalData'

const columns: Column<Appointment>[] = [
  { key: 'token', header: 'Token', accessor: (appointment) => appointment.tokenNumber },
  { key: 'patient', header: 'Patient ID', accessor: (appointment) => appointment.patientId },
  { key: 'date', header: 'Appointment Date', accessor: (appointment) => appointment.appointmentDate },
  {
    key: 'status',
    header: 'Queue Status',
    accessor: (appointment) => (appointment.status === 'WAITING' ? 'SCHEDULED' : appointment.status),
  },
  { key: 'open', header: 'Consultation', accessor: (appointment) => `Open #${appointment.id}` },
]

const DoctorQueuePage = () => {
  const { data: currentUser } = useCurrentUser()
  const { data: users = [] } = useUsers()
  const doctor = users.find((user) => user.username === currentUser?.username)
  const { data: appointments = [], isLoading } = useAppointments(doctor?.id, 'WAITING')

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-semibold text-slate-900">OPD Queue</h2>
      {isLoading ? <p className="text-sm text-slate-600">Loading doctor queue...</p> : null}

      {!isLoading && appointments.length === 0 ? (
        <p className="rounded-md border border-slate-200 bg-white p-4 text-sm text-slate-600">No scheduled appointments in queue.</p>
      ) : null}

      {appointments.length > 0 ? (
        <div className="space-y-3">
          <DataTable columns={columns} rows={appointments} getRowKey={(appointment) => appointment.id} />
          <div className="space-y-2 rounded-lg border border-slate-200 bg-white p-4">
            <h3 className="text-sm font-semibold text-slate-900">Quick Open Consultation</h3>
            {appointments.map((appointment) => (
              <Link
                key={appointment.id}
                to={`/doctor/consultation/${appointment.id}`}
                className="block rounded-md border border-slate-200 px-3 py-2 text-sm text-indigo-700 hover:bg-indigo-50"
              >
                Token {appointment.tokenNumber} · Patient #{appointment.patientId}
              </Link>
            ))}
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default DoctorQueuePage
