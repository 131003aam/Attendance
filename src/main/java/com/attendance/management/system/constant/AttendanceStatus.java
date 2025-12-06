package com.attendance.management.system.constant;

/**
 * 考勤状态常量
 */
public final class AttendanceStatus {
    private AttendanceStatus() {
        // 工具类，禁止实例化
    }

    /** 正常 */
    public static final String NORMAL = "正常";
    
    /** 迟到 */
    public static final String LATE = "迟到";
    
    /** 早退 */
    public static final String EARLY_LEAVE = "早退";
    
    /** 缺勤 */
    public static final String MISSING = "缺勤";
}

