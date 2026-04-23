import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Billing API for invoices, wallet, and commissions.
 */
export const billingApi = {
  /** Fetches invoices by optional status filter. */
  getInvoices: (status: 'ALL' | 'PENDING' | 'PAID') =>
    requestApi<Array<unknown>>(apiClient.get(API_PATHS.billing.invoices, { params: status === 'ALL' ? undefined : { status } })),

  /** Fetches invoice details. */
  getInvoice: (invoiceId: number) => requestApi<unknown>(apiClient.get(`${API_PATHS.billing.invoices}/${invoiceId}`)),

  /** Fetches wallet by family health card number. */
  getWallet: (healthCardNumber: string) => requestApi<unknown>(apiClient.get(`/api/v1/families/${healthCardNumber}`)),

  /** Fetches user commission records. */
  getCommissions: (userId: number) => requestApi<Array<unknown>>(apiClient.get(`${API_PATHS.billing.commissionsByUser}/${userId}`)),

  /** Pays an invoice by payment method. */
  payInvoice: (payload: { invoiceId: number; paymentMethod: 'WALLET' | 'CASH' | 'UPI' }) =>
    requestApi<unknown>(apiClient.post(`${API_PATHS.billing.invoices}/${payload.invoiceId}/pay`, { paymentMethod: payload.paymentMethod })),

  /** Credits wallet balance for a family card. */
  topUpWallet: (payload: { healthCardNumber: string; amount: number; referenceId?: string; description?: string }) =>
    requestApi<unknown>(
      apiClient.post(API_PATHS.billing.walletCredit, {
        healthCardNumber: payload.healthCardNumber,
        amount: payload.amount,
        referenceId: payload.referenceId ?? `wallet-topup-${Date.now()}`,
        description: payload.description ?? 'Wallet top-up from billing UI',
      }),
    ),
}
