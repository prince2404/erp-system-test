import type { PermissionAction } from '../constants/roles'
import { useAuth } from './useAuth'

/**
 * Hook for permission-driven rendering without direct role checks in JSX.
 */
export const usePermission = () => {
  const { hasPermission } = useAuth()

  return {
    hasPermission: (action: PermissionAction) => hasPermission(action),
  }
}
