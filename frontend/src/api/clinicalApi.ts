import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Clinical API for families, patients, and OPD appointments.
 */
export const clinicalApi = {
  /** Fetches all families. */
  getFamilies: () => requestApi<Array<unknown>>(apiClient.get(API_PATHS.clinical.families)),

  /** Fetches a single family by health card number. */
  getFamily: (healthCardNumber: string) =>
    requestApi<unknown>(apiClient.get(`${API_PATHS.clinical.families}/${healthCardNumber}`)),

  /** Fetches appointments with optional filters. */
  getAppointments: (doctorId?: number, status?: string) =>
    requestApi<Array<unknown>>(apiClient.get(API_PATHS.clinical.appointments, { params: { doctorId, status } })),

  /** Fetches appointment details. */
  getAppointment: (appointmentId: number) => requestApi<unknown>(apiClient.get(`${API_PATHS.clinical.appointments}/${appointmentId}`)),

  /** Creates a new family. */
  createFamily: (payload: { familyHeadName: string; centerId: number }) =>
    requestApi<unknown>(apiClient.post(API_PATHS.clinical.families, payload)),

  /** Creates a patient/member under family health card. */
  createPatient: (payload: {
    healthCardNumber: string
    firstName: string
    lastName: string
    dob: string
    gender: string
    bloodGroup: string
  }) =>
    requestApi<unknown>(
      apiClient.post(`${API_PATHS.clinical.families}/${payload.healthCardNumber}/members`, {
        firstName: payload.firstName,
        lastName: payload.lastName,
        dob: payload.dob,
        gender: payload.gender,
        bloodGroup: payload.bloodGroup,
      }),
    ),

  /** Creates an appointment. */
  createAppointment: (payload: {
    patientId: number
    centerId: number
    doctorId: number
    appointmentDate: string
    chiefComplaint: string
  }) => requestApi<unknown>(apiClient.post(API_PATHS.clinical.appointments, payload)),

  /** Creates diagnosis and prescriptions for an appointment. */
  createDiagnosis: (payload: {
    appointmentId: number
    symptoms: string
    medicalNotes: string
    medicineName: string
    dosage: string
    duration: string
  }) =>
    requestApi<unknown>(
      apiClient.post(`${API_PATHS.clinical.appointments}/${payload.appointmentId}/diagnosis`, {
        symptoms: payload.symptoms,
        medicalNotes: payload.medicalNotes,
        prescriptions: [{ medicineName: payload.medicineName, dosage: payload.dosage, duration: payload.duration }],
      }),
    ),

  /** Updates appointment status. */
  updateAppointmentStatus: (payload: { appointmentId: number; status: string }) =>
    requestApi<unknown>(
      apiClient.put(`${API_PATHS.clinical.appointments}/${payload.appointmentId}/status`, {
        status: payload.status,
      }),
    ),
}
