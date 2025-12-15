package com.attendance.management.system.service;

import com.attendance.management.system.dao.EmployeeDAO;
import com.attendance.management.system.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeDAO employeeDAO;

    public boolean authenticate(int employeeId, String password) {
        Employee employee = employeeDAO.findByEmployeeId(employeeId);
        if (employee == null) {
            return false;
        }
        // 去除密码字段可能的空格（CHAR类型会填充空格）
        String dbPassword = employee.getPassword() != null ? employee.getPassword().trim() : "";
        String inputPassword = password != null ? password.trim() : "";
        return dbPassword.equals(inputPassword);
    }

    public Employee getEmployeeDetails(int employeeId) {
        return employeeDAO.findByEmployeeId(employeeId);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }

    public void createEmployee(Employee employee) {
        employeeDAO.insert(employee);
    }

    public void updateEmployee(Employee employee) {
        employeeDAO.update(employee);
    }

    public void deleteEmployee(int employeeId) {
        employeeDAO.delete(employeeId);
    }

    public void resetPassword(int employeeId) {
        // 重置为默认密码
        employeeDAO.updatePassword(employeeId, "123456");
    }

    public boolean updatePassword(int employeeId, String oldPassword, String newPassword) {
        // 验证原密码
        if (!authenticate(employeeId, oldPassword)) {
            return false;
        }
        // 更新密码
        employeeDAO.updatePassword(employeeId, newPassword);
        return true;
    }

    public void updatePhone(int employeeId, String newPhone) {
        employeeDAO.updatePhone(employeeId, newPhone);
    }
}
