-- ============================================
-- 考勤管理系统 - 完整数据库初始化脚本
-- ============================================
-- 说明：此脚本用于创建完整的数据库表结构并插入示例数据
-- 执行前请确保已创建数据库：CREATE DATABASE attendance;
-- 使用方法：mysql -u root -p attendance < init_database.sql

-- ============================================
-- 第一部分：删除已存在的表（按依赖关系倒序）
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS attendance_record;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS position_config;
DROP TABLE IF EXISTS department;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第二部分：创建表结构（按依赖关系顺序）
-- ============================================

-- 1. 部门表
CREATE TABLE department (
    DID CHAR(10) PRIMARY KEY COMMENT '部门ID，格式：D000000001',
    DName VARCHAR(30) NOT NULL COMMENT '部门名称',
    Description VARCHAR(200) COMMENT '部门描述',
    ManagerID INT COMMENT '负责人ID（员工ID）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门信息表';

-- 2. 职务配置表
CREATE TABLE position_config (
    PID CHAR(10) PRIMARY KEY COMMENT '职务ID，格式：P000000001',
    PName VARCHAR(20) NOT NULL COMMENT '职务名称',
    WorkStartTime TIME NOT NULL COMMENT '标准上班时间（HH:MM:SS）',
    WorkEndTime TIME NOT NULL COMMENT '标准下班时间（HH:MM:SS）',
    MonthlyWorkDays INT DEFAULT 21 COMMENT '月工作日（天数）',
    DailyWorkHours DECIMAL(3,1) DEFAULT 8.0 COMMENT '每日工作时长（小时）',
    Description VARCHAR(200) COMMENT '职务描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职务配置表';

-- 3. 员工表
CREATE TABLE employee (
    EID INT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID（自增）',
    DID CHAR(10) NOT NULL COMMENT '部门ID',
    PID CHAR(10) NOT NULL COMMENT '职务ID',
    EName CHAR(20) NOT NULL COMMENT '员工姓名',
    Sex CHAR(2) CHECK (Sex IN ('男', '女')) COMMENT '性别',
    Phone CHAR(11) UNIQUE COMMENT '联系电话（唯一）',
    Password CHAR(20) NOT NULL COMMENT '登录密码',
    Role CHAR(10) DEFAULT 'EMPLOYEE' COMMENT '用户角色：EMPLOYEE-员工, APPROVER-审批人, ADMIN-管理员',
    FOREIGN KEY (DID) REFERENCES department(DID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (PID) REFERENCES position_config(PID) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_did (DID),
    INDEX idx_pid (PID),
    INDEX idx_role (Role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息表';

-- 4. 考勤记录表
CREATE TABLE attendance_record (
    AID INT AUTO_INCREMENT PRIMARY KEY COMMENT '考勤记录ID（自增）',
    EID INT NOT NULL COMMENT '员工ID',
    RecordDate DATE NOT NULL COMMENT '考勤日期',
    CheckInTime DATETIME COMMENT '签到时间',
    CheckInLocation VARCHAR(100) COMMENT '签到地点',
    CheckOutTime DATETIME COMMENT '签退时间',
    CheckOutLocation VARCHAR(100) COMMENT '签退地点',
    Status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '考勤状态：NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, MISSING-缺勤',
    WorkHours DECIMAL(4,2) COMMENT '工作时长（小时）',
    FOREIGN KEY (EID) REFERENCES employee(EID) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_eid (EID),
    INDEX idx_date (RecordDate),
    UNIQUE KEY uk_employee_date (EID, RecordDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 5. 申请表
CREATE TABLE application (
    AID INT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID（自增）',
    EID INT NOT NULL COMMENT '申请人ID（员工ID）',
    ApplicationType VARCHAR(20) NOT NULL COMMENT '申请类型：LEAVE-请假, REISSUE-补卡, OVERTIME-加班, BUSINESS_TRIP-出差',
    StartTime DATETIME NOT NULL COMMENT '开始时间',
    EndTime DATETIME NOT NULL COMMENT '结束时间',
    Reason TEXT NOT NULL COMMENT '申请原因',
    Status VARCHAR(20) DEFAULT 'PENDING' COMMENT '申请状态：PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    ApproverID INT COMMENT '审批人ID',
    ApproveTime DATETIME COMMENT '审批时间',
    RejectReason TEXT COMMENT '拒绝原因',
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 请假特有字段
    LeaveType VARCHAR(20) COMMENT '请假类型：ANNUAL-年假, SICK-病假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, OTHER-其他',
    Attachment VARCHAR(500) COMMENT '附件路径',
    -- 补卡特有字段
    ReissueType VARCHAR(20) COMMENT '补卡类型：MISSING_CHECK_IN-漏签, MISSING_CHECK_OUT-漏退, BOTH-漏签漏退',
    ReissueTime DATETIME COMMENT '补卡时间',
    -- 加班特有字段
    OvertimeType VARCHAR(20) COMMENT '加班类型：WEEKDAY-工作日, WEEKEND-周末, HOLIDAY-节假日',
    IsCompensatory BOOLEAN DEFAULT FALSE COMMENT '是否调休',
    -- 出差特有字段
    Destination VARCHAR(100) COMMENT '目的地',
    Companions VARCHAR(500) COMMENT '随行人员ID列表（JSON格式）',
    Transportation VARCHAR(50) COMMENT '交通工具',
    Budget DECIMAL(10,2) COMMENT '预算金额',
    FOREIGN KEY (EID) REFERENCES employee(EID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (ApproverID) REFERENCES employee(EID) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_eid (EID),
    INDEX idx_status (Status),
    INDEX idx_type (ApplicationType),
    INDEX idx_created (CreatedAt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请表';

-- ============================================
-- 第三部分：插入示例数据
-- ============================================

-- 1. 插入部门数据
INSERT INTO department (DID, DName, Description, ManagerID) VALUES
('D000000001', '技术部', '负责技术开发、系统维护和技术支持', NULL),
('D000000002', '人事部', '负责人事管理、招聘和员工关系', NULL),
('D000000003', '财务部', '负责财务管理、会计核算和预算控制', NULL),
('D000000004', '市场部', '负责市场推广、品牌建设和客户关系', NULL),
('D000000005', '运营部', '负责日常运营、流程优化和业务支持', NULL);

-- 2. 插入职务配置数据
INSERT INTO position_config (PID, PName, WorkStartTime, WorkEndTime, MonthlyWorkDays, DailyWorkHours, Description) VALUES
('P000000001', '总经理', '09:00:00', '18:00:00', 22, 8.0, '公司总经理，负责全面管理'),
('P000000002', '部门经理', '09:00:00', '18:00:00', 21, 8.0, '部门经理，负责部门管理'),
('P000000003', '主管', '08:30:00', '17:30:00', 22, 8.0, '部门主管，负责团队管理'),
('P000000004', '高级员工', '09:00:00', '18:00:00', 21, 8.0, '高级员工，负责核心业务'),
('P000000005', '普通员工', '09:00:00', '18:00:00', 21, 8.0, '普通员工，负责日常业务'),
('P000000006', '实习生', '09:00:00', '18:00:00', 20, 7.5, '实习生，负责辅助工作');

-- 3. 插入员工数据
-- 注意：密码为 '123456'（实际应用中应使用加密存储）
INSERT INTO employee (EID, DID, PID, EName, Sex, Phone, Password, Role) VALUES
(10001, 'D000000001', 'P000000001', '张三', '男', '13800138001', '123456', 'ADMIN'),
(10002, 'D000000001', 'P000000002', '李四', '男', '13800138002', '123456', 'APPROVER'),
(10003, 'D000000002', 'P000000002', '王五', '女', '13800138003', '123456', 'APPROVER'),
(10004, 'D000000001', 'P000000003', '赵六', '男', '13800138004', '123456', 'EMPLOYEE'),
(10005, 'D000000001', 'P000000004', '钱七', '女', '13800138005', '123456', 'EMPLOYEE'),
(10006, 'D000000002', 'P000000005', '孙八', '男', '13800138006', '123456', 'EMPLOYEE'),
(10007, 'D000000003', 'P000000005', '周九', '女', '13800138007', '123456', 'EMPLOYEE'),
(10008, 'D000000004', 'P000000003', '吴十', '男', '13800138008', '123456', 'EMPLOYEE'),
(10009, 'D000000005', 'P000000005', '郑一', '女', '13800138009', '123456', 'EMPLOYEE'),
(10010, 'D000000001', 'P000000006', '王二', '男', '13800138010', '123456', 'EMPLOYEE');

-- 4. 更新部门负责人
UPDATE department SET ManagerID = 10001 WHERE DID = 'D000000001';
UPDATE department SET ManagerID = 10003 WHERE DID = 'D000000002';

-- 5. 插入考勤记录示例数据（最近7天）
INSERT INTO attendance_record (EID, RecordDate, CheckInTime, CheckInLocation, CheckOutTime, CheckOutLocation, Status, WorkHours) VALUES
(10001, CURDATE(), CONCAT(CURDATE(), ' 09:00:00'), '公司', CONCAT(CURDATE(), ' 18:00:00'), '公司', 'NORMAL', 8.0),
(10002, CURDATE(), CONCAT(CURDATE(), ' 09:05:00'), '公司', CONCAT(CURDATE(), ' 18:00:00'), '公司', 'NORMAL', 7.92),
(10004, CURDATE(), CONCAT(CURDATE(), ' 08:30:00'), '公司', CONCAT(CURDATE(), ' 17:30:00'), '公司', 'NORMAL', 8.0),
(10005, CURDATE(), CONCAT(CURDATE(), ' 09:15:00'), '公司', CONCAT(CURDATE(), ' 18:00:00'), '公司', 'LATE', 7.75),
(10006, DATE_SUB(CURDATE(), INTERVAL 1 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 09:00:00'), '公司', CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:00:00'), '公司', 'NORMAL', 8.0),
(10007, DATE_SUB(CURDATE(), INTERVAL 2 DAY), CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 09:00:00'), '公司', CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 17:00:00'), '公司', 'EARLY_LEAVE', 7.0);

-- 6. 插入申请示例数据
INSERT INTO application (EID, ApplicationType, StartTime, EndTime, Reason, Status, LeaveType, CreatedAt) VALUES
(10004, 'LEAVE', CONCAT(CURDATE(), ' 09:00:00'), CONCAT(CURDATE(), ' 18:00:00'), '家里有事需要处理', 'PENDING', 'PERSONAL', NOW()),
(10005, 'LEAVE', CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 09:00:00'), CONCAT(DATE_ADD(CURDATE(), INTERVAL 3 DAY), ' 18:00:00'), '年假休息', 'APPROVED', 'ANNUAL', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10006, 'OVERTIME', CONCAT(CURDATE(), ' 18:00:00'), CONCAT(CURDATE(), ' 21:00:00'), '项目紧急，需要加班完成', 'PENDING', 'WEEKDAY', NOW()),
(10007, 'BUSINESS_TRIP', CONCAT(DATE_ADD(CURDATE(), INTERVAL 5 DAY), ' 09:00:00'), CONCAT(DATE_ADD(CURDATE(), INTERVAL 7 DAY), ' 18:00:00'), '前往北京参加技术会议', 'PENDING', NULL, NOW());

-- 更新已批准申请的审批信息
UPDATE application SET ApproverID = 10002, ApproveTime = DATE_SUB(NOW(), INTERVAL 1 DAY) WHERE AID = 2;

-- ============================================
-- 第四部分：查询验证
-- ============================================

-- 验证数据插入情况
SELECT '部门数量' AS '数据项', COUNT(*) AS '数量' FROM department
UNION ALL
SELECT '职务配置数量', COUNT(*) FROM position_config
UNION ALL
SELECT '员工数量', COUNT(*) FROM employee
UNION ALL
SELECT '考勤记录数量', COUNT(*) FROM attendance_record
UNION ALL
SELECT '申请数量', COUNT(*) FROM application;

-- 显示员工信息（包含部门和职务）
SELECT 
    e.EID AS '员工ID',
    e.EName AS '姓名',
    d.DName AS '部门',
    p.PName AS '职务',
    e.Role AS '角色',
    e.Phone AS '电话'
FROM employee e
LEFT JOIN department d ON e.DID = d.DID
LEFT JOIN position_config p ON e.PID = p.PID
ORDER BY e.EID;

-- ============================================
-- 脚本执行完成
-- ============================================
-- 默认账号信息：
-- 管理员：10001 / 123456 (张三)
-- 审批人：10002 / 123456 (李四), 10003 / 123456 (王五)
-- 员工：10004-10010 / 123456
-- ============================================




















