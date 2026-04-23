/**
 * Role and permission constants for application authorization checks.
 */
export const ROLE_NAMES = [
  'SUPER_ADMIN',
  'ADMIN',
  'STATE_MANAGER',
  'DISTRICT_MANAGER',
  'BLOCK_MANAGER',
  'HR_MANAGER',
  'DOCTOR',
  'PHARMACIST',
  'RECEPTIONIST',
  'STAFF',
  'ASSOCIATE',
  'FAMILY',
] as const

export type RoleName = (typeof ROLE_NAMES)[number]

export const ROLE_ID_BY_NAME: Record<RoleName, number> = {
  SUPER_ADMIN: 1,
  ADMIN: 2,
  STATE_MANAGER: 3,
  DISTRICT_MANAGER: 4,
  BLOCK_MANAGER: 5,
  HR_MANAGER: 6,
  DOCTOR: 7,
  PHARMACIST: 8,
  RECEPTIONIST: 9,
  STAFF: 10,
  ASSOCIATE: 11,
  FAMILY: 12,
}

export type PermissionAction =
  | 'user:view'
  | 'user:create'
  | 'dashboard:view'
  | 'reception:view'
  | 'doctor:view'
  | 'billing:view'
  | 'pharmacy:view'

export const ROLE_PERMISSIONS: Record<RoleName, PermissionAction[]> = {
  SUPER_ADMIN: ['user:view', 'user:create', 'dashboard:view', 'reception:view', 'doctor:view', 'billing:view', 'pharmacy:view'],
  ADMIN: ['user:view', 'dashboard:view'],
  STATE_MANAGER: ['dashboard:view'],
  DISTRICT_MANAGER: ['dashboard:view'],
  BLOCK_MANAGER: ['dashboard:view'],
  HR_MANAGER: ['user:view', 'user:create', 'dashboard:view'],
  DOCTOR: ['doctor:view', 'dashboard:view'],
  PHARMACIST: ['pharmacy:view', 'dashboard:view'],
  RECEPTIONIST: ['reception:view', 'billing:view', 'dashboard:view'],
  STAFF: ['dashboard:view'],
  ASSOCIATE: ['dashboard:view'],
  FAMILY: ['dashboard:view'],
}
