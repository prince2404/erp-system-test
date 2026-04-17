import type { ReactNode } from 'react'

type StatCardProps = {
  title: string
  value: string
  icon: ReactNode
  trend?: string
}

const StatCard = ({ title, value, icon, trend }: StatCardProps) => {
  return (
    <article className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-slate-600">{title}</p>
          <p className="mt-2 text-2xl font-semibold tracking-tight text-slate-900">{value}</p>
        </div>
        <div className="rounded-lg bg-indigo-50 p-2 text-indigo-600">{icon}</div>
      </div>

      {trend ? <p className="mt-3 text-xs font-medium text-emerald-600">{trend}</p> : null}
    </article>
  )
}

export default StatCard
