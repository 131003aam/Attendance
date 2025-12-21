// 用户角色
export type UserRole = 'EMPLOYEE' | 'APPROVER' | 'ADMIN'

// 登录请求
export interface LoginRequest {
  employeeId: string
  password: string
}

// 登录响应
export interface LoginResponse {
  message: string
  success: boolean
  employeeId?: number | null
  employeeName?: string | null
  departmentId?: number | null
  positionId?: number | null
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
  // 新增字段
  overtimeHours?: number
  leaveDays?: number
  businessTripDays?: number
  reissueCount?: number
  // 月度/周度汇总
  monthSummary?: {
    totalDays: number
    normalDays: number
    lateDays: number
    earlyLeaveDays: number
    missingDays: number
    workHours: number
  }
  weekSummary?: {
    totalDays: number
    normalDays: number
    lateDays: number
    earlyLeaveDays: number
    missingDays: number
    workHours: number
  }
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

// 申请类型
export type ApplicationType = 'LEAVE' | 'OVERTIME' | 'BUSINESS_TRIP' | 'REISSUE'

// 请假类型
export type LeaveSubType = 'SICK_LEAVE' | 'ANNUAL_LEAVE' | 'PERSONAL_LEAVE' | 'MARRIAGE_LEAVE' | 'MATERNITY_LEAVE' | 'OTHER'

// 加班类型
export type OvertimeType = 'WEEKDAY' | 'WEEKEND' | 'HOLIDAY'

// 补卡类型
export type ReissueType = 'MISSING_CHECK_IN' | 'MISSING_CHECK_OUT'

// 申请状态
export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

// 申请记录
export interface Application {
  id: number
  employeeId: number
  employeeName?: string
  type: ApplicationType
  startTime: string
  endTime: string
  reason: string
  status: ApplicationStatus
  approverId?: number | null
  approverName?: string | null
  approveTime?: string | null
  rejectReason?: string | null
  createdAt?: string
  // 请假特有字段
  leaveType?: LeaveSubType
  attachment?: string
  // 补卡特有字段
  reissueType?: ReissueType
  reissueTime?: string
  // 加班特有字段
  overtimeType?: OvertimeType
  isCompensatory?: boolean
  // 出差特有字段
  destination?: string
  companions?: number[] // 随行人员ID列表
  transportation?: string
  budget?: number
}

// 申请请求（基础字段）
export interface BaseApplicationRequest {
  type: ApplicationType
  startTime: string
  endTime: string
  reason: string
}

// 请假申请请求
export interface LeaveApplicationRequest extends BaseApplicationRequest {
  type: 'LEAVE'
  leaveType: LeaveSubType
  attachment?: string
}

// 补卡申请请求
export interface ReissueApplicationRequest extends BaseApplicationRequest {
  type: 'REISSUE'
  reissueType: ReissueType
  reissueTime: string
}

// 加班申请请求
export interface OvertimeApplicationRequest extends BaseApplicationRequest {
  type: 'OVERTIME'
  overtimeType: OvertimeType
  isCompensatory: boolean
}

// 出差申请请求
export interface BusinessTripApplicationRequest extends BaseApplicationRequest {
  type: 'BUSINESS_TRIP'
  destination: string
  companions?: number[]
  transportation?: string
  budget?: number
}

// 申请请求（联合类型）
export type ApplicationRequest = 
  | LeaveApplicationRequest 
  | ReissueApplicationRequest 
  | OvertimeApplicationRequest 
  | BusinessTripApplicationRequest

// 审批请求
export interface ApprovalRequest {
  applicationId: number
  approved: boolean
  reason?: string
}

// 部门信息
export interface Department {
  id: string // 字符型，长度10，格式：D000000001
  name: string
  description?: string
  managerId?: number
  managerName?: string
}

// 职务配置
export interface PositionConfig {
  id: string // 字符型，长度10，格式：P000000001
  name: string
  workStartTime: string // 格式: "HH:mm"
  workEndTime: string // 格式: "HH:mm"
  monthlyWorkDays?: number // 月工作日，默认21
  dailyWorkHours?: number // 每日工作时长，默认8.0
  description?: string
}


