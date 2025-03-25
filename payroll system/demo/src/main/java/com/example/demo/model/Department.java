package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int deptID;
    private String name;
    
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
    
    public String getDepartmentDetails() {
        return "Department: " + name + " (ID: " + deptID + ")";
    }
    
    public void addEmployeeToDept(Employee employee) {
        employees.add(employee);
        employee.setDepartment(this);
    }
}