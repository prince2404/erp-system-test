import { useNavigate } from 'react-router-dom'
import { ROUTE_PATHS } from '../../constants/routePaths'
import { useAuth } from '../../hooks/useAuth'

/**
 * Top navigation bar for authenticated dashboard pages.
 */
const Navbar = ({ title }: { title: string }) => {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate(ROUTE_PATHS.login, { replace: true })
  }

  return (
    <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
      <h1 className="text-lg font-semibold">{title}</h1>
      <button
        type="button"
        onClick={handleLogout}
        className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white transition hover:bg-slate-700"
      >
        Logout
      </button>
    </header>
  )
}

export default Navbar
