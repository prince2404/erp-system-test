import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { TOKEN_STORAGE_KEY } from '../../lib/api'

const DashboardLayout = () => {
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <div className="flex min-h-screen">
        <aside className="w-64 border-r border-slate-200 bg-white p-4">
          <Link to="/dashboard" className="mb-8 block text-xl font-bold text-indigo-600">
            ERP System
          </Link>
          <nav className="space-y-2">
            <NavLink
              to="/dashboard"
              end
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
              }
            >
              Dashboard Home
            </NavLink>
          </nav>
        </aside>

        <main className="flex-1">
          <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
            <h1 className="text-lg font-semibold">Dashboard</h1>
            <button
              onClick={handleLogout}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white transition hover:bg-slate-700"
            >
              Logout
            </button>
          </header>

          <section className="p-6">
            <Outlet />
          </section>
        </main>
      </div>
    </div>
  )
}

export default DashboardLayout
