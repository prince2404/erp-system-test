import { useMemo } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'

type ApiEnvelope<T> = {
  success: boolean
  message: string
  data: T
}

export type FamilyMember = {
  id: number
  firstName: string
  lastName: string
  dob: string
  gender: string
  bloodGroup: string
  familyId: number
}

export type Family = {
  id: number
  familyHeadName: string
  healthCardNumber: string
  qrCodeReference: string
  walletBalance: string
  centerId: number
  members: FamilyMember[]
}

export type AppointmentStatus = 'WAITING' | 'IN_CONSULTATION' | 'PHARMACY' | 'BILLING' | 'COMPLETED'

export type Appointment = {
  id: number
  patientId: number
  centerId: number
  doctorId: number
  tokenNumber: string
  status: AppointmentStatus
  appointmentDate: string
  chiefComplaint: string | null
}

const getFamilies = async () => {
  const response = await api.get<ApiEnvelope<Family[]>>('/api/v1/families')
  return response.data.data
}

const getFamily = async (healthCardNumber: string) => {
  const response = await api.get<ApiEnvelope<Family>>(`/api/v1/families/${healthCardNumber}`)
  return response.data.data
}

const getAppointments = async (doctorId?: number, status?: AppointmentStatus) => {
  const response = await api.get<ApiEnvelope<Appointment[]>>('/api/opd/appointments', {
    params: {
      doctorId,
      status,
    },
  })
  return response.data.data
}

const getAppointment = async (appointmentId: number) => {
  const response = await api.get<ApiEnvelope<Appointment>>(`/api/opd/appointments/${appointmentId}`)
  return response.data.data
}

export const useFamilies = () =>
  useQuery({
    queryKey: ['families'],
    queryFn: getFamilies,
  })

export const useFamily = (healthCardNumber?: string) =>
  useQuery({
    queryKey: ['family', healthCardNumber],
    queryFn: () => getFamily(healthCardNumber ?? ''),
    enabled: Boolean(healthCardNumber),
  })

export const usePatients = () => {
  const familiesQuery = useFamilies()

  const patients = useMemo(
    () =>
      (familiesQuery.data ?? []).flatMap((family) =>
        family.members.map((member) => ({
          ...member,
          familyHealthCardNumber: family.healthCardNumber,
          familyHeadName: family.familyHeadName,
          centerId: family.centerId,
        })),
      ),
    [familiesQuery.data],
  )

  return {
    ...familiesQuery,
    data: patients,
  }
}

export const useAppointments = (doctorId?: number, status?: AppointmentStatus) =>
  useQuery({
    queryKey: ['appointments', doctorId ?? null, status ?? null],
    queryFn: () => getAppointments(doctorId, status),
  })

export const useAppointment = (appointmentId?: number) =>
  useQuery({
    queryKey: ['appointment', appointmentId],
    queryFn: () => getAppointment(appointmentId ?? 0),
    enabled: typeof appointmentId === 'number' && Number.isFinite(appointmentId),
  })

export const useCreateFamily = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { familyHeadName: string; centerId: number }) => api.post('/api/v1/families', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

export const useCreatePatient = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: {
      healthCardNumber: string
      firstName: string
      lastName: string
      dob: string
      gender: string
      bloodGroup: string
    }) =>
      api.post(`/api/v1/families/${payload.healthCardNumber}/members`, {
        firstName: payload.firstName,
        lastName: payload.lastName,
        dob: payload.dob,
        gender: payload.gender,
        bloodGroup: payload.bloodGroup,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

export const useCreateAppointment = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: {
      patientId: number
      centerId: number
      doctorId: number
      appointmentDate: string
      chiefComplaint: string
    }) => api.post('/api/opd/appointments', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}

export const useCreateDiagnosis = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: {
      appointmentId: number
      symptoms: string
      medicalNotes: string
      medicineName: string
      dosage: string
      duration: string
    }) =>
      api.post(`/api/opd/appointments/${payload.appointmentId}/diagnosis`, {
        symptoms: payload.symptoms,
        medicalNotes: payload.medicalNotes,
        prescriptions: [
          {
            medicineName: payload.medicineName,
            dosage: payload.dosage,
            duration: payload.duration,
          },
        ],
      }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointment', variables.appointmentId] })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}

export const useUpdateAppointmentStatus = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { appointmentId: number; status: AppointmentStatus }) =>
      api.put(`/api/opd/appointments/${payload.appointmentId}/status`, {
        status: payload.status,
      }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointment', variables.appointmentId] })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}
