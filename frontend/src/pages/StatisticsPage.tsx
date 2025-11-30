import { useState, useEffect } from 'react'
import { getStatistics } from '../api'
import type { Statistics } from '../types'
import './StatisticsPage.css'

const StatisticsPage = () => {
  const [stats, setStats] = useState<Statistics | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadStatistics()
  }, [])

  const loadStatistics = async () => {
    setLoading(true)
    try {
      const data = await getStatistics()
      setStats(data)
    } catch (error) {
      console.error('加载统计信息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="statistics-page"><div className="loading">加载中...</div></div>
  }

  if (!stats) {
    return <div className="statistics-page"><div className="empty-state">暂无统计数据</div></div>
  }

  return (
    <div className="statistics-page">
      <h1>统计汇总</h1>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">本周工作时长</div>
          <div className="stat-value">{stats.weekWorkHours.toFixed(1)} 小时</div>
        </div>

        <div className="stat-card">
          <div className="stat-label">本月工作时长</div>
          <div className="stat-value">{stats.monthWorkHours.toFixed(1)} 小时</div>
        </div>

        <div className="stat-card">
          <div className="stat-label">缺卡天数</div>
          <div className="stat-value">{stats.missingDays} 天</div>
        </div>

        <div className="stat-card">
          <div className="stat-label">迟到次数</div>
          <div className="stat-value warning">{stats.lateCount} 次</div>
        </div>

        <div className="stat-card">
          <div className="stat-label">早退次数</div>
          <div className="stat-value warning">{stats.earlyLeaveCount} 次</div>
        </div>
      </div>
    </div>
  )
}

export default StatisticsPage

