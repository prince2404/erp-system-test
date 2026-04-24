import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Profile API: endpoints for own-profile operations (sections 1–7).
 */
export const profileApi = {
  /** Fetches full profile with all sections. */
  getProfile: () =>
    requestApi<Record<string, unknown>>(apiClient.get(API_PATHS.profile.root)),

  /** Updates own personal information (Section 1). */
  updatePersonal: (payload: Record<string, unknown>) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.profile.personal, payload)),

  // ─── Verification (Section 2) ──────────────────────────────────────
  sendPhoneOtp: () =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyPhoneSend)),

  confirmPhoneOtp: (code: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyPhoneConfirm, { code })),

  sendEmailOtp: () =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyEmailSend)),

  confirmEmailOtp: (code: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyEmailConfirm, { code })),

  submitAadhaar: (aadhaarLast4: string, aadhaarDocUrl?: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyAadhaar, { aadhaarLast4, aadhaarDocUrl })),

  submitPhotoId: (photoIdType: string, photoIdDocUrl?: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.verifyPhotoId, { photoIdType, photoIdDocUrl })),

  // ─── Bank Accounts (Section 3) ────────────────────────────────────
  listBankAccounts: () =>
    requestApi<Array<Record<string, unknown>>>(apiClient.get(API_PATHS.profile.bankAccounts)),

  addBankAccount: (payload: {
    holderName: string
    bankName: string
    accountNumber: string
    ifscCode: string
    accountType: string
    isPrimary: boolean
  }) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.bankAccounts, payload)),

  setPrimaryBankAccount: (id: number, password: string) =>
    requestApi<Record<string, unknown>>(apiClient.patch(`${API_PATHS.profile.bankAccounts}/${id}/primary`, { password })),

  removeBankAccount: (id: number) =>
    requestApi<void>(apiClient.delete(`${API_PATHS.profile.bankAccounts}/${id}`)),

  // ─── Security (Section 4) ─────────────────────────────────────────
  changePassword: (currentPassword: string, newPassword: string) =>
    requestApi<void>(apiClient.patch(API_PATHS.profile.securityPassword, { currentPassword, newPassword })),

  listSessions: () =>
    requestApi<Array<Record<string, unknown>>>(apiClient.get(API_PATHS.profile.securitySessions)),

  revokeSession: (id: number) =>
    requestApi<void>(apiClient.delete(`${API_PATHS.profile.securitySessions}/${id}`)),

  revokeOtherSessions: () =>
    requestApi<void>(apiClient.delete(API_PATHS.profile.securitySessions)),

  loginHistory: () =>
    requestApi<Array<Record<string, unknown>>>(apiClient.get(API_PATHS.profile.securityLoginHistory)),

  updateTwoFa: (enabled: boolean, method: string) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.profile.security2fa, { enabled, method })),

  // ─── Preferences (Section 6) ──────────────────────────────────────
  getPreferences: () =>
    requestApi<Record<string, unknown>>(apiClient.get(API_PATHS.profile.preferences)),

  updatePreferences: (payload: Record<string, unknown>) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.profile.preferences, payload)),

  // ─── Danger Zone (Section 7) ──────────────────────────────────────
  deactivateSelf: (password: string) =>
    requestApi<void>(apiClient.post(API_PATHS.profile.deactivate, { password })),

  requestDataDeletion: (reason: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.profile.dataDeletionRequest, { reason })),
}
