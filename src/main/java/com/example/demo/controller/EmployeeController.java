package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.Tax;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    // Create a new employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }
    
    // Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    // Get employee by ID - modified to use String employeeID
    @GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeByEmployeeID(employeeId);
        return employee.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // Get employee by employee ID (redundant, can be removed)
    @GetMapping("/employeeId/{employeeId}")
    public ResponseEntity<Employee> getEmployeeByEmployeeID(@PathVariable String employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeByEmployeeID(employeeId);
        return employee.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    // Update employee - modified to use String employeeID
    @PutMapping("/{employeeId}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String employeeId, @RequestBody Employee employee) {
        Optional<Employee> existingEmployee = employeeService.getEmployeeByEmployeeID(employeeId);
        if (existingEmployee.isPresent()) {
            employee.setEmployeeID(employeeId);
            Employee updatedEmployee = employeeService.updateEmployee(employee);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Delete employee - modified to use String employeeID
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String employeeId) {
        Optional<Employee> existingEmployee = employeeService.getEmployeeByEmployeeID(employeeId);
        if (existingEmployee.isPresent()) {
            employeeService.deleteEmployee(employeeId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get employees by department
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable int deptId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(deptId);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }
    
    // View salary details - modified to use String employeeID
    @GetMapping("/{employeeId}/salary")
    public ResponseEntity<SalarySlip> viewSalaryDetails(@PathVariable String employeeId) {
        SalarySlip salarySlip = employeeService.viewSalaryDetails(employeeId);
        if (salarySlip != null) {
            return new ResponseEntity<>(salarySlip, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Submit tax details - modified to use String employeeID
    @PostMapping("/{employeeId}/tax")
    public ResponseEntity<Tax> submitTaxDetails(@PathVariable String employeeId, @RequestBody Tax taxDetails) {
        Tax submittedTax = employeeService.submitTaxDetails(employeeId, taxDetails);
        if (submittedTax != null) {
            return new ResponseEntity<>(submittedTax, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Request salary correction - modified to use String employeeID
    @PutMapping("/{employeeId}/salary-correction")
    public ResponseEntity<String> requestSalaryCorrection(@PathVariable String employeeId, @RequestBody SalarySlip correctedSalary) {
        boolean result = employeeService.requestSalaryCorrection(employeeId, correctedSalary);
        if (result) {
            return new ResponseEntity<>("Salary correction request submitted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND);
        }
    }
    
    // Submit timesheet - modified to use String employeeID
    @PostMapping("/{employeeId}/timesheet")
    public ResponseEntity<String> submitTimesheet(@PathVariable String employeeId, @RequestBody Map<String, String> timesheetDetails) {
        boolean result = employeeService.submitTimesheet(employeeId, timesheetDetails.get("details"));
        if (result) {
            return new ResponseEntity<>("Timesheet submitted successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND);
        }
    }
}