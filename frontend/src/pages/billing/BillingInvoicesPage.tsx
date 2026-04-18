import { useMemo, useState } from 'react'
import { isAxiosError } from 'axios'
import { Link } from 'react-router-dom'
import DataTable, { type Column } from '../../components/common/DataTable'
import { useFamilies } from '../../hooks/useClinicalData'
import {
  useInvoice,
  useInvoices,
  usePayInvoice,
  useWallet,
  type Invoice,
  type InvoiceStatusFilter,
} from '../../hooks/useBillingData'

const formatCurrency = (value: string | number) => `₹${Number(value).toFixed(2)}`

const columns: Column<Invoice>[] = [
  { key: 'id', header: 'Invoice #', accessor: (invoice) => invoice.id },
  { key: 'appointment', header: 'Appointment #', accessor: (invoice) => invoice.appointmentId },
  { key: 'family', header: 'Family #', accessor: (invoice) => invoice.familyId },
  { key: 'status', header: 'Status', accessor: (invoice) => invoice.paymentStatus },
  { key: 'total', header: 'Total', accessor: (invoice) => formatCurrency(invoice.totalAmount) },
]

const itemLabelByType = {
  CONSULTATION: 'Consultation fee',
  MEDICINE: 'Medicine',
} as const

const BillingInvoicesPage = () => {
  const [statusFilter, setStatusFilter] = useState<InvoiceStatusFilter>('ALL')
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<number | undefined>()
  const [paymentError, setPaymentError] = useState<string | null>(null)
  const [insufficientWallet, setInsufficientWallet] = useState(false)

  const { data: invoices = [], isLoading } = useInvoices(statusFilter)
  const { data: families = [] } = useFamilies()
  const payInvoice = usePayInvoice()

  const effectiveInvoiceId = useMemo(() => {
    if (selectedInvoiceId && invoices.some((item) => item.id === selectedInvoiceId)) {
      return selectedInvoiceId
    }

    return invoices[0]?.id
  }, [invoices, selectedInvoiceId])
  const { data: invoice } = useInvoice(effectiveInvoiceId)

  const selectedFamily = useMemo(
    () => families.find((family) => family.id === invoice?.familyId),
    [families, invoice?.familyId],
  )

  const walletQuery = useWallet(selectedFamily?.healthCardNumber)

  const onPayWallet = async () => {
    if (!invoice) {
      return
    }

    setPaymentError(null)
    setInsufficientWallet(false)

    try {
      await payInvoice.mutateAsync({ invoiceId: invoice.id, paymentMethod: 'WALLET' })
    } catch (error) {
      const apiMessage = isAxiosError(error) ? (error.response?.data as { message?: string } | undefined)?.message : undefined
      const message = apiMessage ?? (error instanceof Error ? error.message : 'Unable to process wallet payment.')
      const hasInsufficientBalance = message.toLowerCase().includes('insufficient')
      setInsufficientWallet(hasInsufficientBalance)
      setPaymentError(hasInsufficientBalance ? 'Wallet balance is insufficient for this payment.' : message)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-xl font-semibold text-slate-900">Invoices Dashboard</h2>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          Status
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as InvoiceStatusFilter)}
            className="rounded-md border border-slate-300 px-2 py-1"
          >
            <option value="ALL">All</option>
            <option value="PENDING">Pending</option>
            <option value="PAID">Paid</option>
          </select>
        </label>
      </div>

      {isLoading ? <p className="text-sm text-slate-600">Loading invoices...</p> : null}
      {!isLoading ? <DataTable columns={columns} rows={invoices} getRowKey={(invoiceRow) => invoiceRow.id} /> : null}

      {invoices.length > 0 ? (
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <label className="mb-2 block text-sm font-medium text-slate-700">Select Invoice</label>
          <select
            value={effectiveInvoiceId ?? ''}
            onChange={(event) => setSelectedInvoiceId(Number(event.target.value))}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm md:max-w-md"
          >
            {invoices.map((invoiceRow) => (
              <option key={invoiceRow.id} value={invoiceRow.id}>
                #{invoiceRow.id} · Appointment #{invoiceRow.appointmentId} · {invoiceRow.paymentStatus}
              </option>
            ))}
          </select>
        </div>
      ) : null}

      {invoice ? (
        <div className="space-y-4 rounded-lg border border-slate-200 bg-white p-4">
          <div className="grid gap-2 text-sm text-slate-700 md:grid-cols-2">
            <p>
              <span className="font-medium">Invoice:</span> #{invoice.id}
            </p>
            <p>
              <span className="font-medium">Appointment:</span> #{invoice.appointmentId}
            </p>
            <p>
              <span className="font-medium">Status:</span> {invoice.paymentStatus}
            </p>
            <p>
              <span className="font-medium">Total:</span> {formatCurrency(invoice.totalAmount)}
            </p>
            <p>
              <span className="font-medium">Family Wallet:</span>{' '}
              {walletQuery.data ? formatCurrency(walletQuery.data.walletBalance) : 'Unavailable'}
            </p>
          </div>

          <div className="overflow-x-auto rounded-md border border-slate-200">
            <table className="min-w-full text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase text-slate-600">Item</th>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase text-slate-600">Qty</th>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase text-slate-600">Unit Price</th>
                  <th className="px-3 py-2 text-left text-xs font-semibold uppercase text-slate-600">Subtotal</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {invoice.items.map((item) => (
                  <tr key={item.id}>
                    <td className="px-3 py-2 text-slate-700">{itemLabelByType[item.itemType]}</td>
                    <td className="px-3 py-2 text-slate-700">{item.quantity}</td>
                    <td className="px-3 py-2 text-slate-700">{formatCurrency(item.unitPrice)}</td>
                    <td className="px-3 py-2 text-slate-700">{formatCurrency(item.subtotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={onPayWallet}
              disabled={payInvoice.isPending || invoice.paymentStatus !== 'PENDING'}
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
            >
              {payInvoice.isPending ? 'Processing...' : 'Pay with Family Wallet'}
            </button>
            {invoice.paymentStatus === 'PAID' ? <p className="text-sm text-emerald-700">Invoice already paid.</p> : null}
          </div>

          {paymentError ? <p className="text-sm text-red-600">{paymentError}</p> : null}
          {insufficientWallet && selectedFamily ? (
            <p className="rounded-md border border-amber-300 bg-amber-50 p-3 text-sm text-amber-800">
              Insufficient wallet balance.{' '}
              <Link
                to={`/billing/wallet-topup?healthCardNumber=${encodeURIComponent(selectedFamily.healthCardNumber)}&invoiceId=${invoice.id}`}
                className="font-medium underline"
              >
                Top-up wallet now
              </Link>
              .
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  )
}

export default BillingInvoicesPage
