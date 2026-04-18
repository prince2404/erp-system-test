import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useParams } from 'react-router-dom'
import { useUsers } from '../../hooks/useAdminData'
import {
  useAppointment,
  useCreateDiagnosis,
  usePatients,
  useUpdateAppointmentStatus,
} from '../../hooks/useClinicalData'

const schema = z.object({
  temperature: z.string().trim().min(1, 'Temperature is required'),
  bloodPressure: z.string().trim().min(1, 'Blood pressure is required'),
  pulseRate: z.string().trim().min(1, 'Pulse rate is required'),
  spo2: z.string().trim().min(1, 'SpO₂ is required'),
  symptoms: z.string().trim().min(3, 'Symptoms/chief complaint notes are required'),
  consultationNotes: z.string().trim().min(3, 'Consultation notes are required'),
  medicineName: z.string().trim().min(2, 'Medicine/advice name is required'),
  dosage: z.string().trim().min(2, 'Dosage is required'),
  duration: z.string().trim().min(2, 'Duration is required'),
})

type FormValues = z.infer<typeof schema>

const DoctorConsultationPage = () => {
  const params = useParams<{ appointmentId: string }>()
  const appointmentId = Number(params.appointmentId)
  const { data: appointment, isLoading } = useAppointment(appointmentId)
  const { data: users = [] } = useUsers()
  const { data: patients = [] } = usePatients()
  const updateStatus = useUpdateAppointmentStatus()
  const createDiagnosis = useCreateDiagnosis()
  const [completed, setCompleted] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const patient = useMemo(
    () => patients.find((item) => item.id === appointment?.patientId),
    [patients, appointment?.patientId],
  )
  const doctor = useMemo(
    () => users.find((item) => item.id === appointment?.doctorId),
    [users, appointment?.doctorId],
  )

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = async (values: FormValues) => {
    if (!appointment) {
      return
    }

    setError(null)
    setCompleted(false)

    try {
      if (appointment.status === 'WAITING') {
        await updateStatus.mutateAsync({ appointmentId: appointment.id, status: 'IN_CONSULTATION' })
      }

      const vitalsSummary = `Vitals: Temp ${values.temperature}, BP ${values.bloodPressure}, Pulse ${values.pulseRate}, SpO₂ ${values.spo2}.`
      await createDiagnosis.mutateAsync({
        appointmentId: appointment.id,
        symptoms: values.symptoms,
        medicalNotes: `${vitalsSummary} ${values.consultationNotes}`.trim(),
        medicineName: values.medicineName,
        dosage: values.dosage,
        duration: values.duration,
      })

      await updateStatus.mutateAsync({ appointmentId: appointment.id, status: 'BILLING' })
      setCompleted(true)
    } catch {
      setError('Unable to complete consultation. Ensure diagnosis is entered only once and appointment is in queue.')
    }
  }

  if (isLoading) {
    return <p className="text-sm text-slate-600">Loading consultation...</p>
  }

  if (!appointment) {
    return <p className="text-sm text-red-600">Appointment not found.</p>
  }

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-900">Consultation #{appointment.id}</h2>

      <div className="rounded-lg border border-slate-200 bg-white p-4 text-sm text-slate-700">
        <p>
          <span className="font-medium">Patient:</span>{' '}
          {patient ? `${patient.firstName} ${patient.lastName}` : `#${appointment.patientId}`}
        </p>
        <p>
          <span className="font-medium">Doctor:</span> {doctor?.username ?? `#${appointment.doctorId}`}
        </p>
        <p>
          <span className="font-medium">Token:</span> {appointment.tokenNumber}
        </p>
        <p>
          <span className="font-medium">Initial Notes:</span> {appointment.chiefComplaint || 'Not provided at booking.'}
        </p>
      </div>

      {appointment.status === 'BILLING' || appointment.status === 'COMPLETED' ? (
        <p className="rounded-md border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-700">
          Consultation already completed and moved to {appointment.status}.
        </p>
      ) : (
        <form className="grid gap-3 rounded-lg border border-slate-200 bg-white p-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Temperature</label>
            <input {...register('temperature')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.temperature ? <p className="mt-1 text-xs text-red-600">{errors.temperature.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Blood Pressure</label>
            <input {...register('bloodPressure')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.bloodPressure ? <p className="mt-1 text-xs text-red-600">{errors.bloodPressure.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Pulse Rate</label>
            <input {...register('pulseRate')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.pulseRate ? <p className="mt-1 text-xs text-red-600">{errors.pulseRate.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">SpO₂</label>
            <input {...register('spo2')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.spo2 ? <p className="mt-1 text-xs text-red-600">{errors.spo2.message}</p> : null}
          </div>
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Symptoms</label>
            <textarea {...register('symptoms')} rows={2} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.symptoms ? <p className="mt-1 text-xs text-red-600">{errors.symptoms.message}</p> : null}
          </div>
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Consultation Notes</label>
            <textarea {...register('consultationNotes')} rows={3} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.consultationNotes ? <p className="mt-1 text-xs text-red-600">{errors.consultationNotes.message}</p> : null}
          </div>
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-slate-700">Medicine / Advice</label>
            <input {...register('medicineName')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.medicineName ? <p className="mt-1 text-xs text-red-600">{errors.medicineName.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Dosage</label>
            <input {...register('dosage')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.dosage ? <p className="mt-1 text-xs text-red-600">{errors.dosage.message}</p> : null}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Duration</label>
            <input {...register('duration')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
            {errors.duration ? <p className="mt-1 text-xs text-red-600">{errors.duration.message}</p> : null}
          </div>

          {error ? <p className="text-xs text-red-600 md:col-span-2">{error}</p> : null}
          {completed ? <p className="text-xs text-emerald-700 md:col-span-2">Consultation completed. Appointment moved to BILLING.</p> : null}

          <div className="md:col-span-2">
            <button
              type="submit"
              disabled={createDiagnosis.isPending || updateStatus.isPending}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
            >
              {createDiagnosis.isPending || updateStatus.isPending ? 'Processing...' : 'Complete Consultation'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

export default DoctorConsultationPage
