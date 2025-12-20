package com.attendance.management.system.service;

import com.attendance.management.system.dao.ApplicationDAO;
import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.entity.Application;
import com.attendance.management.system.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

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
        // 获取当前月份的补卡申请数量
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        List<Application> reissueApplications = applicationDAO.findByEmployeeId(eid);
        long count = reissueApplications.stream()
                .filter(app -> "REISSUE".equals(app.getApplicationType()))
                .filter(app -> app.getCreatedAt() != null && app.getCreatedAt().isAfter(startOfMonth))
                .count();
        
        return (int) count;
    }
}

