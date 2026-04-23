import { useMemo } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clinicalApi } from '../api/clinicalApi'

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

/**
 * Unwraps normalized API results and raises errors for react-query.
 */
const unwrapApiResult = <T>(result: { data: T | null; error: string | null }): T => {
  if (result.error || result.data === null) {
    throw new Error(result.error ?? 'Unexpected API error')
  }

  return result.data
}

/**
 * Families query.
 */
export const useFamilies = () =>
  useQuery({
    queryKey: ['families'],
    queryFn: async () =>
      unwrapApiResult<Family[]>((await clinicalApi.getFamilies()) as { data: Family[] | null; error: string | null }),
  })

/**
 * Family details query.
 */
export const useFamily = (healthCardNumber?: string) =>
  useQuery({
    queryKey: ['family', healthCardNumber],
    queryFn: async () =>
      unwrapApiResult<Family>(
        (await clinicalApi.getFamily(healthCardNumber ?? '')) as { data: Family | null; error: string | null },
      ),
    enabled: Boolean(healthCardNumber),
  })

/**
 * Derived patient list generated from family data.
 */
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

/**
 * Appointments query with optional doctor/status filters.
 */
export const useAppointments = (doctorId?: number, status?: AppointmentStatus) =>
  useQuery({
    queryKey: ['appointments', doctorId ?? null, status ?? null],
    queryFn: async () =>
      unwrapApiResult<Appointment[]>(
        (await clinicalApi.getAppointments(doctorId, status)) as { data: Appointment[] | null; error: string | null },
      ),
  })

/**
 * Appointment details query.
 */
export const useAppointment = (appointmentId?: number) =>
  useQuery({
    queryKey: ['appointment', appointmentId],
    queryFn: async () =>
      unwrapApiResult<Appointment>(
        (await clinicalApi.getAppointment(appointmentId ?? 0)) as { data: Appointment | null; error: string | null },
      ),
    enabled: typeof appointmentId === 'number' && Number.isFinite(appointmentId),
  })

/**
 * Family creation mutation.
 */
export const useCreateFamily = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { familyHeadName: string; centerId: number }) => unwrapApiResult(await clinicalApi.createFamily(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

/**
 * Patient/member creation mutation.
 */
export const useCreatePatient = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: {
      healthCardNumber: string
      firstName: string
      lastName: string
      dob: string
      gender: string
      bloodGroup: string
    }) => unwrapApiResult(await clinicalApi.createPatient(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

/**
 * Appointment creation mutation.
 */
export const useCreateAppointment = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: {
      patientId: number
      centerId: number
      doctorId: number
      appointmentDate: string
      chiefComplaint: string
    }) => unwrapApiResult(await clinicalApi.createAppointment(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}

/**
 * Diagnosis creation mutation.
 */
export const useCreateDiagnosis = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: {
      appointmentId: number
      symptoms: string
      medicalNotes: string
      medicineName: string
      dosage: string
      duration: string
    }) => unwrapApiResult(await clinicalApi.createDiagnosis(payload)),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointment', variables.appointmentId] })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}

/**
 * Appointment status update mutation.
 */
export const useUpdateAppointmentStatus = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { appointmentId: number; status: AppointmentStatus }) =>
      unwrapApiResult(await clinicalApi.updateAppointmentStatus(payload)),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointment', variables.appointmentId] })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
    },
  })
}
