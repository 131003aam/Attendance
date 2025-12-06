package com.attendance.management.system.controller;

import com.attendance.management.system.entity.AttendanceRecord;
import com.attendance.management.system.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 上班打卡
     */
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody Map<String, String> request) {
        String employeeId = request.get("employeeId");
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "员工ID不能为空"));
        }
        AttendanceRecord record = attendanceService.checkIn(employeeId);
        return ResponseEntity.ok(record);
    }

    /**
     * 下班打卡
     */
    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(@RequestBody Map<String, String> request) {
        String employeeId = request.get("employeeId");
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "员工ID不能为空"));
        }
        AttendanceRecord record = attendanceService.checkOut(employeeId);
        return ResponseEntity.ok(record);
    }

    /**
     * 获取今天的考勤记录
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAttendance(@RequestParam String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "员工ID不能为空"));
        }
        Optional<AttendanceRecord> record = attendanceService.getAttendanceRecord(
                employeeId, LocalDate.now());
        return ResponseEntity.ok(record.orElse(null));
    }

    /**
     * 获取一段时间内的考勤记录
     */
    @GetMapping("/records")
    public ResponseEntity<?> getAttendanceRecords(
            @RequestParam String employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "员工ID不能为空"));
        }
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusWeeks(1);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

            List<AttendanceRecord> records = attendanceService.getAttendanceRecords(employeeId, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "日期格式错误: " + e.getMessage()));
        }
    }

    /**
     * 管理员查询部门考勤记录
     */
    @GetMapping("/department")
    public ResponseEntity<?> getDepartmentAttendance(
            @RequestParam(required = false) String departmentId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<AttendanceRecord> records = attendanceService.getDepartmentAttendanceRecords(
                    departmentId, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "日期格式错误: " + e.getMessage()));
        }
    }
}

