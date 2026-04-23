import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Pharmacy API for medicine catalog and stock/inventory operations.
 */
export const pharmacyApi = {
  /** Fetches medicines catalog. */
  getMedicines: () => requestApi<Array<unknown>>(apiClient.get(API_PATHS.pharmacy.medicines)),

  /** Fetches inventory batches by center id. */
  getInventory: (centerId: number) =>
    requestApi<Array<unknown>>(apiClient.get(`${API_PATHS.pharmacy.centerBatches}/${centerId}/batches`)),

  /** Creates medicine entry. */
  createMedicine: (payload: { name: string; genericName: string; manufacturer: string }) =>
    requestApi<unknown>(apiClient.post(API_PATHS.pharmacy.medicines, payload)),

  /** Adds stock batch. */
  addStock: (payload: {
    medicineId: number
    vendorId: number
    centerId: number
    batchNumber: string
    expiryDate: string
    quantityReceived: number
    unitPrice: number
    sellingPrice: number
  }) => requestApi<unknown>(apiClient.post(API_PATHS.pharmacy.batches, payload)),
}
