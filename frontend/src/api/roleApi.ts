import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Role API: backend-driven role assignment endpoint.
 */
export const roleApi = {
  /** Returns only the roles the current user is allowed to assign. */
  getAssignableRoles: () =>
    requestApi<{ roles: string[] }>(apiClient.get(API_PATHS.roles.assignable)),
}
