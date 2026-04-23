import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * User API functions for current user profile and user listing.
 */
export const userApi = {
  /** Fetches currently authenticated user profile. */
  getCurrentUser: () => requestApi<{ id: number; username: string; role: string; assignedCenterId: number | null }>(apiClient.get(API_PATHS.users.me)),

  /** Fetches paginated users and returns content array. */
  getUsers: () => requestApi<{ content: Array<{ id: number; username: string; email: string; phone: string; role: string }> }>(apiClient.get(`${API_PATHS.users.list}?page=0&size=200`)),
}
