package com.attendance.management.system.service;

import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 员工服务类
 */

@Service
public class EmployeeService {

    /** BCrypt密码前缀 */
    private static final String BCRYPT_PREFIX = "$2a$";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 使用字符串类型的员工ID进行认证（推荐使用）
     */
    public boolean authenticate(String employeeId, String password) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            // 使用PasswordEncoder进行密码验证
            // 如果密码是明文存储的（旧数据），先尝试直接比较，然后可以迁移到加密存储
            String storedPassword = employee.getPassword();
            if (storedPassword != null && storedPassword.startsWith(BCRYPT_PREFIX)) {
                // BCrypt加密的密码
                return passwordEncoder.matches(password, storedPassword);
            } else {
                // 明文密码（向后兼容，建议迁移到加密存储）
                return password.equals(storedPassword);
            }
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
