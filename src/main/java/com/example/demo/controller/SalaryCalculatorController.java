package com.example.demo.controller;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            // Calculate salary
            SalaryCalculation calculation = salaryCalculationService.calculateSalary(employee, month, year);
            
            // Save the calculation to get an ID (critical step to ensure ID is available)
            calculation = salaryCalculationService.saveSalaryCalculation(calculation);
            
            // Log for debugging
            System.out.println("Saved calculation with ID: " + calculation.getId() + " for employee: " + employee.getEmployeeID());
            
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

@PostMapping("/save")
@ResponseBody
public ResponseEntity<Map<String, Object>> saveSalaryCalculations(
        @RequestBody Map<String, Object> requestData) {
    
    try {
        // Debug logging
        System.out.println("Received request data: " + requestData);
        
        @SuppressWarnings("unchecked")
        List<Object> calculationIdsRaw = (List<Object>) requestData.get("calculationIds");
        
        // Convert objects to Long - handle potential number format issues
        List<Long> calculationIds = new ArrayList<>();
        if (calculationIdsRaw != null) {
            for (Object idObj : calculationIdsRaw) {
                try {
                    if (idObj instanceof Integer) {
                        calculationIds.add(((Integer) idObj).longValue());
                    } else if (idObj instanceof Long) {
                        calculationIds.add((Long) idObj);
                    } else if (idObj instanceof String) {
                        calculationIds.add(Long.parseLong((String) idObj));
                    } else if (idObj instanceof Number) {
                        calculationIds.add(((Number) idObj).longValue());
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse ID: " + idObj + " - " + e.getMessage());
                    // Continue with other IDs
                }
            }
        }
        
        // Validate input
        if (calculationIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "No valid salary calculation IDs provided");
            return ResponseEntity.badRequest().body(response);
        }
        
        System.out.println("Processed calculation IDs: " + calculationIds);
        
        // Save the calculations to the database (update their status to APPROVED)
        boolean saveSuccess = salaryCalculationService.saveSalaryCalculations(calculationIds);
        
        if (saveSuccess) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Salary calculations have been saved successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to save salary calculations");
            return ResponseEntity.status(500).body(response);
        }
    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Error saving salary calculations: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

@GetMapping("/details/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> getSalaryCalculationDetails(@PathVariable("id") Long id) {
    try {
        // Retrieve the calculation from the database
        Optional<SalaryCalculation> calculationOpt = salaryCalculationService.findById(id);
        
        if (calculationOpt.isPresent()) {
            SalaryCalculation calculation = calculationOpt.get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            
            // Create a detailed breakdown of the salary calculation
            Map<String, Object> details = new HashMap<>();
            
            // Basic employee info
            Map<String, Object> employeeInfo = new HashMap<>();
            employeeInfo.put("employeeID", calculation.getEmployee().getEmployeeID());
            employeeInfo.put("name", calculation.getEmployee().getFullName());
            employeeInfo.put("designation", calculation.getEmployee().getJobTitle());
            employeeInfo.put("department", calculation.getEmployee().getDepartment() != null ? 
                                          calculation.getEmployee().getDepartment().getName() : "Not Assigned");
            employeeInfo.put("isPartTime", calculation.getEmployee().isPartTime());
            
            details.put("employee", employeeInfo);
            
            // Earnings breakdown
            Map<String, Object> earnings = new HashMap<>();
            earnings.put("basicSalary", calculation.getBasicSalary());
            earnings.put("allowances", calculation.getAllowances());
            earnings.put("overtimePay", calculation.getOvertimePay());
            earnings.put("bonus", calculation.getBonus());
            earnings.put("totalEarnings", calculation.getTotalEarnings());
            
            details.put("earnings", earnings);
            
            // Deductions breakdown
            Map<String, Object> deductions = new HashMap<>();
            deductions.put("incomeTax", calculation.getIncomeTax());
            deductions.put("providentFund", calculation.getProvidentFund());
            deductions.put("otherDeductions", calculation.getOtherDeductions());
            deductions.put("totalDeductions", calculation.getTotalDeductions());
            
            details.put("deductions", deductions);
            
            // Pay summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("grossSalary", calculation.getGrossSalary());
            summary.put("netSalary", calculation.getNetSalary());
            summary.put("payPeriod", calculation.getPayPeriodFormatted());
            
            details.put("summary", summary);
            
            // HRA details if available
            try {
                details.put("hraExemption", calculation.getHraExemption());
                details.put("taxableHra", calculation.getTaxableHra());
            } catch (Exception e) {
                // Field might not exist in all implementations
            }
            
            // Status and metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", calculation.getStatus());
            metadata.put("createdAt", calculation.getCreatedAt());
            metadata.put("createdBy", calculation.getCreatedBy());
            metadata.put("lastModifiedAt", calculation.getLastModifiedAt());
            metadata.put("lastModifiedBy", calculation.getLastModifiedBy());
            metadata.put("remarks", calculation.getRemarks());
            
            details.put("metadata", metadata);
            
            result.put("details", details);
            
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Salary calculation not found");
            return ResponseEntity.status(404).body(error);
        }
    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Error retrieving salary calculation details: " + e.getMessage());
        return ResponseEntity.status(500).body(error);
    }
}




}