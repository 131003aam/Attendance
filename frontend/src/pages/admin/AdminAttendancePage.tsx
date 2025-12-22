import { useState, useEffect } from 'react'
import { getStatistics, getAttendanceRecordsWithFilter, getDepartments, getEmployees } from '../../api'
import type { Statistics, AttendanceRecord, Department, Employee } from '../../types'
import '../StatisticsPage.css'
import './AdminPages.css'

const AdminAttendancePage = () => {
  const [stats, setStats] = useState<Statistics | null>(null)
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  
  // 筛选条件
  const [startDate, setStartDate] = useState(() => {
    const now = new Date()
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
    return firstDay.toISOString().split('T')[0]
  })
  const [endDate, setEndDate] = useState(() => {
    return new Date().toISOString().split('T')[0]
  })
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'MISSING'>('ALL')
  const [viewType, setViewType] = useState<'week' | 'month'>('month')
  const [showDetail, setShowDetail] = useState(false)
  
  // 管理员筛选
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | ''>('')
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | ''>('')
  const [departments, setDepartments] = useState<Department[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])

  useEffect(() => {
    loadDepartments()
  }, [])

  useEffect(() => {
    loadStatistics()
  }, [viewType, selectedDepartmentId, selectedEmployeeId])

  useEffect(() => {
    loadEmployees()
  }, [selectedDepartmentId])

  const loadDepartments = async () => {
    try {
      const data = await getDepartments()
      setDepartments(data)
    } catch (error) {
      console.error('加载部门列表失败:', error)
    }
  }

  const loadEmployees = async () => {
    try {
      const data = await getEmployees()
      if (selectedDepartmentId) {
        const filtered = data.filter(emp => {
          const deptIdStr = String(selectedDepartmentId)
          const empDeptId = String(emp.departmentId)
          return empDeptId === deptIdStr || 
                 empDeptId.replace('D', '') === deptIdStr ||
                 empDeptId === `D${deptIdStr.padStart(9, '0')}`
        })
        setEmployees(filtered)
      } else {
        setEmployees(data)
      }
    } catch (error) {
      console.error('加载员工列表失败:', error)
    }
  }

  const loadStatistics = async () => {
    setLoading(true)
    try {
      const params: any = { type: viewType }
      
      if (selectedEmployeeId) {
        params.employeeId = selectedEmployeeId
      } else if (selectedDepartmentId) {
        params.departmentId = selectedDepartmentId
      }
      
      const data = await getStatistics(params)
      setStats(data)
    } catch (error) {
      console.error('加载统计信息失败:', error)
      alert('加载统计信息失败: ' + (error instanceof Error ? error.message : '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const loadRecords = async () => {
    setLoading(true)
    try {
      const params: any = {}
      
      if (selectedEmployeeId) {
        params.employeeId = selectedEmployeeId
      } else if (selectedDepartmentId) {
        params.departmentId = selectedDepartmentId
      }
      
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      if (statusFilter !== 'ALL') params.status = statusFilter
      
      const data = await getAttendanceRecordsWithFilter(params)
      setRecords(data)
    } catch (error) {
      console.error('加载打卡记录失败:', error)
      alert('加载打卡记录失败: ' + (error instanceof Error ? error.message : '未知错误'))
    } finally {
      setLoading(false)
    }
  }

  const handleQuery = () => {
    loadRecords()
    setShowDetail(true)
  }

  const handleReset = () => {
    const now = new Date()
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
    setStartDate(firstDay.toISOString().split('T')[0])
    setEndDate(new Date().toISOString().split('T')[0])
    setStatusFilter('ALL')
    setSelectedDepartmentId('')
    setSelectedEmployeeId('')
    setShowDetail(false)
    setRecords([])
  }

  const getStatusDates = () => {
    if (!stats || !records.length) return null
    
    const statusMap: Record<string, string[]> = {
      'NORMAL': [],
      'LATE': [],
      'EARLY_LEAVE': [],
      'MISSING': []
    }
    
    if (statusFilter !== 'ALL') {
      records.forEach(record => {
        if (record.status === statusFilter) {
          const date = new Date(record.date).toLocaleDateString('zh-CN')
          statusMap[statusFilter].push(date)
        }
      })
      return { [statusFilter]: statusMap[statusFilter] }
    }
    
    records.forEach(record => {
      const date = new Date(record.date).toLocaleDateString('zh-CN')
      if (record.status === 'NORMAL') {
        statusMap['NORMAL'].push(date)
      } else if (record.status === 'LATE') {
        statusMap['LATE'].push(date)
      } else if (record.status === 'EARLY_LEAVE') {
        statusMap['EARLY_LEAVE'].push(date)
      } else if (record.status === 'MISSING') {
        statusMap['MISSING'].push(date)
      }
    })
    
    return statusMap
  }

  if (loading && !stats) {
    return <div className="statistics-page"><div className="loading">加载中...</div></div>
  }

  const statusDates = getStatusDates()
  const currentSummary = viewType === 'week' ? stats?.weekSummary : stats?.monthSummary

  return (
    <div className="statistics-page">
      <h1>考勤汇总</h1>

      {/* 筛选条件 */}
      <div className="admin-filter-section">
        <h2>筛选条件</h2>
        <div className="filter-row">
          <div className="filter-item">
            <label>部门：</label>
            <select
              value={selectedDepartmentId === '' ? '' : String(selectedDepartmentId)}
              onChange={(e) => {
                const value = e.target.value
                setSelectedDepartmentId(value ? Number(value) : '')
                setSelectedEmployeeId('')
              }}
            >
              <option value="">全部部门</option>
              {departments.map(dept => {
                const deptIdNum = dept.id.replace(/^D0*/, '')
                return (
                  <option key={dept.id} value={deptIdNum}>
                    {dept.name}
                  </option>
                )
              })}
            </select>
          </div>
          <div className="filter-item">
            <label>员工：</label>
            <select
              value={selectedEmployeeId}
              onChange={(e) => setSelectedEmployeeId(e.target.value ? Number(e.target.value) : '')}
            >
              <option value="">全部员工</option>
              {employees.map(emp => (
                <option key={emp.employeeId} value={emp.employeeId}>
                  {emp.name} ({emp.employeeId})
                </option>
              ))}
            </select>
          </div>
          <div className="filter-item">
            <label>查看类型：</label>
            <select 
              value={viewType} 
              onChange={(e) => setViewType(e.target.value as 'week' | 'month')}
            >
              <option value="week">周度</option>
              <option value="month">月度</option>
            </select>
          </div>
          <button onClick={loadStatistics} className="query-btn">刷新统计</button>
        </div>
      </div>

      {/* 综合统计信息 */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">{viewType === 'week' ? '本周' : '本月'}工作时长</div>
            <div className="stat-value">
              {viewType === 'week' 
                ? (stats.weekWorkHours !== undefined ? `${stats.weekWorkHours.toFixed(1)} 小时` : '-')
                : (stats.monthWorkHours !== undefined ? `${stats.monthWorkHours.toFixed(1)} 小时` : '-')}
            </div>
          </div>

          {currentSummary ? (
            <>
              <div className="stat-card">
                <div className="stat-label">正常出勤</div>
                <div className="stat-value success">
                  {currentSummary.normalDays !== undefined ? `${currentSummary.normalDays} 人次` : '-'}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">迟到</div>
                <div className="stat-value warning">
                  {currentSummary.lateDays !== undefined ? `${currentSummary.lateDays} 人次` : '-'}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">早退</div>
                <div className="stat-value warning">
                  {currentSummary.earlyLeaveDays !== undefined ? `${currentSummary.earlyLeaveDays} 人次` : '-'}
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-label">缺卡</div>
                <div className="stat-value error">
                  {currentSummary.missingDays !== undefined ? `${currentSummary.missingDays} 人次` : '-'}
                </div>
              </div>
            </>
          ) : null}

          <div className="stat-card">
            <div className="stat-label">加班时长</div>
            <div className="stat-value">
              {viewType === 'week'
                ? (stats.weekOvertimeHours !== undefined ? `${stats.weekOvertimeHours.toFixed(1)} 小时` : '-')
                : (stats.monthOvertimeHours !== undefined ? `${stats.monthOvertimeHours.toFixed(1)} 小时` : '-')}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-label">请假</div>
            <div className="stat-value">
              {viewType === 'week'
                ? (stats.weekLeaveDays !== undefined ? `${stats.weekLeaveDays} 人次` : '-')
                : (stats.monthLeaveDays !== undefined ? `${stats.monthLeaveDays} 人次` : '-')}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-label">出差</div>
            <div className="stat-value">
              {viewType === 'week'
                ? (stats.weekBusinessTripDays !== undefined ? `${stats.weekBusinessTripDays} 人次` : '-')
                : (stats.monthBusinessTripDays !== undefined ? `${stats.monthBusinessTripDays} 人次` : '-')}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-label">补卡次数</div>
            <div className="stat-value">
              {viewType === 'week'
                ? (stats.weekReissueCount !== undefined ? `${stats.weekReissueCount} 次` : '-')
                : (stats.monthReissueCount !== undefined ? `${stats.monthReissueCount} 次` : '-')}
            </div>
          </div>
        </div>
      )}

      {/* 考勤明细查询 */}
      <div className="filter-section">
        <h2>考勤明细查询</h2>
        <div className="filter-controls">
          <div className="date-inputs">
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              placeholder="开始日期"
            />
            <span>至</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              placeholder="结束日期"
            />
          </div>
          <div className="status-filter">
            <label>状态筛选：</label>
            <select 
              value={statusFilter} 
              onChange={(e) => setStatusFilter(e.target.value as any)}
            >
              <option value="ALL">全部</option>
              <option value="NORMAL">正常</option>
              <option value="LATE">迟到</option>
              <option value="EARLY_LEAVE">早退</option>
              <option value="MISSING">缺卡</option>
            </select>
          </div>
          <div className="button-group">
            <button onClick={handleQuery} className="query-btn">查询</button>
            <button onClick={handleReset} className="reset-btn">重置</button>
          </div>
        </div>
      </div>

      {/* 状态日期列表 */}
      {showDetail && statusDates && (
        <div className="status-dates-section">
          <h2>{statusFilter === 'ALL' ? '特定状态日期' : `${statusFilter === 'NORMAL' ? '正常' : statusFilter === 'LATE' ? '迟到' : statusFilter === 'EARLY_LEAVE' ? '早退' : '缺卡'}日期`}</h2>
          <div className="status-dates-grid">
            {statusFilter === 'ALL' ? (
              <>
                {statusDates['NORMAL'] && statusDates['NORMAL'].length > 0 && (
                  <div className="status-date-group">
                    <h3 className="status-title normal">正常日期</h3>
                    <div className="date-list">
                      {statusDates['NORMAL'].map((date, idx) => (
                        <span key={idx} className="date-tag normal">{date}</span>
                      ))}
                    </div>
                  </div>
                )}
                {statusDates['LATE'] && statusDates['LATE'].length > 0 && (
                  <div className="status-date-group">
                    <h3 className="status-title late">迟到日期</h3>
                    <div className="date-list">
                      {statusDates['LATE'].map((date, idx) => (
                        <span key={idx} className="date-tag late">{date}</span>
                      ))}
                    </div>
                  </div>
                )}
                {statusDates['EARLY_LEAVE'] && statusDates['EARLY_LEAVE'].length > 0 && (
                  <div className="status-date-group">
                    <h3 className="status-title early-leave">早退日期</h3>
                    <div className="date-list">
                      {statusDates['EARLY_LEAVE'].map((date, idx) => (
                        <span key={idx} className="date-tag early-leave">{date}</span>
                      ))}
                    </div>
                  </div>
                )}
                {statusDates['MISSING'] && statusDates['MISSING'].length > 0 && (
                  <div className="status-date-group">
                    <h3 className="status-title missing">缺卡日期</h3>
                    <div className="date-list">
                      {statusDates['MISSING'].map((date, idx) => (
                        <span key={idx} className="date-tag missing">{date}</span>
                      ))}
                    </div>
                  </div>
                )}
              </>
            ) : (
              statusDates[statusFilter] && statusDates[statusFilter].length > 0 ? (
                <div className="status-date-group">
                  <h3 className={`status-title ${statusFilter === 'EARLY_LEAVE' ? 'early-leave' : statusFilter.toLowerCase()}`}>
                    {statusFilter === 'NORMAL' ? '正常' : statusFilter === 'LATE' ? '迟到' : statusFilter === 'EARLY_LEAVE' ? '早退' : '缺卡'}日期
                  </h3>
                  <div className="date-list">
                    {statusDates[statusFilter].map((date, idx) => (
                      <span key={idx} className={`date-tag ${statusFilter === 'EARLY_LEAVE' ? 'early-leave' : statusFilter.toLowerCase()}`}>{date}</span>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="status-date-group">
                  <h3 className={`status-title ${statusFilter === 'EARLY_LEAVE' ? 'early-leave' : statusFilter.toLowerCase()}`}>
                    {statusFilter === 'NORMAL' ? '正常' : statusFilter === 'LATE' ? '迟到' : statusFilter === 'EARLY_LEAVE' ? '早退' : '缺卡'}日期
                  </h3>
                  <div className="date-list">
                    <span className="no-data">无</span>
                  </div>
                </div>
              )
            )}
          </div>
        </div>
      )}

      {/* 每日考勤明细 */}
      {showDetail && (
        <div className="records-section">
          <h2>每日考勤明细</h2>
          {loading ? (
            <div className="loading">加载中...</div>
          ) : records.length === 0 ? (
            <div className="empty-state">
              {statusFilter === 'ALL' ? '暂无打卡记录' : `暂无${statusFilter === 'NORMAL' ? '正常' : statusFilter === 'LATE' ? '迟到' : statusFilter === 'EARLY_LEAVE' ? '早退' : '缺卡'}记录`}
            </div>
          ) : (
            <div className="records-table">
              <table>
                <thead>
                  <tr>
                    <th>日期</th>
                    <th>员工</th>
                    <th>部门</th>
                    <th>上班打卡</th>
                    <th>下班打卡</th>
                    <th>状态</th>
                    <th>工作时长</th>
                  </tr>
                </thead>
                <tbody>
                  {records.map((record) => (
                    <tr key={record.id}>
                      <td>{new Date(record.date).toLocaleDateString('zh-CN')}</td>
                      <td>{record.employeeName || `员工 ${record.employeeId}`}</td>
                      <td>{(record as any).departmentName || employees.find(e => e.employeeId === record.employeeId)?.departmentName || '-'}</td>
                      <td>
                        {record.checkInTime
                          ? new Date(record.checkInTime).toLocaleTimeString('zh-CN')
                          : '-'}
                      </td>
                      <td>
                        {record.checkOutTime
                          ? new Date(record.checkOutTime).toLocaleTimeString('zh-CN')
                          : '-'}
                      </td>
                      <td>
                        <span className={`status-badge ${record.status.toLowerCase()}`}>
                          {record.status === 'NORMAL'
                            ? '正常'
                            : record.status === 'LATE'
                            ? '迟到'
                            : record.status === 'EARLY_LEAVE'
                            ? '早退'
                            : '缺卡'}
                        </span>
                      </td>
                      <td>{record.workHours ? `${record.workHours.toFixed(1)} 小时` : '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default AdminAttendancePage
