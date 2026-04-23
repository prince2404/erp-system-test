import EmptyState from '../../../components/common/EmptyState'
import type { InventoryBatch } from '../../../hooks/usePharmacyData'

type InventoryTableProps = {
  batches: InventoryBatch[]
  todayIsoDate: string
}

/**
 * Inventory list table with visual status highlights.
 */
const InventoryTable = ({ batches, todayIsoDate }: InventoryTableProps) => {
  if (batches.length === 0) {
    return <EmptyState message="No inventory batches found." />
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full divide-y divide-slate-200 text-sm">
        <thead className="bg-slate-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Medicine Name</th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Batch Number</th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Quantity</th>
            <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Expiry Date</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {batches.map((batch) => {
            const isExpired = batch.expiryDate < todayIsoDate
            const isLowStock = batch.quantityAvailable < 50
            const rowClassName = isExpired ? 'bg-rose-50' : isLowStock ? 'bg-amber-50' : 'hover:bg-slate-50'

            return (
              <tr key={batch.id} className={rowClassName}>
                <td className="px-4 py-3 text-slate-700">{batch.medicineName}</td>
                <td className="px-4 py-3 text-slate-700">{batch.batchNumber}</td>
                <td className="px-4 py-3 text-slate-700">{batch.quantityAvailable}</td>
                <td className="px-4 py-3 text-slate-700">{batch.expiryDate}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

export default InventoryTable
