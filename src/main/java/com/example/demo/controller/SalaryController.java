package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
public class SalaryController {

    @Autowired
    private EmployeeService employeeService;
    
    

    /**
     * Display employee salary details page
     */
    @GetMapping("/employee/salary")
    public String viewSalary(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            Model model) {
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get employee details
            Employee employee = employeeService.findByUsername(username);
            
            // If employee is null, redirect to error page
            if (employee == null) {
                return "redirect:/error";
            }
            
            // If month and year are not provided, use current month and year
            LocalDate currentDate = LocalDate.now();
            if (month == null) {
                month = currentDate.getMonthValue();
            }
            if (year == null) {
                year = currentDate.getYear();
            }
            
            // Get salary details for the selected month and year
            SalarySlip salarySlip = employee.getSalaryDetails();
            
            // In a real system, you would fetch historical salary data
            // For now, we'll just use the current salary details
            List<SalarySlip> salaryHistory = null;
            if (salarySlip != null) {
                // This is a placeholder for fetching historical data
                // salaryHistory = salaryService.getSalaryHistory(employee.getEmployeeID());
            }
            
            // Add to model
            model.addAttribute("employee", employee);
            model.addAttribute("salarySlip", salarySlip);
            model.addAttribute("salaryHistory", salaryHistory);
            model.addAttribute("selectedMonth", month);
            model.addAttribute("selectedYear", year);
            model.addAttribute("currentDate", currentDate);
            
            return "employee/salary";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    
    /**
     * Download salary slip
     */
    @GetMapping("/employee/salary/download")
    public String downloadSalarySlip(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            RedirectAttributes redirectAttributes) {
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get employee details
            Employee employee = employeeService.findByUsername(username);
            
            // If employee is null, redirect to error page
            if (employee == null) {
                return "redirect:/error";
            }
            
            // This is a placeholder for actual PDF generation logic
            // In a real system, you would generate a PDF and send it for download
            
            redirectAttributes.addFlashAttribute("success", "Salary slip downloaded successfully!");
            return "redirect:/employee/salary";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error downloading salary slip: " + e.getMessage());
            return "redirect:/employee/salary";
        }
    }
}