package com.attendance.management.system.dao;

import com.attendance.management.system.entity.PositionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

@Repository
public class PositionConfigDAOImpl implements PositionConfigDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<PositionConfig> findAll() {
        String sql = "SELECT * FROM position_config ORDER BY PID";
        return jdbcTemplate.query(sql, new PositionConfigRowMapper());
    }

    @Override
    public PositionConfig findById(String pid) {
        if (pid == null || pid.trim().isEmpty()) {
            return null;
        }
        String trimmedPid = pid.trim();
        System.out.println("PositionConfigDAO查询，PID: [" + trimmedPid + "], 长度: " + trimmedPid.length());
        
        // CHAR(10)类型，MySQL在比较时会自动忽略尾部空格
        // 但为了确保匹配，我们使用TRIM函数比较
        try {
            String sql = "SELECT * FROM position_config WHERE TRIM(PID) = ?";
            PositionConfig result = jdbcTemplate.queryForObject(sql, new Object[]{trimmedPid}, new PositionConfigRowMapper());
            System.out.println("PositionConfigDAO找到配置: " + (result != null ? result.getPName() : "null"));
            return result;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            System.out.println("PositionConfigDAO未找到配置，PID: [" + trimmedPid + "]");
            // 如果TRIM方式找不到，尝试直接等号匹配（MySQL会自动处理CHAR类型的空格）
            try {
                String sql = "SELECT * FROM position_config WHERE PID = ?";
                PositionConfig result = jdbcTemplate.queryForObject(sql, new Object[]{trimmedPid}, new PositionConfigRowMapper());
                System.out.println("PositionConfigDAO找到配置（直接匹配）: " + (result != null ? result.getPName() : "null"));
                return result;
            } catch (org.springframework.dao.EmptyResultDataAccessException e2) {
                System.out.println("PositionConfigDAO所有方式都未找到配置");
                // 输出调试信息：查询所有记录
                try {
                    List<PositionConfig> all = findAll();
                    System.out.println("数据库中所有职务配置:");
                    for (PositionConfig p : all) {
                        String dbPid = p.getPid();
                        System.out.println("  - PID: [" + dbPid + "] (原始长度: " + (dbPid != null ? dbPid.length() : 0) + 
                            ", trim后: [" + (dbPid != null ? dbPid.trim() : "null") + "]), 名称: " + p.getPName());
                    }
                } catch (Exception ex) {
                    System.out.println("查询所有职务配置失败: " + ex.getMessage());
                }
                return null;
            }
        } catch (Exception e) {
            System.out.println("PositionConfigDAO查询异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void insert(PositionConfig positionConfig) {
        String sql = "INSERT INTO position_config (PID, PName, WorkStartTime, WorkEndTime, MonthlyWorkDays, DailyWorkHours, Description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                positionConfig.getPid(),
                positionConfig.getPName(),
                positionConfig.getWorkStartTime(),
                positionConfig.getWorkEndTime(),
                positionConfig.getMonthlyWorkDays(),
                positionConfig.getDailyWorkHours(),
                positionConfig.getDescription());
    }

    @Override
    public void update(PositionConfig positionConfig) {
        String sql = "UPDATE position_config SET PName = ?, WorkStartTime = ?, WorkEndTime = ?, MonthlyWorkDays = ?, DailyWorkHours = ?, Description = ? WHERE PID = ?";
        jdbcTemplate.update(sql,
                positionConfig.getPName(),
                positionConfig.getWorkStartTime(),
                positionConfig.getWorkEndTime(),
                positionConfig.getMonthlyWorkDays(),
                positionConfig.getDailyWorkHours(),
                positionConfig.getDescription(),
                positionConfig.getPid());
    }

    @Override
    public void delete(String pid) {
        String sql = "DELETE FROM position_config WHERE PID = ?";
        jdbcTemplate.update(sql, pid);
    }
}

class PositionConfigRowMapper implements RowMapper<PositionConfig> {
    @Override
    public PositionConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
        PositionConfig positionConfig = new PositionConfig();
        String pid = rs.getString("PID");
        positionConfig.setPid(pid != null ? pid.trim() : null);
        positionConfig.setPName(rs.getString("PName"));
        positionConfig.setWorkStartTime(rs.getTime("WorkStartTime"));
        positionConfig.setWorkEndTime(rs.getTime("WorkEndTime"));
        positionConfig.setMonthlyWorkDays(rs.getInt("MonthlyWorkDays"));
        positionConfig.setDailyWorkHours(rs.getDouble("DailyWorkHours"));
        positionConfig.setDescription(rs.getString("Description"));
        return positionConfig;
    }
}

