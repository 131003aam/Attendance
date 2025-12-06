package com.attendance.management.system.constant;

/**
 * 考勤记录类型常量
 */
public final class RecordType {
    private RecordType() {
        // 工具类，禁止实例化
    }

    /** 正常打卡 */
    public static final String NORMAL = "NORMAL";
    
    /** 请假 */
    public static final String LEAVE = "LEAVE";
    
    /** 加班 */
    public static final String OVERTIME = "OVERTIME";
    
    /** 出差 */
    public static final String BUSINESS_TRIP = "BUSINESS_TRIP";
}

