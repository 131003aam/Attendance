package com.attendance.management.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @Column(name = "EID", length = 10)
    private String employeeId;      // 员工ID (CHAR(10))

    @Column(name = "DID", length = 10, nullable = false)
    private String departmentId;    // 部门ID (CHAR(10))

    @Column(name = "PID", length = 10, nullable = false)
    private String positionId;      // 职务ID (CHAR(10))

    @Column(name = "EName", length = 20, nullable = false)
    private String name;            // 姓名 (VARCHAR(20))

    @Column(name = "Sex", length = 2)
    private String sex;             // 性别 (CHAR(2))

    @Column(name = "Phone", length = 11, unique = true)
    private String phone;           // 联系方式 (CHAR(11))

    @Column(name = "Password", nullable = false)
    private String password;        // 账号密码 (VARCHAR(255), 加密存储)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Employee() {}

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 兼容旧代码的方法（用于向后兼容）
    @Deprecated
    public Integer getEid() {
        try {
            return Integer.parseInt(employeeId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Deprecated
    public void setEid(Integer eid) {
        this.employeeId = eid != null ? String.format("%010d", eid) : null;
    }

    @Deprecated
    public Integer getDid() {
        try {
            return Integer.parseInt(departmentId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Deprecated
    public void setDid(Integer did) {
        this.departmentId = did != null ? String.format("%010d", did) : null;
    }

    @Deprecated
    public Integer getPid() {
        try {
            return Integer.parseInt(positionId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Deprecated
    public void setPid(Integer pid) {
        this.positionId = pid != null ? String.format("%010d", pid) : null;
    }
}
