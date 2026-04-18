import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { TOKEN_STORAGE_KEY } from '../../lib/api'
import { useCurrentUser } from '../../hooks/useAdminData'
import { canAccessUsers } from '../../lib/rbac'

const DashboardLayout = () => {
  const navigate = useNavigate()
  const { data: currentUser } = useCurrentUser()

  const handleLogout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    navigate('/login', { replace: true })
  }

  const role = currentUser?.role
  const isSuperAdmin = role === 'SUPER_ADMIN'
  const canSeeReceptionNav = role === 'RECEPTIONIST' || isSuperAdmin
  const canSeeDoctorNav = role === 'DOCTOR' || isSuperAdmin

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
            <NavLink
              to="/admin/states"
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
              }
            >
              States
            </NavLink>
            <NavLink
              to="/admin/districts"
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
              }
            >
              Districts
            </NavLink>
            <NavLink
              to="/admin/blocks"
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
              }
            >
              Blocks
            </NavLink>
            <NavLink
              to="/admin/centers"
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
              }
            >
              Centers
            </NavLink>
            {canSeeReceptionNav ? (
              <>
                <NavLink
                  to="/reception/families"
                  className={({ isActive }) =>
                    `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
                  }
                >
                  Families
                </NavLink>
                <NavLink
                  to="/reception/patients"
                  className={({ isActive }) =>
                    `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
                  }
                >
                  Patients
                </NavLink>
                <NavLink
                  to="/reception/appointments"
                  className={({ isActive }) =>
                    `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
                  }
                >
                  Appointments
                </NavLink>
              </>
            ) : null}
            {canSeeDoctorNav ? (
              <NavLink
                to="/doctor/queue"
                className={({ isActive }) =>
                  `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
                }
              >
                OPD Queue
              </NavLink>
            ) : null}
            {canAccessUsers(currentUser?.role) ? (
              <NavLink
                to="/admin/users"
                className={({ isActive }) =>
                  `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
                }
              >
                Users
              </NavLink>
            ) : null}
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
