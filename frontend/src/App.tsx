import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import ErrorBoundary from './components/common/ErrorBoundary'
import DashboardLayout from './components/layout/DashboardLayout'
import ProtectedRoute from './components/routing/ProtectedRoute'
import { ROUTE_PATHS } from './constants/routePaths'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import LoginPage from './pages/auth/LoginPage'
import DashboardHomePage from './pages/dashboard/DashboardHomePage'
import BlocksPage from './pages/admin/BlocksPage'
import CentersPage from './pages/admin/CentersPage'
import DistrictsPage from './pages/admin/DistrictsPage'
import StatesPage from './pages/admin/StatesPage'
import UsersPage from './pages/users/UsersPage'
import BillingInvoicesPage from './pages/billing/BillingInvoicesPage'
import WalletTopUpPage from './pages/billing/WalletTopUpPage'
import CommissionLedgerPage from './pages/commissions/CommissionLedgerPage'
import DoctorConsultationPage from './pages/doctor/DoctorConsultationPage'
import DoctorQueuePage from './pages/doctor/DoctorQueuePage'
import InventoryPage from './pages/pharmacy/InventoryPage'
import MedicinesPage from './pages/pharmacy/MedicinesPage'
import AppointmentsPage from './pages/patients/AppointmentsPage'
import FamiliesPage from './pages/patients/FamiliesPage'
import PatientsPage from './pages/patients/PatientsPage'

const queryClient = new QueryClient()

/**
 * Root application component with providers and route map.
 */
const App = () => {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <AuthProvider>
            <BrowserRouter>
              <Routes>
                <Route path={ROUTE_PATHS.login} element={<LoginPage />} />

                <Route element={<ProtectedRoute />}>
                  <Route element={<DashboardLayout />}>
                    <Route path={ROUTE_PATHS.dashboard} element={<DashboardHomePage />} />
                    <Route path={ROUTE_PATHS.admin.states} element={<StatesPage />} />
                    <Route path={ROUTE_PATHS.admin.districts} element={<DistrictsPage />} />
                    <Route path={ROUTE_PATHS.admin.blocks} element={<BlocksPage />} />
                    <Route path={ROUTE_PATHS.admin.centers} element={<CentersPage />} />
                    <Route path={ROUTE_PATHS.admin.users} element={<UsersPage />} />
                    <Route path={ROUTE_PATHS.reception.families} element={<FamiliesPage />} />
                    <Route path={ROUTE_PATHS.reception.patients} element={<PatientsPage />} />
                    <Route path={ROUTE_PATHS.reception.appointments} element={<AppointmentsPage />} />
                    <Route path={ROUTE_PATHS.doctor.queue} element={<DoctorQueuePage />} />
                    <Route path={ROUTE_PATHS.doctor.consultation} element={<DoctorConsultationPage />} />
                    <Route path={ROUTE_PATHS.billing.invoices} element={<BillingInvoicesPage />} />
                    <Route path={ROUTE_PATHS.billing.walletTopUp} element={<WalletTopUpPage />} />
                    <Route path={ROUTE_PATHS.commissions.ledger} element={<CommissionLedgerPage />} />
                    <Route path={ROUTE_PATHS.pharmacy.medicines} element={<MedicinesPage />} />
                    <Route path={ROUTE_PATHS.pharmacy.inventory} element={<InventoryPage />} />
                  </Route>
                </Route>

                <Route path="*" element={<Navigate to={ROUTE_PATHS.dashboard} replace />} />
              </Routes>
            </BrowserRouter>
          </AuthProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  )
}

export default App
