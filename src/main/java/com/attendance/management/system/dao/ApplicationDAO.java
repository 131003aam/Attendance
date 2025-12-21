package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Application;
import java.util.List;

public interface ApplicationDAO {
    Application findById(Integer aid);
    List<Application> findAll();
    List<Application> findByEmployeeId(Integer eid);
    List<Application> findByStatus(String status);
    List<Application> findByEmployeeIdAndStatus(Integer eid, String status);
    void insert(Application application);
    void update(Application application);
    void delete(Integer aid);
}


