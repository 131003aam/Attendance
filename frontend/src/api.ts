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
  Department,
  PositionConfig,
} from './types'

const MOCK_MODE = false

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

export const checkIn = async (data: AttendanceRequest & { employeeId: number }): Promise<AttendanceResponse> => {
  if (MOCK_MODE) {
    return mockCheckIn(data)
  }

  try {
    const response = await api.post<AttendanceResponse>('/attendance/check-in', {
      ...data,
      employeeId: data.employeeId,
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const message = error.response.data?.message || '打卡失败，请稍后重试'
      throw new Error(message)
    }
    throw error instanceof Error ? error : new Error('打卡失败，请稍后重试')
  }
}

export const checkOut = async (data: AttendanceRequest & { employeeId: number }): Promise<AttendanceResponse> => {
  if (MOCK_MODE) {
    return mockCheckOut(data)
  }

  try {
    const response = await api.post<AttendanceResponse>('/attendance/check-out', {
      ...data,
      employeeId: data.employeeId,
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const message = error.response.data?.message || '打卡失败，请稍后重试'
      throw new Error(message)
    }
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

export const getTodayAttendance = async (employeeId: number): Promise<AttendanceRecord | null> => {
  if (MOCK_MODE) {
    return mockGetTodayAttendance()
  }

  try {
    const response = await api.get<AttendanceRecord>('/attendance/today', {
      params: { employeeId },
    })
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null
    }
    throw error instanceof Error ? error : new Error('获取打卡记录失败')
  }
}

// 获取职务配置
export const getPositionConfig = async (employeeId: number): Promise<PositionConfig | null> => {
  try {
    console.log('请求职务配置，员工ID:', employeeId)
    const response = await api.get<PositionConfig>('/attendance/position-config', {
      params: { employeeId },
    })
    console.log('职务配置响应:', response.data)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response) {
      const status = error.response.status
      const errorData = error.response.data
      let errorMessage = '获取职务配置失败'
      
      // 尝试从响应中提取错误信息
      if (errorData) {
        if (typeof errorData === 'string') {
          errorMessage = errorData
        } else if (errorData.error) {
          errorMessage = errorData.error
        } else if (errorData.message) {
          errorMessage = errorData.message
        }
      }
      
      if (status === 404) {
        throw new Error(errorMessage)
      }
      throw new Error(errorMessage)
    }
    throw error instanceof Error ? error : new Error('获取职务配置失败')
  }
}

// 获取打卡记录列表
export const getAttendanceRecords = async (params?: {
  employeeId?: number
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
export const getStatistics = async (params?: {
  employeeId?: number
  departmentId?: number
  startDate?: string
  endDate?: string
  type?: 'week' | 'month'
}): Promise<Statistics> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return {
      weekWorkHours: 40.5,
      monthWorkHours: 168.0,
      missingDays: 0,
      lateCount: 2,
      earlyLeaveCount: 1,
      overtimeHours: 5.5,
      leaveDays: 2,
      reissueCount: 1,
    }
  }

  try {
    const response = await api.get<Statistics>('/attendance/statistics', { params })
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取统计信息失败')
  }
}

// 获取考勤记录（支持状态筛选）
export const getAttendanceRecordsWithFilter = async (params?: {
  employeeId?: number
  departmentId?: number
  startDate?: string
  endDate?: string
  status?: 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'MISSING'
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

// 获取申请列表
export const getApplications = async (employeeId?: number, status?: string): Promise<Application[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return []
  }

  try {
    const params: any = {}
    if (employeeId) params.employeeId = employeeId
    if (status) params.status = status
    const response = await api.get<Application[]>('/applications', { params })
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
    if (axios.isAxiosError(error)) {
      if (error.response) {
        const errorData = error.response.data
        const errorMsg = errorData?.message || errorData?.error || '提交申请失败'
        throw new Error(errorMsg)
      }
      if (error.request) {
        throw new Error('无法连接到服务器，请检查后端服务是否运行')
      }
    }
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

// 审批人：获取待审批列表
export const getPendingApplications = async (): Promise<Application[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return []
  }

  try {
    const response = await api.get<Application[]>('/approval/pending')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取待审批列表失败')
  }
}

// 审批人：审批申请
export const reviewApplication = async (data: ApprovalRequest): Promise<Application> => {
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
    const response = await api.post<Application>(`/approval/applications/${data.applicationId}/review`, data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('审批失败')
  }
}

// 撤销申请
export const cancelApplication = async (applicationId: number): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    await api.post(`/applications/${applicationId}/cancel`)
  } catch (error) {
    throw error instanceof Error ? error : new Error('撤销申请失败')
  }
}

// 获取补卡次数限制
// 修改为接受员工ID参数
export const getReissueLimit = async (employeeId?: number): Promise<{ currentMonth: number; limit: number }> => {
    if (MOCK_MODE) {
        await new Promise((resolve) => setTimeout(resolve, 200))
        return { currentMonth: 0, limit: 3 }
    }

    try {
        const response = await api.get<{ currentMonth: number; limit: number }>('/applications/reissue/limit', {
            params: { employeeId } // 传递员工ID作为查询参数
        })
        return response.data
    } catch (error) {
        throw error instanceof Error ? error : new Error('获取补卡限制失败')
    }
}

// 管理员：获取部门列表
export const getDepartments = async (): Promise<Department[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return [
      { id: 1, name: '技术部', description: '负责技术开发' },
      { id: 2, name: '人事部', description: '负责人事管理' },
      { id: 3, name: '财务部', description: '负责财务管理' },
    ]
  }

  try {
    const response = await api.get<Department[]>('/admin/departments')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取部门列表失败')
  }
}

// 管理员：创建部门
export const createDepartment = async (data: Omit<Department, 'id'>): Promise<Department> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { id: Date.now(), ...data }
  }

  try {
    const response = await api.post<Department>('/admin/departments', data)
    console.log('创建部门API响应:', response.data)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response) {
        const errorData = error.response.data
        const errorMsg = errorData?.message || errorData?.error || '创建部门失败'
        throw new Error(errorMsg)
      }
    }
    throw error instanceof Error ? error : new Error('创建部门失败')
  }
}

