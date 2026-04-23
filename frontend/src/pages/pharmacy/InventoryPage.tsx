import { useMemo, useState } from 'react'
import ErrorState from '../../components/common/ErrorState'
import Loader from '../../components/common/Loader'
import { useCenters, useCurrentUser } from '../../hooks/useAdminData'
import { usePermission } from '../../hooks/usePermission'
import { useAddStock, useInventory, useMedicines } from '../../hooks/usePharmacyData'
import AddStockModal from './components/AddStockModal'
import InventoryTable from './components/InventoryTable'
import { toLocalIsoDate } from '../../utils/dateUtils'

const TODAY_LOCAL_ISO_DATE = toLocalIsoDate(new Date())

/**
 * Inventory page for stock monitoring and batch creation.
 * Access: requires `pharmacy:view` permission.
 */
const InventoryPage = () => {
  const { hasPermission } = usePermission()
  const { data: currentUser } = useCurrentUser()
  const { data: centers = [] } = useCenters()
  const medicinesQuery = useMedicines()

  const defaultCenterId = useMemo(
    () => currentUser?.assignedCenterId ?? centers[0]?.id ?? null,
    [centers, currentUser?.assignedCenterId],
  )

  const [selectedCenterId, setSelectedCenterId] = useState<number | null>(null)
  const [isAddStockOpen, setIsAddStockOpen] = useState(false)
  const activeCenterId = selectedCenterId ?? defaultCenterId
  const inventoryQuery = useInventory(activeCenterId)
  const addStock = useAddStock()

  if (!hasPermission('pharmacy:view')) {
    return <ErrorState message="You do not have permission to access inventory management." />
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold text-slate-900">Center Stock Management</h2>
        <div className="flex flex-wrap items-center gap-2">
          <label className="text-sm text-slate-700">
            Center
            <select
              value={activeCenterId ?? ''}
              onChange={(event) => setSelectedCenterId(event.target.value ? Number(event.target.value) : null)}
              className="ml-2 rounded-md border border-slate-300 px-2 py-1 text-sm"
              disabled={Boolean(currentUser?.assignedCenterId)}
            >
              {centers.map((center) => (
                <option key={center.id} value={center.id}>
                  {center.name}
                </option>
              ))}
            </select>
          </label>
          <button
            type="button"
            onClick={() => setIsAddStockOpen(true)}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white"
          >
            Add Stock
          </button>
        </div>
      </div>

      {inventoryQuery.isLoading ? <Loader message="Loading inventory batches..." /> : null}
      {inventoryQuery.isError ? (
        <ErrorState
          message="Unable to load inventory batches."
          onRetry={() => {
            void inventoryQuery.refetch()
          }}
        />
      ) : null}
      {!inventoryQuery.isLoading && !inventoryQuery.isError ? (
        <InventoryTable batches={inventoryQuery.data ?? []} todayIsoDate={TODAY_LOCAL_ISO_DATE} />
      ) : null}

      <p className="text-xs text-slate-600">
        Highlight legend: <span className="font-medium text-amber-700">low stock</span> (qty &lt; 50),{' '}
        <span className="font-medium text-rose-700">expired</span>.
      </p>

      <AddStockModal
        isOpen={isAddStockOpen}
        onClose={() => setIsAddStockOpen(false)}
        medicines={medicinesQuery.data ?? []}
        minExpiryDate={TODAY_LOCAL_ISO_DATE}
        isSubmitting={addStock.isPending}
        onSubmitStock={async (payload) => {
          if (!activeCenterId) {
            return
          }

          await addStock.mutateAsync({ ...payload, centerId: activeCenterId })
        }}
      />
    </div>
  )
}

export default InventoryPage
