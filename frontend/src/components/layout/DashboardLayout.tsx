import { Outlet } from 'react-router-dom'
import Footer from './Footer'
import Navbar from './Navbar'
import PageWrapper from './PageWrapper'
import Sidebar from './Sidebar'

/**
 * Dashboard shell layout for authenticated users.
 * Access is enforced upstream by ProtectedRoute.
 */
const DashboardLayout = () => {
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <div className="flex min-h-screen">
        <Sidebar />

        <main className="flex flex-1 flex-col">
          <Navbar title="Dashboard" />
          <PageWrapper>
            <Outlet />
          </PageWrapper>
          <Footer />
        </main>
      </div>
    </div>
  )
}

export default DashboardLayout
