import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { pharmacyApi } from '../api/pharmacyApi'

export type Medicine = {
  id: number
  name: string
  genericName: string
  manufacturer: string
  createdAt: string
  updatedAt: string
}

export type InventoryBatch = {
  id: number
  medicineId: number
  medicineName: string
  vendorId: number
  centerId: number
  batchNumber: string
  expiryDate: string
  quantityReceived: number
  quantityAvailable: number
  unitPrice: string
  sellingPrice: string
  createdAt: string
}

/**
 * Unwraps normalized API results and raises errors for react-query.
 */
const unwrapApiResult = <T>(result: { data: T | null; error: string | null }): T => {
  if (result.error || result.data === null) {
    throw new Error(result.error ?? 'Unexpected API error')
  }

  return result.data
}

/**
 * Medicines query.
 */
export const useMedicines = () =>
  useQuery({
    queryKey: ['pharmacy', 'medicines'],
    queryFn: async () =>
      unwrapApiResult<Medicine[]>(
        (await pharmacyApi.getMedicines()) as { data: Medicine[] | null; error: string | null },
      ),
  })

/**
 * Inventory query by center id.
 */
export const useInventory = (centerId?: number | null) =>
  useQuery({
    queryKey: ['pharmacy', 'inventory', centerId],
    queryFn: async () =>
      unwrapApiResult<InventoryBatch[]>(
        (await pharmacyApi.getInventory(centerId ?? 0)) as { data: InventoryBatch[] | null; error: string | null },
      ),
    enabled: typeof centerId === 'number' && Number.isFinite(centerId),
  })

/**
 * Medicine creation mutation.
 */
export const useCreateMedicine = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { name: string; genericName: string; manufacturer: string }) =>
      unwrapApiResult(await pharmacyApi.createMedicine(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'medicines'] })
    },
  })
}

/**
 * Stock batch creation mutation.
 */
export const useAddStock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: {
      medicineId: number
      vendorId: number
      centerId: number
      batchNumber: string
      expiryDate: string
      quantityReceived: number
      unitPrice: number
      sellingPrice: number
    }) => unwrapApiResult(await pharmacyApi.addStock(payload)),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'inventory', variables.centerId] })
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'medicines'] })
    },
  })
}
