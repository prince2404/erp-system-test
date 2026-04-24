import { Link, NavLink } from 'react-router-dom'
import { APP_NAME } from '../../constants/appConstants'
import { ROUTE_PATHS } from '../../constants/routePaths'
import { usePermission } from '../../hooks/usePermission'

type NavItem = {
  path: string
  label: string
}

/**
 * Sidebar navigation that renders only authorized links.
 */
const Sidebar = () => {
  const { hasPermission } = usePermission()

  const baseItems: NavItem[] = [
    { path: ROUTE_PATHS.dashboard, label: 'Dashboard Home' },
    { path: ROUTE_PATHS.admin.states, label: 'States' },
    { path: ROUTE_PATHS.admin.districts, label: 'Districts' },
    { path: ROUTE_PATHS.admin.blocks, label: 'Blocks' },
    { path: ROUTE_PATHS.admin.centers, label: 'Centers' },
    { path: ROUTE_PATHS.commissions.ledger, label: 'Commission Ledger' },
  ]

  const receptionItems: NavItem[] = hasPermission('reception:view')
    ? [
        { path: ROUTE_PATHS.reception.families, label: 'Families' },
        { path: ROUTE_PATHS.reception.patients, label: 'Patients' },
        { path: ROUTE_PATHS.reception.appointments, label: 'Appointments' },
      ]
    : []

  const doctorItems: NavItem[] = hasPermission('doctor:view') ? [{ path: ROUTE_PATHS.doctor.queue, label: 'OPD Queue' }] : []

  const billingItems: NavItem[] = hasPermission('billing:view')
    ? [
        { path: ROUTE_PATHS.billing.invoices, label: 'Billing Invoices' },
        { path: ROUTE_PATHS.billing.walletTopUp, label: 'Wallet Top-Up' },
      ]
    : []

  const pharmacyItems: NavItem[] = hasPermission('pharmacy:view')
    ? [
        { path: ROUTE_PATHS.pharmacy.medicines, label: 'Medicine Catalog' },
        { path: ROUTE_PATHS.pharmacy.inventory, label: 'Inventory' },
      ]
    : []

  const userItems: NavItem[] = hasPermission('user:view') ? [{ path: ROUTE_PATHS.admin.users, label: 'Users' }] : []

  const navItems = [...baseItems, ...receptionItems, ...doctorItems, ...billingItems, ...pharmacyItems, ...userItems]

  return (
    <aside className="w-64 border-r border-slate-200 bg-white p-4">
      <Link to={ROUTE_PATHS.dashboard} className="mb-8 block text-xl font-bold text-indigo-600">
        {APP_NAME}
      </Link>
      <nav className="space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === ROUTE_PATHS.dashboard}
            className={({ isActive }) =>
              `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
            }
          >
            {item.label}
          </NavLink>
        ))}

        {/* Profile link — always visible */}
        <hr className="my-3 border-slate-200" />
        <NavLink
          to={ROUTE_PATHS.profile}
          className={({ isActive }) =>
            `block rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-indigo-100 text-indigo-700' : 'text-slate-700 hover:bg-slate-100'}`
          }
        >
          👤 My Profile
        </NavLink>
      </nav>
    </aside>
  )
}

export default Sidebar
