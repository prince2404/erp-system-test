import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import api from '../lib/api'

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
  username: string
  role: string
}

export type UserSummary = {
  id: number
  username: string
  email: string
  phone: string
  role: string
}

type UserPage = {
  content: UserSummary[]
}

type RegisterPayload = {
  username: string
  password: string
  email: string
  phone?: string
  roleId: number
  assignedStateId?: number
  assignedDistrictId?: number
  assignedBlockId?: number
  assignedCenterId?: number
}

const getStates = async () => {
  const response = await api.get<ApiEnvelope<StateItem[]>>('/api/v1/states')
  return response.data.data
}

const getDistricts = async () => {
  const response = await api.get<ApiEnvelope<DistrictItem[]>>('/api/v1/districts')
  return response.data.data
}

const getBlocks = async () => {
  const response = await api.get<ApiEnvelope<BlockItem[]>>('/api/v1/blocks')
  return response.data.data
}

const getCenters = async () => {
  const response = await api.get<ApiEnvelope<CenterItem[]>>('/api/v1/centers')
  return response.data.data
}

const getUsers = async () => {
  const response = await api.get<ApiEnvelope<UserPage>>('/api/v1/users?page=0&size=200')
  return response.data.data.content
}

const getCurrentUser = async () => {
  const response = await api.get<ApiEnvelope<UserProfile>>('/api/v1/users/me')
  return response.data.data
}

export const useCurrentUser = () =>
  useQuery({
    queryKey: ['current-user'],
    queryFn: getCurrentUser,
  })

export const useStates = () =>
  useQuery({
    queryKey: ['states'],
    queryFn: getStates,
  })

export const useDistricts = () =>
  useQuery({
    queryKey: ['districts'],
    queryFn: getDistricts,
  })

export const useBlocks = () =>
  useQuery({
    queryKey: ['blocks'],
    queryFn: getBlocks,
  })

export const useCenters = () =>
  useQuery({
    queryKey: ['centers'],
    queryFn: getCenters,
  })

export const useUsers = () =>
  useQuery({
    queryKey: ['users'],
    queryFn: getUsers,
  })

export const useCreateState = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { name: string; code: string }) => api.post('/api/v1/states', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['states'] })
    },
  })
}

export const useCreateDistrict = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { name: string; stateId: number }) => api.post('/api/v1/districts', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['districts'] })
    },
  })
}

export const useCreateBlock = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: { name: string; districtId: number }) => api.post('/api/v1/blocks', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['blocks'] })
    },
  })
}

export const useCreateCenter = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: {
      name: string
      centerCode: string
      type: 'CLINIC' | 'HOSPITAL'
      blockId: number
      address: string
      contactNumber: string
    }) => api.post('/api/v1/centers', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['centers'] })
    },
  })
}

export const useCreateUser = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: RegisterPayload) => api.post('/api/v1/auth/register', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
  })
}
