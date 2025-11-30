import { useState, useEffect } from 'react'
import { getAttendanceSummary } from '../../api'
import type { AttendanceRecord } from '../../types'
import './AdminPages.css'

const AdminAttendancePage = () => {
  const [records, setRecords] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  useEffect(() => {
    loadSummary()
  }, [])

  const loadSummary = async () => {
    setLoading(true)
    try {
      const params: any = {}
      if (startDate) params.startDate = startDate
      if (endDate) params.endDate = endDate
      const data = await getAttendanceSummary(params)
      setRecords(data)
    } catch (error) {
      console.error('加载打卡汇总失败:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="admin-page">
      <h1>打卡汇总</h1>

      <div className="filter-section">
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
        <button onClick={loadSummary}>查询</button>
        <button onClick={() => {/* TODO: CSV导出 */}}>导出CSV</button>
      </div>

      {loading ? (
        <div className="loading">加载中...</div>
      ) : (
        <div className="admin-table">
          <table>
            <thead>
              <tr>
                <th>员工</th>
                <th>部门</th>
                <th>日期</th>
                <th>上班打卡</th>
                <th>下班打卡</th>
                <th>状态</th>
                <th>工作时长</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{record.employeeName || `员工 ${record.employeeId}`}</td>
                  <td>-</td>
                  <td>{new Date(record.date).toLocaleDateString('zh-CN')}</td>
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
                  <td>{record.workHours ? `${record.workHours.toFixed(1)}h` : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export default AdminAttendancePage

