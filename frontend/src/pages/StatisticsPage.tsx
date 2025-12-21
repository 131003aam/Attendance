import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getStatistics, getAttendanceRecordsWithFilter, getDepartments, getEmployees } from '../api'
import type { Statistics, AttendanceRecord, Department, Employee } from '../types'
import './StatisticsPage.css'

const StatisticsPage = () => {
  const { user, isAdmin } = useAuth()
  const [stats, setStats] = useState<Statistics | null>(null)
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  
  // 筛选条件
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'MISSING'>('ALL')
  const [viewType, setViewType] = useState<'week' | 'month'>('month')
  const [showDetail, setShowDetail] = useState(false)
  
  // 管理员筛选
  const [selectedDepartmentId, setSelectedDepartmentId] = useState<number | ''>('')
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | ''>('')
  const [departments, setDepartments] = useState<Department[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])

  useEffect(() => {
    if (user?.employeeId) {
      loadStatistics()
      if (isAdmin) {
        loadDepartments()
      }
    }
  }, [user?.employeeId, viewType, isAdmin])

  useEffect(() => {
    if (isAdmin && selectedDepartmentId) {
      loadEmployees()
    }
  }, [selectedDepartmentId, isAdmin])

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
      // 如果选择了部门，筛选该部门的员工
      if (selectedDepartmentId) {
        const filtered = data.filter(emp => emp.departmentId === selectedDepartmentId)
        setEmployees(filtered)
      } else {
        setEmployees(data)
      }
    } catch (error) {
      console.error('加载员工列表失败:', error)
    }
  }

  const loadStatistics = async () => {
    if (!user?.employeeId) return
    
    setLoading(true)
    try {
      const params: any = {
        type: viewType
      }
      
      if (isAdmin) {
        // 管理员可以筛选
        if (selectedEmployeeId) {
          params.employeeId = selectedEmployeeId
        } else if (selectedDepartmentId) {
          params.departmentId = selectedDepartmentId
        }
      } else {
        // 普通员工只能看自己的
        params.employeeId = user.employeeId
      }
      
      const data = await getStatistics(params)
      setStats(data)
    } catch (error) {
      console.error('加载统计信息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadRecords = async () => {
    if (!user?.employeeId) return
    
    setLoading(true)
    try {
      const params: any = {}
      
      if (isAdmin) {
        if (selectedEmployeeId) {
          params.employeeId = selectedEmployeeId
        } else if (selectedDepartmentId) {
          params.departmentId = selectedDepartmentId
        }
      } else {
        params.employeeId = user.employeeId
      }
      
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      if (statusFilter !== 'ALL') params.status = statusFilter
      
      const data = await getAttendanceRecordsWithFilter(params)
      setRecords(data)
    } catch (error) {
      console.error('加载打卡记录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleQuery = async () => {
      await loadRecords();
      // 同时更新统计信息以保持一致性
      await loadStatistics();
      setShowDetail(true);
  }


    const handleReset = () => {
    setStartDate('')
    setEndDate('')
    setStatusFilter('ALL')
    setSelectedDepartmentId('')
    setSelectedEmployeeId('')
    setShowDetail(false)
    setRecords([])
  }

  // 获取状态筛选的日期列表（根据筛选条件只显示对应状态）
  const getStatusDates = () => {
    if (!stats || !records.length) return null
    
    const statusMap: Record<string, string[]> = {
      'NORMAL': [],
      'LATE': [],
      'EARLY_LEAVE': [],
      'MISSING': []
    }
    
    // 如果选择了特定状态，只统计该状态
    if (statusFilter !== 'ALL') {
      records.forEach(record => {
        if (record.status === statusFilter) {
          const date = new Date(record.date).toLocaleDateString('zh-CN')
          statusMap[statusFilter].push(date)
        }
      })
      // 只返回筛选的状态
      return { [statusFilter]: statusMap[statusFilter] }
    }
    
    // 如果选择全部，统计所有状态
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

  // 根据查询到的记录计算实际统计
  const getQueryStatistics = () => {
    if (!records.length)
        return {
            lateDays: 0,
            earlyLeaveDays: 0,
            missingDays: 0,
            normalDays: 0,
            workHours: 0,
            totalDays: 0
        }
    
    const lateDays = records.filter(r => r.status === 'LATE').length
    const earlyLeaveDays = records.filter(r => r.status === 'EARLY_LEAVE').length
    const missingDays = records.filter(r => r.status === 'MISSING').length
    const normalDays = records.filter(r => r.status === 'NORMAL').length
    const workHours = records.reduce((sum, r) => sum + (r.workHours || 0), 0)
    
    return {
      lateDays,
      earlyLeaveDays,
      missingDays,
      normalDays,
      workHours,
      totalDays: records.length
    }
  }

  if (loading && !stats) {
    return <div className="statistics-page"><div className="loading">加载中...</div></div>
  }

  const statusDates = getStatusDates()

  return (
    <div className="statistics-page">
      <h1>统计汇总</h1>

      {/* 管理员筛选 */}
      {isAdmin && (
        <div className="admin-filter-section">
          <h2>筛选条件</h2>
          <div className="filter-row">
            <div className="filter-item">
              <label>部门：</label>
              <select
                value={selectedDepartmentId}
                onChange={(e) => {
                  setSelectedDepartmentId(e.target.value ? Number(e.target.value) : '')
                  setSelectedEmployeeId('')
                }}
                aria-label="选择部门"
              >
                <option value="">全部部门</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id.replace('D', '')}>
                    {dept.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="filter-item">
              <label>员工：</label>
              <select
                value={selectedEmployeeId}
                onChange={(e) => setSelectedEmployeeId(e.target.value ? Number(e.target.value) : '')}
                disabled={!selectedDepartmentId}
                aria-label="选择员工"
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
                aria-label="选择查看类型"
              >
                <option value="week">周度</option>
                <option value="month">月度</option>
              </select>
            </div>
            <button onClick={loadStatistics} className="query-btn">刷新统计</button>
          </div>
        </div>
      )}

      {/* 本周/本月工作时长 */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">本周工作时长</div>
            <div className="stat-value">{stats.weekWorkHours.toFixed(1)} 小时</div>
          </div>

          <div className="stat-card">
            <div className="stat-label">本月工作时长</div>
            <div className="stat-value">{stats.monthWorkHours.toFixed(1)} 小时</div>
          </div>
        </div>
      )}

      {/* 月度/周度汇总 */}
      {stats && (stats.monthSummary || stats.weekSummary) && (
        <div className="summary-section">
          <h2>{viewType === 'week' ? '周度' : '月度'}考勤汇总</h2>
          <div className="summary-grid">
            {viewType === 'week' && stats.weekSummary && (
              <>
                <div className="summary-item">
                  <span className="summary-label">总天数：</span>
                  <span className="summary-value">{stats.weekSummary.totalDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">正常：</span>
                  <span className="summary-value success">{stats.weekSummary.normalDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">迟到：</span>
                  <span className="summary-value warning">{stats.weekSummary.lateDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">早退：</span>
                  <span className="summary-value warning">{stats.weekSummary.earlyLeaveDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">缺卡：</span>
                  <span className="summary-value error">{stats.weekSummary.missingDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">工作时长：</span>
                  <span className="summary-value">{stats.weekSummary.workHours.toFixed(1)} 小时</span>
                </div>
              </>
            )}
            {viewType === 'month' && stats.monthSummary && (
              <>
                <div className="summary-item">
                  <span className="summary-label">总天数：</span>
                  <span className="summary-value">{stats.monthSummary.totalDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">正常：</span>
                  <span className="summary-value success">{stats.monthSummary.normalDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">迟到：</span>
                  <span className="summary-value warning">{stats.monthSummary.lateDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">早退：</span>
                  <span className="summary-value warning">{stats.monthSummary.earlyLeaveDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">缺卡：</span>
                  <span className="summary-value error">{stats.monthSummary.missingDays} 天</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">工作时长：</span>
                  <span className="summary-value">{stats.monthSummary.workHours.toFixed(1)} 小时</span>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* 其他统计信息 */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">缺卡天数</div>
            <div className="stat-value error">
              {(() => {
                // 如果已经查询了记录，使用查询结果的统计
                const queryStats = getQueryStatistics()
                  if (queryStats && showDetail) {
                      // 查询状态下，缺卡天数 = 应出勤天数 - 正常出勤天数
                      const totalDays = queryStats.totalDays;
                      const normalDays = queryStats.normalDays;
                      // 如果需要考虑应出勤天数，可以根据实际情况调整计算方式
                      const missingDays = totalDays - normalDays;
                      return `${missingDays} 天`;
                  }
                  //if (queryStats) {
                //  return `${queryStats.missingDays} 天`
                //}
                // 否则使用汇总统计
                return viewType === 'week' && stats.weekSummary 
                  ? `${stats.weekSummary.missingDays} 天`
                  : stats.monthSummary 
                  ? `${stats.monthSummary.missingDays} 天`
                  : `${stats.missingDays} 天`
              })()}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-label">迟到天数</div>
            <div className="stat-value warning">
              {(() => {
                // 如果已经查询了记录，使用查询结果的统计
                const queryStats = getQueryStatistics()
                if (queryStats) {
                  return `${queryStats.lateDays} 天`
                }
                // 否则使用汇总统计
                return viewType === 'week' && stats.weekSummary 
                  ? `${stats.weekSummary.lateDays} 天`
                  : stats.monthSummary 
                  ? `${stats.monthSummary.lateDays} 天`
                  : `${stats.lateCount} 天`
              })()}
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-label">早退天数</div>
            <div className="stat-value warning">
              {(() => {
                // 如果已经查询了记录，使用查询结果的统计
                const queryStats = getQueryStatistics()
                if (queryStats) {
                  return `${queryStats.earlyLeaveDays} 天`
                }
                // 否则使用汇总统计
                return viewType === 'week' && stats.weekSummary 
                  ? `${stats.weekSummary.earlyLeaveDays} 天`
                  : stats.monthSummary 
                  ? `${stats.monthSummary.earlyLeaveDays} 天`
                  : `${stats.earlyLeaveCount} 天`
              })()}
            </div>
          </div>

          {stats.overtimeHours !== undefined && (
            <div className="stat-card">
              <div className="stat-label">加班时长</div>
              <div className="stat-value">{stats.overtimeHours.toFixed(1)} 小时</div>
            </div>
          )}

          {stats.leaveDays !== undefined && (
            <div className="stat-card">
              <div className="stat-label">请假天数</div>
              <div className="stat-value">{stats.leaveDays} 天</div>
            </div>
          )}

          {stats.reissueCount !== undefined && (
            <div className="stat-card">
              <div className="stat-label">补卡次数</div>
              <div className="stat-value">{stats.reissueCount} 次</div>
            </div>
          )}
        </div>
      )}

      {/* 查询筛选 */}
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
              aria-label="选择状态筛选"
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
              // 显示所有状态
              <>
                {statusDates['NORMAL'] && statusDates['NORMAL'].length > 0 ? (
                  <div className="status-date-group">
                    <h3 className="status-title normal">正常日期</h3>
                    <div className="date-list">
                      {statusDates['NORMAL'].map((date, idx) => (
                        <span key={idx} className="date-tag normal">{date}</span>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="status-date-group">
                    <h3 className="status-title normal">正常日期</h3>
                    <div className="date-list">
                      <span className="no-data">无</span>
                    </div>
                  </div>
                )}
                {statusDates['LATE'] && statusDates['LATE'].length > 0 ? (
                  <div className="status-date-group">
                    <h3 className="status-title late">迟到日期</h3>
                    <div className="date-list">
                      {statusDates['LATE'].map((date, idx) => (
                        <span key={idx} className="date-tag late">{date}</span>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="status-date-group">
                    <h3 className="status-title late">迟到日期</h3>
                    <div className="date-list">
                      <span className="no-data">无</span>
                    </div>
                  </div>
                )}
                {statusDates['EARLY_LEAVE'] && statusDates['EARLY_LEAVE'].length > 0 ? (
                  <div className="status-date-group">
                    <h3 className="status-title early-leave">早退日期</h3>
                    <div className="date-list">
                      {statusDates['EARLY_LEAVE'].map((date, idx) => (
                        <span key={idx} className="date-tag early-leave">{date}</span>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="status-date-group">
                    <h3 className="status-title early-leave">早退日期</h3>
                    <div className="date-list">
                      <span className="no-data">无</span>
                    </div>
                  </div>
                )}
                {statusDates['MISSING'] && statusDates['MISSING'].length > 0 ? (
                  <div className="status-date-group">
                    <h3 className="status-title missing">缺卡日期</h3>
                    <div className="date-list">
                      {statusDates['MISSING'].map((date, idx) => (
                        <span key={idx} className="date-tag missing">{date}</span>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="status-date-group">
                    <h3 className="status-title missing">缺卡日期</h3>
                    <div className="date-list">
                      <span className="no-data">无</span>
                    </div>
                  </div>
                )}
              </>
            ) : (
              // 只显示筛选的状态
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

export default StatisticsPage
