import { useState, useEffect } from 'react'
import { getPositionConfigs, createPositionConfig, updatePositionConfig, deletePositionConfig } from '../../api'
import type { PositionConfig } from '../../types'
import './AdminPages.css'

const AdminPositionsPage = () => {
  const [positions, setPositions] = useState<PositionConfig[]>([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingPos, setEditingPos] = useState<PositionConfig | null>(null)
  const [formData, setFormData] = useState({ 
    name: '', 
    workStartTime: '09:00', 
    workEndTime: '18:00', 
    monthlyWorkDays: 21,
    dailyWorkHours: 8.0,
    description: '' 
  })

  useEffect(() => {
    loadPositions()
  }, [])

  const loadPositions = async () => {
    setLoading(true)
    try {
      const data = await getPositionConfigs()
      console.log('加载的职务数据:', data)
      setPositions(data || [])
    } catch (error) {
      console.error('加载职务配置失败:', error)
      alert(error instanceof Error ? error.message : '加载职务配置失败')
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = () => {
    setEditingPos(null)
    setFormData({ 
      name: '', 
      workStartTime: '09:00', 
      workEndTime: '18:00', 
      monthlyWorkDays: 21,
      dailyWorkHours: 8.0,
      description: '' 
    })
    setShowModal(true)
  }

  const handleEdit = (pos: PositionConfig) => {
    setEditingPos(pos)
    setFormData({
      name: pos.name,
      workStartTime: pos.workStartTime,
      workEndTime: pos.workEndTime,
      monthlyWorkDays: pos.monthlyWorkDays || 21,
      dailyWorkHours: pos.dailyWorkHours || 8.0,
      description: pos.description || '',
    })
    setShowModal(true)
  }

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定要删除该职务配置吗？删除后使用该职务的员工将无法正常打卡。')) {
      return
    }
    try {
      await deletePositionConfig(id)
      await loadPositions()
    } catch (error) {
      console.error('删除职务配置失败:', error)
      alert(error instanceof Error ? error.message : '删除职务配置失败')
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.name.trim()) {
      alert('请输入职务名称')
      return
    }

    try {
      let result
      if (editingPos) {
        result = await updatePositionConfig(editingPos.id, {
          name: formData.name,
          workStartTime: formData.workStartTime,
          workEndTime: formData.workEndTime,
          monthlyWorkDays: formData.monthlyWorkDays,
          dailyWorkHours: formData.dailyWorkHours,
          description: formData.description,
        })
      } else {
        result = await createPositionConfig({
          name: formData.name,
          workStartTime: formData.workStartTime,
          workEndTime: formData.workEndTime,
          monthlyWorkDays: formData.monthlyWorkDays,
          dailyWorkHours: formData.dailyWorkHours,
          description: formData.description,
        })
      }
      console.log('创建/更新职务结果:', result)
      setShowModal(false)
      // 延迟一下再刷新，确保后端数据已保存
      setTimeout(async () => {
        await loadPositions()
      }, 100)
    } catch (error) {
      console.error('保存职务配置失败:', error)
      alert(error instanceof Error ? error.message : '保存职务配置失败')
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>职务管理</h1>
        <button className="create-btn" onClick={handleCreate}>
          新建职务
        </button>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>职务ID</th>
                <th>职务名称</th>
                <th>上班时间</th>
                <th>下班时间</th>
                <th>月工作日</th>
                <th>每日工作时长</th>
                <th>描述</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {positions.map((pos) => (
                <tr key={pos.id}>
                  <td>{pos.id}</td>
                  <td>{pos.name}</td>
                  <td>{pos.workStartTime}</td>
                  <td>{pos.workEndTime}</td>
                  <td>{pos.monthlyWorkDays || 21} 天</td>
                  <td>{pos.dailyWorkHours || 8.0} 小时</td>
                  <td>{pos.description || '-'}</td>
                  <td>
                    <button className="action-btn" onClick={() => handleEdit(pos)}>
                      编辑
                    </button>
                    <button
                      className="action-btn delete"
                      onClick={() => handleDelete(pos.id)}
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
            <h2>{editingPos ? '编辑职务' : '新建职务'}</h2>
            <form onSubmit={handleSubmit}>
              <label>
                <span>职务名称 *</span>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                />
              </label>
              <label>
                <span>上班时间 *</span>
                <input
                  type="time"
                  value={formData.workStartTime}
                  onChange={(e) => setFormData({ ...formData, workStartTime: e.target.value })}
                  required
                />
              </label>
              <label>
                <span>下班时间 *</span>
                <input
                  type="time"
                  value={formData.workEndTime}
                  onChange={(e) => setFormData({ ...formData, workEndTime: e.target.value })}
                  required
                />
              </label>
              <label>
                <span>月工作日 *</span>
                <input
                  type="number"
                  min="1"
                  max="31"
                  value={formData.monthlyWorkDays}
                  onChange={(e) => setFormData({ ...formData, monthlyWorkDays: parseInt(e.target.value) || 21 })}
                  required
                />
              </label>
              <label>
                <span>每日工作时长（小时） *</span>
                <input
                  type="number"
                  min="0.5"
                  max="24"
                  step="0.5"
                  value={formData.dailyWorkHours}
                  onChange={(e) => setFormData({ ...formData, dailyWorkHours: parseFloat(e.target.value) || 8.0 })}
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

export default AdminPositionsPage


