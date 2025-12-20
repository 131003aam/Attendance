package com.attendance.management.system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Application {
    private Integer aid;                      // 申请ID（自增）
    private Integer eid;                      // 申请人ID（员工ID）
    private String applicationType;           // 申请类型：LEAVE-请假, REISSUE-补卡, OVERTIME-加班, BUSINESS_TRIP-出差
    private LocalDateTime startTime;          // 开始时间
    private LocalDateTime endTime;            // 结束时间
    private String reason;                    // 申请原因
    private String status;                    // 申请状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消
    private Integer approverId;               // 审批人ID
    private LocalDateTime approveTime;        // 审批时间
    private String rejectReason;              // 拒绝原因
    private LocalDateTime createdAt;          // 创建时间
    
    // 请假特有字段
    private String leaveType;                 // 请假类型：ANNUAL-年假, SICK-病假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, OTHER-其他
    private String attachment;                // 附件路径
    
    // 补卡特有字段
    private String reissueType;               // 补卡类型：MISSING_CHECK_IN-漏签, MISSING_CHECK_OUT-漏退, BOTH-漏签漏退
    private LocalDateTime reissueTime;        // 补卡时间
    
    // 加班特有字段
    private String overtimeType;              // 加班类型：WEEKDAY-工作日, WEEKEND-周末, HOLIDAY-节假日
    private Boolean isCompensatory;           // 是否调休
    
    // 出差特有字段
    private String destination;               // 目的地
    private String companions;                // 随行人员ID列表（JSON格式）
    private String transportation;            // 交通工具
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

