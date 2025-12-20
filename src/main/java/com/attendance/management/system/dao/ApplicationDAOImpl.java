package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Application findById(Integer aid) {
        String sql = "SELECT * FROM application WHERE AID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new ApplicationRowMapper(), aid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Application> findAll() {
        String sql = "SELECT * FROM application ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new ApplicationRowMapper());
    }

    @Override
    public List<Application> findByEmployeeId(Integer eid) {
        String sql = "SELECT * FROM application WHERE EID = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new ApplicationRowMapper(), eid);
    }

    @Override
    public List<Application> findByStatus(String status) {
        String sql = "SELECT * FROM application WHERE Status = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new ApplicationRowMapper(), status);
    }

    @Override
    public List<Application> findByEmployeeIdAndStatus(Integer eid, String status) {
        String sql = "SELECT * FROM application WHERE EID = ? AND Status = ? ORDER BY CreatedAt DESC";
        return jdbcTemplate.query(sql, new ApplicationRowMapper(), eid, status);
    }

    @Override
    public void insert(Application application) {
        String sql = "INSERT INTO application (EID, ApplicationType, StartTime, EndTime, Reason, Status, " +
                     "ApproverID, ApproveTime, RejectReason, CreatedAt, LeaveType, Attachment, " +
                     "ReissueType, ReissueTime, OvertimeType, IsCompensatory, Destination, Companions, " +
                     "Transportation, Budget) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            int index = 1;
            ps.setInt(index++, application.getEid());
            ps.setString(index++, application.getApplicationType());
            ps.setTimestamp(index++, application.getStartTime() != null ? Timestamp.valueOf(application.getStartTime()) : null);
            ps.setTimestamp(index++, application.getEndTime() != null ? Timestamp.valueOf(application.getEndTime()) : null);
            ps.setString(index++, application.getReason());
            ps.setString(index++, application.getStatus());
            ps.setObject(index++, application.getApproverId());
            ps.setTimestamp(index++, application.getApproveTime() != null ? Timestamp.valueOf(application.getApproveTime()) : null);
            ps.setString(index++, application.getRejectReason());
            ps.setTimestamp(index++, application.getCreatedAt() != null ? Timestamp.valueOf(application.getCreatedAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(index++, application.getLeaveType());
            ps.setString(index++, application.getAttachment());
            ps.setString(index++, application.getReissueType());
            ps.setTimestamp(index++, application.getReissueTime() != null ? Timestamp.valueOf(application.getReissueTime()) : null);
            ps.setString(index++, application.getOvertimeType());
            ps.setBoolean(index++, application.getIsCompensatory() != null ? application.getIsCompensatory() : false);
            ps.setString(index++, application.getDestination());
            ps.setString(index++, application.getCompanions());
            ps.setString(index++, application.getTransportation());
            ps.setBigDecimal(index++, application.getBudget());
            return ps;
        }, keyHolder);
        
        Number key = keyHolder.getKey();
        if (key != null) {
            application.setAid(key.intValue());
        }
    }

    @Override
    public void update(Application application) {
        String sql = "UPDATE application SET EID = ?, ApplicationType = ?, StartTime = ?, EndTime = ?, " +
                     "Reason = ?, Status = ?, ApproverID = ?, ApproveTime = ?, RejectReason = ?, " +
                     "LeaveType = ?, Attachment = ?, ReissueType = ?, ReissueTime = ?, OvertimeType = ?, " +
                     "IsCompensatory = ?, Destination = ?, Companions = ?, Transportation = ?, Budget = ? " +
                     "WHERE AID = ?";
        
        jdbcTemplate.update(sql,
                application.getEid(),
                application.getApplicationType(),
                application.getStartTime() != null ? Timestamp.valueOf(application.getStartTime()) : null,
                application.getEndTime() != null ? Timestamp.valueOf(application.getEndTime()) : null,
                application.getReason(),
                application.getStatus(),
                application.getApproverId(),
                application.getApproveTime() != null ? Timestamp.valueOf(application.getApproveTime()) : null,
                application.getRejectReason(),
                application.getLeaveType(),
                application.getAttachment(),
                application.getReissueType(),
                application.getReissueTime() != null ? Timestamp.valueOf(application.getReissueTime()) : null,
                application.getOvertimeType(),
                application.getIsCompensatory() != null ? application.getIsCompensatory() : false,
                application.getDestination(),
                application.getCompanions(),
                application.getTransportation(),
                application.getBudget(),
                application.getAid());
    }

    @Override
    public void delete(Integer aid) {
        String sql = "DELETE FROM application WHERE AID = ?";
        jdbcTemplate.update(sql, aid);
    }
}

class ApplicationRowMapper implements RowMapper<Application> {
    @Override
    public Application mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Application application = new Application();
        application.setAid(rs.getInt("AID"));
        application.setEid(rs.getInt("EID"));
        application.setApplicationType(rs.getString("ApplicationType"));
        
        Timestamp startTime = rs.getTimestamp("StartTime");
        if (startTime != null) {
            application.setStartTime(startTime.toLocalDateTime());
        }
        
        Timestamp endTime = rs.getTimestamp("EndTime");

 if (endTime != null) {
            application.setEndTime(endTime.toLocalDateTime());
        }
        
        application.setReason(rs.getString("Reason"));
        application.setStatus(rs.getString("Status"));
        
        int approverId = rs.getInt("ApproverID");
        application.setApproverId(rs.wasNull() ? null : approverId);
        
        Timestamp approveTime = rs.getTimestamp("ApproveTime");
        if (approveTime != null) {
            application.setApproveTime(approveTime.toLocalDateTime());
        }
        
        application.setRejectReason(rs.getString("RejectReason"));
        
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            application.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // 请假特有字段
        application.setLeaveType(rs.getString("LeaveType"));
        application.setAttachment(rs.getString("Attachment"));
        
        // 补卡特有字段
        application.setReissueType(rs.getString("ReissueType"));
        Timestamp reissueTime = rs.getTimestamp("ReissueTime");
        if (reissueTime != null) {
            application.setReissueTime(reissueTime.toLocalDateTime());
        }
        
        // 加班特有字段
        application.setOvertimeType(rs.getString("OvertimeType"));
        application.setIsCompensatory(rs.getBoolean("IsCompensatory"));
        if (rs.wasNull()) {
            application.setIsCompensatory(false);
        }
        
        // 出差特有字段
        application.setDestination(rs.getString("Destination"));
        application.setCompanions(rs.getString("Companions"));
        application.setTransportation(rs.getString("Transportation"));
        BigDecimal budget = rs.getBigDecimal("Budget");
        application.setBudget(rs.wasNull() ? null : budget);
        
        return application;
    }
}
