package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;

/**
 * 员工DAO接口（已废弃，建议使用EmployeeRepository）
 * @deprecated 使用 {@link com.attendance.management.system.repository.EmployeeRepository} 替代
 */
@Deprecated
public interface EmployeeDAO {
    /**
     * 根据员工ID查找员工（已废弃）
     * @deprecated 使用 {@link com.attendance.management.system.repository.EmployeeRepository#findByEmployeeId(String)} 替代
     */
    @Deprecated
    Employee findByEmployeeId(int employeeId);
    
    /**
     * 更新密码（已废弃）
     * @deprecated 建议通过Service层更新密码
     */
    @Deprecated
    void updatePassword(int employeeId, String newPassword);
}