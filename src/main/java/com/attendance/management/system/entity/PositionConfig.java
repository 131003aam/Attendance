package com.attendance.management.system.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "position_config")
public class PositionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position_id", unique = true)
    private String positionId;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "work_start_time")
    private LocalTime workStartTime;

    @Column(name = "work_end_time")
    private LocalTime workEndTime;

    @Column(name = "required_work_days")
    private Integer requiredWorkDays;

    @Column(name = "required_daily_hours")
    private Double requiredDailyHours;

    // Constructors
    public PositionConfig() {}

    public PositionConfig(String positionId, String positionName, LocalTime workStartTime,
                          LocalTime workEndTime, Integer requiredWorkDays, Double requiredDailyHours) {
        this.positionId = positionId;
        this.positionName = positionName;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.requiredWorkDays = requiredWorkDays;
        this.requiredDailyHours = requiredDailyHours;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getRequiredDailyHours() {
        return requiredDailyHours;
    }

    public void setRequiredDailyHours(Double requiredDailyHours) {
        this.requiredDailyHours = requiredDailyHours;
    }
}

