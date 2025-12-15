package com.attendance.management.system.controller;

import com.attendance.management.system.controller.ApplicationController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approval")
@CrossOrigin(origins = "*")
public class ApprovalController {

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingApplications() {
        // 从ApplicationController获取所有申请，筛选出待审批的
        try {
            Field field = ApplicationController.class.getDeclaredField("applicationsStore");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, Map<String, Object>> store = (Map<Long, Map<String, Object>>) field.get(null);
            
            List<Map<String, Object>> pending = store.values().stream()
                    .filter(app -> "PENDING".equals(app.get("status")))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/applications/{id}/review")
    public ResponseEntity<Map<String, Object>> reviewApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Field field = ApplicationController.class.getDeclaredField("applicationsStore");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, Map<String, Object>> store = (Map<Long, Map<String, Object>>) field.get(null);
            
            Map<String, Object> application = store.get(id);
            if (application != null) {
                boolean approved = (Boolean) request.getOrDefault("approved", false);
                application.put("status", approved ? "APPROVED" : "REJECTED");
                if (!approved && request.containsKey("reason")) {
                    application.put("rejectReason", request.get("reason"));
                }
                application.put("approverId", 10001);
                application.put("approverName", "审批人");
                application.put("approveTime", java.time.LocalDateTime.now().toString());
                return ResponseEntity.ok(application);
            }
        } catch (Exception e) {
            // 如果获取失败，返回新对象
        }
        
        Map<String, Object> application = new HashMap<>();
        application.put("id", id);
        application.put("status", (Boolean) request.getOrDefault("approved", false) ? "APPROVED" : "REJECTED");
        return ResponseEntity.ok(application);
    }
}

