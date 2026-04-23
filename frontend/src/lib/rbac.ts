import { ROLE_ID_BY_NAME, ROLE_NAMES, type RoleName } from '../constants/roles'
import { permissionService } from '../services/permissionService'

export { ROLE_ID_BY_NAME, ROLE_NAMES, type RoleName }

/**
 * Legacy role helper retained for compatibility; prefer hasPermission('user:create').
 */
export const canCreateState = (role?: string | null) => permissionService.hasPermission(role, 'user:create')

/**
 * Legacy role helper retained for compatibility; prefer hasPermission('user:view').
 */
export const canAccessUsers = (role?: string | null) => permissionService.hasPermission(role, 'user:view')

/**
 * Legacy role helper retained for compatibility.
 */
export const canCreateDistrict = (role?: string | null) => permissionService.hasPermission(role, 'user:create')

/**
 * Legacy role helper retained for compatibility.
 */
export const canCreateBlock = (role?: string | null) => permissionService.hasPermission(role, 'user:create')

/**
 * Legacy role helper retained for compatibility.
 */
export const canCreateCenter = (role?: string | null) => permissionService.hasPermission(role, 'user:create')
