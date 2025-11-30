import { useState, useEffect } from 'react'
import { getApplications, approveApplication } from '../../api'
import type { Application } from '../../types'
import './AdminPages.css'

const AdminApplicationsPage = () => {
  const [applications, setApplications] = useState<Application[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadApplications()
  }, [])

  const loadApplications = async () => {
    setLoading(true)
    try {
      const data = await getApplications()
      setApplications(data)
    } catch (error) {
      console.error('加载申请列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (id: number, approved: boolean) => {
    try {
      await approveApplication({ applicationId: id, approved })
      await loadApplications()
    } catch (error) {
      console.error('审批失败:', error)
    }
  }

  return (
    <div className="admin-page">
      <h1>审批管理</h1>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>员工</th>
                <th>类型</th>
                <th>时间</th>
                <th>理由</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {applications.map((app) => (
                <tr key={app.id}>
                  <td>{app.employeeName || `员工 ${app.employeeId}`}</td>
                  <td>
                    {app.type === 'LEAVE'
                      ? '请假'
                      : app.type === 'OVERTIME'
                      ? '加班'
                      : '出差'}
                  </td>
                  <td>
                    {new Date(app.startTime).toLocaleString('zh-CN')} 至{' '}
                    {new Date(app.endTime).toLocaleString('zh-CN')}
                  </td>
                  <td>{app.reason}</td>
                  <td>
                    <span className={`status-badge ${app.status.toLowerCase()}`}>
                      {app.status === 'PENDING'
                        ? '待审批'
                        : app.status === 'APPROVED'
                        ? '已通过'
                        : '已驳回'}
                    </span>
                  </td>
                  <td>
                    {app.status === 'PENDING' && (
                      <>
                        <button
                          className="action-btn approve"
                          onClick={() => handleApprove(app.id, true)}
                        >
                          通过
                        </button>
                        <button
                          className="action-btn reject"
                          onClick={() => handleApprove(app.id, false)}
                        >
                          驳回
                        </button>
                      </>
                    )}
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

export default AdminApplicationsPage

