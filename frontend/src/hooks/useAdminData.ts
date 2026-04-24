import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../api/adminApi'
import { userApi } from '../api/userApi'

/**
 * Shared API envelope type for compatibility with existing type imports.
 */
export type ApiEnvelope<T> = {
  success: boolean
  message: string
  data: T
}

export type StateItem = {
  id: number
  name: string
  code: string
}

export type DistrictItem = {
  id: number
  name: string
  stateId: number
}

export type BlockItem = {
  id: number
  name: string
  districtId: number
}

export type CenterItem = {
  id: number
  name: string
  centerCode: string
  type: 'CLINIC' | 'HOSPITAL'
  blockId: number
  address: string
  contactNumber: string
}

export type UserProfile = {
  id: number
  username: string
  role: string
  assignedCenterId: number | null
}

export type UserSummary = {
  id: number
  username: string
  email: string
  phone: string
  role: string
  status: string
}

type UserPage = {
  content: UserSummary[]
}

type ManagedUserPayload = {
  username: string
  password: string
  email: string
  phone?: string
  role: string
  assignedStateId?: number
  assignedDistrictId?: number
  assignedBlockId?: number
  assignedCenterId?: number
}

/**
 * Unwraps normalized API results and throws for react-query error handling.
 */
const unwrapApiResult = <T,>(result: { data: T | null; error: string | null }): T => {
  if (result.error || result.data === null) {
    throw new Error(result.error ?? 'Unexpected API error')
  }

  return result.data
}

/**
 * Provides current user query data.
 */
export const useCurrentUser = () =>
  useQuery({
    queryKey: ['current-user'],
    queryFn: async () => unwrapApiResult<UserProfile>(await userApi.getCurrentUser()),
  })

/**
 * Provides states query data.
 */
export const useStates = () =>
  useQuery({
    queryKey: ['states'],
    queryFn: async () => unwrapApiResult<StateItem[]>(await adminApi.getStates()),
  })

/**
 * Provides districts query data.
 */
export const useDistricts = () =>
  useQuery({
    queryKey: ['districts'],
    queryFn: async () => unwrapApiResult<DistrictItem[]>(await adminApi.getDistricts()),
  })

/**
 * Provides blocks query data.
 */
export const useBlocks = () =>
  useQuery({
    queryKey: ['blocks'],
    queryFn: async () => unwrapApiResult<BlockItem[]>(await adminApi.getBlocks()),
  })

/**
 * Provides centers query data.
 */
export const useCenters = () =>
  useQuery({
    queryKey: ['centers'],
    queryFn: async () => unwrapApiResult<CenterItem[]>(await adminApi.getCenters()),
  })

/**
 * Provides users query data.
 */
export const useUsers = () =>
  useQuery({
    queryKey: ['users'],
    queryFn: async () => {
      const result = await userApi.getUsers()
      const page = unwrapApiResult<UserPage>(result)
      return page.content
    },
  })

/**
 * Mutation hook for state creation.
 */
export const useCreateState = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { name: string; code: string }) => unwrapApiResult(await adminApi.createState(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['states'] })
    },
  })
}

/**
 * Mutation hook for district creation.
 */
export const useCreateDistrict = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { name: string; stateId: number }) => unwrapApiResult(await adminApi.createDistrict(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['districts'] })
    },
  })
}

/**
 * Mutation hook for block creation.
 */
export const useCreateBlock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: { name: string; districtId: number }) => unwrapApiResult(await adminApi.createBlock(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['blocks'] })
    },
  })
}

/**
 * Mutation hook for center creation.
 */
export const useCreateCenter = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: {
      name: string
      centerCode: string
      type: 'CLINIC' | 'HOSPITAL'
      blockId: number
      address: string
      contactNumber: string
    }) => unwrapApiResult(await adminApi.createCenter(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['centers'] })
    },
  })
}

/**
 * Mutation hook for user creation using managed-user endpoint.
 */
export const useCreateUser = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (payload: ManagedUserPayload) => unwrapApiResult(await userApi.createManagedUser(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}
