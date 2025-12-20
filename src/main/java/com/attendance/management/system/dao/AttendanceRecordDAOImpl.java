package com.attendance.management.system.dao;

import com.attendance.management.system.entity.AttendanceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Repository
public class AttendanceRecordDAOImpl implements AttendanceRecordDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public AttendanceRecord findByEmployeeIdAndDate(Integer eid, LocalDate date) {
        String sql = "SELECT * FROM attendance_record WHERE EID = ? AND RecordDate = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new AttendanceRecordRowMapper(), eid, date);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void insert(AttendanceRecord record) {
        String sql = "INSERT INTO attendance_record (EID, RecordDate, CheckInTime, CheckInLocation, CheckOutTime, CheckOutLocation, Status, WorkHours) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                record.getEid(),
                record.getRecordDate(),
                record.getCheckInTime() != null ? Timestamp.valueOf(record.getCheckInTime()) : null,
                record.getCheckInLocation(),
                record.getCheckOutTime() != null ? Timestamp.valueOf(record.getCheckOutTime()) : null,
                record.getCheckOutLocation(),
                record.getStatus(),
                record.getWorkHours());
    }

    @Override
    public void update(AttendanceRecord record) {
        String sql = "UPDATE attendance_record SET CheckInTime = ?, CheckInLocation = ?, CheckOutTime = ?, CheckOutLocation = ?, Status = ?, WorkHours = ? WHERE AID = ?";
        jdbcTemplate.update(sql,
                record.getCheckInTime() != null ? Timestamp.valueOf(record.getCheckInTime()) : null,
                record.getCheckInLocation(),
                record.getCheckOutTime() != null ? Timestamp.valueOf(record.getCheckOutTime()) : null,
                record.getCheckOutLocation(),
                record.getStatus(),
                record.getWorkHours(),
                record.getAid());
    }

    @Override
    public List<AttendanceRecord> findByEmployeeId(Integer eid) {
        String sql = "SELECT * FROM attendance_record WHERE EID = ? ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), eid);
    }

    @Override
    public List<AttendanceRecord> findByEmployeeIdAndDateRange(Integer eid, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM attendance_record WHERE EID = ? AND RecordDate BETWEEN ? AND ? ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), eid, startDate, endDate);
    }

    @Override
    public List<AttendanceRecord> findByEmployeeIdAndDateRangeAndStatus(Integer eid, LocalDate startDate, LocalDate endDate, String status) {
        String sql = "SELECT * FROM attendance_record WHERE EID = ? AND RecordDate BETWEEN ? AND ? AND Status = ? ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), eid, startDate, endDate, status);
    }

    @Override
    public List<AttendanceRecord> findByDepartmentIdAndDateRange(Integer departmentId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT ar.* FROM attendance_record ar " +
                     "INNER JOIN employee e ON ar.EID = e.EID " +
                     "WHERE e.DID = ? AND ar.RecordDate BETWEEN ? AND ? " +
                     "ORDER BY ar.RecordDate DESC";
        String did = String.format("D%09d", departmentId);
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), did, startDate, endDate);
    }

    @Override
    public List<AttendanceRecord> findByDepartmentIdAndDateRangeAndStatus(Integer departmentId, LocalDate startDate, LocalDate endDate, String status) {
        String sql = "SELECT ar.* FROM attendance_record ar " +
                     "INNER JOIN employee e ON ar.EID = e.EID " +
                     "WHERE e.DID = ? AND ar.RecordDate BETWEEN ? AND ? AND ar.Status = ? " +
                     "ORDER BY ar.RecordDate DESC";
        String did = String.format("D%09d", departmentId);
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), did, startDate, endDate, status);
    }

    @Override
    public List<AttendanceRecord> findAllByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM attendance_record WHERE RecordDate BETWEEN ? AND ? ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), startDate, endDate);
    }

    @Override
    public List<AttendanceRecord> findAllByDateRangeAndStatus(LocalDate startDate, LocalDate endDate, String status) {
        String sql = "SELECT * FROM attendance_record WHERE RecordDate BETWEEN ? AND ? AND Status = ? ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper(), startDate, endDate, status);
    }

    @Override
    public List<AttendanceRecord> findAll() {
        String sql = "SELECT * FROM attendance_record ORDER BY RecordDate DESC";
        return jdbcTemplate.query(sql, new AttendanceRecordRowMapper());
    }
}

class AttendanceRecordRowMapper implements RowMapper<AttendanceRecord> {
    @Override
    public AttendanceRecord mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        AttendanceRecord record = new AttendanceRecord();
        record.setAid(rs.getInt("AID"));
        record.setEid(rs.getInt("EID"));
        record.setRecordDate(rs.getDate("RecordDate").toLocalDate());
        
        Timestamp checkInTime = rs.getTimestamp("CheckInTime");
        if (checkInTime != null) {
            record.setCheckInTime(checkInTime.toLocalDateTime());
        }
        record.setCheckInLocation(rs.getString("CheckInLocation"));
        
        Timestamp checkOutTime = rs.getTimestamp("CheckOutTime");
        if (checkOutTime != null) {
            record.setCheckOutTime(checkOutTime.toLocalDateTime());
        }
        record.setCheckOutLocation(rs.getString("CheckOutLocation"));
        record.setStatus(rs.getString("Status"));
        
        Double workHours = rs.getDouble("WorkHours");
        if (!rs.wasNull()) {
            record.setWorkHours(workHours);
        }
        
        return record;
    }
}



