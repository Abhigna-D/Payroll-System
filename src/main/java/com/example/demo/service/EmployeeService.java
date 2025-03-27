package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.Tax;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    
    Employee createEmployee(Employee employee);
    
    List<Employee> getAllEmployees();
    
    Optional<Employee> getEmployeeByEmployeeID(String employeeID);
    
    Employee updateEmployee(Employee employee);
    
    void deleteEmployee(String employeeID);
    
    List<Employee> getEmployeesByDepartment(int deptId);
    
    SalarySlip viewSalaryDetails(String employeeID);
    
    Tax submitTaxDetails(String employeeID, Tax taxDetails);
    
    boolean requestSalaryCorrection(String employeeID, SalarySlip correctedSalary);
    
    boolean submitTimesheet(String employeeID, String details);
}