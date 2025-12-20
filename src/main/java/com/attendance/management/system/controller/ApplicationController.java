package com.attendance.management.system.controller;

import com.attendance.management.system.entity.Application;
import com.attendance.management.system.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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
            map.put("companions", parseCompanions(app.getCompanions()));
            map.put("transportation", app.getTransportation());
            map.put("budget", app.getBudget());
        }

        return map;
    }

    /**
     * 将 Map 转换为 Application 实体
     */
    private Application mapToApplication(Map<String, Object> request) {
        Application app = new Application();

        // 处理 employeeId
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
        app.setEid(employeeId);

        // 基础字段
        app.setApplicationType((String) request.get("type"));
        
        // 时间字段
        if (request.get("startTime") != null) {
            app.setStartTime(parseDateTime(request.get("startTime")));
        }
        if (request.get("endTime") != null) {
            app.setEndTime(parseDateTime(request.get("endTime")));
        }
        if (request.get("reissueTime") != null) {
            app.setReissueTime(parseDateTime(request.get("reissueTime")));
        }
        
        app.setReason((String) request.get("reason"));
        app.setStatus("PENDING"); // 默认状态

        // 处理不同类型的特有字段
        String type = (String) request.get("type");
        if ("LEAVE".equals(type)) {
            app.setLeaveType((String) request.get("leaveType"));
            app.setAttachment((String) request.get("attachment"));
        } else if ("REISSUE".equals(type)) {
            app.setReissueType((String) request.get("reissueType"));
            if (request.get("reissueTime") != null) {
                app.setReissueTime(parseDateTime(request.get("reissueTime")));
            }
        } else if ("OVERTIME".equals(type)) {
            app.setOvertimeType((String) request.get("overtimeType"));
            Object isCompensatory = request.get("isCompensatory");
            if (isCompensatory instanceof Boolean) {
                app.setIsCompensatory((Boolean) isCompensatory);
            } else if (isCompensatory instanceof String) {
                app.setIsCompensatory(Boolean.parseBoolean((String) isCompensatory));
            }
        } else if ("BUSINESS_TRIP".equals(type)) {
            app.setDestination((String) request.get("destination"));
            app.setCompanions(formatCompanions(request.get("companions")));
            app.setTransportation((String) request.get("transportation"));
            Object budgetObj = request.get("budget");
            if (budgetObj != null) {
                if (budgetObj instanceof Number) {
                    app.setBudget(BigDecimal.valueOf(((Number) budgetObj).doubleValue()));
                } else if (budgetObj instanceof String) {
                    app.setBudget(new BigDecimal((String) budgetObj));
                }
            }
        }

        return app;
    }

    private LocalDateTime parseDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return null;
        }
        if (dateTimeObj instanceof String) {
            String dateTimeStr = (String) dateTimeObj;
            // 支持多种格式
            try {
                return LocalDateTime.parse(dateTimeStr, FORMATTER);
            } catch (Exception e) {
                try {
                    return LocalDateTime.parse(dateTimeStr);
                } catch (Exception e2) {
                    return null;
                }
            }
        }
        return null;
    }

    private String formatCompanions(Object companionsObj) {
        if (companionsObj == null) {
            return null;
        }
        if (companionsObj instanceof List) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.writeValueAsString(companionsObj);
            } catch (Exception e) {
                return null;
            }
        } else if (companionsObj instanceof String) {
            return (String) companionsObj;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> parseCompanions(String companionsStr) {
        if (companionsStr == null || companionsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return (List<Integer>) mapper.readValue(companionsStr, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getApplications(
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) String status) {
        try {
            List<Application> applications;
            if (employeeId != null && status != null) {
                applications = applicationService.getApplicationsByEmployeeIdAndStatus(employeeId, status);
            } else if (employeeId != null) {
                applications = applicationService.getApplicationsByEmployeeId(employeeId);
            } else if (status != null) {
                applications = applicationService.getPendingApplications();
                if (!"PENDING".equals(status)) {
                    applications = applicationService.getAllApplications().stream()
                            .filter(app -> status.equals(app.getStatus()))
                            .collect(Collectors.toList());
                }
            } else {
                applications = applicationService.getAllApplications();
            }

            List<Map<String, Object>> result = applications.stream()
                    .map(this::applicationToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitApplication(@RequestBody Map<String, Object> request) {
        try {
            Application application = mapToApplication(request);
            Application saved = applicationService.submitApplication(application);
            Map<String, Object> result = applicationToMap(saved);
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "提交申请失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Integer id) {
        try {
            Application application = applicationService.cancelApplication(id);
            if (application == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "申请不存在");
                error.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "申请已撤销");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "撤销申请失败: " + e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/reissue/limit")
    public ResponseEntity<Map<String, Object>> getReissueLimit(
            @RequestParam(required = false) Integer employeeId) {
        try {
            Integer eid = employeeId != null ? employeeId : 10002; // 默认值
            int currentMonth = applicationService.getReissueLimit(eid);
            Map<String, Object> limit = new HashMap<>();
            limit.put("currentMonth", currentMonth);
            limit.put("limit", 3);
            return ResponseEntity.ok(limit);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取补卡限制失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
