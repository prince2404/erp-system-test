import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { billingApi } from '../api/billingApi'

export type InvoiceItem = {
  id: number
  itemType: 'CONSULTATION' | 'MEDICINE'
  referenceId: number
  quantity: number
  unitPrice: string
  subtotal: string
}

export type Invoice = {
  id: number
  appointmentId: number
  familyId: number
  totalAmount: string
  paymentStatus: 'PENDING' | 'PAID' | 'REFUNDED'
  paymentMethod: 'WALLET' | 'CASH' | 'UPI' | null
  createdAt: string
  updatedAt: string
  items: InvoiceItem[]
}

export type InvoiceStatusFilter = 'ALL' | 'PENDING' | 'PAID'

export type WalletDetails = {
  id: number
  familyHeadName: string
  healthCardNumber: string
  walletBalance: string
}

export type CommissionRecord = {
  id: number
  invoiceId: number
  recipientUserId: number
  roleId: number
  amount: string
  percentageApplied: string
  status: 'PENDING' | 'SETTLED'
  createdAt: string
}

/**
 * Unwraps normalized API results and raises meaningful query errors.
 */
const unwrapApiResult = <T>(result: { data: T | null; error: string | null }): T => {
  if (result.error || result.data === null) {
    throw new Error(result.error ?? 'Unexpected API error')
  }

  return result.data
}

/**
 * Billing invoices query with optional status filtering.
 */
export const useInvoices = (status: InvoiceStatusFilter = 'ALL') =>
  useQuery({
    queryKey: ['billing', 'invoices', status],
    queryFn: async () =>
      unwrapApiResult<Invoice[]>(
        (await billingApi.getInvoices(status)) as { data: Invoice[] | null; error: string | null },
      ),
  })

/**
 * Single invoice query.
 */
export const useInvoice = (invoiceId?: number) =>
  useQuery({
    queryKey: ['billing', 'invoice', invoiceId],
    queryFn: async () =>
      unwrapApiResult<Invoice>((await billingApi.getInvoice(invoiceId ?? 0)) as { data: Invoice | null; error: string | null }),
    enabled: typeof invoiceId === 'number' && Number.isFinite(invoiceId),
  })

/**
 * Wallet details query by health card number.
 */
export const useWallet = (healthCardNumber?: string) =>
  useQuery({
    queryKey: ['billing', 'wallet', healthCardNumber],
    queryFn: async () =>
      unwrapApiResult<WalletDetails>(
        (await billingApi.getWallet(healthCardNumber ?? '')) as { data: WalletDetails | null; error: string | null },
      ),
    enabled: Boolean(healthCardNumber),
  })

/**
 * Commission records query by user id.
 */
export const useCommissions = (userId?: number) =>
  useQuery({
    queryKey: ['billing', 'commissions', userId],
    queryFn: async () =>
      unwrapApiResult<CommissionRecord[]>(
        (await billingApi.getCommissions(userId ?? 0)) as { data: CommissionRecord[] | null; error: string | null },
      ),
    enabled: typeof userId === 'number' && Number.isFinite(userId),
  })

/**
 * Invoice payment mutation.
 */
export const usePayInvoice = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { invoiceId: number; paymentMethod: 'WALLET' | 'CASH' | 'UPI' }) =>
      unwrapApiResult(await billingApi.payInvoice(payload)),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['billing', 'invoices'] })
      queryClient.invalidateQueries({ queryKey: ['billing', 'invoice', variables.invoiceId] })
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

/**
 * Wallet top-up mutation.
 */
export const useTopUpWallet = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { healthCardNumber: string; amount: number; referenceId?: string; description?: string }) =>
      unwrapApiResult(await billingApi.topUpWallet(payload)),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
      queryClient.invalidateQueries({ queryKey: ['billing', 'wallet', variables.healthCardNumber] })
    },
  })
}
