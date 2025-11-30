import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getApplications } from '../api'
import type { Application } from '../types'
import './ApplicationsPage.css'

const ApplicationsPage = () => {
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

  const getTypeText = (type: string) => {
    switch (type) {
      case 'LEAVE':
        return '请假'
      case 'OVERTIME':
        return '加班'
      case 'BUSINESS_TRIP':
        return '出差'
      default:
        return type
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '待审批'
      case 'APPROVED':
        return '已通过'
      case 'REJECTED':
        return '已驳回'
      default:
        return status
    }
  }

  const getStatusClass = (status: string) => {
    return status.toLowerCase()
  }

  return (
    <div className="applications-page">
      <div className="page-header">
        <h1>我的申请</h1>
        <Link to="/applications/new" className="new-application-btn">
          新建申请
        </Link>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : applications.length === 0 ? (
        <div className="empty-state">
          <p>暂无申请记录</p>
          <Link to="/applications/new" className="new-application-btn">
            创建第一个申请
          </Link>
        </div>
      ) : (
        <div className="applications-list">
          {applications.map((app) => (
            <div key={app.id} className="application-card">
              <div className="application-header">
                <span className="application-type">{getTypeText(app.type)}</span>
                <span className={`application-status ${getStatusClass(app.status)}`}>
                  {getStatusText(app.status)}
                </span>
              </div>
              <div className="application-body">
                <div className="application-item">
                  <span className="item-label">时间：</span>
                  <span className="item-value">
                    {new Date(app.startTime).toLocaleString('zh-CN')} 至{' '}
                    {new Date(app.endTime).toLocaleString('zh-CN')}
                  </span>
                </div>
                <div className="application-item">
                  <span className="item-label">理由：</span>
                  <span className="item-value">{app.reason}</span>
                </div>
                {app.approverName && (
                  <div className="application-item">
                    <span className="item-label">审批人：</span>
                    <span className="item-value">{app.approverName}</span>
                  </div>
                )}
                {app.rejectReason && (
                  <div className="application-item">
                    <span className="item-label">驳回原因：</span>
                    <span className="item-value error">{app.rejectReason}</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ApplicationsPage

