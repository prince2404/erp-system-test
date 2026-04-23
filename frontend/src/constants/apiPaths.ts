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
