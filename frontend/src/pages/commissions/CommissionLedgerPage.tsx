import { useMemo } from 'react'
import DataTable, { type Column } from '../../components/common/DataTable'
import { useCurrentUser } from '../../hooks/useAdminData'
import { useCommissions, type CommissionRecord } from '../../hooks/useBillingData'

const formatCurrency = (value: string | number) => `₹${Number(value).toFixed(2)}`

const columns: Column<CommissionRecord>[] = [
  { key: 'invoice', header: 'Invoice #', accessor: (record) => record.invoiceId },
  { key: 'amount', header: 'Amount', accessor: (record) => formatCurrency(record.amount) },
  {
    key: 'status',
    header: 'Status',
    accessor: (record) => (record.status === 'SETTLED' ? 'PAID' : record.status),
  },
  {
    key: 'date',
    header: 'Date',
    accessor: (record) => new Date(record.createdAt).toLocaleDateString(),
  },
]

const CommissionLedgerPage = () => {
  const { data: currentUser } = useCurrentUser()
  const { data: commissions = [], isLoading } = useCommissions(currentUser?.id)

  const totals = useMemo(() => {
    const totalEarned = commissions.reduce((sum, item) => sum + Number(item.amount), 0)
    const totalPending = commissions
      .filter((item) => item.status === 'PENDING')
      .reduce((sum, item) => sum + Number(item.amount), 0)

    return {
      totalEarned,
      totalPending,
    }
  }, [commissions])

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-semibold text-slate-900">Commission Ledger</h2>

      <div className="grid gap-3 md:grid-cols-2">
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <p className="text-xs uppercase tracking-wide text-slate-500">Total Earned</p>
          <p className="mt-1 text-2xl font-semibold text-slate-900">{formatCurrency(totals.totalEarned)}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <p className="text-xs uppercase tracking-wide text-slate-500">Total Pending</p>
          <p className="mt-1 text-2xl font-semibold text-amber-700">{formatCurrency(totals.totalPending)}</p>
        </div>
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading commission records...</p> : null}
      {!isLoading ? (
        <DataTable
          columns={columns}
          rows={commissions}
          getRowKey={(record) => record.id}
          emptyText="No commission entries found for your account."
        />
      ) : null}
    </div>
  )
}

export default CommissionLedgerPage
