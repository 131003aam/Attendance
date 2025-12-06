package com.attendance.management.system.exception;

/**
 * 考勤相关异常
 */
public class AttendanceException extends RuntimeException {
    public AttendanceException(String message) {
        super(message);
    }

    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
    }
}

