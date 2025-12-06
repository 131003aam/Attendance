-- 考勤管理系统数据库表结构
-- 根据需求分析规范设计

-- 删除已存在的表（按依赖顺序）
DROP TABLE IF EXISTS attendance_record;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS position_config;
DROP TABLE IF EXISTS department;

-- 部门表
CREATE TABLE department (
    DID CHAR(10) PRIMARY KEY COMMENT '部门ID',
    DName VARCHAR(30) NOT NULL COMMENT '部门名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '部门表';

-- 职务配置表
CREATE TABLE position_config (
    PID CHAR(10) PRIMARY KEY COMMENT '职务ID',
    PName VARCHAR(20) NOT NULL COMMENT '职务名称',
    PsTime TIME COMMENT '标准上班时间(HH:MM:SS)',
    PeTime TIME COMMENT '标准下班时间(HH:MM:SS)',
    PMonthWork INT(2) COMMENT '月工作日要求',
    PDayWork DECIMAL(2,0) COMMENT '日工作小时数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '职务配置表';

-- 员工表
CREATE TABLE employee (
    EID CHAR(10) PRIMARY KEY COMMENT '员工ID',
    DID CHAR(10) NOT NULL COMMENT '部门ID',
    PID CHAR(10) NOT NULL COMMENT '职务ID',
    EName VARCHAR(20) NOT NULL COMMENT '姓名',
    Sex CHAR(2) CHECK (Sex IN ('男', '女')) COMMENT '性别',
    Phone CHAR(11) UNIQUE COMMENT '联系方式',
    Password VARCHAR(255) NOT NULL COMMENT '账号密码(加密存储)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (DID) REFERENCES department(DID),
    FOREIGN KEY (PID) REFERENCES position_config(PID)
) COMMENT '员工表';

-- 考勤记录表
CREATE TABLE attendance_record (
    ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '考勤记录ID',
    EID CHAR(10) NOT NULL COMMENT '员工ID',
    NDate DATE NOT NULL COMMENT '打卡日期(YYYY-MM-DD)',
    NsTime TIME COMMENT '签到时间(HH:MM:SS)',
    NeTime TIME COMMENT '签退时间(HH:MM:SS)',
    record_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '记录类型(NORMAL,LEAVE,OVERTIME,BUSINESS_TRIP)',
    NStatus VARCHAR(10) CHECK (NStatus IN ('正常', '迟到', '早退', '缺勤', '请假', '出差', '旷工')) COMMENT '考勤状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (EID) REFERENCES employee(EID),
    INDEX `idx_employee_date` (`EID`, `NDate`),
    INDEX `idx_date` (`NDate`)
) COMMENT '考勤记录表';
