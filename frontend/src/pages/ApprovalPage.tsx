import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getApplications, reviewApplication } from '../api'
import type { Application, ApprovalRequest } from '../types'
import './ApprovalPage.css'

const ApprovalPage = () => {
  const [applications, setApplications] = useState<Application[]>([])
  const [loading, setLoading] = useState(false)
  const [filter, setFilter] = useState<'all' | 'pending' | 'approved' | 'rejected'>('pending')
  const [typeFilter, setTypeFilter] = useState<Application['type'] | 'all'>('all')
  const [reviewingId, setReviewingId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')
  const { user } = useAuth()

  useEffect(() => {
    loadApplications()
  }, [filter, typeFilter])

  const loadApplications = async () => {
    setLoading(true)
    try {
      // 根据筛选器确定状态参数
      let status: string | undefined
      if (filter === 'pending') {
        status = 'PENDING'
      } else if (filter === 'approved') {
        status = 'APPROVED'
      } else if (filter === 'rejected') {
        status = 'REJECTED'
      }
      // filter === 'all' 时不传status，获取所有申请

      // 获取申请列表（不传employeeId，获取所有申请）
      const data = await getApplications(undefined, status)
      
      // 类型筛选
      let filtered = data
      if (typeFilter !== 'all') {
        filtered = filtered.filter(app => app.type === typeFilter)
      }

      setApplications(filtered)
    } catch (error) {
      console.error('加载申请列表失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleReview = async (id: number, approved: boolean) => {
    if (!approved && !rejectReason.trim()) {
      alert('驳回申请需要填写驳回理由')
      return
    }

    if (!user?.employeeId) {
      alert('无法获取当前用户信息，请重新登录')
      return
    }

    setReviewingId(id)
    try {
      const request: ApprovalRequest = {
        applicationId: id,
        approved,
        reason: approved ? undefined : rejectReason,
        approverId: user.employeeId,
      }
      await reviewApplication(request)
      setRejectReason('')
      await loadApplications()
    } catch (error) {
      console.error('审批失败:', error)
      alert(error instanceof Error ? error.message : '审批失败')
    } finally {
      setReviewingId(null)
    }
  }

  const getTypeText = (type: Application['type']) => {
    switch (type) {
      case 'LEAVE': return '请假'
      case 'OVERTIME': return '加班'
      case 'BUSINESS_TRIP': return '出差'
      case 'REISSUE': return '补卡'
      default: return type
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '待审批'
      case 'APPROVED': return '已通过'
      case 'REJECTED': return '已驳回'
      case 'CANCELLED': return '已撤销'
      default: return status
    }
  }

  const getStatusClass = (status: string) => {
    return status.toLowerCase()
  }

  return (
    <div className="approval-page">
      <h1>审批中心</h1>

      <div className="approval-filters">
        <div className="filter-group">
          <label htmlFor="status-filter">状态筛选：</label>
          <select id="status-filter" value={filter} onChange={(e) => setFilter(e.target.value as any)}>
            <option value="all">全部</option>
            <option value="pending">待审批</option>
            <option value="approved">已通过</option>
            <option value="rejected">已驳回</option>
          </select>
        </div>
        <div className="filter-group">
          <label htmlFor="type-filter">类型筛选：</label>
          <select id="type-filter" value={typeFilter} onChange={(e) => setTypeFilter(e.target.value as any)}>
            <option value="all">全部</option>
            <option value="LEAVE">请假</option>
            <option value="REISSUE">补卡</option>
            <option value="OVERTIME">加班</option>
            <option value="BUSINESS_TRIP">出差</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : applications.length === 0 ? (
        <div className="empty-state">暂无申请记录</div>
      ) : (
        <div className="approval-list">
          {applications.map((app) => (
            <div key={app.id} className="approval-card">
              <div className="approval-header">
                <div className="approval-meta">
                  <span className="application-type">{getTypeText(app.type)}</span>
                  <span className={`application-status ${getStatusClass(app.status)}`}>
                    {getStatusText(app.status)}
                  </span>
                </div>
                <div className="employee-info">
                  <span>申请人：员工 {app.employeeId}</span>
                </div>
              </div>

              <div className="approval-body">
                <div className="approval-item">
                  <span className="item-label">时间：</span>
                  <span className="item-value">
                    {new Date(app.startTime).toLocaleString('zh-CN')} 至{' '}
                    {new Date(app.endTime).toLocaleString('zh-CN')}
                  </span>
                </div>
                <div className="approval-item">
                  <span className="item-label">理由：</span>
                  <span className="item-value">{app.reason}</span>
                </div>

                {/* 请假特有信息 */}
                {app.type === 'LEAVE' && app.leaveType && (
                  <div className="approval-item">
                    <span className="item-label">请假类型：</span>
                    <span className="item-value">
                      {app.leaveType === 'SICK_LEAVE' ? '病假' :
                       app.leaveType === 'ANNUAL_LEAVE' ? '年假' :
                       app.leaveType === 'PERSONAL_LEAVE' ? '事假' :
                       app.leaveType === 'MARRIAGE_LEAVE' ? '婚假' :
                       app.leaveType === 'MATERNITY_LEAVE' ? '产假' : '其他'}
                    </span>
                  </div>
                )}

                {/* 补卡特有信息 */}
                {app.type === 'REISSUE' && app.reissueTime && (
                  <div className="approval-item">
                    <span className="item-label">补卡时间：</span>
                    <span className="item-value">
                      {new Date(app.reissueTime).toLocaleString('zh-CN')}
                    </span>
                  </div>
                )}

                {/* 加班特有信息 */}
                {app.type === 'OVERTIME' && (
                  <>
                    {app.overtimeType && (
                      <div className="approval-item">
                        <span className="item-label">加班类型：</span>
                        <span className="item-value">
                          {app.overtimeType === 'WEEKDAY' ? '平日加班' :
                           app.overtimeType === 'WEEKEND' ? '周末加班' : '节假日加班'}
                        </span>
                      </div>
                    )}
                    {app.isCompensatory !== undefined && (
                      <div className="approval-item">
                        <span className="item-label">是否调休：</span>
                        <span className="item-value">{app.isCompensatory ? '是' : '否'}</span>
                      </div>
                    )}
                  </>
                )}

                {/* 出差特有信息 */}
                {app.type === 'BUSINESS_TRIP' && (
                  <>
                    {app.destination && (
                      <div className="approval-item">
                        <span className="item-label">出差地点：</span>
                        <span className="item-value">{app.destination}</span>
                      </div>
                    )}
                    {app.companions && app.companions.length > 0 && (
                      <div className="approval-item">
                        <span className="item-label">随行人员：</span>
                        <span className="item-value">{app.companions.join(', ')}</span>
                      </div>
                    )}
                    {app.transportation && (
                      <div className="approval-item">
                        <span className="item-label">交通工具：</span>
                        <span className="item-value">{app.transportation}</span>
                      </div>
                    )}
                    {app.budget && (
                      <div className="approval-item">
                        <span className="item-label">费用预算：</span>
                        <span className="item-value">¥{app.budget}</span>
                      </div>
                    )}
                  </>
                )}

                {app.rejectReason && (
                  <div className="approval-item">
                    <span className="item-label">驳回原因：</span>
                    <span className="item-value error">{app.rejectReason}</span>
                  </div>
                )}
              </div>

              {app.status === 'PENDING' && (
                <div className="approval-actions">
                  <div className="reject-section">
                    <input
                      type="text"
                      placeholder="驳回理由（驳回时必填）"
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      className="reject-reason-input"
                    />
                  </div>
                  <div className="action-buttons">
                    <button
                      className="approve-btn"
                      onClick={() => handleReview(app.id, true)}
                      disabled={reviewingId === app.id}
                    >
                      {reviewingId === app.id ? '处理中...' : '通过'}
                    </button>
                    <button
                      className="reject-btn"
                      onClick={() => handleReview(app.id, false)}
                      disabled={reviewingId === app.id}
                    >
                      {reviewingId === app.id ? '处理中...' : '驳回'}
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default ApprovalPage

