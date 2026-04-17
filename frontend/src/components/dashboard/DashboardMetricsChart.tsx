import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

type ChartDataPoint = {
  day: string
  revenue: number
  visits: number
}

type DashboardMetricsChartProps = {
  totalRevenue: number
  dailyOpdVisits: number
}

const labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
const distribution = [0.12, 0.14, 0.13, 0.15, 0.16, 0.14, 0.16]

const createChartData = (totalRevenue: number, dailyOpdVisits: number): ChartDataPoint[] => {
  const revenueTotal = Math.max(totalRevenue, 0)
  const weeklyVisitsEstimate = Math.max(dailyOpdVisits * 7, 0)

  return labels.map((day, index) => ({
    day,
    revenue: Math.round(revenueTotal * distribution[index]),
    visits: Math.round(weeklyVisitsEstimate * distribution[index]),
  }))
}

const DashboardMetricsChart = ({ totalRevenue, dailyOpdVisits }: DashboardMetricsChartProps) => {
  const data = createChartData(totalRevenue, dailyOpdVisits)

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4">
        <h3 className="text-base font-semibold text-slate-900">Weekly Trend Snapshot</h3>
        <p className="text-sm text-slate-600">Revenue and OPD visit estimates derived from available dashboard totals.</p>
      </div>

      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="day" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="revenue" fill="#4f46e5" radius={[4, 4, 0, 0]} name="Revenue" />
            <Bar dataKey="visits" fill="#14b8a6" radius={[4, 4, 0, 0]} name="OPD Visits" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

export default DashboardMetricsChart
