package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class EmployeeDAOImpl implements EmployeeDAO {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Employee findByEmployeeId(int employeeId) {
        String sql = "SELECT * FROM employee WHERE EID = ?";
        try {
            // 将int转换为10位字符串格式
            String employeeIdStr = String.format("%010d", employeeId);
            return jdbcTemplate.queryForObject(sql, new EmployeeRowMapper(), employeeIdStr);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updatePassword(int employeeId, String newPassword) {
        String sql = "UPDATE employee SET Password = ? WHERE EID = ?";
        // 将int转换为10位字符串格式
        String employeeIdStr = String.format("%010d", employeeId);
        jdbcTemplate.update(sql, newPassword, employeeIdStr);
    }
}

class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        // 数据库字段是CHAR(10)，直接读取字符串
        employee.setEmployeeId(rs.getString("EID"));
        employee.setDepartmentId(rs.getString("DID"));
        employee.setPositionId(rs.getString("PID"));
        employee.setName(rs.getString("EName"));
        employee.setSex(rs.getString("Sex"));
        employee.setPhone(rs.getString("Phone"));
        employee.setPassword(rs.getString("Password"));
        return employee;
    }
}
