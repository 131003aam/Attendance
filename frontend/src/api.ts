import axios from 'axios'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  AttendanceRequest,
  AttendanceResponse,
  AttendanceRecord,
  Statistics,
  Employee,
  ApplicationRequest,
  Application,
  ApprovalRequest,
} from './types'

const MOCK_MODE = true

const MOCK_USERS: Record<
  string,
  { password: string; name: string; departmentId: number; positionId: number; role: 'ADMIN' | 'EMPLOYEE' }
> = {
  '10001': { password: '123456', name: '管理员', departmentId: 1, positionId: 1, role: 'ADMIN' },
  '10002': { password: '123456', name: '张三', departmentId: 2, positionId: 2, role: 'EMPLOYEE' },
  '10003': { password: '123456', name: '李四', departmentId: 3, positionId: 3, role: 'EMPLOYEE' },
}

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

const mockLogin = async (credentials: LoginRequest): Promise<LoginResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 400))
  const user = MOCK_USERS[credentials.employeeId]

  if (user && user.password === credentials.password) {
    return {
      message: '登录成功',
      success: true,
      employeeId: Number(credentials.employeeId),
      employeeName: user.name,
      departmentId: user.departmentId,
      positionId: user.positionId,
      role: user.role,
    }
  }

  return {
    message: '账号或密码错误',
    success: false,
  }
}

export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  if (MOCK_MODE) {
    return mockLogin(credentials)
  }

  try {
  const response = await api.post<LoginResponse>('/login', {
    employeeId: Number(credentials.employeeId),
    password: credentials.password,
  })
  return response.data
  } catch (error) {
    // 处理网络错误
    if (axios.isAxiosError(error)) {
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        throw new Error('请求超时，请检查网络连接')
      }
      if (error.response) {
        // 服务器返回了错误状态码
        const status = error.response.status
        if (status === 401) {
          return {
            message: '账号或密码错误',
            success: false,
          }
        }
        if (status === 500) {
          throw new Error('服务器内部错误，请稍后重试')
        }
        if (status >= 400 && status < 500) {
          throw new Error(`请求错误：${error.response.data?.message || '请检查输入信息'}`)
        }
        throw new Error(`服务器错误 (${status})，请稍后重试`)
      }
      if (error.request) {
        // 请求已发出但没有收到响应
        throw new Error('无法连接到服务器，请检查后端服务是否运行')
      }
    }
    // 其他错误
    throw error instanceof Error ? error : new Error('未知错误，请稍后重试')
  }
}

// 注册
const mockRegister = async (data: RegisterRequest): Promise<RegisterResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 600))
  return {
    message: '注册成功',
    success: true,
    employeeId: Number(data.employeeId),
  }
}

export const register = async (data: RegisterRequest): Promise<RegisterResponse> => {
  if (MOCK_MODE) {
    return mockRegister(data)
  }

  try {
    const response = await api.post<RegisterResponse>('/register', data)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const status = error.response.status
      if (status === 400) {
        return {
          message: error.response.data?.message || '注册信息有误，请检查后重试',
          success: false,
        }
      }
    }
    throw error instanceof Error ? error : new Error('注册失败，请稍后重试')
  }
}

// 打卡
const mockCheckIn = async (data: AttendanceRequest): Promise<AttendanceResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 500))
  return {
    message: '上班打卡成功',
    success: true,
    record: {
      id: Date.now(),
      employeeId: 10002,
      date: new Date().toISOString().split('T')[0],
      checkInTime: new Date().toISOString(),
      checkInLocation: data.location || '未知位置',
      status: 'NORMAL',
    },
  }
}

const mockCheckOut = async (data: AttendanceRequest): Promise<AttendanceResponse> => {
  await new Promise((resolve) => setTimeout(resolve, 500))
  return {
    message: '下班打卡成功',
    success: true,
    record: {
      id: Date.now(),
      employeeId: 10002,
      date: new Date().toISOString().split('T')[0],
      checkOutTime: new Date().toISOString(),
      checkOutLocation: data.location || '未知位置',
      status: 'NORMAL',
    },
  }
}

