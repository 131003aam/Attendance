package com.attendance.management.system.controller;

import com.attendance.management.system.entity.Application;
import com.attendance.management.system.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approval")
@CrossOrigin(origins = "*")
public class ApprovalController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 将 Application 实体转换为 Map（用于 JSON 响应）
     */
    private Map<String, Object> applicationToMap(Application app) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", app.getAid());
        map.put("employeeId", app.getEid());
        map.put("employeeName", applicationService.getEmployeeName(app.getEid()));
        map.put("type", app.getApplicationType());
        if (app.getStartTime() != null) {
            map.put("startTime", app.getStartTime().toString());
        }
        if (app.getEndTime() != null) {
            map.put("endTime", app.getEndTime().toString());
        }
        map.put("reason", app.getReason());
        map.put("status", app.getStatus());
        if (app.getApproverId() != null) {
            map.put("approverId", app.getApproverId());
            map.put("approverName", applicationService.getEmployeeName(app.getApproverId()));
        }
        if (app.getApproveTime() != null) {
            map.put("approveTime", app.getApproveTime().toString());
        }
        map.put("rejectReason", app.getRejectReason());
        if (app.getCreatedAt() != null) {
            map.put("createdAt", app.getCreatedAt().toString());
        }

        // 请假特有字段
        if ("LEAVE".equals(app.getApplicationType())) {
            map.put("leaveType", app.getLeaveType());
            map.put("attachment", app.getAttachment());
        }

        // 补卡特有字段
        if ("REISSUE".equals(app.getApplicationType())) {
            map.put("reissueType", app.getReissueType());
            if (app.getReissueTime() != null) {
                map.put("reissueTime", app.getReissueTime().toString());
            }
        }

        // 加班特有字段
        if ("OVERTIME".equals(app.getApplicationType())) {
            map.put("overtimeType", app.getOvertimeType());
            map.put("isCompensatory", app.getIsCompensatory());
        }

        // 出差特有字段
        if ("BUSINESS_TRIP".equals(app.getApplicationType())) {
            map.put("destination", app.getDestination());
            map.put("companions", app.getCompanions());
            map.put("transportation", app.getTransportation());
            map.put("budget", app.getBudget());
        }

        return map;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingApplications() {
        try {
            List<Application> pending = applicationService.getPendingApplications();
            List<Map<String, Object>> result = pending.stream()
                    .map(this::applicationToMap)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/applications/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewApplication(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Boolean approved = (Boolean) request.getOrDefault("approved", false);
            
            // 从请求中获取审批人ID
            Integer approverId = null;
            Object approverIdObj = request.get("approverId");
            if (approverIdObj instanceof Number) {
                approverId = ((Number) approverIdObj).intValue();
            } else if (approverIdObj instanceof String) {
                try {
                    approverId = Integer.parseInt((String) approverIdObj);
                } catch (NumberFormatException e) {
                    // 解析失败
                }
            }
            
            // 如果无法获取审批人ID，返回错误
            if (approverId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "无法获取审批人信息，请重新登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Application application;
            if (approved) {
                application = applicationService.approveApplication(id, approverId);
            } else {
                String reason = (String) request.get("reason");
                application = applicationService.rejectApplication(id, approverId, reason);
            }

            if (application == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "申请不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> result = applicationToMap(application);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "审批失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
