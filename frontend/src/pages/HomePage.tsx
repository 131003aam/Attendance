import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { checkIn, checkOut, getTodayAttendance, getPositionConfig } from '../api'
import type { AttendanceType, AttendanceRecord, PositionConfig } from '../types'
import axios from 'axios'
import './HomePage.css'

const HomePage = () => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [todayRecord, setTodayRecord] = useState<AttendanceRecord | null>(null)
  const [currentTime, setCurrentTime] = useState(new Date())
  const [location, setLocation] = useState<string>('')
  const [positionConfig, setPositionConfig] = useState<PositionConfig | null>(null)

  useEffect(() => {
    if (user?.employeeId) {
      loadPositionConfig()
      loadTodayAttendance()
    }
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)
    return () => clearInterval(timer)
  }, [user?.employeeId])

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

  const loadPositionConfig = async () => {
    if (!user?.employeeId) {
      setMessage('用户信息不完整，无法加载职务配置')
      return
    }
    // 清除之前的错误消息
    setMessage('')
    try {
      const config = await getPositionConfig(user.employeeId)
      console.log('前端接收到职务配置:', config)
      if (config) {
        console.log('职务配置详情:', {
          id: config.id,
          name: config.name,
          workStartTime: config.workStartTime,
          workEndTime: config.workEndTime
        })
        setPositionConfig(config)
        // 清除错误消息
        setMessage('')
      } else {
        setMessage('未找到职务配置，请联系管理员')
        console.error('职务配置为空，员工ID:', user.employeeId, '职务ID:', user.positionId)
      }
    } catch (error) {
      console.error('加载职务配置失败:', error)
      const errorMessage = error instanceof Error ? error.message : '加载职务配置失败，请稍后重试'
      // 将"Not Found"转换为更友好的消息
      if (errorMessage.includes('Not Found') || errorMessage.includes('404') || errorMessage.includes('not found')) {
        setMessage('未找到职务配置，请联系管理员设置')
      } else if (errorMessage.includes('员工不存在')) {
        setMessage('员工信息不存在，请联系管理员')
      } else if (errorMessage.includes('未分配职务')) {
        setMessage('员工未分配职务，请联系管理员')
      } else {
        setMessage(errorMessage)
      }
    }
  }

  const loadTodayAttendance = async () => {
    if (!user?.employeeId) return
    try {
      const record = await getTodayAttendance(user.employeeId)
      console.log('今日打卡记录:', record)
      // 确保只有真正有打卡记录时才设置
      if (record && (record.checkInTime || record.checkOutTime)) {
        setTodayRecord(record)
      } else {
        // 如果没有打卡记录，设置为null
        setTodayRecord(null)
      }
    } catch (error) {
      // 404是正常的（今天还没有打卡），不显示错误
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        setTodayRecord(null)
        return
      }
      console.error('加载今日打卡记录失败:', error)
      // 发生错误时也设置为null，确保不会显示错误的打卡状态
      setTodayRecord(null)
    }
  }

  const getButtonState = () => {
    // 如果没有职务配置，无法判断打卡时间
    if (!positionConfig) {
      // 如果已经有错误消息，显示错误消息；否则显示加载中
      if (message && message.includes('职务配置')) {
        return { text: message, disabled: true, type: null }
      }
      return { text: '加载职务配置中...', disabled: true, type: null }
    }

    // 如果已经有今日记录，根据记录状态判断
    if (todayRecord) {
      const hasCheckIn = !!todayRecord.checkInTime
      const hasCheckOut = !!todayRecord.checkOutTime

      if (hasCheckIn && hasCheckOut) {
        return { text: '今日已打卡完成', disabled: true, type: null }
      }

      if (hasCheckIn && !hasCheckOut) {
        // 已上班打卡，等待下班打卡
        const currentTimeStr = currentTime.toTimeString().slice(0, 5) // HH:MM
        const workEndTime = formatTime(positionConfig.workEndTime) // 提取HH:MM
        const allowedStartTime = subtractMinutes(workEndTime, 30) // 标准下班时间前30分钟
        const allowedEndTime = addMinutes(workEndTime, 30) // 标准下班时间后30分钟
        
        // 下班打卡允许范围：标准下班时间前30分钟到后30分钟
        if (isTimeInRange(currentTimeStr, allowedStartTime, allowedEndTime)) {
          return { text: '下班打卡', disabled: false, type: 'CHECK_OUT' as AttendanceType }
        }
        if (isTimeBefore(currentTimeStr, allowedStartTime)) {
          return { text: `未到下班打卡时间（${allowedStartTime}开始）`, disabled: true, type: null }
        }
        return { text: `已错过下班打卡时间（最晚${allowedEndTime}）`, disabled: true, type: null }
      }
    }

    // 没有上班打卡记录
    const currentTimeStr = currentTime.toTimeString().slice(0, 5) // HH:MM
    const workStartTime = formatTime(positionConfig.workStartTime) // 提取HH:MM
    const allowedStartTime = subtractMinutes(workStartTime, 30) // 标准上班时间前30分钟
    const allowedEndTime = addMinutes(workStartTime, 30) // 标准上班时间后30分钟
    
    // 上班打卡允许范围：标准上班时间前30分钟到后30分钟
    if (isTimeInRange(currentTimeStr, allowedStartTime, allowedEndTime)) {
      return { text: '上班打卡', disabled: false, type: 'CHECK_IN' as AttendanceType }
    }
    if (isTimeBefore(currentTimeStr, allowedStartTime)) {
      return { text: `未到打卡时间（${allowedStartTime}开始）`, disabled: true, type: null }
    }
    // 超过允许范围，显示已错过
    if (isTimeAfter(currentTimeStr, allowedEndTime)) {
      return { text: `已错过上班打卡时间（最晚${allowedEndTime}）`, disabled: true, type: null }
    }

    return { text: '今日已打卡完成', disabled: true, type: null }
  }

  // 辅助函数：判断时间是否在范围内
  const isTimeInRange = (time: string, start: string, end: string): boolean => {
    return isTimeAfterOrEqual(time, start) && isTimeBeforeOrEqual(time, end)
  }

  // 辅助函数：判断时间是否早于
  const isTimeBefore = (time1: string, time2: string): boolean => {
    return time1 < time2
  }

  // 辅助函数：判断时间是否晚于
  const isTimeAfter = (time1: string, time2: string): boolean => {
    return time1 > time2
  }

  // 辅助函数：判断时间是否早于或等于
  const isTimeBeforeOrEqual = (time1: string, time2: string): boolean => {
    return time1 <= time2
  }

  // 辅助函数：判断时间是否晚于或等于
  const isTimeAfterOrEqual = (time1: string, time2: string): boolean => {
    return time1 >= time2
  }

  // 辅助函数：时间加分钟
  const addMinutes = (time: string, minutes: number): string => {
    const [h, m] = time.split(':').map(Number)
    const totalMinutes = h * 60 + m + minutes
    const newHour = Math.floor(totalMinutes / 60) % 24
    const newMinute = totalMinutes % 60
    return `${String(newHour).padStart(2, '0')}:${String(newMinute).padStart(2, '0')}`
  }

  // 辅助函数：时间减分钟
  const subtractMinutes = (time: string, minutes: number): string => {
    const [h, m] = time.split(':').map(Number)
    const totalMinutes = h * 60 + m - minutes
    const newHour = (Math.floor(totalMinutes / 60) % 24 + 24) % 24
    const newMinute = (totalMinutes % 60 + 60) % 60
    return `${String(newHour).padStart(2, '0')}:${String(newMinute).padStart(2, '0')}`
  }

  // 辅助函数：格式化时间字符串（从HH:mm:ss提取HH:mm）
  const formatTime = (timeStr: string): string => {
    return timeStr.slice(0, 5) // 提取前5个字符（HH:mm）
  }

  const handleAttendance = async () => {
    if (!user?.employeeId) {
      setMessage('用户信息不完整，无法打卡')
      return
    }

    const buttonState = getButtonState()
    if (!buttonState.type || buttonState.disabled) return

    setLoading(true)
    setMessage('')

    try {
      const result = buttonState.type === 'CHECK_IN' 
        ? await checkIn({ location, employeeId: user.employeeId })
        : await checkOut({ location, employeeId: user.employeeId })

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

        {message && message.trim() !== '' && (
          <div className={`attendance-message ${message.includes('成功') ? 'success' : 'error'}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  )
}

export default HomePage

