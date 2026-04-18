import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'
import type { ApiEnvelope } from './useAdminData'

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

const getMedicines = async () => {
  const response = await api.get<ApiEnvelope<Medicine[]>>('/api/inventory/medicines')
  return response.data.data
}

const getInventory = async (centerId: number) => {
  const response = await api.get<ApiEnvelope<InventoryBatch[]>>(`/api/inventory/centers/${centerId}/batches`)
  return response.data.data
}

export const useMedicines = () =>
  useQuery({
    queryKey: ['pharmacy', 'medicines'],
    queryFn: getMedicines,
  })

export const useInventory = (centerId?: number | null) =>
  useQuery({
    queryKey: ['pharmacy', 'inventory', centerId],
    queryFn: () => getInventory(centerId ?? 0),
    enabled: typeof centerId === 'number' && Number.isFinite(centerId),
  })

export const useCreateMedicine = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { name: string; genericName: string; manufacturer: string }) =>
      api.post('/api/inventory/medicines', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'medicines'] })
    },
  })
}

export const useAddStock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: {
      medicineId: number
      vendorId: number
      centerId: number
      batchNumber: string
      expiryDate: string
      quantityReceived: number
      unitPrice: number
      sellingPrice: number
    }) =>
      api.post('/api/inventory/batches', {
        medicineId: payload.medicineId,
        vendorId: payload.vendorId,
        centerId: payload.centerId,
        batchNumber: payload.batchNumber,
        expiryDate: payload.expiryDate,
        quantityReceived: payload.quantityReceived,
        unitPrice: payload.unitPrice,
        sellingPrice: payload.sellingPrice,
      }),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'inventory', variables.centerId] })
      queryClient.invalidateQueries({ queryKey: ['pharmacy', 'medicines'] })
    },
  })
}
