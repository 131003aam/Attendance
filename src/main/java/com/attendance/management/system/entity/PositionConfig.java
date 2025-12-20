package com.attendance.management.system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Time;

@Entity
@Table(name = "position_config")
public class PositionConfig {
    @Id
    @Column(name = "PID", length = 10)
    private String pid;                    // 职务ID（字符型，长度10）

    @Column(name = "PName", nullable = false, length = 20)
    private String pName;                 // 职务名称（字符型，长度20）

    @Column(name = "WorkStartTime", nullable = false)
    private Time workStartTime;            // 标准上班时间（时间型，HH:MM:SS）

    @Column(name = "WorkEndTime", nullable = false)
    private Time workEndTime;              // 标准下班时间（时间型，HH:MM:SS）

    @Column(name = "MonthlyWorkDays")
    private Integer monthlyWorkDays;       // 月工作日（数值型，长度2）

    @Column(name = "DailyWorkHours", precision = 3, scale = 1)
    private BigDecimal dailyWorkHours;  // 或者去掉 scale 属性

    @Column(name = "Description", length = 200)
    private String description;            // 描述

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPName() {
        return pName;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    public Time getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(Time workStartTime) {
        this.workStartTime = workStartTime;
    }

    public Time getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(Time workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Integer getMonthlyWorkDays() {
        return monthlyWorkDays;
    }

    public void setMonthlyWorkDays(Integer monthlyWorkDays) {
        this.monthlyWorkDays = monthlyWorkDays;
    }

    public BigDecimal getDailyWorkHours() {
        return dailyWorkHours;
    }

    public void setDailyWorkHours(BigDecimal dailyWorkHours) {
        this.dailyWorkHours = dailyWorkHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}




















