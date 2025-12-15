import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import type { UserRole } from '../types'

interface ProtectedRouteProps {
  children: React.ReactNode
  requireAdmin?: boolean
  requireApprover?: boolean
  allowedRoles?: UserRole[]
}

const ProtectedRoute = ({ 
  children, 
  requireAdmin = false,
  requireApprover = false,
  allowedRoles
}: ProtectedRouteProps) => {
  const { isAuthenticated, isAdmin, isApprover, user } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  // 如果指定了允许的角色列表，检查用户角色是否在列表中
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/home" replace />
  }

  // 管理员权限检查
  if (requireAdmin && !isAdmin) {
    return <Navigate to="/home" replace />
  }

  // 审批人权限检查
  if (requireApprover && !isApprover && !isAdmin) {
    return <Navigate to="/home" replace />
  }

  return <>{children}</>
}

export default ProtectedRoute

