import { useQuery } from '@tanstack/react-query'
import { dashboardApi } from '../api/dashboardApi'

export type DashboardMetrics = {
  totalRevenue: number
  totalActiveCenters: number
  totalPendingCommissions: number
  dailyOpdVisits: number
}

/**
 * Dashboard metrics query used by home dashboard page.
 */
export const useDashboardMetrics = () => {
  return useQuery({
    queryKey: ['dashboard-metrics'],
    queryFn: async (): Promise<DashboardMetrics> => {
      const result = await dashboardApi.getMetrics()
      if (result.error || !result.data) {
        throw new Error(result.error ?? 'Unable to load dashboard metrics')
      }

      return result.data
    },
  })
}
