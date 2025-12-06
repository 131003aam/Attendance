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

class LoginRequest {
    private Object employeeId;  // 支持String或Integer类型
    private String password;

    // Getters and Setters
    public Object getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Object employeeId) {
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
    private boolean success;
    private String employeeId;      // 改为String类型
    private String employeeName;
    private String departmentId;    // 改为String类型
    private String positionId;      // 改为String类型

    public LoginResponse(String message,
                         boolean success,
                         String employeeId,
                         String employeeName,
                         String departmentId,
                         String positionId) {
        this.message = message;
        this.success = success;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentId = departmentId;
        this.positionId = positionId;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }
}

