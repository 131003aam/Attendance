package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;
import java.util.List;

public interface EmployeeDAO {
    Employee findByEmployeeId(int employeeId);
    List<Employee> findAll();
    List<Employee> findByDepartmentId(Integer departmentId);
    void insert(Employee employee);
    void update(Employee employee);
    void delete(int employeeId);
    void updatePassword(int employeeId, String newPassword);
    void updatePhone(int employeeId, String newPhone);
}