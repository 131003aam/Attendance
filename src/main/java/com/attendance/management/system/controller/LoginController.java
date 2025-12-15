package com.attendance.management.system.controller;

import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoginController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        // 简单的注册逻辑（实际应该验证数据并保存到数据库）
        RegisterResponse response = new RegisterResponse();
        response.setMessage("注册成功");
        response.setSuccess(true);
        response.setEmployeeId(10000 + (int)(Math.random() * 1000)); // 临时生成ID
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmployeeId() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    new LoginResponse("请输入完整的账号和密码", false, null, null, null, null, null)
            );
        }

        if (employeeService.authenticate(request.getEmployeeId(), request.getPassword())) {
            Employee employee = employeeService.getEmployeeDetails(request.getEmployeeId());
            // 从数据库读取角色，如果为空则默认为EMPLOYEE
            String role = employee.getRole();
            if (role == null || role.trim().isEmpty()) {
                role = "EMPLOYEE";
            }
            
            // 将字符串ID转换为数字（从"D000000001"提取"000000001"并转换为1）
            Integer departmentId = null;
            Integer positionId = null;
            try {
                String did = employee.getDid();
                if (did != null && did.length() >= 2) {
                    // 提取数字部分（去掉第一个字符）
                    String didNum = did.substring(1);
                    departmentId = Integer.parseInt(didNum);
                }
            } catch (Exception e) {
                // 如果转换失败，保持为null
            }
            try {
                String pid = employee.getPid();
                if (pid != null && pid.length() >= 2) {
                    // 提取数字部分（去掉第一个字符）
                    String pidNum = pid.substring(1);
                    positionId = Integer.parseInt(pidNum);
                }
            } catch (Exception e) {
                // 如果转换失败，保持为null
            }
            
            LoginResponse response = new LoginResponse(
                    "登录成功",
                    true,
                    employee.getEid(),
                    employee.getName(),
                    departmentId,
                    positionId,
                    role.trim().toUpperCase()
            );
            return ResponseEntity.ok(response);
        } else {
            LoginResponse response = new LoginResponse("用户名或密码错误", false, null, null, null, null, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/profile/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, Object> request) {
        System.out.println("========== 收到更新密码请求 ==========");
        System.out.println("请求参数: " + request);
        Map<String, Object> response = new HashMap<>();
        try {
            Integer employeeId = (Integer) request.get("employeeId");
            String oldPassword = (String) request.get("oldPassword");
            String newPassword = (String) request.get("newPassword");
            System.out.println("employeeId: " + employeeId);

            if (employeeId == null || oldPassword == null || newPassword == null) {
                response.put("success", false);
                response.put("message", "参数不完整");
                return ResponseEntity.badRequest().body(response);
            }

            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "新密码至少6位");
                return ResponseEntity.badRequest().body(response);
            }

            if (employeeService.updatePassword(employeeId, oldPassword, newPassword)) {
                response.put("success", true);
                response.put("message", "密码更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "原密码不对");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新密码失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/profile/update-phone")
    public ResponseEntity<Map<String, Object>> updatePhone(@RequestBody Map<String, Object> request) {
        System.out.println("========== 收到更新手机号请求 ==========");
        System.out.println("请求参数: " + request);
        Map<String, Object> response = new HashMap<>();
        try {
            Integer employeeId = (Integer) request.get("employeeId");
            String phone = (String) request.get("phone");
            System.out.println("employeeId: " + employeeId);
            System.out.println("phone: " + phone);

            if (employeeId == null || phone == null || phone.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "参数不完整");
                return ResponseEntity.badRequest().body(response);
            }

            if (phone.length() != 11) {
                response.put("success", false);
                response.put("message", "手机号必须为11位");
                return ResponseEntity.badRequest().body(response);
            }

            employeeService.updatePhone(employeeId, phone);
            response.put("success", true);
            response.put("message", "手机号更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新手机号失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
    private String role;

    public LoginResponse(String message,
                         boolean success,
                         Integer employeeId,
                         String employeeName,
                         Integer departmentId,
                         Integer positionId,
                         String role) {
        this.message = message;
        this.success = success;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentId = departmentId;
        this.positionId = positionId;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

class RegisterRequest {
    private String name;
    private String phone;
    private String employeeId;
    private Integer departmentId;
    private String password;
    private String confirmPassword;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}

class RegisterResponse {
    private String message;
    private boolean success;
    private Integer employeeId;

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
}

