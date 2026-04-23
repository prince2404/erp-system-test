import type { PermissionAction, RoleName } from '../constants/roles'
import { ROLE_PERMISSIONS } from '../constants/roles'

/**
 * Evaluates role-based permission checks for UI access decisions.
 */
export const permissionService = {
  /** Returns true when role grants a given action permission. */
  hasPermission: (role: string | null | undefined, action: PermissionAction) => {
    if (!role) {
      return false
    }

    const rolePermissions = ROLE_PERMISSIONS[role as RoleName] ?? []
    return rolePermissions.includes(action)
  },
}
