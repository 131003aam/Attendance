package com.attendance.management.system.service;

import com.attendance.management.system.dao.ApplicationDAO;
import com.attendance.management.system.dao.AttendanceRecordDAO;
import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.dao.PositionConfigDAO;
import com.attendance.management.system.entity.Application;
import com.attendance.management.system.entity.AttendanceRecord;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.entity.PositionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRecordDAO attendanceRecordDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private PositionConfigDAO positionConfigDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private EmployeeService employeeService;


    /**
     * 获取今日考勤记录，并根据当前职务配置重新计算状态
     */
    public AttendanceRecord getTodayAttendance(Integer employeeId) {
        try {
            AttendanceRecord record = attendanceRecordDAO.findByEmployeeIdAndDate(employeeId, LocalDate.now());
            if (record == null) {
                return null;
            }
            
            // 根据当前职务配置重新计算考勤状态
            Employee employee = employeeDAO.findByEmployeeId(employeeId);
            if (employee != null && employee.getPid() != null && !employee.getPid().trim().isEmpty()) {
                PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
                if (positionConfig != null && positionConfig.getWorkStartTime() != null && record.getCheckInTime() != null) {
                    // 重新判断是否迟到
                    LocalTime workStartTime = positionConfig.getWorkStartTime().toLocalTime();
                    LocalTime checkInTime = record.getCheckInTime().toLocalTime();
                    
                    String newStatus = "NORMAL";
                    if (checkInTime.isAfter(workStartTime)) {
                        newStatus = "LATE";
                    }
                    
                    // 如果已下班打卡，判断是否早退
                    if (record.getCheckOutTime() != null && positionConfig.getWorkEndTime() != null) {
                        LocalTime workEndTime = positionConfig.getWorkEndTime().toLocalTime();
                        LocalTime checkOutTime = record.getCheckOutTime().toLocalTime();
                        
                        if (checkOutTime.isBefore(workEndTime)) {
                            if ("LATE".equals(newStatus)) {
                                // 既迟到又早退，保持LATE状态
                            } else {
                                newStatus = "EARLY_LEAVE";
                            }
                        }
                    }
                    
                    // 更新状态
                    record.setStatus(newStatus);
                }
            }
            
            return record;
        } catch (Exception e) {
            System.err.println("获取今日考勤记录失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取今日考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上班打卡
     * @param employeeId 员工ID
     * @param location 打卡位置
     * @return 打卡结果信息
     */
    public String checkIn(Integer employeeId, String location) {
        try {
            // 获取员工信息
            Employee employee = employeeDAO.findByEmployeeId(employeeId);
            if (employee == null) {
                return "员工不存在";
            }

            // 检查员工是否分配了职务
            if (employee.getPid() == null || employee.getPid().trim().isEmpty()) {
                return "员工未分配职务，无法打卡";
            }

            // 获取职务配置
            PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
            if (positionConfig == null) {
                return "未找到职务配置";
            }

            if (positionConfig.getWorkStartTime() == null) {
                return "职务配置不完整，缺少上班时间";
            }

            LocalDate today = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            LocalTime workStartTime = positionConfig.getWorkStartTime().toLocalTime();
            
            // 计算允许的打卡时间范围：标准上班时间前30分钟到后30分钟
            LocalTime allowedStartTime = workStartTime.minusMinutes(30);
            LocalTime allowedEndTime = workStartTime.plusMinutes(30);
            
            // 验证打卡时间：必须在标准上班时间前后30分钟内
            if (currentTime.isBefore(allowedStartTime)) {
                return "未到打卡时间（" + allowedStartTime + "开始）";
            }
            if (currentTime.isAfter(allowedEndTime)) {
                return "已错过上班打卡时间（最晚" + allowedEndTime + "）";
            }

            // 检查今日是否已打卡
            AttendanceRecord todayRecord = attendanceRecordDAO.findByEmployeeIdAndDate(employeeId, today);
            if (todayRecord != null && todayRecord.getCheckInTime() != null) {
                return "今日已上班打卡，不能重复打卡";
            }

            // 创建或更新考勤记录
            if (todayRecord == null) {
                todayRecord = new AttendanceRecord();
                todayRecord.setEid(employeeId);
                todayRecord.setRecordDate(today);
                todayRecord.setStatus("NORMAL");
            }

            LocalDateTime checkInDateTime = LocalDateTime.now();
            todayRecord.setCheckInTime(checkInDateTime);
            if (location != null) {
                todayRecord.setCheckInLocation(location);
            }
            
            // 判断是否迟到：打卡时间晚于标准上班时间算迟到
            if (currentTime.isAfter(workStartTime)) {
                todayRecord.setStatus("LATE");
            } else {
                todayRecord.setStatus("NORMAL");
            }

            if (todayRecord.getAid() == null) {
                attendanceRecordDAO.insert(todayRecord);
            } else {
                attendanceRecordDAO.update(todayRecord);
            }

            return "上班打卡成功";
        } catch (Exception e) {
            System.err.println("上班打卡失败: " + e.getMessage());
            e.printStackTrace();
            return "上班打卡失败: " + e.getMessage();
        }
    }

    /**
     * 下班打卡
     * @param employeeId 员工ID
     * @param location 打卡位置
     * @return 打卡结果信息
     */
    public String checkOut(Integer employeeId, String location) {
        try {
            // 获取员工信息
            Employee employee = employeeDAO.findByEmployeeId(employeeId);
            if (employee == null) {
                return "员工不存在";
            }

            // 检查员工是否分配了职务
            if (employee.getPid() == null || employee.getPid().trim().isEmpty()) {
                return "员工未分配职务，无法打卡";
            }

            // 获取职务配置
            PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
            if (positionConfig == null) {
                return "未找到职务配置";
            }

            if (positionConfig.getWorkEndTime() == null) {
                return "职务配置不完整，缺少下班时间";
            }

            LocalDate today = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            LocalTime workEndTime = positionConfig.getWorkEndTime().toLocalTime();
            
            // 计算允许的打卡时间范围：标准下班时间前30分钟到后30分钟
            LocalTime allowedStartTime = workEndTime.minusMinutes(30);
            LocalTime allowedEndTime = workEndTime.plusMinutes(30);
            
            // 验证打卡时间：必须在标准下班时间前后30分钟内
            if (currentTime.isBefore(allowedStartTime)) {
                return "未到下班打卡时间（" + allowedStartTime + "开始）";
            }
            if (currentTime.isAfter(allowedEndTime)) {
                return "已错过下班打卡时间（最晚" + allowedEndTime + "）";
            }

            // 检查今日是否已打卡
            AttendanceRecord todayRecord = attendanceRecordDAO.findByEmployeeIdAndDate(employeeId, today);
            if (todayRecord == null || todayRecord.getCheckInTime() == null) {
                return "请先完成上班打卡";
            }
            if (todayRecord.getCheckOutTime() != null) {
                return "今日已下班打卡，不能重复打卡";
            }

            // 更新考勤记录
            LocalDateTime checkOutDateTime = LocalDateTime.now();
            todayRecord.setCheckOutTime(checkOutDateTime);
            if (location != null) {
                todayRecord.setCheckOutLocation(location);
            }
            
            // 判断是否早退：打卡时间早于标准下班时间算早退
            String currentStatus = todayRecord.getStatus();
            if (currentTime.isBefore(workEndTime)) {
                // 早退
                if ("LATE".equals(currentStatus)) {
                    // 如果既迟到又早退，保持LATE状态
                    // 不改变状态
                } else {
                    todayRecord.setStatus("EARLY_LEAVE");
                }
            } else {
                // 正常下班，但如果之前是迟到，保持LATE状态
                if (!"LATE".equals(currentStatus)) {
                    todayRecord.setStatus("NORMAL");
                }
            }
            
            // 重新计算工作时长（必须在下班打卡时计算）
            if (todayRecord.getCheckInTime() != null && todayRecord.getCheckOutTime() != null) {
                long minutes = java.time.Duration.between(
                        todayRecord.getCheckInTime().toLocalTime(),
                        todayRecord.getCheckOutTime().toLocalTime()
                ).toMinutes();
                // 确保工作时长为正数
                if (minutes > 0) {
                    todayRecord.setWorkHours(BigDecimal.valueOf(minutes / 60.0));
                } else {
                    todayRecord.setWorkHours(BigDecimal.ZERO);
                }
                System.out.println("计算工作时长: " + minutes + " 分钟 = " + todayRecord.getWorkHours() + " 小时");
            } else {
                System.err.println("警告: 无法计算工作时长，checkInTime或checkOutTime为空");
            }

            attendanceRecordDAO.update(todayRecord);

            return "下班打卡成功";
        } catch (Exception e) {
            System.err.println("下班打卡失败: " + e.getMessage());
            e.printStackTrace();
            return "下班打卡失败: " + e.getMessage();
        }
    }

    /**
     * 获取考勤记录列表（支持状态筛选和部门筛选）
     */
    public List<AttendanceRecord> getAttendanceRecords(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate, String status) {
        if (employeeId != null) {
            if (startDate != null && endDate != null) {
                if (status != null && !status.isEmpty()) {
                    return attendanceRecordDAO.findByEmployeeIdAndDateRangeAndStatus(employeeId, startDate, endDate, status);
                } else {
                    return attendanceRecordDAO.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
                }
            }
            return attendanceRecordDAO.findByEmployeeId(employeeId);
        } else if (departmentId != null) {
            if (startDate != null && endDate != null) {
                if (status != null && !status.isEmpty()) {
                    return attendanceRecordDAO.findByDepartmentIdAndDateRangeAndStatus(departmentId, startDate, endDate, status);
                } else {
                    return attendanceRecordDAO.findByDepartmentIdAndDateRange(departmentId, startDate, endDate);
                }
            }
            return new ArrayList<>();
        } else {
            // 查询全部员工的记录
            if (startDate != null && endDate != null) {
                if (status != null && !status.isEmpty()) {
                    return attendanceRecordDAO.findAllByDateRangeAndStatus(startDate, endDate, status);
                } else {
                    return attendanceRecordDAO.findAllByDateRange(startDate, endDate);
                }
            } else {
                // 如果没有日期范围，查询所有记录（但通常应该有日期范围）
                // 如果只指定了状态，需要特殊处理（暂时返回空列表，建议前端必须提供日期范围）
                if (status != null && !status.isEmpty()) {
                    // 如果只有状态筛选但没有日期，返回空列表
                    return new ArrayList<>();
                }
                return attendanceRecordDAO.findAll();
            }
        }
    }

    /**
     * 获取统计信息（支持员工、部门筛选）
     */
    public Map<String, Object> getStatistics(Integer employeeId, Integer departmentId, String type) {
        System.out.println("========== AttendanceService.getStatistics ==========");
        System.out.println("employeeId: " + employeeId);
        System.out.println("departmentId: " + departmentId);
        System.out.println("type: " + type);
        
        Map<String, Object> stats = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = LocalDate.of(now.getYear(), now.getMonth(), 1);
        
        System.out.println("weekStart: " + weekStart);
        System.out.println("monthStart: " + monthStart);
        System.out.println("now: " + now);
        
        // 获取记录
        List<AttendanceRecord> weekRecords;
        List<AttendanceRecord> monthRecords;
        
        if (employeeId != null) {
            System.out.println("查询单个员工: " + employeeId);
            weekRecords = attendanceRecordDAO.findByEmployeeIdAndDateRange(employeeId, weekStart, now);
            monthRecords = attendanceRecordDAO.findByEmployeeIdAndDateRange(employeeId, monthStart, now);
        } else if (departmentId != null) {
            System.out.println("查询部门: " + departmentId);
            weekRecords = attendanceRecordDAO.findByDepartmentIdAndDateRange(departmentId, weekStart, now);
            monthRecords = attendanceRecordDAO.findByDepartmentIdAndDateRange(departmentId, monthStart, now);
        } else {
            // 查询全部员工的记录
            System.out.println("查询全部员工");
            weekRecords = attendanceRecordDAO.findAllByDateRange(weekStart, now);
            monthRecords = attendanceRecordDAO.findAllByDateRange(monthStart, now);
        }
        
        System.out.println("本周记录数: " + weekRecords.size());
        System.out.println("本月记录数: " + monthRecords.size());

        // 计算本周工作时长
        double weekWorkHours = weekRecords.stream()
                .filter(r -> r.getWorkHours() != null)
                .mapToDouble(r -> r.getWorkHours().doubleValue())
                .sum();

        // 计算本月工作时长
        double monthWorkHours = monthRecords.stream()
                .filter(r -> r.getWorkHours() != null)
                .mapToDouble(r -> r.getWorkHours().doubleValue())
                .sum();

        System.out.println("本周工作时长: " + weekWorkHours);
        System.out.println("本月工作时长: " + monthWorkHours);
        
        // 计算汇总信息
        // 注意：当查询部门或全部员工时，employeeId为null，calculateSummary会正确处理
        Map<String, Object> weekSummary = calculateSummary(weekRecords, weekStart, now, employeeId, departmentId);
        Map<String, Object> monthSummary = calculateSummary(monthRecords, monthStart, now, employeeId, departmentId);
        
        System.out.println("周度汇总: " + weekSummary);
        System.out.println("月度汇总: " + monthSummary);
        
        // 计算本月缺卡天数
        Object missingDaysObj = monthSummary.get("missingDays");
        int missingDays = missingDaysObj != null ? ((Number) missingDaysObj).intValue() : 0;
        
        // 计算迟到和早退次数（月度）
        long lateCount = monthRecords.stream()
                .filter(r -> "LATE".equals(r.getStatus()))
                .count();
        
        long earlyLeaveCount = monthRecords.stream()
                .filter(r -> "EARLY_LEAVE".equals(r.getStatus()))
                .count();
        
        // 计算周度的加班时长、请假人次、出差人次、补卡次数（从application表查询）
        double weekOvertimeHours = calculateOvertimeHours(employeeId, departmentId, weekStart, now);
        int weekLeaveDays = calculateLeaveDays(employeeId, departmentId, weekStart, now);
        int weekBusinessTripDays = calculateBusinessTripDays(employeeId, departmentId, weekStart, now);
        int weekReissueCount = calculateReissueCount(employeeId, departmentId, weekStart, now);
        
        // 计算月度的加班时长、请假人次、出差人次、补卡次数（从application表查询）
        double monthOvertimeHours = calculateOvertimeHours(employeeId, departmentId, monthStart, now);
        int monthLeaveDays = calculateLeaveDays(employeeId, departmentId, monthStart, now);
        int monthBusinessTripDays = calculateBusinessTripDays(employeeId, departmentId, monthStart, now);
        int monthReissueCount = calculateReissueCount(employeeId, departmentId, monthStart, now);
        
        // 如果是单个员工查询，计算请假天数（天数跨度），用于个人界面显示
        int monthLeaveDaysCount = 0;
        int weekLeaveDaysCount = 0;
        if (employeeId != null) {
            // 个人查询：计算请假天数（统计日期跨度）
            weekLeaveDaysCount = calculateLeaveDaysCount(employeeId, weekStart, now);
            monthLeaveDaysCount = calculateLeaveDaysCount(employeeId, monthStart, now);
        }
        
        stats.put("weekWorkHours", weekWorkHours);
        stats.put("monthWorkHours", monthWorkHours);
        stats.put("missingDays", missingDays);
        stats.put("lateCount", (int)lateCount);
        stats.put("earlyLeaveCount", (int)earlyLeaveCount);
        
        // 周度数据
        stats.put("weekOvertimeHours", weekOvertimeHours);
        stats.put("weekLeaveDays", weekLeaveDays);
        stats.put("weekBusinessTripDays", weekBusinessTripDays);
        stats.put("weekReissueCount", weekReissueCount);
        
        // 月度数据
        stats.put("monthOvertimeHours", monthOvertimeHours);
        stats.put("monthLeaveDays", monthLeaveDays);
        stats.put("monthBusinessTripDays", monthBusinessTripDays);
        stats.put("monthReissueCount", monthReissueCount);
        
        // 为了向后兼容，保留旧的字段名（使用月度数据）
        stats.put("overtimeHours", monthOvertimeHours);
        stats.put("leaveDays", monthLeaveDays);  // 人次（用于管理员界面）
        stats.put("businessTripDays", monthBusinessTripDays);
        stats.put("reissueCount", monthReissueCount);
        
        // 个人界面专用：请假天数（天数跨度）
        if (employeeId != null) {
            stats.put("weekLeaveDaysCount", weekLeaveDaysCount);
            stats.put("monthLeaveDaysCount", monthLeaveDaysCount);
            // 个人界面使用天数，覆盖 leaveDays 字段
            stats.put("leaveDays", monthLeaveDaysCount);
        }
        
        stats.put("weekSummary", weekSummary);
        stats.put("monthSummary", monthSummary);
        
        return stats;
    }
    
    /**
     * 计算汇总信息
     */
    private Map<String, Object> calculateSummary(List<AttendanceRecord> records, LocalDate startDate, LocalDate endDate, Integer employeeId, Integer departmentId) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalDays = 0;
        int normalDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int missingDays = 0;
        double workHours = 0.0;
        
        // 正常出勤改为人次：计算所有正常上下班的次数（有checkInTime和checkOutTime的记录）
        normalDays = (int) records.stream()
                .filter(r -> "NORMAL".equals(r.getStatus()))
                .filter(r -> r.getCheckInTime() != null && r.getCheckOutTime() != null)
                .count();
        
        lateDays = (int) records.stream()
                .filter(r -> "LATE".equals(r.getStatus()))
                .count();
        
        earlyLeaveDays = (int) records.stream()
                .filter(r -> "EARLY_LEAVE".equals(r.getStatus()))
                .count();

        workHours = records.stream()
                .filter(r -> r.getWorkHours() != null)
                .mapToDouble(r -> r.getWorkHours().doubleValue())
                .sum();

        // 计算应出勤天数：根据实际日期范围计算工作日数（周一到周五）
        boolean isMonth = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) >= 20; // 判断是否为月度统计
        
        // 计算从startDate到endDate之间的实际工作日数（周一到周五）
        long workDays = startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> {
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    return dayOfWeek >= 1 && dayOfWeek <= 5; // 周一到周五
                })
                .count();
        
        if (employeeId != null) {
            // 单个员工：使用实际工作日数
            if (isMonth) {
                // 月度：使用从本月1号到当天的实际工作日数
                totalDays = (int) workDays;
            } else {
                // 周度：计算实际工作日数，但最多5天
                totalDays = (int) Math.min(workDays, 5); // 周度最多5天
            }

            // 缺卡人次 = 应出勤天数 - (正常+迟到+早退)人次
            missingDays = Math.max(0, totalDays - (normalDays + lateDays + earlyLeaveDays));
        } else {
            // 查询部门或全部员工时，需要计算所有员工的应打卡次数
            List<Employee> employees;
            if (departmentId != null) {
                // 查询指定部门的员工
                employees = employeeService.getEmployeesByDepartmentId(departmentId);
            } else {
                // 查询全部员工
                employees = employeeService.getAllEmployees();
            }
            
            int expectedDaysPerEmployee;
            if (isMonth) {
                // 月度：使用从本月1号到当天的实际工作日数
                expectedDaysPerEmployee = (int) workDays;
            } else {
                // 周度：计算实际工作日数，但最多5天
                expectedDaysPerEmployee = (int) Math.min(workDays, 5); // 周度每人最多5天
            }
            
            // 计算所有员工的应打卡总次数 = 每人应出勤天数 × 员工数
            int totalExpectedCheckIns = expectedDaysPerEmployee * employees.size();
            
            // 计算实际打卡次数（正常+迟到+早退）
            int actualCheckIns = normalDays + lateDays + earlyLeaveDays;
            
            // 缺卡人次 = 应打卡次数 - 实际打卡次数
            missingDays = Math.max(0, totalExpectedCheckIns - actualCheckIns);
            totalDays = expectedDaysPerEmployee;
        }
        
        summary.put("totalDays", totalDays);
        summary.put("normalDays", normalDays);
        summary.put("lateDays", lateDays);
        summary.put("earlyLeaveDays", earlyLeaveDays);
        summary.put("missingDays", missingDays);
        summary.put("workHours", workHours);
        
        return summary;
    }

    /**
     * 计算加班时长（查询application表，计算已批准的加班申请时长）
     */
    private double calculateOvertimeHours(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        List<Application> overtimeApps;
        
        if (employeeId != null) {
            // 查询指定员工的已批准加班申请
            overtimeApps = applicationDAO.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        } else if (departmentId != null) {
            // 查询部门下所有员工的已批准加班申请
            List<Employee> employees = employeeService.getAllEmployees();
            String deptIdStr = String.format("D%09d", departmentId);
            List<Integer> employeeIds = employees.stream()
                    .filter(emp -> deptIdStr.equals(emp.getDid()))
                    .map(Employee::getEid)
                    .collect(java.util.stream.Collectors.toList());
            
            overtimeApps = new java.util.ArrayList<>();
            for (Integer eid : employeeIds) {
                overtimeApps.addAll(applicationDAO.findByEmployeeIdAndStatus(eid, "APPROVED"));
            }
        } else {
            // 查询所有员工的已批准加班申请
            List<Application> allApps = applicationDAO.findAll();
            overtimeApps = allApps.stream()
                    .filter(app -> "OVERTIME".equals(app.getApplicationType()))
                    .filter(app -> "APPROVED".equals(app.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return overtimeApps.stream()
                .filter(app -> "OVERTIME".equals(app.getApplicationType()))
                .filter(app -> app.getStartTime() != null && app.getEndTime() != null)
                .filter(app -> {
                    LocalDate appDate = app.getStartTime().toLocalDate();
                    return !appDate.isBefore(startDate) && !appDate.isAfter(endDate);
                })
                .mapToDouble(app -> {
                    long minutes = java.time.Duration.between(
                            app.getStartTime(),
                            app.getEndTime()).toMinutes();
                    return minutes / 60.0;
                })
                .sum();
    }

    /**
     * 计算请假人次（查询application表，计算已批准的请假申请人次）
     */
    private int calculateLeaveDays(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        List<Application> leaveApps;
        
        if (employeeId != null) {
            // 查询指定员工的已批准请假申请
            leaveApps = applicationDAO.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        } else if (departmentId != null) {
            // 查询部门下所有员工的已批准请假申请
            List<Employee> employees = employeeService.getAllEmployees();
            String deptIdStr = String.format("D%09d", departmentId);
            List<Integer> employeeIds = employees.stream()
                    .filter(emp -> deptIdStr.equals(emp.getDid()))
                    .map(Employee::getEid)
                    .collect(java.util.stream.Collectors.toList());
            
            leaveApps = new java.util.ArrayList<>();
            for (Integer eid : employeeIds) {
                leaveApps.addAll(applicationDAO.findByEmployeeIdAndStatus(eid, "APPROVED"));
            }
        } else {
            // 查询所有员工的已批准请假申请
            List<Application> allApps = applicationDAO.findAll();
            leaveApps = allApps.stream()
                    .filter(app -> "LEAVE".equals(app.getApplicationType()))
                    .filter(app -> "APPROVED".equals(app.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 改为人次：统计在日期范围内的请假申请次数（每人每次申请算1人次）
        return (int) leaveApps.stream()
                .filter(app -> "LEAVE".equals(app.getApplicationType()))
                .filter(app -> app.getStartTime() != null && app.getEndTime() != null)
                .filter(app -> {
                    LocalDate appStartDate = app.getStartTime().toLocalDate();
                    LocalDate appEndDate = app.getEndTime().toLocalDate();
                    // 检查请假日期是否在统计范围内
                    return !appEndDate.isBefore(startDate) && !appStartDate.isAfter(endDate);
                })
                .count(); // 改为count，统计人次而不是天数
    }

    /**
     * 计算请假天数（统计日期跨度，用于个人界面）
     * 计算请假申请在统计范围内的实际天数
     */
    private int calculateLeaveDaysCount(Integer employeeId, LocalDate startDate, LocalDate endDate) {
        // 查询指定员工的已批准请假申请
        List<Application> leaveApps = applicationDAO.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        
        int totalDays = 0;
        for (Application app : leaveApps) {
            if (!"LEAVE".equals(app.getApplicationType())) {
                continue;
            }
            if (app.getStartTime() == null || app.getEndTime() == null) {
                continue;
            }
            
            LocalDate appStartDate = app.getStartTime().toLocalDate();
            LocalDate appEndDate = app.getEndTime().toLocalDate();
            
            // 检查请假日期是否与统计范围有重叠
            if (appEndDate.isBefore(startDate) || appStartDate.isAfter(endDate)) {
                continue; // 没有重叠，跳过
            }
            
            // 计算重叠部分的天数
            LocalDate overlapStart = appStartDate.isBefore(startDate) ? startDate : appStartDate;
            LocalDate overlapEnd = appEndDate.isAfter(endDate) ? endDate : appEndDate;
            
            // 计算天数（包含开始和结束日期）
            long days = java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            totalDays += (int) days;
        }
        
        return totalDays;
    }

    /**
     * 计算出差人次（查询application表，计算已批准的出差申请人次）
     */
    private int calculateBusinessTripDays(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        List<Application> tripApps;
        
        if (employeeId != null) {
            // 查询指定员工的已批准出差申请
            tripApps = applicationDAO.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        } else if (departmentId != null) {
            // 查询部门下所有员工的已批准出差申请
            List<Employee> employees = employeeService.getAllEmployees();
            String deptIdStr = String.format("D%09d", departmentId);
            List<Integer> employeeIds = employees.stream()
                    .filter(emp -> deptIdStr.equals(emp.getDid()))
                    .map(Employee::getEid)
                    .collect(java.util.stream.Collectors.toList());
            
            tripApps = new java.util.ArrayList<>();
            for (Integer eid : employeeIds) {
                tripApps.addAll(applicationDAO.findByEmployeeIdAndStatus(eid, "APPROVED"));
            }
        } else {
            // 查询所有员工的已批准出差申请
            List<Application> allApps = applicationDAO.findAll();
            tripApps = allApps.stream()
                    .filter(app -> "BUSINESS_TRIP".equals(app.getApplicationType()))
                    .filter(app -> "APPROVED".equals(app.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 改为人次：统计在日期范围内的出差申请次数（每人每次申请算1人次）
        return (int) tripApps.stream()
                .filter(app -> "BUSINESS_TRIP".equals(app.getApplicationType()))
                .filter(app -> app.getStartTime() != null && app.getEndTime() != null)
                .filter(app -> {
                    LocalDate appStartDate = app.getStartTime().toLocalDate();
                    LocalDate appEndDate = app.getEndTime().toLocalDate();
                    // 检查出差日期是否在统计范围内
                    return !appEndDate.isBefore(startDate) && !appStartDate.isAfter(endDate);
                })
                .count(); // 改为count，统计人次而不是天数
    }

    /**
     * 计算补卡次数（查询application表，计算已批准的补卡申请次数）
     * 统计本月审批通过的补卡申请，按审批时间统计
     */
    private int calculateReissueCount(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        List<Application> reissueApps;
        
        if (employeeId != null) {
            // 查询指定员工的已批准补卡申请
            reissueApps = applicationDAO.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        } else if (departmentId != null) {
            // 查询部门下所有员工的已批准补卡申请
            List<Employee> employees = employeeService.getAllEmployees();
            String deptIdStr = String.format("D%09d", departmentId);
            List<Integer> employeeIds = employees.stream()
                    .filter(emp -> deptIdStr.equals(emp.getDid()))
                    .map(Employee::getEid)
                    .collect(java.util.stream.Collectors.toList());
            
            reissueApps = new java.util.ArrayList<>();
            for (Integer eid : employeeIds) {
                reissueApps.addAll(applicationDAO.findByEmployeeIdAndStatus(eid, "APPROVED"));
            }
        } else {
            // 查询所有员工的已批准补卡申请
            List<Application> allApps = applicationDAO.findAll();
            reissueApps = allApps.stream()
                    .filter(app -> "REISSUE".equals(app.getApplicationType()))
                    .filter(app -> "APPROVED".equals(app.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return (int) reissueApps.stream()
                .filter(app -> "REISSUE".equals(app.getApplicationType()))
                .filter(app -> app.getApproveTime() != null) // 必须有审批时间
                .filter(app -> {
                    // 按审批时间统计，而不是补卡时间
                    LocalDate approveDate = app.getApproveTime().toLocalDate();
                    return !approveDate.isBefore(startDate) && !approveDate.isAfter(endDate);
                })
                .count();
    }
}

