import { useState, useEffect } from 'react'
import { getEmployees } from '../../api'
import type { Employee } from '../../types'
import './AdminPages.css'

const AdminEmployeesPage = () => {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadEmployees()
  }, [])

  const loadEmployees = async () => {
    setLoading(true)
    try {
      const data = await getEmployees()
      setEmployees(data)
    } catch (error) {
      console.error('加载员工列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-page">
      <h1>员工管理</h1>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>员工编号</th>
                <th>姓名</th>
                <th>手机号</th>
                <th>部门</th>
                <th>角色</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((emp) => (
                <tr key={emp.employeeId}>
                  <td>{emp.employeeId}</td>
                  <td>{emp.name}</td>
                  <td>{emp.phone}</td>
                  <td>{emp.departmentName || `部门 ${emp.departmentId}`}</td>
                  <td>{emp.role === 'ADMIN' ? '管理员' : '员工'}</td>
                  <td>
                    <span className={`status-badge ${emp.status.toLowerCase()}`}>
                      {emp.status === 'ACTIVE' ? '启用' : '停用'}
                    </span>
                  </td>
                  <td>
                    <button className="action-btn">编辑</button>
                    <button className="action-btn">重置密码</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default AdminEmployeesPage

