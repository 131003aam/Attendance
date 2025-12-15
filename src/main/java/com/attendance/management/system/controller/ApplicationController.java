package com.attendance.management.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    // 临时存储申请数据（实际应该使用数据库）
    private static final Map<Long, Map<String, Object>> applicationsStore = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1000);

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getApplications() {
        // 返回存储的申请列表
        return ResponseEntity.ok(new ArrayList<>(applicationsStore.values()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitApplication(@RequestBody Map<String, Object> request) {
        try {
            Long id = idGenerator.getAndIncrement();
            Map<String, Object> application = new HashMap<>();
            application.put("id", id.intValue()); // 转换为int以匹配前端类型
            
            // 从请求中获取employeeId，如果没有则使用默认值（实际应该从认证信息中获取）
            Object employeeIdObj = request.get("employeeId");
            Integer employeeId;
            if (employeeIdObj instanceof Number) {
                employeeId = ((Number) employeeIdObj).intValue();
            } else if (employeeIdObj instanceof String) {
                try {
                    employeeId = Integer.parseInt((String) employeeIdObj);
                } catch (NumberFormatException e) {
                    employeeId = 10002; // 默认值
                }
            } else {
                employeeId = 10002; // 默认值
            }
            
            application.put("employeeId", employeeId);
            // 显示员工ID而不是名称（根据用户要求）
            application.put("employeeName", "员工 " + employeeId);
            
            application.put("type", request.get("type"));
            application.put("startTime", request.get("startTime"));
            application.put("endTime", request.get("endTime"));
            application.put("reason", request.get("reason"));
            application.put("status", "PENDING");
            application.put("createdAt", LocalDateTime.now().toString());
            
            // 处理不同类型的特有字段
            String type = (String) request.get("type");
            if ("LEAVE".equals(type)) {
                application.put("leaveType", request.get("leaveType"));
                application.put("attachment", request.get("attachment"));
            } else if ("REISSUE".equals(type)) {
                application.put("reissueType", request.get("reissueType"));
                application.put("reissueTime", request.get("reissueTime"));
            } else if ("OVERTIME".equals(type)) {
                application.put("overtimeType", request.get("overtimeType"));
                application.put("isCompensatory", request.get("isCompensatory"));
            } else if ("BUSINESS_TRIP".equals(type)) {
                application.put("destination", request.get("destination"));
                application.put("companions", request.get("companions"));
                application.put("transportation", request.get("transportation"));
                application.put("budget", request.get("budget"));
            }
            
            // 保存到临时存储
            applicationsStore.put(id, application);
            
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "提交申请失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Long id) {
        Map<String, Object> application = applicationsStore.get(id);
        if (application != null) {
            application.put("status", "CANCELLED");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "申请已撤销");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reissue/limit")
    public ResponseEntity<Map<String, Object>> getReissueLimit() {
        Map<String, Object> limit = new HashMap<>();
        limit.put("currentMonth", 0);
        limit.put("limit", 3);
        return ResponseEntity.ok(limit);
    }
}

