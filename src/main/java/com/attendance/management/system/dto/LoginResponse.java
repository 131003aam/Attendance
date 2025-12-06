package com.attendance.management.system.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
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

