CREATE TABLE employees (
                           id VARCHAR(10) PRIMARY KEY,
                           employee_id VARCHAR(10) UNIQUE NOT NULL,
                           name VARCHAR(50) NOT NULL,
                           gender ENUM('男', '女'),
                           department_id VARCHAR(20),
                           position_id VARCHAR(20),
                           contact_info VARCHAR(100),
                           status ENUM('在职', '离职') DEFAULT '在职',
                           user_role ENUM('employee', 'admin') DEFAULT 'employee',
                           password_hash VARCHAR(255) NOT NULL
);