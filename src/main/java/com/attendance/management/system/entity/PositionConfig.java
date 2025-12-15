package com.attendance.management.system.entity;

import java.sql.Time;

public class PositionConfig {
    private String pid;                    // 职务ID（字符型，长度10）
    private String pName;                 // 职务名称（字符型，长度20）
    private Time workStartTime;            // 标准上班时间（时间型，HH:MM:SS）
    private Time workEndTime;              // 标准下班时间（时间型，HH:MM:SS）
    private Integer monthlyWorkDays;       // 月工作日（数值型，长度2）
    private Double dailyWorkHours;         // 每日工作时长（数值型，长度2）
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

    public Double getDailyWorkHours() {
        return dailyWorkHours;
    }

    public void setDailyWorkHours(Double dailyWorkHours) {
        this.dailyWorkHours = dailyWorkHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}




















