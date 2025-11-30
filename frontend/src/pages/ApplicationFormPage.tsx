import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { submitApplication } from '../api'
import type { LeaveType } from '../types'
import './ApplicationFormPage.css'

const ApplicationFormPage = () => {
  const navigate = useNavigate()
  const [type, setType] = useState<LeaveType>('LEAVE')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [reason, setReason] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setMessage('')

    if (!startTime || !endTime || !reason.trim()) {
      setMessage('请填写完整信息')
      return
    }

    if (new Date(startTime) >= new Date(endTime)) {
      setMessage('结束时间必须晚于开始时间')
      return
    }

    setLoading(true)
    try {
      await submitApplication({ type, startTime, endTime, reason })
      setMessage('申请提交成功！')
      setTimeout(() => {
        navigate('/applications')
      }, 1500)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '提交失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="application-form-page">
      <h1>新建申请</h1>

      <form onSubmit={handleSubmit} className="application-form">
        <label>
          <span>申请类型 *</span>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as LeaveType)}
            disabled={loading}
          >
            <option value="LEAVE">请假</option>
            <option value="OVERTIME">加班</option>
            <option value="BUSINESS_TRIP">出差</option>
          </select>
        </label>

        <label>
          <span>开始时间 *</span>
          <input
            type="datetime-local"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            disabled={loading}
            required
          />
        </label>

        <label>
          <span>结束时间 *</span>
          <input
            type="datetime-local"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            disabled={loading}
            required
          />
        </label>

        <label>
          <span>申请理由 *</span>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="请详细说明申请理由"
            rows={5}
            disabled={loading}
            required
          />
        </label>

        <div className="form-actions">
          <button type="button" onClick={() => navigate('/applications')} disabled={loading}>
            取消
          </button>
          <button type="submit" disabled={loading}>
            {loading ? '提交中...' : '提交申请'}
          </button>
        </div>
      </form>

      {message && (
        <div className={`form-message ${message.includes('成功') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}
    </div>
  )
}

export default ApplicationFormPage

