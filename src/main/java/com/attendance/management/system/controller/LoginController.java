package com.attendance.management.system.controller;

import com.attendance.management.system.dto.LoginRequest;
import com.attendance.management.system.dto.LoginResponse;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    private final EmployeeService employeeService;

    public LoginController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmployeeId() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new LoginResponse("请输入完整的账号和密码", false, null, null, null, null)
            );
        }

        // 支持字符串和整数类型的员工ID
        String employeeIdStr;
        if (request.getEmployeeId() instanceof String) {
            employeeIdStr = (String) request.getEmployeeId();
        } else if (request.getEmployeeId() instanceof Integer) {
            employeeIdStr = String.format("%010d", (Integer) request.getEmployeeId());
        } else {
            employeeIdStr = String.valueOf(request.getEmployeeId());
        }

        if (employeeService.authenticate(employeeIdStr, request.getPassword())) {
            Employee employee = employeeService.getEmployeeDetails(employeeIdStr);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse("用户信息获取失败", false, null, null, null, null));
            }
            LoginResponse response = new LoginResponse(
                    "登录成功",
                    true,
                    employee.getEmployeeId(),
                    employee.getName(),
                    employee.getDepartmentId(),
                    employee.getPositionId()
            );
            return ResponseEntity.ok(response);
        } else {
            LoginResponse response = new LoginResponse("用户名或密码错误", false, null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

