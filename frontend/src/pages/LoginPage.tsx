import { FormEvent, useState, useRef, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../api'
import { useAuth } from '../contexts/AuthContext'
import type { LoginRequest } from '../types'
import './LoginPage.css'

type Status = 'idle' | 'success' | 'error'

const LoginPage = () => {
  const navigate = useNavigate()
  const { login: authLogin } = useAuth()
  const [employeeId, setEmployeeId] = useState('')
  const [password, setPassword] = useState('')
  const [status, setStatus] = useState<Status>('idle')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [profile, setProfile] = useState<{
    employeeId?: number | null
    name?: string | null
    departmentId?: number | null
    positionId?: number | null
    role?: string | null
  } | null>(null)
  
  const employeeIdInputRef = useRef<HTMLInputElement>(null)
  const passwordInputRef = useRef<HTMLInputElement>(null)

  // 页面加载时自动聚焦到员工编号输入框
  useEffect(() => {
    employeeIdInputRef.current?.focus()
  }, [])

  const handleIdInput = (value: string) => {
    const digitsOnly = value.replace(/\D/g, '')
    if (digitsOnly.length <= 10) {
      setEmployeeId(digitsOnly)
      // 清除之前的错误信息
      if (status === 'error') {
        setStatus('idle')
        setMessage('')
      }
    }
  }

  const handlePasswordInput = (value: string) => {
    setPassword(value)
    // 清除之前的错误信息
    if (status === 'error') {
      setStatus('idle')
      setMessage('')
    }
  }

  const handleReset = () => {
    setEmployeeId('')
    setPassword('')
    setStatus('idle')
    setMessage('')
    setProfile(null)
    employeeIdInputRef.current?.focus()
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMessage('')
    setProfile(null)

    if (!employeeId || !password) {
      setStatus('error')
      setMessage('请输入完整的账号和密码')
      return
    }

    setLoading(true)

    try {
      const payload: LoginRequest = { employeeId, password }
      const response = await login(payload)

      if (response.success && response.employeeId) {
        setStatus('success')
        setMessage('登录成功！正在跳转...')
        
        // 处理ID转换：后端返回String，前端UserInfo使用number
        // 如果后端返回字符串，提取数字部分
        const employeeIdNum = typeof response.employeeId === 'string' 
          ? parseInt(response.employeeId.replace(/^0+/, '') || '0', 10) 
          : (response.employeeId as number)
        const departmentIdNum = typeof response.departmentId === 'string'
          ? parseInt(response.departmentId.replace(/^0+/, '') || '0', 10)
          : (response.departmentId as number || 0)
        const positionIdNum = typeof response.positionId === 'string'
          ? parseInt(response.positionId.replace(/^0+/, '') || '0', 10)
          : (response.positionId as number || 0)
        
        setProfile({
          employeeId: employeeIdNum,
          name: response.employeeName ?? '',
          departmentId: departmentIdNum,
          positionId: positionIdNum,
          role: response.role ?? 'EMPLOYEE',
        })
        
        // 保存用户信息到 AuthContext
        authLogin({
          employeeId: employeeIdNum,
          name: response.employeeName ?? '',
          phone: '',
          departmentId: departmentIdNum,
          positionId: positionIdNum,
          role: (response.role === 'ADMIN' ? 'ADMIN' : 'EMPLOYEE') as 'ADMIN' | 'EMPLOYEE',
        })
        
        // 跳转到首页
        setTimeout(() => {
          navigate('/home')
        }, 1000)
      } else {
        setStatus('error')
        setMessage(response.message || '账号或密码错误，请检查后重试')
        // 错误时聚焦到密码输入框
        setTimeout(() => {
          passwordInputRef.current?.focus()
          passwordInputRef.current?.select()
        }, 100)
      }
    } catch (error) {
      setStatus('error')
      const errorMessage = error instanceof Error 
        ? `网络异常：${error.message}` 
        : '网络连接失败，请检查网络后重试'
      setMessage(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-shell">
      <div className="login-frame">
        <header className="login-header">
          <p className="system-label">ATTENDANCE</p>
          <h1>考勤管理系统</h1>
          <p>请使用分配的员工编号登录</p>
          <Link to="/register" className="register-link">
            还没有账号？立即注册
          </Link>
        </header>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            <span>员工编号</span>
            <input
              ref={employeeIdInputRef}
              inputMode="numeric"
              pattern="\d*"
              placeholder="仅可输入数字"
              value={employeeId}
              onChange={(e) => handleIdInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && employeeId && !password) {
                  e.preventDefault()
                  passwordInputRef.current?.focus()
                }
              }}
              maxLength={10}
              autoComplete="username"
              disabled={loading || status === 'success'}
            />
          </label>
          <label>
            <span>账号密码</span>
            <input
              ref={passwordInputRef}
              type="password"
              placeholder="请输入密码"
              value={password}
              onChange={(e) => handlePasswordInput(e.target.value)}
              maxLength={20}
              autoComplete="current-password"
              disabled={loading || status === 'success'}
            />
          </label>
          <div className="button-group">
            <button 
              type="submit" 
              disabled={loading || status === 'success'}
              className="submit-button"
            >
              {loading ? (
                <>
                  <span className="spinner"></span>
                  <span>验证中...</span>
                </>
              ) : status === 'success' ? (
                '已登录'
              ) : (
                '进入系统'
              )}
            </button>
            {status === 'success' && (
              <button
                type="button"
                onClick={handleReset}
                className="reset-button"
              >
                重新登录
              </button>
            )}
          </div>
        </form>

        {message && (
          <div className={`login-message ${status}`}>
            <p className="message-text">{message}</p>
            {status === 'success' && profile && (
              <div className="profile">
                <div className="profile-item">
                  <span className="profile-label">员工编号：</span>
                  <span className="profile-value">{profile.employeeId}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">姓名：</span>
                  <span className="profile-value">{profile.name || '未设置'}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">部门ID：</span>
                  <span className="profile-value">{profile.departmentId ?? '未设置'}</span>
                </div>
                <div className="profile-item">
                  <span className="profile-label">职务ID：</span>
                  <span className="profile-value">{profile.positionId ?? '未设置'}</span>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default LoginPage


