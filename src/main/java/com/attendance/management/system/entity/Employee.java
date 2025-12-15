package com.attendance.management.system.entity;

public class Employee {
    private Integer eid;           // 员工ID
    private String did;            // 部门ID（字符型，长度10）
    private String pid;            // 职务ID（字符型，长度10）
    private String name;           // 姓名
    private String sex;            // 性别
    private String phone;          // 联系方式
    private String password;       // 账号密码
    private String role;           // 用户角色：EMPLOYEE, APPROVER, ADMIN

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