export const checkIn = async (data: AttendanceRequest): Promise<AttendanceResponse> => {
  if (MOCK_MODE) {
    return mockCheckIn(data)
  }

  try {
    const response = await api.post<AttendanceResponse>('/attendance/check-in', data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('打卡失败，请稍后重试')
  }
}

export const checkOut = async (data: AttendanceRequest): Promise<AttendanceResponse> => {
  if (MOCK_MODE) {
    return mockCheckOut(data)
  }

  try {
    const response = await api.post<AttendanceResponse>('/attendance/check-out', data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('打卡失败，请稍后重试')
  }
}

// 获取今日打卡记录
const mockGetTodayAttendance = async (): Promise<AttendanceRecord | null> => {
  await new Promise((resolve) => setTimeout(resolve, 300))
  const today = new Date().toISOString().split('T')[0]
  const hour = new Date().getHours()
  
  if (hour < 7) {
    return null
  }
  
  return {
    id: 1,
    employeeId: 10002,
    date: today,
    checkInTime: hour >= 7 && hour < 10 ? new Date().toISOString() : `2024-01-01T08:30:00Z`,
    checkInLocation: '公司',
    checkOutTime: hour >= 15 ? new Date().toISOString() : null,
    checkOutLocation: hour >= 15 ? '公司' : null,
    status: hour < 8 ? 'LATE' : 'NORMAL',
  }
}

export const getTodayAttendance = async (): Promise<AttendanceRecord | null> => {
  if (MOCK_MODE) {
    return mockGetTodayAttendance()
  }

  try {
    const response = await api.get<AttendanceRecord>('/attendance/today')
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null
    }
    throw error instanceof Error ? error : new Error('获取打卡记录失败')
  }
}

// 获取打卡记录列表
export const getAttendanceRecords = async (params?: {
  startDate?: string
  endDate?: string
  type?: 'week' | 'month'
}): Promise<AttendanceRecord[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return []
  }

  try {
    const response = await api.get<AttendanceRecord[]>('/attendance/records', { params })
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取打卡记录失败')
  }
}

// 获取统计信息
export const getStatistics = async (): Promise<Statistics> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return {
      weekWorkHours: 40.5,
      monthWorkHours: 168.0,
      missingDays: 0,
      lateCount: 2,
      earlyLeaveCount: 1,
    }
  }

  try {
    const response = await api.get<Statistics>('/attendance/statistics')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取统计信息失败')
  }
}

// 获取申请列表
export const getApplications = async (): Promise<Application[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return []
  }

  try {
    const response = await api.get<Application[]>('/applications')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取申请列表失败')
  }
}

// 提交申请
export const submitApplication = async (data: ApplicationRequest): Promise<Application> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 500))
    return {
      id: Date.now(),
      employeeId: 10002,
      type: data.type,
      startTime: data.startTime,
      endTime: data.endTime,
      reason: data.reason,
      status: 'PENDING',
    }
  }

  try {
    const response = await api.post<Application>('/applications', data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('提交申请失败')
  }
}

// 管理员：获取员工列表
export const getEmployees = async (): Promise<Employee[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return [
      {
        employeeId: 10001,
        name: '管理员',
        phone: '13800138000',
        departmentId: 1,
        departmentName: '技术部',
        positionId: 1,
        status: 'ACTIVE',
        role: 'ADMIN',
      },
      {
        employeeId: 10002,
        name: '张三',
        phone: '13800138001',
        departmentId: 2,
        departmentName: '人事部',
        positionId: 2,
        status: 'ACTIVE',
        role: 'EMPLOYEE',
      },
    ]
  }

  try {
    const response = await api.get<Employee[]>('/admin/employees')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取员工列表失败')
  }
}

// 管理员：获取打卡汇总
export const getAttendanceSummary = async (params?: {
  departmentId?: number
  employeeId?: number
  startDate?: string
  endDate?: string
}): Promise<AttendanceRecord[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return []
  }

  try {
    const response = await api.get<AttendanceRecord[]>('/admin/attendance', { params })
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取打卡汇总失败')
  }
}

// 管理员：审批申请
export const approveApplication = async (data: ApprovalRequest): Promise<Application> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return {
      id: data.applicationId,
      employeeId: 10002,
      type: 'LEAVE',
      startTime: '',
      endTime: '',
      reason: '',
      status: data.approved ? 'APPROVED' : 'REJECTED',
    }
  }

  try {
    const response = await api.post<Application>(`/admin/applications/${data.applicationId}/approve`, data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('审批失败')
  }
}

