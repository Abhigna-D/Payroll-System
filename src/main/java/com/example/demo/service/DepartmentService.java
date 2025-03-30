package com.example.demo.service;

import com.example.demo.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Optional<Department> getDepartmentById(Long id);
    Department saveDepartment(Department department);
}