package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class DepartmentDAOImpl implements DepartmentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Department> findAll() {
        String sql = "SELECT * FROM department ORDER BY DID";
        return jdbcTemplate.query(sql, new DepartmentRowMapper());
    }

    @Override
    public Department findById(String did) {
        String sql = "SELECT * FROM department WHERE DID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{did}, new DepartmentRowMapper());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void insert(Department department) {
        String sql = "INSERT INTO department (DID, DName, Description, ManagerID) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                department.getDid(),
                department.getDName(),
                department.getDescription(),
                department.getManagerId());
    }

    @Override
    public void update(Department department) {
        String sql = "UPDATE department SET DName = ?, Description = ?, ManagerID = ? WHERE DID = ?";
        jdbcTemplate.update(sql,
                department.getDName(),
                department.getDescription(),
                department.getManagerId(),
                department.getDid());
    }

    @Override
    public void delete(String did) {
        String sql = "DELETE FROM department WHERE DID = ?";
        jdbcTemplate.update(sql, did);
    }
}

class DepartmentRowMapper implements RowMapper<Department> {
    @Override
    public Department mapRow(ResultSet rs, int rowNum) throws SQLException {
        Department department = new Department();
        department.setDid(rs.getString("DID"));
        department.setDName(rs.getString("DName"));
        department.setDescription(rs.getString("Description"));
        int managerId = rs.getInt("ManagerID");
        department.setManagerId(rs.wasNull() ? null : managerId);
        return department;
    }
}




















