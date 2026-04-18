import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/routing/ProtectedRoute'
import DashboardLayout from './components/layout/DashboardLayout'
import LoginPage from './pages/LoginPage'
import DashboardHomePage from './pages/DashboardHomePage'
import StatesPage from './pages/admin/StatesPage'
import DistrictsPage from './pages/admin/DistrictsPage'
import BlocksPage from './pages/admin/BlocksPage'
import CentersPage from './pages/admin/CentersPage'
import UsersPage from './pages/admin/UsersPage'
import FamiliesPage from './pages/reception/FamiliesPage'
import PatientsPage from './pages/reception/PatientsPage'
import AppointmentsPage from './pages/reception/AppointmentsPage'
import DoctorQueuePage from './pages/doctor/DoctorQueuePage'
import DoctorConsultationPage from './pages/doctor/DoctorConsultationPage'

const queryClient = new QueryClient()

const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<DashboardLayout />}>
              <Route path="/dashboard" element={<DashboardHomePage />} />
              <Route path="/admin/states" element={<StatesPage />} />
              <Route path="/admin/districts" element={<DistrictsPage />} />
              <Route path="/admin/blocks" element={<BlocksPage />} />
              <Route path="/admin/centers" element={<CentersPage />} />
              <Route path="/admin/users" element={<UsersPage />} />
              <Route path="/reception/families" element={<FamiliesPage />} />
              <Route path="/reception/patients" element={<PatientsPage />} />
              <Route path="/reception/appointments" element={<AppointmentsPage />} />
              <Route path="/doctor/queue" element={<DoctorQueuePage />} />
              <Route path="/doctor/consultation/:appointmentId" element={<DoctorConsultationPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
