import { useMemo, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import DataTable, { type Column } from '../../components/common/DataTable'
import { useUsers, type UserSummary } from '../../hooks/useAdminData'
import {
  useAppointments,
  useCreateAppointment,
  usePatients,
  type Appointment,
} from '../../hooks/useClinicalData'

const schema = z.object({
  patientId: z.string().min(1, 'Please select a patient'),
  doctorId: z.string().min(1, 'Please select a doctor'),
  appointmentDate: z.string().min(1, 'Please select appointment date'),
  chiefComplaint: z.string().trim().min(3, 'Please capture initial notes/chief complaint'),
})

type FormValues = z.infer<typeof schema>

const columns: Column<Appointment>[] = [
  { key: 'token', header: 'Token', accessor: (appointment) => appointment.tokenNumber },
  { key: 'patient', header: 'Patient ID', accessor: (appointment) => appointment.patientId },
  { key: 'doctor', header: 'Doctor ID', accessor: (appointment) => appointment.doctorId },
  { key: 'date', header: 'Date', accessor: (appointment) => appointment.appointmentDate },
  { key: 'status', header: 'Status', accessor: (appointment) => appointment.status },
]

const AppointmentsPage = () => {
  const { data: patients = [] } = usePatients()
  const { data: users = [] } = useUsers()
  const { data: appointments = [], isLoading } = useAppointments()
  const createAppointment = useCreateAppointment()
  const [bookingError, setBookingError] = useState<string | null>(null)

  const doctors = useMemo(
    () => users.filter((user: UserSummary) => user.role === 'DOCTOR'),
    [users],
  )

  const {
    register,
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      appointmentDate: new Date().toISOString().slice(0, 10),
    },
  })

  const selectedPatientId = useWatch({ control, name: 'patientId' })
  const selectedPatient = useMemo(
    () => patients.find((patient) => patient.id === Number(selectedPatientId)),
    [patients, selectedPatientId],
  )

  const onSubmit = async (values: FormValues) => {
    if (!selectedPatient) {
      return
    }

    setBookingError(null)
    try {
      await createAppointment.mutateAsync({
        patientId: Number(values.patientId),
        doctorId: Number(values.doctorId),
        centerId: selectedPatient.centerId,
        appointmentDate: values.appointmentDate,
        chiefComplaint: values.chiefComplaint,
      })

      reset({
        patientId: '',
        doctorId: '',
        appointmentDate: new Date().toISOString().slice(0, 10),
        chiefComplaint: '',
      })
    } catch {
      setBookingError('Unable to book appointment. Please verify patient/doctor mapping.')
    }
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-900">Appointment Booking</h2>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <form className="grid gap-3 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Patient</label>
            <select {...register('patientId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select patient</option>
              {patients.map((patient) => (
                <option key={patient.id} value={patient.id}>
                  {patient.firstName} {patient.lastName} ({patient.familyHealthCardNumber})
                </option>
              ))}
            </select>
            {errors.patientId ? <p className="mt-1 text-xs text-red-600">{errors.patientId.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Doctor</label>
            <select {...register('doctorId')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
              <option value="">Select doctor</option>
              {doctors.map((doctor) => (
                <option key={doctor.id} value={doctor.id}>
                  {doctor.username}
                </option>
              ))}
            </select>
            {errors.doctorId ? <p className="mt-1 text-xs text-red-600">{errors.doctorId.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Appointment Date</label>
            <input type="date" {...register('appointmentDate')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.appointmentDate ? <p className="mt-1 text-xs text-red-600">{errors.appointmentDate.message}</p> : null}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Initial Notes / Chief Complaint</label>
            <textarea {...register('chiefComplaint')} rows={3} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.chiefComplaint ? <p className="mt-1 text-xs text-red-600">{errors.chiefComplaint.message}</p> : null}
          </div>

          {bookingError ? <p className="text-xs text-red-600 md:col-span-2">{bookingError}</p> : null}

          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={createAppointment.isPending}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
            >
              {createAppointment.isPending ? 'Booking...' : 'Book Appointment'}
            </button>
          </div>
        </form>
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading appointments...</p> : <DataTable columns={columns} rows={appointments} getRowKey={(appointment) => appointment.id} />}
    </div>
  )
}

export default AppointmentsPage
