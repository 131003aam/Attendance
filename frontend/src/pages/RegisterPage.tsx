import { FormEvent, useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api'
import type { RegisterRequest } from '../types'
import './RegisterPage.css'

const RegisterPage = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<RegisterRequest>({
    name: '',
    phone: '',
    employeeId: '',
    departmentId: 1,
    password: '',
    confirmPassword: '',
  })
  const [status, setStatus] = useState<'idle' | 'success' | 'error'>('idle')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  
  const nameInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    nameInputRef.current?.focus()
  }, [])

  const handleInputChange = (field: keyof RegisterRequest, value: string | number) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
    if (status === 'error') {
      setStatus('idle')
      setMessage('')
    }
  }

  const handlePhoneInput = (value: string) => {
    const digitsOnly = value.replace(/\D/g, '')
    if (digitsOnly.length <= 11) {
      handleInputChange('phone', digitsOnly)
    }
  }

  const handleEmployeeIdInput = (value: string) => {
    const digitsOnly = value.replace(/\D/g, '')
    if (digitsOnly.length <= 10) {
      handleInputChange('employeeId', digitsOnly)
    }
  }

  const validateForm = (): boolean => {
    if (!formData.name.trim()) {
      setStatus('error')
      setMessage('请输入姓名')
      return false
    }
    if (!formData.phone || formData.phone.length !== 11) {
      setStatus('error')
      setMessage('请输入正确的11位手机号')
      return false
    }
    if (!formData.employeeId) {
      setStatus('error')
      setMessage('请输入工号')
      return false
    }
    if (!formData.password || formData.password.length < 6) {
      setStatus('error')
      setMessage('密码至少6位')
      return false
    }
    if (formData.password !== formData.confirmPassword) {
      setStatus('error')
      setMessage('两次输入的密码不一致')
      return false
    }
    return true
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setMessage('')

    if (!validateForm()) {
      return
    }

    setLoading(true)

    try {
      const response = await register(formData)
      if (response.success) {
        setStatus('success')
        setMessage('注册成功！正在跳转到登录页面...')
        setTimeout(() => {
          navigate('/login')
        }, 1500)
      } else {
        setStatus('error')
        setMessage(response.message || '注册失败，请稍后重试')
      }
    } catch (error) {
      setStatus('error')
      setMessage(error instanceof Error ? error.message : '注册失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="register-shell">
      <div className="register-frame">
        <header className="register-header">
          <p className="system-label">REGISTER</p>
          <h1>用户注册</h1>
          <p>填写信息完成注册，默认权限为普通员工</p>
          <Link to="/login" className="login-link">
            已有账号？立即登录
          </Link>
        </header>

        <form className="register-form" onSubmit={handleSubmit}>
          <label>
            <span>姓名 *</span>
            <input
              ref={nameInputRef}
              type="text"
              placeholder="请输入真实姓名"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              maxLength={20}
              disabled={loading || status === 'success'}
            />
          </label>

          <label>
            <span>手机号 *</span>
            <input
              type="tel"
              inputMode="numeric"
              placeholder="请输入11位手机号"
              value={formData.phone}
              onChange={(e) => handlePhoneInput(e.target.value)}
              maxLength={11}
              disabled={loading || status === 'success'}
            />
          </label>

          <label>
            <span>工号 *</span>
            <input
              type="text"
              inputMode="numeric"
              placeholder="请输入工号（仅数字）"
              value={formData.employeeId}
              onChange={(e) => handleEmployeeIdInput(e.target.value)}
              maxLength={10}
              disabled={loading || status === 'success'}
            />
          </label>

          <label>
            <span>部门 *</span>
            <select
              value={formData.departmentId}
              onChange={(e) => handleInputChange('departmentId', Number(e.target.value))}
              disabled={loading || status === 'success'}
            >
              <option value={1}>技术部</option>
              <option value={2}>人事部</option>
              <option value={3}>财务部</option>
              <option value={4}>市场部</option>
              <option value={5}>运营部</option>
            </select>
          </label>

          <label>
            <span>密码 *</span>
            <input
              type="password"
              placeholder="至少6位字符"
              value={formData.password}
              onChange={(e) => handleInputChange('password', e.target.value)}
              maxLength={20}
              disabled={loading || status === 'success'}
            />
          </label>

          <label>
            <span>确认密码 *</span>
            <input
              type="password"
              placeholder="请再次输入密码"
              value={formData.confirmPassword}
              onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
              maxLength={20}
              disabled={loading || status === 'success'}
            />
          </label>

          <button type="submit" disabled={loading || status === 'success'} className="submit-button">
            {loading ? (
              <>
                <span className="spinner"></span>
                <span>注册中...</span>
              </>
            ) : status === 'success' ? (
              '注册成功'
            ) : (
              '完成注册'
            )}
          </button>
        </form>

        {message && (
          <div className={`register-message ${status}`}>
            <p>{message}</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default RegisterPage

