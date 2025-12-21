package com.attendance.management.system.controller;

import com.attendance.management.system.entity.AttendanceRecord;
import com.attendance.management.system.entity.Department;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.entity.PositionConfig;
import com.attendance.management.system.service.AttendanceService;
import com.attendance.management.system.service.DepartmentService;
import com.attendance.management.system.service.EmployeeService;
import com.attendance.management.system.service.PositionConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;
    
    // 测试端点，用于验证控制器是否正常工作
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        System.out.println("========== 测试端点被调用 ==========");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "控制器正常工作");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PositionConfigService positionConfigService;

    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        // 从请求中获取员工ID（实际应该从session或token中获取）
        Integer employeeId = (Integer) request.get("employeeId");
        if (employeeId == null) {
            response.put("message", "员工ID不能为空");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }

        String location = (String) request.getOrDefault("location", "未知位置");
        String result = attendanceService.checkIn(employeeId, location);
        
        if (result.contains("成功")) {
            AttendanceRecord record = attendanceService.getTodayAttendance(employeeId);
            response.put("message", result);
            response.put("success", true);
            
            Map<String, Object> recordMap = new HashMap<>();
            if (record != null) {
                recordMap.put("id", record.getAid());
                recordMap.put("employeeId", record.getEid());
                recordMap.put("date", record.getRecordDate().toString());
                if (record.getCheckInTime() != null) {
                    recordMap.put("checkInTime", record.getCheckInTime().toString());
                }
                recordMap.put("checkInLocation", record.getCheckInLocation());
                recordMap.put("status", record.getStatus());
            }
            response.put("record", recordMap);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", result);
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/check-out")
    public ResponseEntity<Map<String, Object>> checkOut(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        // 从请求中获取员工ID（实际应该从session或token中获取）
        Integer employeeId = (Integer) request.get("employeeId");
        if (employeeId == null) {
            response.put("message", "员工ID不能为空");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }

        String location = (String) request.getOrDefault("location", "未知位置");
        String result = attendanceService.checkOut(employeeId, location);
        
        if (result.contains("成功")) {
            AttendanceRecord record = attendanceService.getTodayAttendance(employeeId);
            response.put("message", result);
            response.put("success", true);
            
            Map<String, Object> recordMap = new HashMap<>();
            if (record != null) {
                recordMap.put("id", record.getAid());
                recordMap.put("employeeId", record.getEid());
                recordMap.put("date", record.getRecordDate().toString());
                if (record.getCheckInTime() != null) {
                    recordMap.put("checkInTime", record.getCheckInTime().toString());
                }
                recordMap.put("checkInLocation", record.getCheckInLocation());
                if (record.getCheckOutTime() != null) {
                    recordMap.put("checkOutTime", record.getCheckOutTime().toString());
                }
                recordMap.put("checkOutLocation", record.getCheckOutLocation());
                recordMap.put("status", record.getStatus());
                recordMap.put("workHours", record.getWorkHours());
            }
            response.put("record", recordMap);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", result);
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping(value = "/today", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getTodayAttendance(@RequestParam(required = true) Integer employeeId) {
        try {
            AttendanceRecord record = attendanceService.getTodayAttendance(employeeId);
            if (record == null) {
                Map<String, Object> emptyRecord = new HashMap<>();
                emptyRecord.put("employeeId", employeeId);
                emptyRecord.put("date", LocalDate.now().toString());
                emptyRecord.put("status", "MISSING");
                return ResponseEntity.ok(emptyRecord);
            }
            
            Map<String, Object> recordMap = new HashMap<>();
            if (record.getAid() != null) {
                recordMap.put("id", record.getAid());
            }
            recordMap.put("employeeId", record.getEid());
            recordMap.put("date", record.getRecordDate().toString());
            if (record.getCheckInTime() != null) {
                recordMap.put("checkInTime", record.getCheckInTime().toString());
            }
            if (record.getCheckInLocation() != null) {
                recordMap.put("checkInLocation", record.getCheckInLocation());
            }
            if (record.getCheckOutTime() != null) {
                recordMap.put("checkOutTime", record.getCheckOutTime().toString());
            }
            if (record.getCheckOutLocation() != null) {
                recordMap.put("checkOutLocation", record.getCheckOutLocation());
            }
            if (record.getStatus() != null) {
                recordMap.put("status", record.getStatus());
            }
            if (record.getWorkHours() != null) {
                recordMap.put("workHours", record.getWorkHours());
            }
            
            return ResponseEntity.ok(recordMap);
        } catch (Exception e) {
            System.err.println("获取今日考勤记录失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取今日考勤记录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping(value = "/position-config", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getPositionConfig(@RequestParam(name = "employeeId", required = true) Integer employeeId) {
        System.out.println("========== 收到position-config请求 ==========");
        System.out.println("员工ID参数: " + employeeId);
        System.out.println("请求路径: /api/attendance/position-config");
        try {
            if (employeeId == null) {
                System.out.println("员工ID为空");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "员工ID不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            System.out.println("获取职务配置，员工ID: " + employeeId);
            Employee employee = employeeService.getEmployeeDetails(employeeId);
            if (employee == null) {
                System.out.println("员工不存在: " + employeeId);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "员工不存在，员工ID: " + employeeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            System.out.println("找到员工: " + employee.getName() + ", PID: [" + employee.getPid() + "]");
            
            if (employee.getPid() == null || employee.getPid().trim().isEmpty()) {
                System.out.println("员工未分配职务: " + employeeId);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "员工未分配职务，员工ID: " + employeeId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            String pid = employee.getPid().trim();
            System.out.println("查询职务配置，PID: [" + pid + "]");
            PositionConfig positionConfig = positionConfigService.getPositionConfigById(pid);
            if (positionConfig == null) {
                System.out.println("职务配置不存在，PID: [" + pid + "]");
                Map<String, Object> error = new HashMap<>();
                error.put("error", "职务配置不存在，职务ID: [" + pid + "]");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            System.out.println("找到职务配置: " + positionConfig.getPName());

            Map<String, Object> configMap = new HashMap<>();
            if (positionConfig.getPid() != null) {
                configMap.put("id", positionConfig.getPid());
            }
            if (positionConfig.getPName() != null) {
                configMap.put("name", positionConfig.getPName());
            }
            if (positionConfig.getWorkStartTime() != null) {
                configMap.put("workStartTime", positionConfig.getWorkStartTime().toString());
            }
            if (positionConfig.getWorkEndTime() != null) {
                configMap.put("workEndTime", positionConfig.getWorkEndTime().toString());
            }
            if (positionConfig.getMonthlyWorkDays() != null) {
                configMap.put("monthlyWorkDays", positionConfig.getMonthlyWorkDays());
            }
            if (positionConfig.getDailyWorkHours() != null) {
                configMap.put("dailyWorkHours", positionConfig.getDailyWorkHours());
            }
            
            return ResponseEntity.ok(configMap);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取职务配置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/records")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceRecords(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        
        System.out.println("========== 收到记录查询请求 ==========");
        System.out.println("employeeId: " + employeeId);
        System.out.println("departmentId: " + departmentId);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("status: " + status);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        
        List<AttendanceRecord> records = attendanceService.getAttendanceRecords(employeeId, departmentId, start, end, status);
        System.out.println("查询到记录数: " + records.size());
        
        List<Map<String, Object>> recordList = records.stream().map(record -> {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getAid());
            recordMap.put("employeeId", record.getEid());
            
            // 获取员工名称和部门信息
            Employee employee = employeeService.getEmployeeDetails(record.getEid());
            if (employee != null) {
                recordMap.put("employeeName", employee.getName());
                // 获取部门名称
                if (employee.getDid() != null) {
                    Department department = departmentService.getDepartmentById(employee.getDid());
                    if (department != null) {
                        recordMap.put("departmentName", department.getDName());
                    }
                }
            }
            
            recordMap.put("date", record.getRecordDate().toString());
            if (record.getCheckInTime() != null) {
                recordMap.put("checkInTime", record.getCheckInTime().toString());
            }
            recordMap.put("checkInLocation", record.getCheckInLocation());
            if (record.getCheckOutTime() != null) {
                recordMap.put("checkOutTime", record.getCheckOutTime().toString());
            }
            recordMap.put("checkOutLocation", record.getCheckOutLocation());
            recordMap.put("status", record.getStatus());
            recordMap.put("workHours", record.getWorkHours());
            return recordMap;
        }).collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(recordList);
    }

    @GetMapping(value = "/statistics", params = {}, produces = "application/json")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(value = "employeeId", required = false) Integer employeeId,
            @RequestParam(value = "departmentId", required = false) Integer departmentId,
            @RequestParam(value = "type", required = false) String type) {
        try {
            System.out.println("========== 收到统计请求 ==========");
            System.out.println("employeeId: " + employeeId);
            System.out.println("departmentId: " + departmentId);
            System.out.println("type: " + type);
            Map<String, Object> stats = attendanceService.getStatistics(employeeId, departmentId, type);
            System.out.println("统计结果: " + stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("统计查询出错: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

