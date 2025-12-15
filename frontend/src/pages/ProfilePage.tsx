import { useState, FormEvent } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { updatePassword, updatePhone } from '../api'
import './ProfilePage.css'

const ProfilePage = () => {
  const { user, updateUser } = useAuth()
  const [phone, setPhone] = useState(user?.phone || '')
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const handleUpdatePhone = async (e: FormEvent) => {
    e.preventDefault()
    if (!user?.employeeId) {
      setMessage('用户信息不完整')
      return
    }

    if (!phone || phone.length !== 11) {
      setMessage('请输入正确的11位手机号')
      return
    }

    setLoading(true)
    setMessage('')
    try {
      await updatePhone(user.employeeId, phone)
      setMessage('手机号更新成功')
      // 更新用户上下文中的手机号
      updateUser({ phone })
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '更新手机号失败')
    } finally {
      setLoading(false)
    }
  }

  const handleUpdatePassword = async (e: FormEvent) => {
    e.preventDefault()
    setMessage('')
    
    if (!oldPassword || !newPassword || !confirmPassword) {
      setMessage('请填写完整信息')
      return
    }

    if (newPassword !== confirmPassword) {
      setMessage('两次输入的密码不一致')
      return
    }

    if (newPassword.length < 6) {
      setMessage('新密码至少6位')
      return
    }

    if (!user?.employeeId) {
      setMessage('用户信息不完整')
      return
    }

    setLoading(true)
    try {
      await updatePassword(user.employeeId, oldPassword, newPassword)
      setMessage('密码更新成功')
      setOldPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (error) {
      // 检查是否是原密码错误
      const errorMessage = error instanceof Error ? error.message : '更新密码失败'
      if (errorMessage.includes('原密码') || errorMessage.includes('原密码错误') || errorMessage.includes('原密码不对')) {
        setMessage('原密码不对，请重新输入')
      } else {
        setMessage(errorMessage)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="profile-page">
      <h1>个人中心</h1>

      <div className="profile-section">
        <h2>个人信息</h2>
        <div className="info-item">
          <span className="info-label">员工编号：</span>
          <span className="info-value">{user?.employeeId}</span>
        </div>
        <div className="info-item">
          <span className="info-label">姓名：</span>
          <span className="info-value">{user?.name}</span>
        </div>
        <div className="info-item">
          <span className="info-label">部门：</span>
          <span className="info-value">{user?.departmentName || `部门 ${user?.departmentId}`}</span>
        </div>
      </div>

      <div className="profile-section">
        <h2>修改手机号</h2>
        <form onSubmit={handleUpdatePhone} className="profile-form">
          <label>
            <span>手机号</span>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 11))}
              placeholder="请输入11位手机号"
              maxLength={11}
              disabled={loading}
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? '更新中...' : '更新手机号'}
          </button>
        </form>
      </div>

      <div className="profile-section">
        <h2>修改密码</h2>
        <form onSubmit={handleUpdatePassword} className="profile-form">
          <label>
            <span>原密码</span>
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              placeholder="请输入原密码"
              disabled={loading}
            />
          </label>
          <label>
            <span>新密码</span>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="至少6位字符"
              disabled={loading}
            />
          </label>
          <label>
            <span>确认新密码</span>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="请再次输入新密码"
              disabled={loading}
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? '更新中...' : '更新密码'}
          </button>
        </form>
      </div>

      {message && (
        <div className={`profile-message ${message.includes('成功') ? 'success' : 'error'}`}>
          {message}
        </div>
      )}
    </div>
  )
}

export default ProfilePage

