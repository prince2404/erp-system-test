import { useNavigate, Link } from 'react-router-dom'
import { ROUTE_PATHS } from '../../constants/routePaths'
import { useAuth } from '../../hooks/useAuth'

/**
 * Top navigation bar for authenticated dashboard pages.
 */
const Navbar = ({ title }: { title: string }) => {
  const navigate = useNavigate()
  const { logout, user } = useAuth()

  const handleLogout = () => {
    logout()
    navigate(ROUTE_PATHS.login, { replace: true })
  }

  return (
    <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
      <h1 className="text-lg font-semibold">{title}</h1>
      <div className="flex items-center gap-3">
        <Link
          to={ROUTE_PATHS.profile}
          className="rounded-md px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
        >
          {user?.username ?? 'Profile'}
        </Link>
        <button
          type="button"
          onClick={handleLogout}
          className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white transition hover:bg-slate-700"
        >
          Logout
        </button>
      </div>
    </header>
  )
}

export default Navbar
