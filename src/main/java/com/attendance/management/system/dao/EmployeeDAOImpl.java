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
    public Employee findByEmployeeId(int employeeId) {
        String sql = "SELECT * FROM employee WHERE EID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{employeeId}, new EmployeeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updatePassword(int employeeId, String newPassword) {
        String sql = "UPDATE employee SET Password = ? WHERE EID = ?";
        jdbcTemplate.update(sql, newPassword, employeeId);
    }
}

class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        employee.setEid(rs.getInt("EID"));
        employee.setDid(rs.getInt("DID"));
        employee.setPid(rs.getInt("PID"));
        employee.setName(rs.getString("EName"));
        employee.setSex(rs.getString("Sex"));
        employee.setPhone(rs.getString("Phone"));
        employee.setPassword(rs.getString("Password"));
        return employee;
    }
}
