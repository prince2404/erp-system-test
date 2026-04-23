import { Navigate, Outlet } from 'react-router-dom'
import { ROUTE_PATHS } from '../../constants/routePaths'
import { useAuth } from '../../hooks/useAuth'

/**
 * Guards private routes and redirects unauthenticated users to login.
 */
const ProtectedRoute = () => {
  const { token } = useAuth()

  if (!token) {
    return <Navigate to={ROUTE_PATHS.login} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
