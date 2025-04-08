package com.example.demo.controller;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.model.Department;
import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.service.SalaryCalculationService;

@Controller
@RequestMapping("/finance/salary-calculator")
public class SalaryCalculatorController {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private SalaryCalculationService salaryCalculationService;
    
    @GetMapping
    public String showSalaryCalculatorPage(Model model) {
        // Load all employees and departments for the selection dropdowns
        List<Employee> employees = employeeRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        
        model.addAttribute("employees", employees);
        model.addAttribute("departments", departments);
        
        return "finance/salary-calculator";
    }
    
    @GetMapping("/filter")
    public String filterEmployees(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            Model model) {
        
        List<Employee> filteredEmployees;
        
        if (departmentId != null) {
            // Filter by department id directly
            filteredEmployees = employeeRepository.findByDepartmentId(departmentId);
        } else {
            // Get all employees
            filteredEmployees = employeeRepository.findAll();
        }
        
        // Add all departments for the dropdown
        List<Department> departments = departmentRepository.findAll();
        
        model.addAttribute("employees", filteredEmployees);
        model.addAttribute("departments", departments);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedDepartmentId", departmentId);
        
        return "finance/salary-calculator :: #employeeTable";
    }
    
 @PostMapping("/calculate")
@ResponseBody
public ResponseEntity<Map<String, Object>> calculateSalaries(
        @RequestBody Map<String, Object> requestData) {
    
    try {
        int month = Integer.parseInt(requestData.get("month").toString());
        int year = Integer.parseInt(requestData.get("year").toString());
        @SuppressWarnings("unchecked")
        List<String> employeeIds = (List<String>) requestData.get("selectedEmployees");
        
        // Validate input
        if (month < 1 || month > 12 || year < 2000 || employeeIds == null || employeeIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Invalid input parameters");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Get the selected employees from the database
        List<Employee> selectedEmployees = employeeRepository.findAllById(employeeIds);
        
        // Create response object
        Map<String, Object> response = new HashMap<>();
        
        // Format month name for display
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String periodName = monthName + " " + year;
        
        // Create simplified calculation results for the response
        List<Map<String, Object>> calculationResults = new ArrayList<>();
        double totalNetSalary = 0;
        double totalBonuses = 0;
        
        // Calculate salary for each employee and build a simplified response object
        for (Employee employee : selectedEmployees) {
            SalaryCalculation calculation = salaryCalculationService.calculateSalary(employee, month, year);
            
            Map<String, Object> calcData = new HashMap<>();
            calcData.put("id", calculation.getId());
            
            Map<String, Object> empData = new HashMap<>();
            empData.put("employeeID", employee.getEmployeeID());
            empData.put("FullName", employee.getFullName());
            calcData.put("employee", empData);
            
            calcData.put("basicSalary", calculation.getBasicSalary());
            calcData.put("allowances", calculation.getAllowances());
            calcData.put("bonus", calculation.getBonus());
            calcData.put("overtimePay", calculation.getOvertimePay());
            calcData.put("incomeTax", calculation.getIncomeTax());
            calcData.put("providentFund", calculation.getProvidentFund());
            calcData.put("otherDeductions", calculation.getOtherDeductions());
            calcData.put("netSalary", calculation.getNetSalary());
            
            calculationResults.add(calcData);
            
            totalNetSalary += calculation.getNetSalary();
            totalBonuses += calculation.getBonus();
        }
        
        response.put("success", true);
        response.put("period", periodName);
        response.put("calculationResults", calculationResults);
        response.put("totalNetSalary", totalNetSalary);
        response.put("totalBonuses", totalBonuses);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Error calculating salaries: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}
}