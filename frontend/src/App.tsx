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
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
