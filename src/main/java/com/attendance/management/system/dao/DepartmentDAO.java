package com.attendance.management.system.dao;

import com.attendance.management.system.entity.Department;
import java.util.List;

public interface DepartmentDAO {
    List<Department> findAll();
    Department findById(String did);
    void insert(Department department);
    void update(Department department);
    void delete(String did);
}




















