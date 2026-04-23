import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Dashboard API for aggregate metric cards/charts.
 */
export const dashboardApi = {
  /** Fetches dashboard metrics for landing dashboard page. */
  getMetrics: () => requestApi<{ totalRevenue: number; totalActiveCenters: number; totalPendingCommissions: number; dailyOpdVisits: number }>(apiClient.get(API_PATHS.dashboard.metrics)),
}
