package com.attendance.management.system.service;

import com.attendance.management.system.dao.AttendanceRecordDAO;
import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.dao.PositionConfigDAO;
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

    /**
     * 获取今日考勤记录，并根据当前职务配置重新计算状态
     */
    public AttendanceRecord getTodayAttendance(Integer employeeId) {
        AttendanceRecord record = attendanceRecordDAO.findByEmployeeIdAndDate(employeeId, LocalDate.now());
        if (record == null) {
            return null;
        }
        
        // 根据当前职务配置重新计算考勤状态
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee != null && employee.getPid() != null) {
            PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
            if (positionConfig != null && record.getCheckInTime() != null) {
                // 重新判断是否迟到
                LocalTime workStartTime = positionConfig.getWorkStartTime().toLocalTime();
                LocalTime checkInTime = record.getCheckInTime().toLocalTime();
                
                String newStatus = "NORMAL";
                if (checkInTime.isAfter(workStartTime)) {
                    newStatus = "LATE";
                }
                
                // 如果已下班打卡，判断是否早退
                if (record.getCheckOutTime() != null) {
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
    }

    /**
     * 上班打卡
     * @param employeeId 员工ID
     * @param location 打卡位置
     * @return 打卡结果信息
     */
    public String checkIn(Integer employeeId, String location) {
        // 获取员工信息
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee == null) {
            return "员工不存在";
        }

        // 获取职务配置
        PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid());
        if (positionConfig == null) {
            return "未找到职务配置";
        }

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalTime workStartTime = positionConfig.getWorkStartTime().toLocalTime();
        
        // 计算允许的打卡时间范围：标准上班时间前1小时到标准上班时间
        LocalTime allowedStartTime = workStartTime.minusHours(1);
        
        // 验证打卡时间：必须在标准上班时间前1小时到标准上班时间之间
        if (currentTime.isBefore(allowedStartTime)) {
            return "未到打卡时间（" + allowedStartTime + "开始）";
        }
        if (currentTime.isAfter(workStartTime)) {
            return "已错过上班打卡时间（最晚" + workStartTime + "）";
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

        todayRecord.setCheckInTime(LocalDateTime.now());
        todayRecord.setCheckInLocation(location);
        
        // 判断是否迟到
        if (currentTime.isAfter(workStartTime.minusMinutes(1))) {
            todayRecord.setStatus("LATE");
        }

        if (todayRecord.getAid() == null) {
            attendanceRecordDAO.insert(todayRecord);
        } else {
            attendanceRecordDAO.update(todayRecord);
        }

        return "上班打卡成功";
    }

    /**
     * 下班打卡
     * @param employeeId 员工ID
     * @param location 打卡位置
     * @return 打卡结果信息
     */
    public String checkOut(Integer employeeId, String location) {
        // 获取员工信息
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee == null) {
            return "员工不存在";
        }

        // 获取职务配置
        PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid());
        if (positionConfig == null) {
            return "未找到职务配置";
        }

        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalTime workEndTime = positionConfig.getWorkEndTime().toLocalTime();
        
        // 计算允许的打卡时间范围：标准下班时间到标准下班时间后1小时
        LocalTime allowedEndTime = workEndTime.plusHours(1);
        
        // 验证打卡时间：必须在标准下班时间到标准下班时间后1小时之间
        if (currentTime.isBefore(workEndTime)) {
            return "未到下班打卡时间（" + workEndTime + "开始）";
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
        todayRecord.setCheckOutTime(LocalDateTime.now());
        todayRecord.setCheckOutLocation(location);
        
        // 判断是否早退
        if (currentTime.isBefore(workEndTime.plusMinutes(1))) {
            if (!"LATE".equals(todayRecord.getStatus())) {
                todayRecord.setStatus("EARLY_LEAVE");
            } else {
                // 如果既迟到又早退，保持LATE状态
            }
        }

        // 计算工作时长
        if (todayRecord.getCheckInTime() != null && todayRecord.getCheckOutTime() != null) {
            long minutes = java.time.Duration.between(
                    todayRecord.getCheckInTime().toLocalTime(),
                    todayRecord.getCheckOutTime().toLocalTime()
            ).toMinutes();
            todayRecord.setWorkHours(BigDecimal.valueOf(minutes / 60.0));
        }

        attendanceRecordDAO.update(todayRecord);

        return "下班打卡成功";
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
        Map<String, Object> weekSummary = calculateSummary(weekRecords, weekStart, now, employeeId);
        Map<String, Object> monthSummary = calculateSummary(monthRecords, monthStart, now, employeeId);
        
        System.out.println("周度汇总: " + weekSummary);
        System.out.println("月度汇总: " + monthSummary);
        
        // 计算本月缺卡天数
        Object missingDaysObj = monthSummary.get("missingDays");
        int missingDays = missingDaysObj != null ? ((Number) missingDaysObj).intValue() : 0;
        
        // 计算迟到和早退次数
        long lateCount = monthRecords.stream()
                .filter(r -> "LATE".equals(r.getStatus()))
                .count();
        
        long earlyLeaveCount = monthRecords.stream()
                .filter(r -> "EARLY_LEAVE".equals(r.getStatus()))
                .count();
        
        // 计算加班时长、请假天数、补卡次数（需要查询申请表，暂时返回0）
        double overtimeHours = calculateOvertimeHours(employeeId, departmentId, monthStart, now);
        int leaveDays = calculateLeaveDays(employeeId, departmentId, monthStart, now);
        int reissueCount = calculateReissueCount(employeeId, departmentId, monthStart, now);
        
        stats.put("weekWorkHours", weekWorkHours);
        stats.put("monthWorkHours", monthWorkHours);
        stats.put("missingDays", missingDays);
        stats.put("lateCount", (int)lateCount);
        stats.put("earlyLeaveCount", (int)earlyLeaveCount);
        stats.put("overtimeHours", overtimeHours);
        stats.put("leaveDays", leaveDays);
        stats.put("reissueCount", reissueCount);
        stats.put("weekSummary", weekSummary);
        stats.put("monthSummary", monthSummary);
        
        return stats;
    }
    
    /**
     * 计算汇总信息
     */
    private Map<String, Object> calculateSummary(List<AttendanceRecord> records, LocalDate startDate, LocalDate endDate, Integer employeeId) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalDays = 0;
        int normalDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int missingDays = 0;
        double workHours = 0.0;
        
        // 计算实际记录的天数（按日期去重，因为同一天可能有多个员工的记录）
        long actualDays = records.stream()
                .map(r -> r.getRecordDate())
                .distinct()
                .count();
        
        normalDays = (int) records.stream()
                .filter(r -> "NORMAL".equals(r.getStatus()))
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

        // 计算应出勤天数（如果有员工ID）
        if (employeeId != null) {
            Employee employee = employeeDAO.findByEmployeeId(employeeId);
            if (employee != null && employee.getPid() != null) {
                PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
                if (positionConfig != null) {
                    // 计算日期范围内的工作日数（简化处理，排除周末）
                    long workDays = startDate.datesUntil(endDate.plusDays(1))
                            .filter(date -> {
                                int dayOfWeek = date.getDayOfWeek().getValue();
                                return dayOfWeek >= 1 && dayOfWeek <= 5; // 周一到周五
                            })
                            .count();
                    totalDays = (int) workDays;
                    missingDays = Math.max(0, totalDays - (int)actualDays);
                }
            }
        } else {
            // 查询全部员工时，totalDays为实际记录天数，missingDays为0（因为无法计算应出勤天数）
            totalDays = (int) actualDays;
            missingDays = 0; // 查询全部员工时，无法计算缺卡天数
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
     * 计算加班时长（需要查询申请表，暂时返回0）
     */
    private double calculateOvertimeHours(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        // TODO: 查询application表，计算已批准的加班申请时长
        // 暂时返回0
        return 0.0;
    }
    
    /**
     * 计算请假天数（需要查询申请表，暂时返回0）
     */
    private int calculateLeaveDays(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        // TODO: 查询application表，计算已批准的请假申请天数
        // 暂时返回0
        return 0;
    }
    
    /**
     * 计算补卡次数（需要查询申请表，暂时返回0）
     */
    private int calculateReissueCount(Integer employeeId, Integer departmentId, LocalDate startDate, LocalDate endDate) {
        // TODO: 查询application表，计算已批准的补卡申请次数
        // 暂时返回0
        return 0;
    }
}

