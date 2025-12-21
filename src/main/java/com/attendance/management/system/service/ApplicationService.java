package com.attendance.management.system.service;

import com.attendance.management.system.dao.ApplicationDAO;
import com.attendance.management.system.dao.AttendanceRecordDAO;
import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.dao.PositionConfigDAO;
import com.attendance.management.system.entity.Application;
import com.attendance.management.system.entity.AttendanceRecord;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.entity.PositionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private AttendanceRecordDAO attendanceRecordDAO;

    @Autowired
    private PositionConfigDAO positionConfigDAO;

    public Application getApplicationById(Integer aid) {
        return applicationDAO.findById(aid);
    }

    public List<Application> getAllApplications() {
        return applicationDAO.findAll();
    }

    public List<Application> getApplicationsByEmployeeId(Integer eid) {
        return applicationDAO.findByEmployeeId(eid);
    }

    public List<Application> getPendingApplications() {
        return applicationDAO.findByStatus("PENDING");
    }

    public List<Application> getApplicationsByEmployeeIdAndStatus(Integer eid, String status) {
        return applicationDAO.findByEmployeeIdAndStatus(eid, status);
    }

    public Application submitApplication(Application application) {
        // 设置默认值
        if (application.getStatus() == null) {
            application.setStatus("PENDING");
        }
        if (application.getCreatedAt() == null) {
            application.setCreatedAt(LocalDateTime.now());
        }
        if (application.getIsCompensatory() == null) {
            application.setIsCompensatory(false);
        }
        
        applicationDAO.insert(application);
        return application;
    }

    public Application updateApplication(Application application) {
        applicationDAO.update(application);
        return application;
    }

    public Application approveApplication(Integer aid, Integer approverId) {
        Application application = applicationDAO.findById(aid);
        if (application != null) {
            application.setStatus("APPROVED");
            application.setApproverId(approverId);
            application.setApproveTime(LocalDateTime.now());
            applicationDAO.update(application);
        }
        return application;
    }

    public Application rejectApplication(Integer aid, Integer approverId, String rejectReason) {
        Application application = applicationDAO.findById(aid);
        if (application != null) {
            application.setStatus("REJECTED");
            application.setApproverId(approverId);
            application.setApproveTime(LocalDateTime.now());
            application.setRejectReason(rejectReason);
            applicationDAO.update(application);
        }
        return application;
    }

    public Application cancelApplication(Integer aid) {
        Application application = applicationDAO.findById(aid);
        if (application != null) {
            application.setStatus("CANCELLED");
            applicationDAO.update(application);
        }
        return application;
    }

    public void deleteApplication(Integer aid) {
        applicationDAO.delete(aid);
    }

    public String getEmployeeName(Integer eid) {
        Employee employee = employeeDAO.findByEmployeeId(eid);
        return employee != null ? employee.getName() : "员工 " + eid;
    }

    public int getReissueLimit(Integer eid) {
        // 获取当前月份的已批准补卡申请数量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

        List<Application> reissueApplications = applicationDAO.findByEmployeeIdAndStatus(eid, "APPROVED");
        long count = reissueApplications.stream()
                .filter(app -> "REISSUE".equals(app.getApplicationType()))
                .filter(app -> app.getReissueTime() != null)
                .filter(app -> {
                    LocalDateTime reissueTime = app.getReissueTime();
                    return !reissueTime.isBefore(startOfMonth) && !reissueTime.isAfter(endOfMonth);
                })
                .count();

        return (int) count;
    }

    public void processReissueApproval(Application reissueApplication) {
        // 获取员工ID和补卡时间
        Integer employeeId = reissueApplication.getEid();
        LocalDateTime reissueTime = reissueApplication.getReissueTime();
        if (employeeId != null && reissueTime != null) {
            // 查询对应的考勤记录
            LocalDate recordDate = reissueTime.toLocalDate();
            AttendanceRecord attendanceRecord = attendanceRecordDAO.findByEmployeeIdAndDate(employeeId, recordDate);

            if (!"NORMAL".equals(attendanceRecord.getStatus())) {
                // 根据补卡类型更新考勤记录
                String reissueType = reissueApplication.getReissueType();
                if ("MISSING_CHECK_IN".equals(reissueType)) {
                    // 补上班卡
                    attendanceRecord.setCheckInTime(reissueTime);
                } else if ("MISSING_CHECK_OUT".equals(reissueType)) {
                    // 补下班卡
                    attendanceRecord.setCheckOutTime(reissueTime);
                } else if ("BOTH".equals(reissueType)) {
                    // 补全天卡
                    attendanceRecord.setCheckInTime(reissueTime);
                    attendanceRecord.setCheckOutTime(reissueTime);
                }

                // 更新考勤状态和工作时长
                updateAttendanceStatusAndWorkHours(attendanceRecord);

                // 保存更新后的考勤记录
                attendanceRecordDAO.update(attendanceRecord);
            }
        }
    }
    private void updateAttendanceStatusAndWorkHours(AttendanceRecord record) {
        // 根据当前职务配置重新计算考勤状态
        Employee employee = employeeDAO.findByEmployeeId(record.getEid());
        if (employee != null && employee.getPid() != null) {
            PositionConfig positionConfig = positionConfigDAO.findById(employee.getPid().trim());
            if (positionConfig != null && record.getCheckInTime() != null) {
                // 重新判断是否迟到
                LocalTime workStartTime = positionConfig.getWorkStartTime().toLocalTime();
                LocalTime checkInTime = record.getCheckInTime().toLocalTime();

                String newStatus = "NORMAL";
                if (checkInTime.isAfter(workStartTime)) {
                    newStatus = "LATE";
                }

                // 如果已下班打卡，判断是否早退
                if (record.getCheckOutTime() != null) {
                    LocalTime workEndTime = positionConfig.getWorkEndTime().toLocalTime();
                    LocalTime checkOutTime = record.getCheckOutTime().toLocalTime();

                    if (checkOutTime.isBefore(workEndTime)) {
                        if ("LATE".equals(newStatus)) {
                            // 既迟到又早退，保持LATE状态
                        } else {
                            newStatus = "EARLY_LEAVE";
                        }
                    }
                }

                // 更新状态
                record.setStatus(newStatus);
            }
        }

        // 计算工作时长
        if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
            long minutes = java.time.Duration.between(
                    record.getCheckInTime().toLocalTime(),
                    record.getCheckOutTime().toLocalTime()
            ).toMinutes();
            record.setWorkHours(BigDecimal.valueOf(minutes / 60.0));
        }
    }

}

