import { useState, useEffect } from 'react'
import { getEmployees, createEmployee, updateEmployee, resetEmployeePassword, deleteEmployee, getDepartments, getPositionConfigs } from '../../api'
import type { Employee, Department, PositionConfig } from '../../types'
import './AdminPages.css'

const AdminEmployeesPage = () => {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [departments, setDepartments] = useState<Department[]>([])
  const [positions, setPositions] = useState<PositionConfig[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingEmp, setEditingEmp] = useState<Employee | null>(null)
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    departmentId: '',
    positionId: '',
    role: 'EMPLOYEE' as 'EMPLOYEE' | 'APPROVER' | 'ADMIN',
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  })

  useEffect(() => {
    loadEmployees()
    loadDepartments()
    loadPositions()
  }, [])

  const loadEmployees = async () => {
    setLoading(true)
    try {
      const data = await getEmployees()
      setEmployees(data)
    } catch (error) {
      console.error('加载员工列表失败:', error)
      alert(error instanceof Error ? error.message : '加载员工列表失败')
    } finally {
      setLoading(false)
    }
  }

  const loadDepartments = async () => {
    try {
      const data = await getDepartments()
      setDepartments(data)
    } catch (error) {
      console.error('加载部门列表失败:', error)
    }
  }

  const loadPositions = async () => {
    try {
      const data = await getPositionConfigs()
      setPositions(data)
    } catch (error) {
      console.error('加载职务列表失败:', error)
    }
  }

  const handleCreate = () => {
    setEditingEmp(null)
    setFormData({
      name: '',
      phone: '',
      departmentId: '',
      positionId: '',
      role: 'EMPLOYEE',
      status: 'ACTIVE',
    })
    setShowModal(true)
  }

  const handleEdit = (emp: Employee) => {
    setEditingEmp(emp)
    setFormData({
      name: emp.name,
      phone: emp.phone,
      departmentId: emp.departmentId.toString(),
      positionId: emp.positionId.toString(),
      role: emp.role,
      status: emp.status,
    })
    setShowModal(true)
  }

  const handleDelete = async (employeeId: number) => {
    if (!window.confirm('确定要删除该员工吗？此操作不可恢复。')) {
      return
    }
    try {
      await deleteEmployee(employeeId)
      await loadEmployees()
    } catch (error) {
      console.error('删除员工失败:', error)
      alert(error instanceof Error ? error.message : '删除员工失败')
    }
  }

  const handleResetPassword = async (employeeId: number) => {
    if (!window.confirm('确定要重置该员工的密码吗？')) {
      return
    }
    try {
      await resetEmployeePassword(employeeId)
      alert('密码重置成功')
    } catch (error) {
      console.error('重置密码失败:', error)
      alert(error instanceof Error ? error.message : '重置密码失败')
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.name.trim()) {
      alert('请输入员工姓名')
      return
    }

    if (!formData.departmentId || formData.departmentId === '') {
      alert('请选择部门')
      return
    }

    if (!formData.positionId || formData.positionId === '') {
      alert('请选择职务')
      return
    }

    const deptId = Number(formData.departmentId)
    const posId = Number(formData.positionId)

    if (isNaN(deptId) || deptId <= 0) {
      alert('部门ID无效')
      return
    }

    if (isNaN(posId) || posId <= 0) {
      alert('职务ID无效')
      return
    }

    try {
      if (editingEmp) {
        await updateEmployee(editingEmp.employeeId, {
          name: formData.name,
          phone: formData.phone,
          departmentId: deptId,
          positionId: posId,
          role: formData.role,
          status: formData.status,
        })
      } else {
        await createEmployee({
          name: formData.name,
          phone: formData.phone,
          departmentId: deptId,
          positionId: posId,
          role: formData.role,
          status: formData.status,
        })
      }
      setShowModal(false)
      await loadEmployees()
    } catch (error: any) {
      console.error('保存员工失败:', error)
      const errorMessage = error?.response?.data?.message || error?.message || '保存员工失败'
      alert(errorMessage)
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>员工管理</h1>
        <button className="create-btn" onClick={handleCreate}>
          新建员工
        </button>
      </div>

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
                  <td>
                    {emp.role === 'ADMIN' ? '管理员' :
                     emp.role === 'APPROVER' ? '审批人' : '员工'}
                  </td>
                  <td>
                    <span className={`status-badge ${emp.status.toLowerCase()}`}>
                      {emp.status === 'ACTIVE' ? '启用' : '停用'}
                    </span>
                  </td>
                  <td>
                    <button className="action-btn" onClick={() => handleEdit(emp)}>
                      编辑
                    </button>
                    <button
                      className="action-btn"
                      onClick={() => handleResetPassword(emp.employeeId)}
                    >
                      重置密码
                    </button>
                    <button
                      className="action-btn delete"
                      onClick={() => handleDelete(emp.employeeId)}
                    >
                      删除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingEmp ? '编辑员工' : '新建员工'}</h2>
            <form onSubmit={handleSubmit}>
              <label>
                <span>姓名 *</span>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </label>
              <label>
                <span>手机号</span>
                <input
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                />
              </label>
              <label>
                <span>部门 *</span>
                <select
                  value={formData.departmentId}
                  onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                  required
                >
                  <option value="">请选择</option>
                  {departments.map(dept => (
                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                  ))}
                </select>
              </label>
              <label>
                <span>职务 *</span>
                <select
                  value={formData.positionId}
                  onChange={(e) => setFormData({ ...formData, positionId: e.target.value })}
                  required
                >
                  <option value="">请选择</option>
                  {positions.map(pos => (
                    <option key={pos.id} value={pos.id}>{pos.name}</option>
                  ))}
                </select>
              </label>
              <label>
                <span>角色 *</span>
                <select
                  value={formData.role}
                  onChange={(e) => setFormData({ ...formData, role: e.target.value as any })}
                  required
                >
                  <option value="EMPLOYEE">员工</option>
                  <option value="APPROVER">审批人</option>
                  <option value="ADMIN">管理员</option>
                </select>
              </label>
              <label>
                <span>状态 *</span>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  required
                >
                  <option value="ACTIVE">启用</option>
                  <option value="INACTIVE">停用</option>
                </select>
              </label>
              <div className="modal-actions">
                <button type="button" onClick={() => setShowModal(false)}>
                  取消
                </button>
                <button type="submit">保存</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default AdminEmployeesPage

