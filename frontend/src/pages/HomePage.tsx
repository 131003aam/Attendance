import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { checkIn, checkOut, getTodayAttendance } from '../api'
import type { AttendanceType, AttendanceRecord } from '../types'
import './HomePage.css'

const HomePage = () => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [todayRecord, setTodayRecord] = useState<AttendanceRecord | null>(null)
  const [currentTime, setCurrentTime] = useState(new Date())
  const [location, setLocation] = useState<string>('')

  useEffect(() => {
    loadTodayAttendance()
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    // 获取位置信息
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords
          setLocation(`${latitude.toFixed(6)}, ${longitude.toFixed(6)}`)
        },
        () => {
          setLocation('位置获取失败')
        }
      )
    }
  }, [])

  const loadTodayAttendance = async () => {
    try {
      const record = await getTodayAttendance()
      setTodayRecord(record)
    } catch (error) {
      console.error('加载今日打卡记录失败:', error)
    }
  }

  const getButtonState = () => {
    const hour = currentTime.getHours()
    const hasCheckIn = !!todayRecord?.checkInTime
    const hasCheckOut = !!todayRecord?.checkOutTime

    if (hasCheckIn && hasCheckOut) {
      return { text: '今日已打卡', disabled: true, type: null }
    }

    if (!hasCheckIn) {
      // 上班打卡时间：7:00-10:00
      if (hour >= 7 && hour < 10) {
        return { text: '上班打卡（7:00-10:00）', disabled: false, type: 'CHECK_IN' as AttendanceType }
      }
      if (hour < 7) {
        return { text: '未到打卡时间（7:00开始）', disabled: true, type: null }
      }
      return { text: '已错过上班打卡时间', disabled: true, type: null }
    }

    if (hasCheckIn && !hasCheckOut) {
      // 下班打卡时间：15:00-21:00
      if (hour >= 15 && hour < 21) {
        return { text: '下班打卡（15:00-21:00）', disabled: false, type: 'CHECK_OUT' as AttendanceType }
      }
      if (hour < 15) {
        return { text: '未到下班打卡时间（15:00开始）', disabled: true, type: null }
      }
      return { text: '已错过下班打卡时间', disabled: true, type: null }
    }

    return { text: '今日已打卡', disabled: true, type: null }
  }

  const handleAttendance = async () => {
    const buttonState = getButtonState()
    if (!buttonState.type || buttonState.disabled) return

    setLoading(true)
    setMessage('')

    try {
      const result = buttonState.type === 'CHECK_IN' 
        ? await checkIn({ location })
        : await checkOut({ location })

      if (result.success) {
        setMessage(`打卡成功！时间：${new Date().toLocaleString()}，位置：${result.record?.checkInLocation || result.record?.checkOutLocation || location}`)
        await loadTodayAttendance()
      } else {
        setMessage(result.message || '打卡失败，请稍后重试')
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '打卡失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  const buttonState = getButtonState()
  const timeString = currentTime.toLocaleTimeString('zh-CN', { hour12: false })

  return (
    <div className="home-page">
      <div className="home-header">
        <h1>欢迎，{user?.name}</h1>
        <p className="current-time">当前时间：{timeString}</p>
      </div>

      <div className="attendance-section">
        <div className="attendance-card">
          <h2>打卡</h2>
          <button
            className={`attendance-button ${buttonState.disabled ? 'disabled' : ''} ${loading ? 'loading' : ''}`}
            onClick={handleAttendance}
            disabled={buttonState.disabled || loading}
          >
            {loading ? (
              <>
                <span className="spinner"></span>
                <span>打卡中...</span>
              </>
            ) : (
              buttonState.text
            )}
          </button>
          {location && (
            <p className="location-info">位置：{location}</p>
          )}
        </div>

        {todayRecord && (
          <div className="today-record">
            <h3>今日打卡记录</h3>
            <div className="record-item">
              <span className="record-label">上班打卡：</span>
              <span className="record-value">
                {todayRecord.checkInTime 
                  ? `${new Date(todayRecord.checkInTime).toLocaleTimeString()} ${todayRecord.checkInLocation ? `(${todayRecord.checkInLocation})` : ''}`
                  : '未打卡'}
              </span>
            </div>
            <div className="record-item">
              <span className="record-label">下班打卡：</span>
              <span className="record-value">
                {todayRecord.checkOutTime 
                  ? `${new Date(todayRecord.checkOutTime).toLocaleTimeString()} ${todayRecord.checkOutLocation ? `(${todayRecord.checkOutLocation})` : ''}`
                  : '未打卡'}
              </span>
            </div>
            <div className="record-item">
              <span className="record-label">状态：</span>
              <span className={`record-status ${todayRecord.status.toLowerCase()}`}>
                {todayRecord.status === 'NORMAL' ? '正常' :
                 todayRecord.status === 'LATE' ? '迟到' :
                 todayRecord.status === 'EARLY_LEAVE' ? '早退' : '缺卡'}
              </span>
            </div>
          </div>
        )}

        {message && (
          <div className={`attendance-message ${message.includes('成功') ? 'success' : 'error'}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  )
}

export default HomePage

