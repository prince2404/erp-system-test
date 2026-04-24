import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { profileApi } from '../api/profileApi'
import { roleApi } from '../api/roleApi'
import { userApi } from '../api/userApi'

/**
 * Unwraps normalized API results and throws for react-query error handling.
 */
const unwrap = <T,>(result: { data: T | null; error: string | null }): T => {
  if (result.error || result.data === null) {
    throw new Error(result.error ?? 'Unexpected API error')
  }
  return result.data
}

// ─── Own Profile ──────────────────────────────────────────────────────
export const useOwnProfile = () =>
  useQuery({
    queryKey: ['profile', 'own'],
    queryFn: async () => unwrap(await profileApi.getProfile()),
  })

export const useUpdatePersonal = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => unwrap(await profileApi.updatePersonal(payload)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

// ─── Verification ─────────────────────────────────────────────────────
export const useSendPhoneOtp = () =>
  useMutation({ mutationFn: async () => unwrap(await profileApi.sendPhoneOtp()) })

export const useConfirmPhoneOtp = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (code: string) => unwrap(await profileApi.confirmPhoneOtp(code)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

export const useSendEmailOtp = () =>
  useMutation({ mutationFn: async () => unwrap(await profileApi.sendEmailOtp()) })

export const useConfirmEmailOtp = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (code: string) => unwrap(await profileApi.confirmEmailOtp(code)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

export const useSubmitAadhaar = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (payload: { aadhaarLast4: string; aadhaarDocUrl?: string }) =>
      unwrap(await profileApi.submitAadhaar(payload.aadhaarLast4, payload.aadhaarDocUrl)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

export const useSubmitPhotoId = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (payload: { photoIdType: string; photoIdDocUrl?: string }) =>
      unwrap(await profileApi.submitPhotoId(payload.photoIdType, payload.photoIdDocUrl)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

// ─── Bank Accounts ────────────────────────────────────────────────────
export const useBankAccounts = () =>
  useQuery({
    queryKey: ['profile', 'bank-accounts'],
    queryFn: async () => unwrap(await profileApi.listBankAccounts()),
  })

export const useAddBankAccount = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (payload: {
      holderName: string; bankName: string; accountNumber: string
      ifscCode: string; accountType: string; isPrimary: boolean
    }) => unwrap(await profileApi.addBankAccount(payload)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'bank-accounts'] }) },
  })
}

export const useSetPrimaryBank = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: number; password: string }) =>
      unwrap(await profileApi.setPrimaryBankAccount(args.id, args.password)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'bank-accounts'] }) },
  })
}

export const useRemoveBankAccount = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => { const r = await profileApi.removeBankAccount(id); if (r.error) throw new Error(r.error) },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'bank-accounts'] }) },
  })
}

// ─── Security ─────────────────────────────────────────────────────────
export const useChangePassword = () =>
  useMutation({
    mutationFn: async (args: { currentPassword: string; newPassword: string }) => {
      const r = await profileApi.changePassword(args.currentPassword, args.newPassword)
      if (r.error) throw new Error(r.error)
    },
  })

export const useSessions = () =>
  useQuery({
    queryKey: ['profile', 'sessions'],
    queryFn: async () => unwrap(await profileApi.listSessions()),
  })

export const useRevokeSession = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => { const r = await profileApi.revokeSession(id); if (r.error) throw new Error(r.error) },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'sessions'] }) },
  })
}

export const useRevokeOtherSessions = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async () => { const r = await profileApi.revokeOtherSessions(); if (r.error) throw new Error(r.error) },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'sessions'] }) },
  })
}

export const useLoginHistory = () =>
  useQuery({
    queryKey: ['profile', 'login-history'],
    queryFn: async () => unwrap(await profileApi.loginHistory()),
  })

export const useUpdateTwoFa = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { enabled: boolean; method: string }) =>
      unwrap(await profileApi.updateTwoFa(args.enabled, args.method)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile'] }) },
  })
}

// ─── Preferences ──────────────────────────────────────────────────────
export const usePreferences = () =>
  useQuery({
    queryKey: ['profile', 'preferences'],
    queryFn: async () => unwrap(await profileApi.getPreferences()),
  })

export const useUpdatePreferences = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (payload: Record<string, unknown>) => unwrap(await profileApi.updatePreferences(payload)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['profile', 'preferences'] }) },
  })
}

// ─── Danger Zone ──────────────────────────────────────────────────────
export const useDeactivateSelf = () =>
  useMutation({
    mutationFn: async (password: string) => {
      const r = await profileApi.deactivateSelf(password)
      if (r.error) throw new Error(r.error)
    },
  })

export const useRequestDataDeletion = () =>
  useMutation({
    mutationFn: async (reason: string) => unwrap(await profileApi.requestDataDeletion(reason)),
  })

// ─── Roles ────────────────────────────────────────────────────────────
export const useAssignableRoles = () =>
  useQuery({
    queryKey: ['roles', 'assignable'],
    queryFn: async () => unwrap(await roleApi.getAssignableRoles()),
  })

// ─── User Management (Admin) ─────────────────────────────────────────
export const useUserToggles = (userId: number) =>
  useQuery({
    queryKey: ['user-toggles', userId],
    queryFn: async () => unwrap(await userApi.getToggles(userId)),
    enabled: userId > 0,
  })

export const useUpdateToggle = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { userId: number; targetRole: string; canCreate?: boolean; canEdit?: boolean; canDelete?: boolean }) => {
      const { userId, ...payload } = args
      return unwrap(await userApi.updateToggle(userId, payload))
    },
    onSuccess: (_data, vars) => { qc.invalidateQueries({ queryKey: ['user-toggles', vars.userId] }) },
  })
}

export const useDeactivateUser = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => { const r = await userApi.deactivateUser(id); if (r.error) throw new Error(r.error) },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }) },
  })
}

export const useReactivateUser = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: number) => { const r = await userApi.reactivateUser(id); if (r.error) throw new Error(r.error) },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }) },
  })
}

export const useEditableForm = (userId: number | null) =>
  useQuery({
    queryKey: ['user-editable', userId],
    queryFn: async () => unwrap(await userApi.getEditableForm(userId!)),
    enabled: userId !== null && userId > 0,
  })

export const useSafeEditUser = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: number; payload: Record<string, unknown> }) =>
      unwrap(await userApi.safeEdit(args.id, args.payload)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }) },
  })
}

export const useDangerousEditUser = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (args: { id: number; payload: Record<string, unknown> }) =>
      unwrap(await userApi.dangerousEdit(args.id, args.payload)),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }) },
  })
}
