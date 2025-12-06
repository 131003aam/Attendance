package com.attendance.management.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "position_config")
public class PositionConfig {
    @Id
    @Column(name = "PID", length = 10)
    private String positionId;          // 职务ID (CHAR(10))

    @Column(name = "PName", length = 20, nullable = false)
    private String positionName;        // 职务名称 (VARCHAR(20))

    @Column(name = "PsTime")
    private LocalTime workStartTime;    // 标准上班时间 (TIME)

    @Column(name = "PeTime")
    private LocalTime workEndTime;      // 标准下班时间 (TIME)

    @Column(name = "PMonthWork")
    private Integer requiredWorkDays;   // 月工作日要求 (INT(2))

    @Column(name = "PDayWork")
    private Integer requiredDailyHours; // 日工作小时数 (DECIMAL(2,0))

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
    public PositionConfig() {}

    public PositionConfig(String positionId, String positionName, LocalTime workStartTime,
                          LocalTime workEndTime, Integer requiredWorkDays, Integer requiredDailyHours) {
        this.positionId = positionId;
        this.positionName = positionName;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.requiredWorkDays = requiredWorkDays;
        this.requiredDailyHours = requiredDailyHours;
    }

    // Getters and Setters
    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public LocalTime getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }

    public LocalTime getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Integer getRequiredWorkDays() {
        return requiredWorkDays;
    }

    public void setRequiredWorkDays(Integer requiredWorkDays) {
        this.requiredWorkDays = requiredWorkDays;
    }

    public Integer getRequiredDailyHours() {
        return requiredDailyHours;
    }

    public void setRequiredDailyHours(Integer requiredDailyHours) {
        this.requiredDailyHours = requiredDailyHours;
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

    // 兼容方法：用于处理可能传递的Double类型
    @Deprecated
    public Double getRequiredDailyHoursAsDouble() {
        return requiredDailyHours != null ? requiredDailyHours.doubleValue() : null;
    }

    @Deprecated
    public void setRequiredDailyHoursAsDouble(Double hours) {
        this.requiredDailyHours = hours != null ? hours.intValue() : null;
    }
}