// 管理员：更新部门
export const updateDepartment = async (id: number | string, data: Partial<Department>): Promise<Department> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { id: typeof id === 'number' ? id : parseInt(id.replace(/[^0-9]/g, '')), ...data } as Department
  }

  try {
    // 将数字ID转换为字符串格式（D000000001）
    const idStr = typeof id === 'number' ? `D${String(id).padStart(9, '0')}` : id
    const response = await api.put<Department>(`/admin/departments/${idStr}`, data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('更新部门失败')
  }
}

// 管理员：删除部门
export const deleteDepartment = async (id: number | string): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    // 将数字ID转换为字符串格式（D000000001）
    const idStr = typeof id === 'number' ? `D${String(id).padStart(9, '0')}` : id
    await api.delete(`/admin/departments/${idStr}`)
  } catch (error: any) {
    // 从响应中提取错误消息
    const errorMessage = error?.response?.data?.message || error?.message || '删除部门失败'
    throw new Error(errorMessage)
  }
}

// 管理员：获取职务配置列表
export const getPositionConfigs = async (): Promise<PositionConfig[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return [
      { id: 1, name: '标准班', workStartTime: '09:00', workEndTime: '18:00', description: '标准工作时间' },
      { id: 2, name: '早班', workStartTime: '08:00', workEndTime: '17:00', description: '早班工作时间' },
      { id: 3, name: '晚班', workStartTime: '14:00', workEndTime: '23:00', description: '晚班工作时间' },
    ]
  }

  try {
    const response = await api.get<PositionConfig[]>('/admin/positions')
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('获取职务配置失败')
  }
}

// 管理员：创建职务配置
export const createPositionConfig = async (data: Omit<PositionConfig, 'id'>): Promise<PositionConfig> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { id: Date.now(), ...data }
  }

  try {
    const response = await api.post<PositionConfig>('/admin/positions', data)
    console.log('创建职务API响应:', response.data)
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error)) {
      if (error.response) {
        const errorData = error.response.data
        const errorMsg = errorData?.message || errorData?.error || '创建职务配置失败'
        throw new Error(errorMsg)
      }
    }
    throw error instanceof Error ? error : new Error('创建职务配置失败')
  }
}

