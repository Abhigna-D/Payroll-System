package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;

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
    
    // Changed from Tax to TaxDeclaration
    TaxDeclaration submitTaxDetails(String employeeID, TaxDeclaration taxDetails);
    
    boolean requestSalaryCorrection(String employeeID, SalarySlip correctedSalary);
    
    boolean submitTimesheet(String employeeID, String details);

    // Changed to return Employee directly instead of Optional<Employee>
    Employee findByUsername(String username);
    Employee findByEmployeeId(String employeeId);
    
    // Find all employees in a department
    List<Employee> findByDepartmentId(Long departmentId);
}