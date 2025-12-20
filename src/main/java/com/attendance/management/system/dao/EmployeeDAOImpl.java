package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EmployeeDAOImpl implements EmployeeDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Employee findByEmployeeId(int employeeId) {
        String sql = "SELECT * FROM employee WHERE EID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new EmployeeRowMapper(), employeeId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Employee> findAll() {
        String sql = "SELECT * FROM employee ORDER BY EID";
        return jdbcTemplate.query(sql, new EmployeeRowMapper());
    }

    @Override
    public void insert(Employee employee) {
        String sql = "INSERT INTO employee (DID, PID, EName, Sex, Phone, Password, Role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                employee.getDid(), // 直接使用字符串ID
                employee.getPid(), // 直接使用字符串ID
                employee.getName(),
                employee.getSex(),
                employee.getPhone(),
                employee.getPassword(),
                employee.getRole());
    }

    @Override
    public void update(Employee employee) {
        String sql = "UPDATE employee SET DID = ?, PID = ?, EName = ?, Sex = ?, Phone = ?, Role = ? WHERE EID = ?";
        jdbcTemplate.update(sql,
                employee.getDid(), // 直接使用字符串ID
                employee.getPid(), // 直接使用字符串ID
                employee.getName(),
                employee.getSex(),
                employee.getPhone(),
                employee.getRole(),
                employee.getEid());
    }

    @Override
    public void delete(int employeeId) {
        String sql = "DELETE FROM employee WHERE EID = ?";
        jdbcTemplate.update(sql, employeeId);
    }

    @Override
    public void updatePassword(int employeeId, String newPassword) {
        String sql = "UPDATE employee SET Password = ? WHERE EID = ?";
        jdbcTemplate.update(sql, newPassword, employeeId);
    }

    @Override
    public void updatePhone(int employeeId, String newPhone) {
        String sql = "UPDATE employee SET Phone = ? WHERE EID = ?";
        jdbcTemplate.update(sql, newPhone, employeeId);
    }
}

class EmployeeRowMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Employee employee = new Employee();
        employee.setEid(rs.getInt("EID"));
        // DID和PID现在是CHAR(10)类型，直接使用字符串
        String did = rs.getString("DID");
        String pid = rs.getString("PID");
        employee.setDid(did != null ? did.trim() : null);
        employee.setPid(pid != null ? pid.trim() : null);
        employee.setName(rs.getString("EName"));
        employee.setSex(rs.getString("Sex"));
        employee.setPhone(rs.getString("Phone"));
        employee.setPassword(rs.getString("Password"));
        // 读取Role字段，如果不存在则默认为EMPLOYEE
        try {
            String role = rs.getString("Role");
            employee.setRole(role != null ? role.trim() : "EMPLOYEE");
        } catch (SQLException e) {
            // 如果Role字段不存在，使用默认值
            employee.setRole("EMPLOYEE");
        }
        return employee;
    }
}
