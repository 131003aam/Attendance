package com.attendance.management.system.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_record")
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AID")
    private Integer aid;                    // 考勤记录ID

    @Column(name = "EID", nullable = false)
    private Integer eid;                    // 员工ID

    @Column(name = "RecordDate", nullable = false)
    private LocalDate recordDate;           // 考勤日期

    @Column(name = "CheckInTime")
    private LocalDateTime checkInTime;      // 签到时间

    @Column(name = "CheckInLocation", length = 100)
    private String checkInLocation;         // 签到地点

    @Column(name = "CheckOutTime")
    private LocalDateTime checkOutTime;     // 签退时间

    @Column(name = "CheckOutLocation", length = 100)
    private String checkOutLocation;        // 签退地点

    @Column(name = "Status", length = 20)
    private String status;                  // 考勤状态：NORMAL, LATE, EARLY_LEAVE, MISSING

    @Column(name = "WorkHours", precision = 4, scale = 2)
    private BigDecimal workHours;               // 工作时长（小时）

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckInLocation() {
        return checkInLocation;
    }

    public void setCheckInLocation(String checkInLocation) {
        this.checkInLocation = checkInLocation;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getCheckOutLocation() {
        return checkOutLocation;
    }

    public void setCheckOutLocation(String checkOutLocation) {
        this.checkOutLocation = checkOutLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }
}




















