import { useState, FormEvent, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { submitApplication, getReissueLimit, searchEmployees } from '../api'
import type { 
  ApplicationType, 
  ApplicationRequest,
  LeaveSubType,
  OvertimeType,
  ReissueType,
  Employee
} from '../types'
import './ApplicationFormPage.css'

const ApplicationFormPage = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  
  // 获取当前登录用户的ID
  const currentEmployeeId = user?.employeeId
  
  // 从URL参数获取申请类型（用于从异常记录页面跳转补卡）
  const urlType = searchParams.get('type') as ApplicationType | null
  const reissueTime = searchParams.get('reissueTime') || ''
  
  const [type, setType] = useState<ApplicationType>(urlType || 'LEAVE')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [reason, setReason] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  
  // 请假特有字段
  const [leaveType, setLeaveType] = useState<LeaveSubType>('PERSONAL_LEAVE')
  const [attachment, setAttachment] = useState<File | null>(null)
  
  // 补卡特有字段
  const [reissueType, setReissueType] = useState<ReissueType>('MISSING_CHECK_IN')
  const [reissueTimeValue, setReissueTimeValue] = useState(reissueTime)
  const [reissueLimit, setReissueLimit] = useState<{ currentMonth: number; limit: number } | null>(null)
  
  // 加班特有字段
  const [overtimeType, setOvertimeType] = useState<OvertimeType>('WEEKDAY')
  const [isCompensatory, setIsCompensatory] = useState(false)
  
  // 出差特有字段
  const [destination, setDestination] = useState('')
  const [companions, setCompanions] = useState<number[]>([])
  const [companionSearch, setCompanionSearch] = useState('')
  const [companionResults, setCompanionResults] = useState<Employee[]>([])
  const [transportation, setTransportation] = useState('')
  const [budget, setBudget] = useState('')

  useEffect(() => {
    if (urlType) {
      setType(urlType)
    }
    if (reissueTime) {
      setReissueTimeValue(reissueTime)
    }
  }, [urlType, reissueTime])

  useEffect(() => {
    // 如果是补卡申请，检查补卡次数限制
    if (type === 'REISSUE') {
      loadReissueLimit()
    }
  }, [type])

  const loadReissueLimit = async () => {
    try {
      const limit = await getReissueLimit()
      setReissueLimit(limit)
    } catch (error) {
      console.error('获取补卡限制失败:', error)
    }
  }

  const handleCompanionSearch = async (keyword: string) => {
    if (!keyword.trim()) {
      setCompanionResults([])
      return
    }
    try {
      const results = await searchEmployees(keyword)
      setCompanionResults(results)
    } catch (error) {
      console.error('搜索员工失败:', error)
    }
  }

  const addCompanion = (employee: Employee) => {
    if (!companions.includes(employee.employeeId)) {
      setCompanions([...companions, employee.employeeId])
    }
    setCompanionSearch('')
    setCompanionResults([])
  }

  const removeCompanion = (employeeId: number) => {
    setCompanions(companions.filter(id => id !== employeeId))
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setMessage('')

    // 基础字段验证
    if (!startTime || !endTime || !reason.trim()) {
      setMessage('请填写完整信息')
      return
    }

    if (new Date(startTime) >= new Date(endTime)) {
      setMessage('结束时间必须晚于开始时间')
      return
    }

    // 补卡申请特殊验证
    if (type === 'REISSUE') {
      if (!reissueTimeValue) {
        setMessage('请选择补卡时间')
        return
      }
      if (reissueLimit && reissueLimit.currentMonth >= reissueLimit.limit) {
        setMessage(`本月补卡次数已达上限（${reissueLimit.limit}次），无法提交补卡申请`)
        return
      }
    }

    // 出差申请特殊验证
    if (type === 'BUSINESS_TRIP') {
      if (!destination.trim()) {
        setMessage('请填写出差地点')
        return
      }
      if (destination.length > 50) {
        setMessage('出差地点不能超过50字')
        return
      }
    }

    setLoading(true)
    try {
      let requestData: ApplicationRequest

      switch (type) {
        case 'LEAVE':
          requestData = {
            type: 'LEAVE',
            startTime,
            endTime,
            reason,
            leaveType,
            attachment: attachment ? await fileToBase64(attachment) : undefined,
            employeeId: currentEmployeeId, // 添加当前用户ID
          } as any
          break
        case 'REISSUE':
          requestData = {
            type: 'REISSUE',
            startTime,
            endTime,
            reason,
            reissueType,
            reissueTime: reissueTimeValue,
            employeeId: currentEmployeeId, // 添加当前用户ID
          } as any
          break
        case 'OVERTIME':
          requestData = {
            type: 'OVERTIME',
            startTime,
            endTime,
            reason,
            overtimeType,
            isCompensatory,
            employeeId: currentEmployeeId, // 添加当前用户ID
          } as any
          break
        case 'BUSINESS_TRIP':
          requestData = {
            type: 'BUSINESS_TRIP',
            startTime,
            endTime,
            reason,
            destination,
            companions: companions.length > 0 ? companions : undefined,
            transportation: transportation || undefined,
            budget: budget ? Number(budget) : undefined,
            employeeId: currentEmployeeId, // 添加当前用户ID
          } as any
          break
        default:
          throw new Error('未知的申请类型')
      }

      const result = await submitApplication(requestData)
      console.log('提交申请结果:', result)
      setMessage('申请提交成功！')
      setTimeout(() => {
        navigate('/applications')
      }, 1500)
    } catch (error) {
      console.error('提交申请错误:', error)
      const errorMessage = error instanceof Error ? error.message : '提交失败，请稍后重试'
      setMessage(errorMessage)
      alert(errorMessage) // 添加alert以便用户看到错误
    } finally {
      setLoading(false)
    }
  }

  const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = reject
      reader.readAsDataURL(file)
    })
  }

  const getTypeText = (t: ApplicationType) => {
    switch (t) {
      case 'LEAVE': return '请假'
      case 'REISSUE': return '补卡'
      case 'OVERTIME': return '加班'
      case 'BUSINESS_TRIP': return '出差'
      default: return t
    }
  }

  const canSubmitReissue = reissueLimit 
    ? reissueLimit.currentMonth < reissueLimit.limit 
    : true

  return (
    <div className="application-form-page">
      <h1>新建申请</h1>

      <form onSubmit={handleSubmit} className="application-form">
        {/* 申请类型 */}
        <label>
          <span>申请类型 *</span>
          <select
            value={type}
            onChange={(e) => setType(e.target.value as ApplicationType)}
            disabled={loading || !!urlType}
          >
            <option value="LEAVE">请假</option>
            <option value="REISSUE">补卡</option>
            <option value="OVERTIME">加班</option>
            <option value="BUSINESS_TRIP">出差</option>
          </select>
        </label>

        {/* 补卡次数提示 */}
        {type === 'REISSUE' && reissueLimit && (
          <div className="info-banner">
            本月补卡次数：{reissueLimit.currentMonth} / {reissueLimit.limit}
            {!canSubmitReissue && <span className="error-text">（已达上限）</span>}
          </div>
        )}

        {/* 补卡特有字段 */}
        {type === 'REISSUE' && (
          <>
            <label>
              <span>补卡类型 *</span>
              <select
                value={reissueType}
                onChange={(e) => setReissueType(e.target.value as ReissueType)}
                disabled={loading}
              >
                <option value="MISSING_CHECK_IN">漏打上班卡</option>
                <option value="MISSING_CHECK_OUT">漏打下班卡</option>
              </select>
            </label>
            <label>
              <span>补卡时间 *</span>
              <input
                type="datetime-local"
                value={reissueTimeValue}
                onChange={(e) => setReissueTimeValue(e.target.value)}
                disabled={loading}
                required
              />
            </label>
          </>
        )}

        {/* 请假特有字段 */}
        {type === 'LEAVE' && (
          <>
            <label>
              <span>请假类型 *</span>
              <select
                value={leaveType}
                onChange={(e) => setLeaveType(e.target.value as LeaveSubType)}
                disabled={loading}
              >
                <option value="SICK_LEAVE">病假</option>
                <option value="ANNUAL_LEAVE">年假</option>
                <option value="PERSONAL_LEAVE">事假</option>
                <option value="MARRIAGE_LEAVE">婚假</option>
                <option value="MATERNITY_LEAVE">产假</option>
                <option value="OTHER">其他</option>
              </select>
            </label>
            <label>
              <span>附件（选填）</span>
              <input
                type="file"
                accept="image/*,.pdf"
                onChange={(e) => setAttachment(e.target.files?.[0] || null)}
                disabled={loading}
              />
              {attachment && <span className="file-name">{attachment.name}</span>}
            </label>
          </>
        )}

        {/* 加班特有字段 */}
        {type === 'OVERTIME' && (
          <>
            <label>
              <span>加班类型 *</span>
              <select
                value={overtimeType}
                onChange={(e) => setOvertimeType(e.target.value as OvertimeType)}
                disabled={loading}
              >
                <option value="WEEKDAY">平日加班</option>
                <option value="WEEKEND">周末加班</option>
                <option value="HOLIDAY">节假日加班</option>
              </select>
            </label>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={isCompensatory}
                onChange={(e) => setIsCompensatory(e.target.checked)}
                disabled={loading}
              />
              <span>是否调休</span>
            </label>
          </>
        )}

        {/* 出差特有字段 */}
        {type === 'BUSINESS_TRIP' && (
          <>
            <label>
              <span>出差地点 *</span>
              <input
                type="text"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                placeholder="不超过50字"
                maxLength={50}
                disabled={loading}
                required
              />
            </label>
            <label>
              <span>随行人员（选填）</span>
              <div className="companion-selector">
                <input
                  type="text"
                  value={companionSearch}
                  onChange={(e) => {
                    setCompanionSearch(e.target.value)
                    handleCompanionSearch(e.target.value)
                  }}
                  placeholder="搜索员工姓名或编号"
                  disabled={loading}
                />
                {companionResults.length > 0 && (
                  <div className="companion-results">
                    {companionResults.map(emp => (
                      <div
                        key={emp.employeeId}
                        className="companion-result-item"
                        onClick={() => addCompanion(emp)}
                      >
                        {emp.name} ({emp.employeeId})
                      </div>
                    ))}
                  </div>
                )}
                {companions.length > 0 && (
                  <div className="companion-list">
                    {companions.map(id => {
                      const emp = companionResults.find(e => e.employeeId === id)
                      return (
                        <span key={id} className="companion-tag">
                          {emp?.name || id}
                          <button
                            type="button"
                            onClick={() => removeCompanion(id)}
                            disabled={loading}
                          >
                            ×
                          </button>
                        </span>
                      )
                    })}
                  </div>
                )}
              </div>
            </label>
            <label>
              <span>交通工具（选填）</span>
              <select
                value={transportation}
                onChange={(e) => setTransportation(e.target.value)}
                disabled={loading}
              >
                <option value="">请选择</option>
                <option value="TRAIN">火车</option>
                <option value="HIGH_SPEED_RAIL">高铁</option>
                <option value="PLANE">飞机</option>
                <option value="CAR">自驾</option>
                <option value="OTHER">其他</option>
              </select>
            </label>
            <label>
              <span>费用预算（选填）</span>
              <input
                type="number"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                placeholder="单位：元"
                min="0"
                disabled={loading}
              />
            </label>
          </>
        )}

        {/* 通用字段 */}
        {type !== 'REISSUE' && (
          <>
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
          </>
        )}

        <label>
          <span>申请理由 *</span>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="请详细说明申请理由（最多200字）"
            rows={5}
            maxLength={200}
            disabled={loading}
            required
          />
          <span className="char-count">{reason.length}/200</span>
        </label>

        <div className="form-actions">
          <button type="button" onClick={() => navigate('/applications')} disabled={loading}>
            取消
          </button>
          <button 
            type="submit" 
            disabled={loading || (type === 'REISSUE' && !canSubmitReissue)}
          >
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
