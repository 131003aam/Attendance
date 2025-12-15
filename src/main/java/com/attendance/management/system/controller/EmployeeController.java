package com.attendance.management.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchEmployees(
            @RequestParam(required = false) String keyword) {
        // 返回空列表
        return ResponseEntity.ok(new ArrayList<>());
    }
}




















