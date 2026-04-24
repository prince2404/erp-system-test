/**
 * Centralized API path constants to avoid hardcoded endpoint strings.
 */
export const API_PATHS = {
  auth: {
    login: '/api/auth/login',
    refresh: import.meta.env.VITE_AUTH_REFRESH_PATH ?? '/api/auth/refresh',
    register: '/api/v1/auth/register',
  },
  users: {
    me: '/api/v1/users/me',
    list: '/api/v1/users',
    create: '/api/v1/users',
    safeEdit: (id: number) => `/api/v1/users/${id}/safe`,
    dangerousEdit: (id: number) => `/api/v1/users/${id}`,
    editableForm: (id: number) => `/api/v1/users/${id}/editable`,
    deactivate: (id: number) => `/api/v1/users/${id}/deactivate`,
    reactivate: (id: number) => `/api/v1/users/${id}/reactivate`,
    toggles: (id: number) => `/api/v1/users/${id}/toggles`,
    profile: (id: number) => `/api/v1/users/${id}/profile`,
    personal: (id: number) => `/api/v1/users/${id}/personal`,
    verifyBankAccount: (userId: number, bankId: number) => `/api/v1/users/${userId}/bank-accounts/${bankId}/verify`,
    reviewAadhaar: (id: number) => `/api/v1/users/${id}/verify/aadhaar/review`,
    reviewPhotoId: (id: number) => `/api/v1/users/${id}/verify/photo-id/review`,
  },
  roles: {
    assignable: '/api/v1/roles/assignable',
  },
  profile: {
    root: '/api/v1/profile',
    personal: '/api/v1/profile/personal',
    verifyPhoneSend: '/api/v1/profile/verify/phone/send',
    verifyPhoneConfirm: '/api/v1/profile/verify/phone/confirm',
    verifyEmailSend: '/api/v1/profile/verify/email/send',
    verifyEmailConfirm: '/api/v1/profile/verify/email/confirm',
    verifyAadhaar: '/api/v1/profile/verify/aadhaar',
    verifyPhotoId: '/api/v1/profile/verify/photo-id',
    bankAccounts: '/api/v1/profile/bank-accounts',
    securityPassword: '/api/v1/profile/security/password',
    securitySessions: '/api/v1/profile/security/sessions',
    securityLoginHistory: '/api/v1/profile/security/login-history',
    security2fa: '/api/v1/profile/security/2fa',
    preferences: '/api/v1/profile/preferences',
    deactivate: '/api/v1/profile/deactivate',
    dataDeletionRequest: '/api/v1/profile/data-deletion-request',
  },
  admin: {
    states: '/api/v1/states',
    districts: '/api/v1/districts',
    blocks: '/api/v1/blocks',
    centers: '/api/v1/centers',
  },
  clinical: {
    families: '/api/v1/families',
    appointments: '/api/opd/appointments',
  },
  dashboard: {
    metrics: '/api/dashboard/metrics',
  },
  billing: {
    invoices: '/api/billing/invoices',
    walletCredit: '/api/v1/wallet/credit',
    commissionsByUser: '/api/commissions/user',
  },
  pharmacy: {
    medicines: '/api/inventory/medicines',
    batches: '/api/inventory/batches',
    centerBatches: '/api/inventory/centers',
  },
} as const
