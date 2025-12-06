package com.attendance.management.system.service;

import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeDAO employeeDAO; // 保留用于向后兼容

    /**
     * 使用字符串类型的员工ID进行认证（推荐使用）
     */
    public boolean authenticate(String employeeId, String password) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            return password.equals(employee.getPassword());
        }
        return false;
    }

    /**
     * 使用整数类型的员工ID进行认证（向后兼容）
     */
    public boolean authenticate(int employeeId, String password) {
        String employeeIdStr = String.format("%010d", employeeId);
        return authenticate(employeeIdStr, password);
    }

    /**
     * 根据字符串类型的员工ID获取员工详情（推荐使用）
     */
    public Employee getEmployeeDetails(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElse(null);
    }

    /**
     * 根据整数类型的员工ID获取员工详情（向后兼容）
     */
    public Employee getEmployeeDetails(int employeeId) {
        String employeeIdStr = String.format("%010d", employeeId);
        return getEmployeeDetails(employeeIdStr);
    }
}
