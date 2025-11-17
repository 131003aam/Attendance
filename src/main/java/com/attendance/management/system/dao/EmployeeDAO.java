package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;

public interface EmployeeDAO {
    Employee findByEmployeeId(String employeeId);
    void updatePassword(String employeeId, String newPasswordHash);
}