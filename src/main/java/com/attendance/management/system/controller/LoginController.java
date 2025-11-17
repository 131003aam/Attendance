package com.attendance.management.system.controller;

import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (employeeService.authenticate(request.getEmployeeId(), request.getPassword())) {
            Employee employee = employeeService.getEmployeeDetails(request.getEmployeeId());
            LoginResponse response = new LoginResponse("登录成功", employee.getUserRole(), true);
            return ResponseEntity.ok(response);
        } else {
            LoginResponse response = new LoginResponse("用户名或密码错误", null, false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

class LoginRequest {
    private String employeeId;
    private String password;

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class LoginResponse {
    private String message;
    private String role;
    private boolean success;

    public LoginResponse(String message, String role, boolean success) {
        this.message = message;
        this.role = role;
        this.success = success;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

