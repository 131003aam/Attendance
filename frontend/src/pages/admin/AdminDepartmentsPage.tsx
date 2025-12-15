import { useState, useEffect } from 'react'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '../../api'
import type { Department } from '../../types'
import './AdminPages.css'

const AdminDepartmentsPage = () => {
  const [departments, setDepartments] = useState<Department[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingDept, setEditingDept] = useState<Department | null>(null)
  const [formData, setFormData] = useState({ name: '', description: '', managerId: '' })

  useEffect(() => {
    loadDepartments()
  }, [])

  const loadDepartments = async () => {
    setLoading(true)
    try {
      const data = await getDepartments()
      console.log('加载的部门数据:', data)
      setDepartments(data || [])
    } catch (error) {
      console.error('加载部门列表失败:', error)
      alert(error instanceof Error ? error.message : '加载部门列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = () => {
    setEditingDept(null)
    setFormData({ name: '', description: '', managerId: '' })
    setShowModal(true)
  }

  const handleEdit = (dept: Department) => {
    setEditingDept(dept)
    setFormData({
      name: dept.name,
      description: dept.description || '',
      managerId: dept.managerId?.toString() || '',
    })
    setShowModal(true)
  }

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定要删除该部门吗？删除后该部门下的员工将无法正常使用系统。')) {
      return
    }
    try {
      await deleteDepartment(id)
      await loadDepartments()
    } catch (error) {
      console.error('删除部门失败:', error)
      alert(error instanceof Error ? error.message : '删除部门失败')
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.name.trim()) {
      alert('请输入部门名称')
      return
    }

    try {
      let result
      if (editingDept) {
        result = await updateDepartment(editingDept.id, {
          name: formData.name,
          description: formData.description,
          managerId: formData.managerId ? Number(formData.managerId) : undefined,
        })
      } else {
        result = await createDepartment({
          name: formData.name,
          description: formData.description,
          managerId: formData.managerId ? Number(formData.managerId) : undefined,
        })
      }
      console.log('创建/更新部门结果:', result)
      setShowModal(false)
      // 延迟一下再刷新，确保后端数据已保存
      setTimeout(async () => {
        await loadDepartments()
      }, 100)
    } catch (error) {
      console.error('保存部门失败:', error)
      alert(error instanceof Error ? error.message : '保存部门失败')
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>部门管理</h1>
        <button className="create-btn" onClick={handleCreate}>
          新建部门
        </button>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>部门ID</th>
                <th>部门名称</th>
                <th>描述</th>
                <th>负责人ID</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {departments.map((dept) => (
                <tr key={dept.id}>
                  <td>{dept.id}</td>
                  <td>{dept.name}</td>
                  <td>{dept.description || '-'}</td>
                  <td>{dept.managerId || '-'}</td>
                  <td>
                    <button className="action-btn" onClick={() => handleEdit(dept)}>
                      编辑
                    </button>
                    <button
                      className="action-btn delete"
                      onClick={() => handleDelete(dept.id)}
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
            <h2>{editingDept ? '编辑部门' : '新建部门'}</h2>
            <form onSubmit={handleSubmit}>
              <label>
                <span>部门名称 *</span>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </label>
              <label>
                <span>描述</span>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                />
              </label>
              <label>
                <span>负责人ID</span>
                <input
                  type="number"
                  value={formData.managerId}
                  onChange={(e) => setFormData({ ...formData, managerId: e.target.value })}
                />
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

export default AdminDepartmentsPage