// 管理员：更新职务配置
export const updatePositionConfig = async (id: number | string, data: Partial<PositionConfig>): Promise<PositionConfig> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { id: typeof id === 'number' ? id : parseInt(id.replace(/[^0-9]/g, '')), ...data } as PositionConfig
  }

  try {
    // 将数字ID转换为字符串格式（P000000001）
    const idStr = typeof id === 'number' ? `P${String(id).padStart(9, '0')}` : id
    const response = await api.put<PositionConfig>(`/admin/positions/${idStr}`, data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('更新职务配置失败')
  }
}

// 管理员：删除职务配置
export const deletePositionConfig = async (id: number | string): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    // 将数字ID转换为字符串格式（P000000001）
    const idStr = typeof id === 'number' ? `P${String(id).padStart(9, '0')}` : id
    await api.delete(`/admin/positions/${idStr}`)
  } catch (error: any) {
    // 从响应中提取错误消息
    const errorMessage = error?.response?.data?.message || error?.message || '删除职务配置失败'
    throw new Error(errorMessage)
  }
}

// 管理员：创建员工
export const createEmployee = async (data: Omit<Employee, 'employeeId'>): Promise<Employee> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { employeeId: Date.now(), ...data }
  }

  try {
    const response = await api.post<Employee>('/admin/employees', data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('创建员工失败')
  }
}

// 管理员：更新员工
export const updateEmployee = async (employeeId: number, data: Partial<Employee>): Promise<Employee> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 400))
    return { employeeId, ...data } as Employee
  }

  try {
    const response = await api.put<Employee>(`/admin/employees/${employeeId}`, data)
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('更新员工失败')
  }
}

// 管理员：重置员工密码
export const resetEmployeePassword = async (employeeId: number): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    await api.post(`/admin/employees/${employeeId}/reset-password`)
  } catch (error) {
    throw error instanceof Error ? error : new Error('重置密码失败')
  }
}

// 更新密码
export const updatePassword = async (employeeId: number, oldPassword: string, newPassword: string): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    const response = await api.post<{ success: boolean; message: string }>('/profile/update-password', {
      employeeId,
      oldPassword,
      newPassword
    })
    if (!response.data.success) {
      // 保留后端返回的具体错误信息，特别是"原密码错误"
      throw new Error(response.data.message || '更新密码失败')
    }
  } catch (error: any) {
    // 如果是axios错误，尝试从响应中获取错误信息
    if (error.response && error.response.data && error.response.data.message) {
      throw new Error(error.response.data.message)
    }
    // 如果是已经抛出的Error，直接抛出
    if (error instanceof Error) {
      throw error
    }
    throw new Error('更新密码失败')
  }
}

// 更新手机号
export const updatePhone = async (employeeId: number, phone: string): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    const response = await api.post<{ success: boolean; message: string }>('/profile/update-phone', {
      employeeId,
      phone
    })
    if (!response.data.success) {
      throw new Error(response.data.message || '更新手机号失败')
    }
  } catch (error) {
    throw error instanceof Error ? error : new Error('更新手机号失败')
  }
}

// 管理员：删除员工
export const deleteEmployee = async (employeeId: number): Promise<void> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return
  }

  try {
    await api.delete(`/admin/employees/${employeeId}`)
  } catch (error) {
    throw error instanceof Error ? error : new Error('删除员工失败')
  }
}

// 获取员工列表（用于选择随行人员等）
export const searchEmployees = async (keyword?: string): Promise<Employee[]> => {
  if (MOCK_MODE) {
    await new Promise((resolve) => setTimeout(resolve, 300))
    return []
  }

  try {
    const response = await api.get<Employee[]>('/employees/search', { params: { keyword } })
    return response.data
  } catch (error) {
    throw error instanceof Error ? error : new Error('搜索员工失败')
  }
}

