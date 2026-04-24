import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * User API functions for current user profile, user listing, and management.
 */
export const userApi = {
  /** Fetches currently authenticated user profile. */
  getCurrentUser: () =>
    requestApi<{ id: number; username: string; role: string; assignedCenterId: number | null; status?: string }>(
      apiClient.get(API_PATHS.users.me),
    ),

  /** Fetches paginated users and returns content array. */
  getUsers: () =>
    requestApi<{
      content: Array<{ id: number; username: string; email: string; phone: string; role: string; status: string }>
    }>(apiClient.get(`${API_PATHS.users.list}?page=0&size=200`)),

  /** Creates a managed user (backend-driven role assignment). */
  createManagedUser: (payload: {
    username: string
    password: string
    email: string
    phone?: string
    role: string
    assignedStateId?: number
    assignedDistrictId?: number
    assignedBlockId?: number
    assignedCenterId?: number
  }) => requestApi<unknown>(apiClient.post(API_PATHS.users.create, payload)),

  /** Fetches editable form fields for a user (safe fields for all, dangerous for admin). */
  getEditableForm: (id: number) =>
    requestApi<Record<string, unknown>>(apiClient.get(API_PATHS.users.editableForm(id))),

  /** Updates safe fields (name, phone, email) for a user. */
  safeEdit: (id: number, payload: Record<string, unknown>) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.users.safeEdit(id), payload)),

  /** Updates dangerous fields (role, scope) — Admin/SuperAdmin only. */
  dangerousEdit: (id: number, payload: Record<string, unknown>) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.users.dangerousEdit(id), payload)),

  /** Deactivates a user (soft deactivation). */
  deactivateUser: (id: number) =>
    requestApi<void>(apiClient.post(API_PATHS.users.deactivate(id))),

  /** Reactivates a deactivated user. */
  reactivateUser: (id: number) =>
    requestApi<void>(apiClient.post(API_PATHS.users.reactivate(id))),

  /** Fetches toggle settings for a user (Super Admin only). */
  getToggles: (id: number) =>
    requestApi<Array<{ targetRole: string; canCreate: boolean; canEdit: boolean; canDelete: boolean; updatedAt: string; updatedBy: number | null }>>(
      apiClient.get(API_PATHS.users.toggles(id)),
    ),

  /** Updates a toggle setting for a user (Super Admin only). */
  updateToggle: (id: number, payload: { targetRole: string; canCreate?: boolean; canEdit?: boolean; canDelete?: boolean }) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.users.toggles(id), payload)),

  /** Fetches any user's full profile (Super Admin only). */
  getUserProfile: (id: number) =>
    requestApi<Record<string, unknown>>(apiClient.get(API_PATHS.users.profile(id))),

  /** Updates any user's personal info (Super Admin/Admin only). */
  updateUserPersonal: (id: number, payload: Record<string, unknown>) =>
    requestApi<Record<string, unknown>>(apiClient.patch(API_PATHS.users.personal(id), payload)),

  /** Verifies a user's bank account (Admin/Super Admin only). */
  verifyBankAccount: (userId: number, bankId: number) =>
    requestApi<void>(apiClient.post(API_PATHS.users.verifyBankAccount(userId, bankId))),

  /** Reviews Aadhaar verification (Admin/Super Admin only). */
  reviewAadhaar: (id: number, approve: boolean, reason?: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.users.reviewAadhaar(id), { approve, reason })),

  /** Reviews photo ID verification (Admin/Super Admin only). */
  reviewPhotoId: (id: number, approve: boolean, reason?: string) =>
    requestApi<Record<string, unknown>>(apiClient.post(API_PATHS.users.reviewPhotoId(id), { approve, reason })),
}
