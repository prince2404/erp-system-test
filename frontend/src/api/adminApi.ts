import { API_PATHS } from '../constants/apiPaths'
import { requestApi, apiClient } from './axiosInstance'

/**
 * Admin API for hierarchy resources (states/districts/blocks/centers).
 */
export const adminApi = {
  /** Fetches states list. */
  getStates: () => requestApi<Array<{ id: number; name: string; code: string }>>(apiClient.get(API_PATHS.admin.states)),

  /** Fetches districts list. */
  getDistricts: () => requestApi<Array<{ id: number; name: string; stateId: number }>>(apiClient.get(API_PATHS.admin.districts)),

  /** Fetches blocks list. */
  getBlocks: () => requestApi<Array<{ id: number; name: string; districtId: number }>>(apiClient.get(API_PATHS.admin.blocks)),

  /** Fetches centers list. */
  getCenters: () =>
    requestApi<
      Array<{ id: number; name: string; centerCode: string; type: 'CLINIC' | 'HOSPITAL'; blockId: number; address: string; contactNumber: string }>
    >(apiClient.get(API_PATHS.admin.centers)),

  /** Creates a state. */
  createState: (payload: { name: string; code: string }) => requestApi<unknown>(apiClient.post(API_PATHS.admin.states, payload)),

  /** Creates a district. */
  createDistrict: (payload: { name: string; stateId: number }) =>
    requestApi<unknown>(apiClient.post(API_PATHS.admin.districts, payload)),

  /** Creates a block. */
  createBlock: (payload: { name: string; districtId: number }) => requestApi<unknown>(apiClient.post(API_PATHS.admin.blocks, payload)),

  /** Creates a center. */
  createCenter: (payload: {
    name: string
    centerCode: string
    type: 'CLINIC' | 'HOSPITAL'
    blockId: number
    address: string
    contactNumber: string
  }) => requestApi<unknown>(apiClient.post(API_PATHS.admin.centers, payload)),
}
