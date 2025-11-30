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

        if (employeeService.authenticate(request.getEmployeeId(), request.getPassword())) {
            Employee employee = employeeService.getEmployeeDetails(request.getEmployeeId());
            LoginResponse response = new LoginResponse(
                    "登录成功",
                    true,
                    employee.getEid(),
                    employee.getName(),
                    employee.getDid(),
                    employee.getPid()
            );
            return ResponseEntity.ok(response);
        } else {
            LoginResponse response = new LoginResponse("用户名或密码错误", false, null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}

class LoginRequest {
    private Integer employeeId;
    private String password;

    // Getters and Setters
    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
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
    private Integer employeeId;
    private String employeeName;
    private Integer departmentId;
    private Integer positionId;

    public LoginResponse(String message,
                         boolean success,
                         Integer employeeId,
                         String employeeName,
                         Integer departmentId,
                         Integer positionId) {
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

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }
}

