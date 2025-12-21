package com.attendance.management.system.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "application")
    public class Application {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "AID")
        private Integer aid;                      // 申请ID（自增）

        @Column(name = "EID", nullable = false)
        private Integer eid;                      // 申请人ID（员工ID）

        @Column(name = "ApplicationType", nullable = false, length = 20)
        private String applicationType;           // 申请类型：LEAVE-请假, REISSUE-补卡, OVERTIME-加班, BUSINESS_TRIP-出差

        @Column(name = "StartTime", nullable = false)
        private LocalDateTime startTime;          // 开始时间

        @Column(name = "EndTime", nullable = false)
        private LocalDateTime endTime;            // 结束时间

        @Column(name = "Reason", nullable = false, columnDefinition = "TEXT")
        private String reason;                    // 申请原因

        @Column(name = "Status", length = 20)
        private String status;                    // 申请状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消

        @Column(name = "ApproverID")
        private Integer approverId;               // 审批人ID

        @Column(name = "ApproveTime")
        private LocalDateTime approveTime;        // 审批时间

        @Column(name = "RejectReason", columnDefinition = "TEXT")
        private String rejectReason;              // 拒绝原因

        @Column(name = "CreatedAt")
        private LocalDateTime createdAt;          // 创建时间

        // 请假特有字段
        @Column(name = "LeaveType", length = 20)
        private String leaveType;                 // 请假类型：ANNUAL-年假, SICK-病假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, OTHER-其他

        @Column(name = "Attachment", length = 500)
        private String attachment;                // 附件路径

        // 补卡特有字段
        @Column(name = "ReissueType", length = 20)
        private String reissueType;               // 补卡类型：MISSING_CHECK_IN-漏签, MISSING_CHECK_OUT-漏退, BOTH-漏签漏退

        @Column(name = "ReissueTime")
        private LocalDateTime reissueTime;        // 补卡时间

        // 加班特有字段
        @Column(name = "OvertimeType", length = 20)
        private String overtimeType;              // 加班类型：WEEKDAY-工作日, WEEKEND-周末, HOLIDAY-节假日

        @Column(name = "IsCompensatory")
        private Boolean isCompensatory;           // 是否调休

        // 出差特有字段
        @Column(name = "Destination", length = 100)
        private String destination;               // 目的地

        @Column(name = "Companions", length = 500)
        private String companions;                // 随行人员ID列表（JSON格式）

        @Column(name = "Transportation", length = 50)
        private String transportation;            // 交通工具

        @Column(name = "Budget", precision = 10, scale = 2)
        private BigDecimal budget;                // 预算金额

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

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getApproverId() {
        return approverId;
    }

    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getReissueType() {
        return reissueType;
    }

    public void setReissueType(String reissueType) {
        this.reissueType = reissueType;
    }

    public LocalDateTime getReissueTime() {
        return reissueTime;
    }

    public void setReissueTime(LocalDateTime reissueTime) {
        this.reissueTime = reissueTime;
    }

    public String getOvertimeType() {
        return overtimeType;
    }

    public void setOvertimeType(String overtimeType) {
        this.overtimeType = overtimeType;
    }

    public Boolean getIsCompensatory() {
        return isCompensatory;
    }

    public void setIsCompensatory(Boolean isCompensatory) {
        this.isCompensatory = isCompensatory;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCompanions() {
        return companions;
    }

    public void setCompanions(String companions) {
        this.companions = companions;
    }

    public String getTransportation() {
        return transportation;
    }

    public void setTransportation(String transportation) {
        this.transportation = transportation;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}




