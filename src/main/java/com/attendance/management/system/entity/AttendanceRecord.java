package com.attendance.management.system.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_record")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;                    // 考勤记录ID

    @Column(name = "EID", length = 10, nullable = false)
    private String employeeId;          // 员工ID (CHAR(10))

    @Column(name = "NDate", nullable = false)
    private LocalDate date;             // 打卡日期 (DATE)

    @Column(name = "NsTime")
    private LocalTime checkInTime;      // 签到时间 (TIME)

    @Column(name = "NeTime")
    private LocalTime checkOutTime;     // 签退时间 (TIME)

    @Column(name = "record_type", length = 20)
    private String recordType;          // 记录类型 (VARCHAR(20))

    @Column(name = "NStatus", length = 10)
    private String status;              // 考勤状态 (VARCHAR(10))

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
    public AttendanceRecord() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    // 兼容方法：用于处理前端可能传递的LocalDateTime
    public void setCheckInTimeAsDateTime(LocalDateTime dateTime) {
        this.checkInTime = dateTime != null ? dateTime.toLocalTime() : null;
    }

    public void setCheckOutTimeAsDateTime(LocalDateTime dateTime) {
        this.checkOutTime = dateTime != null ? dateTime.toLocalTime() : null;
    }

    public LocalDateTime getCheckInTimeAsDateTime() {
        return checkInTime != null && date != null 
            ? LocalDateTime.of(date, checkInTime) 
            : null;
    }

    public LocalDateTime getCheckOutTimeAsDateTime() {
        return checkOutTime != null && date != null 
            ? LocalDateTime.of(date, checkOutTime) 
            : null;
    }
}
