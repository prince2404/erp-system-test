import { Activity, Building2, IndianRupee, Wallet } from 'lucide-react'
import DashboardMetricsChart from '../components/dashboard/DashboardMetricsChart'
import StatCard from '../components/dashboard/StatCard'
import { useDashboardMetrics } from '../hooks/useDashboardMetrics'

const currencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
})

const numberFormatter = new Intl.NumberFormat('en-IN')

const LoadingSkeleton = () => {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, index) => (
          <div key={index} className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="h-4 w-24 rounded bg-slate-200" />
            <div className="mt-3 h-8 w-32 rounded bg-slate-200" />
            <div className="mt-3 h-3 w-20 rounded bg-slate-100" />
          </div>
        ))}
      </div>

      <div className="h-80 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="h-full rounded bg-slate-100" />
      </div>
    </div>
  )
}

const DashboardHomePage = () => {
  const { data, isLoading, isError, refetch, isRefetching } = useDashboardMetrics()

  if (isLoading) {
    return <LoadingSkeleton />
  }

  if (isError || !data) {
    return (
      <div className="rounded-xl border border-red-100 bg-red-50 p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-red-800">Unable to load dashboard metrics</h2>
        <p className="mt-2 text-sm text-red-700">
          Please check your network connection or login session and try again.
        </p>
        <button
          type="button"
          onClick={() => refetch()}
          disabled={isRefetching}
          className="mt-4 rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isRefetching ? 'Retrying...' : 'Retry'}
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Total Revenue"
          value={currencyFormatter.format(data.totalRevenue)}
          icon={<IndianRupee size={18} />}
        />
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
        <StatCard
          title="Daily OPD Visits"
          value={numberFormatter.format(data.dailyOpdVisits)}
          icon={<Activity size={18} />}
        />
      </div>

      <DashboardMetricsChart
        totalRevenue={data.totalRevenue}
        dailyOpdVisits={data.dailyOpdVisits}
      />
    </div>
  )
}

export default DashboardHomePage
