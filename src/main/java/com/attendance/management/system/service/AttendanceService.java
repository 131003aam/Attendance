package com.attendance.management.system.service;

import com.attendance.management.system.constant.AttendanceStatus;
import com.attendance.management.system.constant.RecordType;
import com.attendance.management.system.entity.AttendanceRecord;
import com.attendance.management.system.entity.Employee;
import com.attendance.management.system.entity.PositionConfig;
import com.attendance.management.system.exception.AttendanceException;
import com.attendance.management.system.exception.EmployeeNotFoundException;
import com.attendance.management.system.exception.PositionConfigNotFoundException;
import com.attendance.management.system.repository.AttendanceRecordRepository;
import com.attendance.management.system.repository.EmployeeRepository;
import com.attendance.management.system.repository.PositionConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionConfigRepository positionConfigRepository;

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             EmployeeRepository employeeRepository,
                             PositionConfigRepository positionConfigRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.positionConfigRepository = positionConfigRepository;
    }

    /**
     * 上班打卡
     */
    public AttendanceRecord checkIn(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 查询员工信息
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("员工不存在: " + employeeId));

        // 查询职务配置
        PositionConfig positionConfig = positionConfigRepository
                .findByPositionId(employee.getPositionId())
                .orElseThrow(() -> new PositionConfigNotFoundException("未找到该职务的配置信息: " + employee.getPositionId()));

        // 查询当天是否已有打卡记录
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository
                .findByEmployeeIdAndDate(employeeId, today);

        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            // 如果已经有上班打卡记录，则不允许重复打卡
            if (record.getCheckInTime() != null) {
                throw new AttendanceException("今天已经打过上班卡了");
            }
        } else {
            record = new AttendanceRecord();
            record.setEmployeeId(employeeId);
            record.setDate(today);
            record.setRecordType(RecordType.NORMAL);
        }

        // 设置上班打卡时间
        record.setCheckInTime(now);

        // 根据职务配置判断是否迟到
        LocalTime standardStartTime = positionConfig.getWorkStartTime();
        if (standardStartTime != null && now.isAfter(standardStartTime.plusMinutes(30))) {
            record.setStatus(AttendanceStatus.LATE);
        } else {
            record.setStatus(AttendanceStatus.NORMAL);
        }

        return attendanceRecordRepository.save(record);
    }

    /**
     * 下班打卡
     */
    public AttendanceRecord checkOut(String employeeId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 查询员工信息
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("员工不存在: " + employeeId));

        // 查询当天打卡记录
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new AttendanceException("请先打上班卡"));

        // 检查是否已经打过下班卡
        if (record.getCheckOutTime() != null) {
            throw new AttendanceException("今天已经打过下班卡了");
        }

        // 查询职务配置
        PositionConfig positionConfig = positionConfigRepository
                .findByPositionId(employee.getPositionId())
                .orElseThrow(() -> new PositionConfigNotFoundException("未找到该职务的配置信息: " + employee.getPositionId()));

        // 设置下班打卡时间
        record.setCheckOutTime(now);

        // 根据职务配置判断是否早退
        LocalTime standardEndTime = positionConfig.getWorkEndTime();
        if (standardEndTime != null && now.isBefore(standardEndTime.minusMinutes(30))) {
            // 如果当前状态不是迟到，则标记为早退
            if (!AttendanceStatus.LATE.equals(record.getStatus())) {
                record.setStatus(AttendanceStatus.EARLY_LEAVE);
            }
            // 如果已经是迟到状态，则保持迟到状态
        } else {
            // 如果当前状态不是迟到，则标记为正常
            if (!AttendanceStatus.LATE.equals(record.getStatus())) {
                record.setStatus(AttendanceStatus.NORMAL);
            }
            // 如果已经是迟到状态，则保持迟到状态
        }

        return attendanceRecordRepository.save(record);
    }

    /**
     * 获取员工某天的考勤记录
     */
    public Optional<AttendanceRecord> getAttendanceRecord(String employeeId, LocalDate date) {
        return attendanceRecordRepository.findByEmployeeIdAndDate(employeeId, date);
    }

    /**
     * 获取员工一段时间内的考勤记录
     */
    public List<AttendanceRecord> getAttendanceRecords(String employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
    }

    /**
     * 管理员查询部门考勤记录
     */
    public List<AttendanceRecord> getDepartmentAttendanceRecords(String departmentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.findByDepartmentAndDateRange(departmentId, startDate, endDate);
    }

    /**
     * 获取所有考勤记录（用于汇总）
     */
    public List<AttendanceRecord> getAllAttendanceRecords(LocalDate startDate, LocalDate endDate) {
        return attendanceRecordRepository.findByDateRange(startDate, endDate);
    }
}


