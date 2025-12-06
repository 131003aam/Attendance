// 用户角色
export type UserRole = 'EMPLOYEE' | 'ADMIN'

// 登录请求
export interface LoginRequest {
  employeeId: string
  password: string
}

// 登录响应
export interface LoginResponse {
  message: string
  success: boolean
  employeeId?: number | string | null  // 支持number和string类型
  employeeName?: string | null
  departmentId?: number | string | null  // 支持number和string类型
  positionId?: number | string | null  // 支持number和string类型
  role?: UserRole | null
  token?: string | null
}

// 注册请求
export interface RegisterRequest {
  name: string
  phone: string
  employeeId: string
  departmentId: number
  password: string
  confirmPassword: string
}

// 注册响应
export interface RegisterResponse {
  message: string
  success: boolean
  employeeId?: number | null
}

// 用户信息
export interface UserInfo {
  employeeId: number
  name: string
  phone: string
  departmentId: number
  departmentName?: string
  positionId: number
  positionName?: string
  role: UserRole
}

// 打卡类型
export type AttendanceType = 'CHECK_IN' | 'CHECK_OUT'

// 打卡状态
export type AttendanceStatus = 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'MISSING'

// 打卡记录
export interface AttendanceRecord {
  id: number
  employeeId: number
  employeeName?: string
  date: string
  checkInTime?: string | null
  checkInLocation?: string | null
  checkOutTime?: string | null
  checkOutLocation?: string | null
  status: AttendanceStatus
  workHours?: number | null
}

// 打卡请求
export interface AttendanceRequest {
  type: AttendanceType
  latitude?: number
  longitude?: number
  location?: string
}

// 打卡响应
export interface AttendanceResponse {
  message: string
  success: boolean
  record?: AttendanceRecord
}

// 统计信息
export interface Statistics {
  weekWorkHours: number
  monthWorkHours: number
  missingDays: number
  lateCount: number
  earlyLeaveCount: number
}

// 员工信息（管理员用）
export interface Employee {
  employeeId: number
  name: string
  phone: string
  departmentId: number
  departmentName?: string
  positionId: number
  positionName?: string
  status: 'ACTIVE' | 'INACTIVE'
  role: UserRole
}

// 请假/加班/出差类型
export type LeaveType = 'LEAVE' | 'OVERTIME' | 'BUSINESS_TRIP'

// 申请状态
export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

// 申请记录
export interface Application {
  id: number
  employeeId: number
  employeeName?: string
  type: LeaveType
  startTime: string
  endTime: string
  reason: string
  status: ApplicationStatus
  approverId?: number | null
  approverName?: string | null
  approveTime?: string | null
  rejectReason?: string | null
}

// 申请请求
export interface ApplicationRequest {
  type: LeaveType
  startTime: string
  endTime: string
  reason: string
}

// 审批请求
export interface ApprovalRequest {
  applicationId: number
  approved: boolean
  reason?: string
}


