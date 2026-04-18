import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'
import type { ApiEnvelope } from './useAdminData'

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

const getInvoices = async (status: InvoiceStatusFilter) => {
  const response = await api.get<ApiEnvelope<Invoice[]>>('/api/billing/invoices', {
    params: status === 'ALL' ? undefined : { status },
  })
  return response.data.data
}

const getInvoice = async (invoiceId: number) => {
  const response = await api.get<ApiEnvelope<Invoice>>(`/api/billing/invoices/${invoiceId}`)
  return response.data.data
}

const getWallet = async (healthCardNumber: string) => {
  const response = await api.get<ApiEnvelope<WalletDetails>>(`/api/v1/families/${healthCardNumber}`)
  return response.data.data
}

const getCommissions = async (userId: number) => {
  const response = await api.get<ApiEnvelope<CommissionRecord[]>>(`/api/commissions/user/${userId}`)
  return response.data.data
}

export const useInvoices = (status: InvoiceStatusFilter = 'ALL') =>
  useQuery({
    queryKey: ['billing', 'invoices', status],
    queryFn: () => getInvoices(status),
  })

export const useInvoice = (invoiceId?: number) =>
  useQuery({
    queryKey: ['billing', 'invoice', invoiceId],
    queryFn: () => getInvoice(invoiceId ?? 0),
    enabled: typeof invoiceId === 'number' && Number.isFinite(invoiceId),
  })

export const useWallet = (healthCardNumber?: string) =>
  useQuery({
    queryKey: ['billing', 'wallet', healthCardNumber],
    queryFn: () => getWallet(healthCardNumber ?? ''),
    enabled: Boolean(healthCardNumber),
  })

export const useCommissions = (userId?: number) =>
  useQuery({
    queryKey: ['billing', 'commissions', userId],
    queryFn: () => getCommissions(userId ?? 0),
    enabled: typeof userId === 'number' && Number.isFinite(userId),
  })

export const usePayInvoice = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { invoiceId: number; paymentMethod: 'WALLET' | 'CASH' | 'UPI' }) =>
      api.post(`/api/billing/invoices/${payload.invoiceId}/pay`, { paymentMethod: payload.paymentMethod }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['billing', 'invoices'] })
      queryClient.invalidateQueries({ queryKey: ['billing', 'invoice', variables.invoiceId] })
      queryClient.invalidateQueries({ queryKey: ['families'] })
    },
  })
}

export const useTopUpWallet = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { healthCardNumber: string; amount: number; referenceId?: string; description?: string }) =>
      api.post('/api/v1/wallet/credit', {
        healthCardNumber: payload.healthCardNumber,
        amount: payload.amount,
        referenceId: payload.referenceId ?? `wallet-topup-${Date.now()}`,
        description: payload.description ?? 'Wallet top-up from billing UI',
      }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
      queryClient.invalidateQueries({ queryKey: ['billing', 'wallet', variables.healthCardNumber] })
    },
  })
}
