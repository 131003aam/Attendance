import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getAttendanceRecords } from '../api'
import type { AttendanceRecord } from '../types'
import './AttendancePage.css'

const AttendancePage = () => {
  const { user } = useAuth()
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [filter, setFilter] = useState<'week' | 'month' | 'custom'>('week')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  useEffect(() => {
    if (user?.employeeId) {
      loadRecords()
    }
  }, [filter, user?.employeeId])

  const loadRecords = async () => {
    if (!user?.employeeId) return
    
    setLoading(true)
    try {
      const employeeId = String(user.employeeId).padStart(10, '0')
      let params: any = {
        employeeId: employeeId
      }
      
      if (filter === 'week') {
        // 本周
        const today = new Date()
        const weekStart = new Date(today)
        weekStart.setDate(today.getDate() - today.getDay())
        params.startDate = weekStart.toISOString().split('T')[0]
        params.endDate = today.toISOString().split('T')[0]
      } else if (filter === 'month') {
        // 本月
        const today = new Date()
        params.startDate = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0]
        params.endDate = today.toISOString().split('T')[0]
      } else if (filter === 'custom' && startDate && endDate) {
        params.startDate = startDate
        params.endDate = endDate
      }
      
      const data = await getAttendanceRecords(params)
      setRecords(data)
    } catch (error) {
      console.error('加载打卡记录失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'NORMAL':
        return '正常'
      case 'LATE':
        return '迟到'
      case 'EARLY_LEAVE':
        return '早退'
      case 'MISSING':
        return '缺卡'
      default:
        return status
    }
  }

  const getStatusClass = (status: string) => {
    return status.toLowerCase()
  }

  return (
    <div className="attendance-page">
      <h1>打卡记录</h1>

      <div className="filter-section">
        <div className="filter-buttons">
          <button
            className={filter === 'week' ? 'active' : ''}
            onClick={() => setFilter('week')}
          >
            本周
          </button>
          <button
            className={filter === 'month' ? 'active' : ''}
            onClick={() => setFilter('month')}
          >
            本月
          </button>
          <button
            className={filter === 'custom' ? 'active' : ''}
            onClick={() => setFilter('custom')}
          >
            自定义
          </button>
        </div>

        {filter === 'custom' && (
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
            <button onClick={loadRecords}>查询</button>
          </div>
        )}
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : records.length === 0 ? (
        <div className="empty-state">暂无打卡记录</div>
      ) : (
        <div className="records-table">
          <table>
            <thead>
              <tr>
                <th>日期</th>
                <th>上班打卡</th>
                <th>下班打卡</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{new Date(record.date).toLocaleDateString('zh-CN')}</td>
                  <td>
                    {record.checkInTime ? (
                      <>
                        {new Date(record.checkInTime).toLocaleTimeString('zh-CN')}
                        {record.checkInLocation && (
                          <span className="location"> ({record.checkInLocation})</span>
                        )}
                      </>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td>
                    {record.checkOutTime ? (
                      <>
                        {new Date(record.checkOutTime).toLocaleTimeString('zh-CN')}
                        {record.checkOutLocation && (
                          <span className="location"> ({record.checkOutLocation})</span>
                        )}
                      </>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusClass(record.status)}`}>
                      {getStatusText(record.status)}
                    </span>
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

export default AttendancePage

