import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Auth API: login and registration requests.
 */
export const authApi = {
  /** Executes user login and returns token payload. */
  login: (payload: { username: string; password: string }) =>
    requestApi<{ token: string; refreshToken?: string }>(apiClient.post(API_PATHS.auth.login, payload)),

  /** Registers a new user account. */
  register: (payload: {
    username: string
    password: string
    email: string
    phone?: string
    roleId: number
    assignedStateId?: number
    assignedDistrictId?: number
    assignedBlockId?: number
    assignedCenterId?: number
  }) => requestApi<unknown>(apiClient.post(API_PATHS.auth.register, payload)),
}
