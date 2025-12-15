import { ReactNode } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Layout.css'

interface LayoutProps {
  children: ReactNode
}

const Layout = ({ children }: LayoutProps) => {
  const { user, isAdmin, isApprover, isEmployee, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  // 员工菜单
  const employeeMenuItems = [
    { path: '/home', label: '首页' },
    { path: '/statistics', label: '统计汇总' },
    { path: '/applications', label: '我的申请' },
    { path: '/profile', label: '个人中心' },
  ]

  // 审批人菜单
  const approverMenuItems = [
    { path: '/home', label: '首页' },
    { path: '/approval', label: '审批中心' },
    { path: '/profile', label: '个人中心' },
  ]

  // 管理员菜单
  const adminMenuItems = [
    { path: '/home', label: '首页' },
    { path: '/admin/employees', label: '员工管理' },
    { path: '/admin/departments', label: '部门管理' },
    { path: '/admin/positions', label: '职务管理' },
    { path: '/admin/attendance', label: '考勤汇总' },
    { path: '/admin/applications', label: '审批管理' },
    { path: '/profile', label: '个人中心' },
  ]

  // 根据角色决定显示哪个菜单
  let menuItems = employeeMenuItems
  if (isAdmin) {
    menuItems = adminMenuItems
  } else if (isApprover) {
    menuItems = approverMenuItems
  } else if (isEmployee) {
    menuItems = employeeMenuItems
  }

  return (
    <div className="layout">
      <header className="layout-header">
        <div className="header-content">
          <h1 className="logo">考勤管理系统</h1>
          <nav className="nav-menu">
            {menuItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={location.pathname === item.path ? 'active' : ''}
              >
                {item.label}
              </Link>
            ))}
          </nav>
          <div className="user-info">
            <span className="user-name">{user?.name}</span>
            <button onClick={handleLogout} className="logout-btn">
              退出
            </button>
          </div>
        </div>
      </header>
      <main className="layout-main">{children}</main>
    </div>
  )
}

export default Layout

