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
import com.example.demo.service.PayslipService;
import com.example.demo.service.SalaryCalculationService;

@Controller
@RequestMapping("/finance/payslips")
public class PayslipController {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private SalaryCalculationService salaryCalculationService;
    
    @Autowired
    private PayslipService payslipService;
    
    @GetMapping
    public String showPayslipsPage(Model model) {
        // Load all employees and departments for the selection dropdowns
        List<Employee> employees = employeeRepository.findAll();
        List<Department> departments = departmentRepository.findAll();
        
        // Add current month and year as default values
        java.time.LocalDate today = java.time.LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        
        model.addAttribute("employees", employees);
        model.addAttribute("departments", departments);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);
        
        return "finance/payslips";
    }
    
    @GetMapping("/filter")
public String filterEmployees(
    @RequestParam("month") int month,
    @RequestParam("year") int year,
    @RequestParam(value = "departmentId", required = false) String departmentIdStr,
    Model model) {

    try {
        Long departmentId = null;
        if (departmentIdStr != null && !departmentIdStr.isEmpty() && !"null".equals(departmentIdStr)) {
            try {
                departmentId = Long.parseLong(departmentIdStr);
            } catch (NumberFormatException e) {
                System.err.println("Invalid department ID format: " + departmentIdStr);
            }
        }
    
        List<Employee> filteredEmployees;
        if (departmentId != null && departmentId > 0) {
            filteredEmployees = employeeRepository.findByDepartmentId(departmentId);
        } else {
            filteredEmployees = employeeRepository.findAll();
        }
    
        // Create data for all employees, don't check for approval status
        List<Map<String, Object>> employeesWithStatus = new ArrayList<>();
        
        for (Employee employee : filteredEmployees) {
            Map<String, Object> empData = new HashMap<>();
            empData.put("employee", employee);
            
            // Get the salary calculation regardless of status
            SalaryCalculation salaryCalc = salaryCalculationService.findByEmployeeAndMonthAndYear(employee, month, year);
            
            // Always set to true to allow generating payslips without approval check
            empData.put("hasApprovedCalculation", true);
            empData.put("salaryCalculation", salaryCalc);
            
            employeesWithStatus.add(empData);
        }
        
        List<Department> departments = departmentRepository.findAll();
        
        model.addAttribute("employeesWithStatus", employeesWithStatus);
        model.addAttribute("departments", departments);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("monthName", Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        
        return "finance/payslips :: #employeeTable";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Error filtering employees: " + e.getMessage());
        model.addAttribute("employeesWithStatus", new ArrayList<>());
        return "finance/payslips :: #employeeTable";
    }
}
@GetMapping("/view/{employeeId}/{month}/{year}")
public String viewPayslip(
        @PathVariable("employeeId") String employeeId,
        @PathVariable("month") int month,
        @PathVariable("year") int year,
        Model model) {
    
    // Find the employee
    Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
    
    if (!employeeOpt.isPresent()) {
        model.addAttribute("error", "Employee not found");
        return "finance/error";
    }
    
    Employee employee = employeeOpt.get();
    
    // Find salary calculation for this employee for the specified month/year
    SalaryCalculation salaryCalc = salaryCalculationService.findByEmployeeAndMonthAndYear(employee, month, year);
    
    if (salaryCalc == null) {
        model.addAttribute("error", "No salary calculation found for the specified month and year");
        return "finance/error";
    }
    
    // Remove this approval check
    // if (!"APPROVED".equals(salaryCalc.getStatus())) {
    //     model.addAttribute("error", "Salary calculation is not approved yet");
    //     return "finance/error";
    // }
    
    // Generate payslip data
    Map<String, Object> payslipData = payslipService.generatePayslipData(salaryCalc);
    
    model.addAttribute("payslipData", payslipData);
    model.addAttribute("employee", employee);
    model.addAttribute("salaryCalc", salaryCalc);
    model.addAttribute("month", Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    model.addAttribute("year", year);
    
    return "finance/payslip-view";
}
@GetMapping("/generate/{employeeId}/{month}/{year}")
@ResponseBody
public ResponseEntity<Map<String, Object>> generatePayslip(
        @PathVariable("employeeId") String employeeId,
        @PathVariable("month") int month,
        @PathVariable("year") int year) {
    
    Map<String, Object> response = new HashMap<>();
    
    try {
        // Find the employee
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        
        if (!employeeOpt.isPresent()) {
            response.put("success", false);
            response.put("error", "Employee not found");
            return ResponseEntity.badRequest().body(response);
        }
        
        Employee employee = employeeOpt.get();
        
        // Find salary calculation for this employee for the specified month/year
        SalaryCalculation salaryCalc = salaryCalculationService.findByEmployeeAndMonthAndYear(employee, month, year);
        
        // If no calculation exists, create a default one
        if (salaryCalc == null) {
            salaryCalc = new SalaryCalculation(employee, month, year);
            // Set default values based on employee's salary details or other data
            if (employee.getSalaryDetails() != null) {
                salaryCalc.setBasicSalary(employee.getSalaryDetails().getBasicSalary() != null ? 
                                        employee.getSalaryDetails().getBasicSalary() : 0.0);
                salaryCalc.setGrossSalary(employee.getSalaryDetails().getGrossSalary() != null ?
                                        employee.getSalaryDetails().getGrossSalary() : 0.0);
                // Set other fields as needed
            }
            salaryCalc.setStatus("APPROVED"); // Always set to approved
            salaryCalc.calculateNetSalary(); // Calculate the net salary
            
            // Save the calculation
            salaryCalc = salaryCalculationService.saveSalaryCalculation(salaryCalc);
        } else {
            // If calculation exists but isn't approved, approve it
            if (!"APPROVED".equals(salaryCalc.getStatus())) {
                salaryCalc.setStatus("APPROVED");
                salaryCalc = salaryCalculationService.saveSalaryCalculation(salaryCalc);
            }
        }
        
        // Generate the payslip
        boolean generated = payslipService.generatePayslip(salaryCalc);
        
        if (generated) {
            response.put("success", true);
            response.put("message", "Payslip generated successfully");
            response.put("viewUrl", "/finance/payslips/view/" + employeeId + "/" + month + "/" + year);
        } else {
            response.put("success", false);
            response.put("error", "Failed to generate payslip");
        }
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        e.printStackTrace();
        response.put("success", false);
        response.put("error", "Error generating payslip: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

    @PostMapping("/generate-multiple")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateMultiplePayslips(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int month = Integer.parseInt(requestData.get("month").toString());
            int year = Integer.parseInt(requestData.get("year").toString());
            
            @SuppressWarnings("unchecked")
            List<String> employeeIds = (List<String>) requestData.get("employeeIds");
            
            if (employeeIds == null || employeeIds.isEmpty()) {
                response.put("success", false);
                response.put("error", "No employees selected");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> successList = new ArrayList<>();
            List<String> failureList = new ArrayList<>();
            
            for (String employeeId : employeeIds) {
                try {
                    Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
                    
                    if (!employeeOpt.isPresent()) {
                        failureList.add(employeeId + " (Employee not found)");
                        continue;
                    }
                    
                    Employee employee = employeeOpt.get();
                    
                    SalaryCalculation salaryCalc = salaryCalculationService.findByEmployeeAndMonthAndYear(employee, month, year);
                    
                    // If no calculation exists, create one
                    // Always make sure the status is approved when creating or updating calculations
if (salaryCalc == null) {
    salaryCalc = new SalaryCalculation(employee, month, year);
    // Set default values based on employee's salary details
    if (employee.getSalaryDetails() != null) {
        salaryCalc.setBasicSalary(employee.getSalaryDetails().getBasicSalary() != null ? 
                                employee.getSalaryDetails().getBasicSalary() : 0.0);
        salaryCalc.setGrossSalary(employee.getSalaryDetails().getGrossSalary() != null ?
                                employee.getSalaryDetails().getGrossSalary() : 0.0);
    }
    salaryCalc.setStatus("APPROVED");
    salaryCalc.calculateNetSalary();
    salaryCalc = salaryCalculationService.saveSalaryCalculation(salaryCalc);
} else if (!"APPROVED".equals(salaryCalc.getStatus())) {
    salaryCalc.setStatus("APPROVED");
    salaryCalc = salaryCalculationService.saveSalaryCalculation(salaryCalc);
}
                    boolean generated = payslipService.generatePayslip(salaryCalc);
                    
                    if (generated) {
                        successList.add(employee.getFullName());
                    } else {
                        failureList.add(employee.getFullName() + " (Generation failed)");
                    }
                } catch (Exception e) {
                    failureList.add(employeeId + " (Error: " + e.getMessage() + ")");
                }
            }
            
            response.put("success", true);
            response.put("successCount", successList.size());
            response.put("failureCount", failureList.size());
            response.put("successList", successList);
            response.put("failureList", failureList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Error generating payslips: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}