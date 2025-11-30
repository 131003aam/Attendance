package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;

public interface EmployeeDAO {
    Employee findByEmployeeId(int employeeId);
    void updatePassword(int employeeId, String newPassword);
}