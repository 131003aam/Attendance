import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'

// 页面组件
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import HomePage from './pages/HomePage'
import AttendancePage from './pages/AttendancePage'
import StatisticsPage from './pages/StatisticsPage'
import ProfilePage from './pages/ProfilePage'
import ApplicationsPage from './pages/ApplicationsPage'
import ApplicationFormPage from './pages/ApplicationFormPage'

// 管理员页面
import AdminEmployeesPage from './pages/admin/AdminEmployeesPage'
import AdminAttendancePage from './pages/admin/AdminAttendancePage'
import AdminApplicationsPage from './pages/admin/AdminApplicationsPage'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/"
            element={<Navigate to="/login" replace />}
          />
          <Route
            path="*"
            element={<Navigate to="/login" replace />}
          />
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <Layout>
                  <HomePage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/attendance"
            element={
              <ProtectedRoute>
                <Layout>
                  <AttendancePage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/statistics"
            element={
              <ProtectedRoute>
                <Layout>
                  <StatisticsPage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/applications"
            element={
              <ProtectedRoute>
                <Layout>
                  <ApplicationsPage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/applications/new"
            element={
              <ProtectedRoute>
                <Layout>
                  <ApplicationFormPage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Layout>
                  <ProfilePage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/employees"
            element={
              <ProtectedRoute requireAdmin>
                <Layout>
                  <AdminEmployeesPage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/attendance"
            element={
              <ProtectedRoute requireAdmin>
                <Layout>
                  <AdminAttendancePage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/applications"
            element={
              <ProtectedRoute requireAdmin>
                <Layout>
                  <AdminApplicationsPage />
                </Layout>
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App

