import { Activity, Building2, IndianRupee, Wallet } from 'lucide-react'
import DashboardMetricsChart from '../../components/dashboard/DashboardMetricsChart'
import ErrorState from '../../components/common/ErrorState'
import StatCard from '../../components/dashboard/StatCard'
import { useDashboardMetrics } from '../../hooks/useDashboardMetrics'

const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

const numberFormatter = new Intl.NumberFormat('en-IN')

/**
 * Dashboard landing page showing KPIs and trends.
 * Access: authenticated users with dashboard route access.
 */
const DashboardHomePage = () => {
  const { data, isLoading, isError, refetch, isRefetching } = useDashboardMetrics()

  if (isLoading) {
    return <p className="text-sm text-slate-600">Loading dashboard metrics...</p>
  }

  if (isError || !data) {
    return (
      <ErrorState
        message="Unable to load dashboard metrics. Please check your network connection and try again."
        onRetry={() => {
          void refetch()
        }}
      />
    )
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Total Revenue" value={currencyFormatter.format(data.totalRevenue)} icon={<IndianRupee size={18} />} />
        <StatCard
          title="Total Active Centers"
          value={numberFormatter.format(data.totalActiveCenters)}
          icon={<Building2 size={18} />}
        />
        <StatCard
          title="Pending Commissions"
          value={currencyFormatter.format(data.totalPendingCommissions)}
          icon={<Wallet size={18} />}
        />
        <StatCard title="Daily OPD Visits" value={numberFormatter.format(data.dailyOpdVisits)} icon={<Activity size={18} />} />
      </div>

      <DashboardMetricsChart totalRevenue={data.totalRevenue} dailyOpdVisits={data.dailyOpdVisits} />
      {isRefetching ? <p className="text-xs text-slate-500">Refreshing metrics...</p> : null}
    </div>
  )
}

export default DashboardHomePage
