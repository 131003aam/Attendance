package com.attendance.management.system.dao;

import com.attendance.management.system.entity.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordDAO {
    AttendanceRecord findByEmployeeIdAndDate(Integer eid, LocalDate date);
    void insert(AttendanceRecord record);
    void update(AttendanceRecord record);
    List<AttendanceRecord> findByEmployeeId(Integer eid);
    List<AttendanceRecord> findByEmployeeIdAndDateRange(Integer eid, LocalDate startDate, LocalDate endDate);
    // 新增方法：支持状态筛选
    List<AttendanceRecord> findByEmployeeIdAndDateRangeAndStatus(Integer eid, LocalDate startDate, LocalDate endDate, String status);
    // 新增方法：按部门查询
    List<AttendanceRecord> findByDepartmentIdAndDateRange(Integer departmentId, LocalDate startDate, LocalDate endDate);
    // 新增方法：按部门和状态查询
    List<AttendanceRecord> findByDepartmentIdAndDateRangeAndStatus(Integer departmentId, LocalDate startDate, LocalDate endDate, String status);
    // 新增方法：查询所有记录（按日期范围）
    List<AttendanceRecord> findAllByDateRange(LocalDate startDate, LocalDate endDate);
    // 新增方法：查询所有记录（按日期范围和状态）
    List<AttendanceRecord> findAllByDateRangeAndStatus(LocalDate startDate, LocalDate endDate, String status);
    // 新增方法：查询所有记录（无日期限制）
    List<AttendanceRecord> findAll();
}



