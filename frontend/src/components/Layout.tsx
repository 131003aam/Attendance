import { ReactNode } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Layout.css'

interface LayoutProps {
  children: ReactNode
}

const Layout = ({ children }: LayoutProps) => {
  const { user, isAdmin, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const employeeMenuItems = [
    { path: '/home', label: '首页' },
    { path: '/attendance', label: '打卡记录' },
    { path: '/statistics', label: '统计汇总' },
    { path: '/applications', label: '我的申请' },
    { path: '/profile', label: '个人中心' },
  ]

  const adminMenuItems = [
    { path: '/admin/employees', label: '员工管理' },
    { path: '/admin/attendance', label: '打卡汇总' },
    { path: '/admin/applications', label: '审批管理' },
  ]

  const menuItems = isAdmin ? adminMenuItems : employeeMenuItems

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

