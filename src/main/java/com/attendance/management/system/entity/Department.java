package com.attendance.management.system.entity;

public class Department {
    private String did;           // 部门ID（字符型，长度10）
    private String dName;         // 部门名称（字符型，长度30）
    private String description;   // 描述
    private Integer managerId;    // 负责人ID

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getDName() {
        return dName;
    }

    public void setDName(String dName) {
        this.dName = dName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }
}




















