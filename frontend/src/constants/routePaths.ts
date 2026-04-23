/**
 * Route path constants used by router configuration and navigation links.
 */
export const ROUTE_PATHS = {
  login: '/login',
  dashboard: '/dashboard',
  admin: {
    states: '/admin/states',
    districts: '/admin/districts',
    blocks: '/admin/blocks',
    centers: '/admin/centers',
    users: '/admin/users',
  },
  reception: {
    families: '/reception/families',
    patients: '/reception/patients',
    appointments: '/reception/appointments',
  },
  doctor: {
    queue: '/doctor/queue',
    consultation: '/doctor/consultation/:appointmentId',
  },
  billing: {
    invoices: '/billing/invoices',
    walletTopUp: '/billing/wallet-topup',
  },
  commissions: {
    ledger: '/commissions/ledger',
  },
  pharmacy: {
    medicines: '/pharmacy/medicines',
    inventory: '/pharmacy/inventory',
  },
} as const
