import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse, isAxiosError } from 'axios'
import { API_PATHS } from '../constants/apiPaths'
import { REFRESH_TOKEN_STORAGE_KEY, TOKEN_STORAGE_KEY } from '../constants/appConstants'
import type { ApiEnvelope, ApiResult } from './types'

/**
 * Centralized Axios client with auth token handling and refresh flow.
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

const clearStoredTokens = () => {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY)
}

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY)
      if (!refreshToken) {
        clearStoredTokens()
        return Promise.reject(error)
      }

      try {
        const refreshResponse = await axios.post<ApiEnvelope<{ token?: string; accessToken?: string; refreshToken?: string }>>(
          `${import.meta.env.VITE_API_BASE_URL}${API_PATHS.auth.refresh}`,
          { refreshToken },
        )

        const nextAccessToken = refreshResponse.data.data.accessToken ?? refreshResponse.data.data.token
        const nextRefreshToken = refreshResponse.data.data.refreshToken

        if (!nextAccessToken) {
          clearStoredTokens()
          return Promise.reject(error)
        }

        localStorage.setItem(TOKEN_STORAGE_KEY, nextAccessToken)
        if (nextRefreshToken) {
          localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, nextRefreshToken)
        }

        originalRequest.headers = {
          ...originalRequest.headers,
          Authorization: `Bearer ${nextAccessToken}`,
        }

        return apiClient.request(originalRequest)
      } catch {
        clearStoredTokens()
      }
    }

    return Promise.reject(error)
  },
)

/**
 * Normalizes all request outcomes to a consistent shape for callers.
 */
export const requestApi = async <T>(request: Promise<AxiosResponse<ApiEnvelope<T>>>): Promise<ApiResult<T>> => {
  try {
    const response = await request
    return {
      data: response.data.data,
      error: null,
      status: response.status,
    }
  } catch (error) {
    if (isAxiosError(error)) {
      return {
        data: null,
        error: error.response?.data?.message ?? error.message ?? 'Unexpected API error',
        status: error.response?.status ?? 500,
      }
    }

    return {
      data: null,
      error: 'Unexpected API error',
      status: 500,
    }
  }
}
