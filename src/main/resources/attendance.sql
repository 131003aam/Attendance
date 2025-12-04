DROP TABLE IF EXISTS employee;

CREATE TABLE employee (
<<<<<<< Updated upstream
    EID INT AUTO_INCREMENT PRIMARY KEY,
    DID INT NOT NULL,
    PID INT NOT NULL,
    EName CHAR(20) NOT NULL,
    Sex CHAR(2) CHECK (Sex IN ('男', '女')),
    Phone CHAR(11) UNIQUE,
    Password CHAR(20) NOT NULL
);
=======
                          EID INT AUTO_INCREMENT PRIMARY KEY,
                          DID INT NOT NULL,
                          PID INT NOT NULL,
                          EName CHAR(20) NOT NULL,
                          Sex CHAR(2) CHECK (Sex IN ('男', '女')),
                          Phone CHAR(11) UNIQUE,
                          Password CHAR(20) NOT NULL
);

CREATE TABLE position_config (
                                 PID VARCHAR(50) UNIQUE NOT NULL COMMENT '职务ID',
                                 PName VARCHAR(20) COMMENT '职务名称',
                                 PsTime TIME COMMENT '标准上班时间',
                                 PeTime TIME COMMENT '标准下班时间',
                                 PMonthWork INT COMMENT '月工作日要求',
                                 PDayWork DECIMAL(3,1) COMMENT '日工作小时数',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '职务配置表';

CREATE TABLE `attendance_record` (
                                     ID INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
                                     EID VARCHAR(50) NOT NULL COMMENT '员工ID',
                                     NDate DATE NOT NULL COMMENT '打卡日期',
                                     NsTime DATETIME COMMENT '上班打卡时间',
                                     NeTime DATETIME COMMENT '下班打卡时间',
                                     record_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '记录类型(NORMAL,LEAVE,OVERTIME,BUSINESS_TRIP)',
                                     NStatus VARCHAR(20) CHECK (NStatus IN ('正常', '异常')) COMMENT '出勤状态',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     INDEX `idx_employee_date` (`EID`, `NDate`),
                                     INDEX `idx_date` (`NDate`)
) COMMENT '考勤记录表';
>>>>>>> Stashed changes
