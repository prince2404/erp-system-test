/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useMemo, useState, type PropsWithChildren } from 'react'
import { useQuery } from '@tanstack/react-query'
import { userApi } from '../api/userApi'
import type { PermissionAction } from '../constants/roles'
import { REFRESH_TOKEN_STORAGE_KEY, TOKEN_STORAGE_KEY } from '../constants/appConstants'
import { permissionService } from '../services/permissionService'

export type AuthUser = {
  id: number
  username: string
  role: string
  assignedCenterId: number | null
}

type AuthContextValue = {
  token: string | null
  user: AuthUser | null
  isLoadingUser: boolean
  login: (token: string, refreshToken?: string | null) => void
  logout: () => void
  hasPermission: (action: PermissionAction) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * Auth provider that manages token lifecycle and user permission context.
 */
export const AuthProvider = ({ children }: PropsWithChildren) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY))

  const { data: userData, isLoading } = useQuery({
    queryKey: ['auth', 'current-user'],
    queryFn: async () => {
      const response = await userApi.getCurrentUser()
      return response.data
    },
    enabled: Boolean(token),
  })

  const login = useCallback((nextToken: string, refreshToken?: string | null) => {
    localStorage.setItem(TOKEN_STORAGE_KEY, nextToken)
    if (refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken)
    }
    setToken(nextToken)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY)
    setToken(null)
  }, [])

  const hasPermission = useCallback(
    (action: PermissionAction) => permissionService.hasPermission(userData?.role, action),
    [userData?.role],
  )

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user: userData ?? null,
      isLoadingUser: isLoading,
      login,
      logout,
      hasPermission,
    }),
    [hasPermission, isLoading, login, logout, token, userData],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Returns auth context state for auth-aware components and hooks.
 */
export const useAuthContext = () => {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider')
  }

  return context
}
