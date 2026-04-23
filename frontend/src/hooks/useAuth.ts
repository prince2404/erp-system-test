import { useAuthContext } from '../context/AuthContext'

/**
 * Hook exposing authenticated user and auth actions.
 */
export const useAuth = () => useAuthContext()
