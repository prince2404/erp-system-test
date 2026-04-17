import { Navigate, Outlet } from 'react-router-dom'
import { TOKEN_STORAGE_KEY } from '../../lib/api'

const ProtectedRoute = () => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY)

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
