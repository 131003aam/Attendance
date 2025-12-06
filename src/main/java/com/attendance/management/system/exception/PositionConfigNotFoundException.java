package com.attendance.management.system.exception;

/**
 * 职务配置未找到异常
 */
public class PositionConfigNotFoundException extends RuntimeException {
    public PositionConfigNotFoundException(String message) {
        super(message);
    }

    public PositionConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

