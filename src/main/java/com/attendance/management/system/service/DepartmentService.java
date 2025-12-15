package com.attendance.management.system.service;

import com.attendance.management.system.dao.DepartmentDAO;
import com.attendance.management.system.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentDAO departmentDAO;

    public List<Department> getAllDepartments() {
        return departmentDAO.findAll();
    }

    public Department getDepartmentById(String did) {
        return departmentDAO.findById(did);
    }

    public void createDepartment(Department department) {
        departmentDAO.insert(department);
    }

    public void updateDepartment(Department department) {
        departmentDAO.update(department);
    }

    public void deleteDepartment(String did) {
        departmentDAO.delete(did);
    }
}




















