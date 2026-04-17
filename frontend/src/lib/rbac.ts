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

export const canCreateState = (role?: string | null) => role === 'SUPER_ADMIN'

export const canAccessUsers = (role?: string | null) =>
  role === 'SUPER_ADMIN' || role === 'ADMIN' || role === 'HR_MANAGER'

export const canCreateDistrict = (role?: string | null) =>
  role === 'SUPER_ADMIN' || role === 'ADMIN' || role === 'STATE_MANAGER'

export const canCreateBlock = (role?: string | null) =>
  canCreateDistrict(role) || role === 'DISTRICT_MANAGER'

export const canCreateCenter = (role?: string | null) => canCreateBlock(role) || role === 'BLOCK_MANAGER'
