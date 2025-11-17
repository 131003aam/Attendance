package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class EmployeeDAOImpl implements EmployeeDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Employee findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{employeeId}, new EmployeeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updatePassword(String employeeId, String newPasswordHash) {
        String sql = "UPDATE employees SET password_hash = ? WHERE employee_id = ?";
        jdbcTemplate.update(sql, newPasswordHash, employeeId);
    }
}

class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getString("id"));
        employee.setEmployeeId(rs.getString("employee_id"));
        employee.setName(rs.getString("name"));
        employee.setGender(rs.getString("gender"));
        employee.setDepartmentId(rs.getString("department_id"));
        employee.setPositionId(rs.getString("position_id"));
        employee.setContactInfo(rs.getString("contact_info"));
        employee.setStatus(rs.getString("status"));
        employee.setUserRole(rs.getString("user_role"));
        employee.setPasswordHash(rs.getString("password_hash"));
        return employee;
    }
}
