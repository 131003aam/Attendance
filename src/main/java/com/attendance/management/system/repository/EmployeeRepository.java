package com.attendance.management.system.repository;

import com.attendance.management.system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    /**
     * 根据员工ID查找员工
     * @param employeeId 员工ID (CHAR(10))
     * @return 员工信息
     */
    Optional<Employee> findByEmployeeId(String employeeId);
    
    /**
     * 根据部门ID查找员工列表
     * @param departmentId 部门ID (CHAR(10))
     * @return 员工列表
     */
    java.util.List<Employee> findByDepartmentId(String departmentId);
    
    /**
     * 根据手机号查找员工
     * @param phone 手机号 (CHAR(11))
     * @return 员工信息
     */
    Optional<Employee> findByPhone(String phone);
    
    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
}

