package com.attendance.management.system.dto;

/**
 * 登录请求DTO
 */
public class LoginRequest {
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

