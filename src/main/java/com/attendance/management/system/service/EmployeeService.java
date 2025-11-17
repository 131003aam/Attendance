package com.attendance.management.system.service;

import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.dao.EmployeeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public boolean authenticate(String employeeId, String password) {
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee != null && passwordEncoder.matches(password, employee.getPasswordHash())) {
            return true;
        }
        return false;
    }

    public Employee getEmployeeDetails(String employeeId) {
        return employeeDAO.findByEmployeeId(employeeId);
    }

    public boolean isAdmin(Employee employee) {
        return "admin".equals(employee.getUserRole());
    }
}
