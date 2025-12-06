package com.attendance.management.system.repository;

import com.attendance.management.system.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByEmployeeIdAndDate(String employeeId, LocalDate date);

    List<AttendanceRecord> findByEmployeeId(String employeeId);

    List<AttendanceRecord> findByEmployeeIdAndDateBetween(String employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.date BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE (:departmentId IS NULL OR ar.employeeId IN " +
            "(SELECT e.employeeId FROM com.attendance.management.system.entity.Employee e WHERE e.departmentId = :departmentId)) " +
            "AND ar.date BETWEEN :startDate AND :endDate")
    List<AttendanceRecord> findByDepartmentAndDateRange(@Param("departmentId") String departmentId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
}
