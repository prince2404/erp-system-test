import { useQuery } from '@tanstack/react-query'
import api from '../lib/api'

export type DashboardMetrics = {
  totalRevenue: number
  totalActiveCenters: number
  totalPendingCommissions: number
  dailyOpdVisits: number
}

type ApiEnvelope<T> = {
  success: boolean
  message: string
  data: T
}

const getDashboardMetrics = async (): Promise<DashboardMetrics> => {
  const response = await api.get<ApiEnvelope<DashboardMetrics>>('/api/dashboard/metrics')
  return response.data.data
}

export const useDashboardMetrics = () => {
  return useQuery({
    queryKey: ['dashboard-metrics'],
    queryFn: getDashboardMetrics,
  })
}
