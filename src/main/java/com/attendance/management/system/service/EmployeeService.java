package com.attendance.management.system.service;

import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDAO employeeDAO;

    public boolean authenticate(int employeeId, String password) {
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        return employee != null && password.equals(employee.getPassword());
    }

    public Employee getEmployeeDetails(int employeeId) {
        return employeeDAO.findByEmployeeId(employeeId);
    }
}
