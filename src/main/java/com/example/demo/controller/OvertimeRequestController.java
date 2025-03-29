package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.OvertimeRequest;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.OvertimeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class OvertimeRequestController {

    @Autowired
    private OvertimeRequestService overtimeRequestService;

    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Display overtime request form
     */
    @GetMapping("/employee/overtime-form")
    public String showOvertimeForm(Model model) {
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
            
            // Add employee to model
            model.addAttribute("employee", employee);
            
            return "employee/overtime-form";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * Handle overtime request submission
     */
    @PostMapping("/employee/overtime/request")
    public String submitOvertimeRequest(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("employeeName") String employeeName,
            @RequestParam("overtimeDate") LocalDate overtimeDate,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endTime") String endTimeStr,
            @RequestParam("reason") String reason,
            @RequestParam("employmentType") String employmentType,
            @RequestParam(value = "status", defaultValue = "PENDING") String status,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get employee details
            Employee employee = employeeService.findByUsername(username);
            
            // Verify the employee ID matches the logged-in user
            if (employee == null || !employee.getEmployeeID().equals(employeeId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized request");
                return "redirect:/employee/attendance";
            }
            
            // Create new overtime request
            OvertimeRequest overtimeRequest = new OvertimeRequest();
            overtimeRequest.setEmployeeId(employeeId);
            overtimeRequest.setEmployeeName(employeeName);
            overtimeRequest.setOvertimeDate(overtimeDate);
            overtimeRequest.setStartTime(LocalTime.parse(startTimeStr));
            overtimeRequest.setEndTime(LocalTime.parse(endTimeStr));
            overtimeRequest.setReason(reason);
            overtimeRequest.setEmploymentType(employmentType);
            
            // Set status (default to PENDING if not provided)
            OvertimeRequest.OvertimeRequestStatus requestStatus = OvertimeRequest.OvertimeRequestStatus.valueOf(status);
            overtimeRequest.setStatus(requestStatus);
            
            // Set request date to current date
            overtimeRequest.setRequestDate(LocalDate.now());
            
            // Save the overtime request
            overtimeRequestService.saveOvertimeRequest(overtimeRequest);
            
            redirectAttributes.addFlashAttribute("successMessage", "Overtime request submitted successfully");
            return "redirect:/employee/attendance";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting overtime request: " + e.getMessage());
            return "redirect:/employee/attendance";
        }
    }
    
    /**
     * View employee's overtime requests
     */
    @GetMapping("/employee/overtime/requests")
    public String viewEmployeeOvertimeRequests(Model model) {
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
            
            // Get overtime requests for the employee
            List<OvertimeRequest> overtimeRequests = overtimeRequestService.getEmployeeOvertimeRequests(employee.getEmployeeID());
            
            // Add to model
            model.addAttribute("employee", employee);
            model.addAttribute("overtimeRequests", overtimeRequests);
            
            return "employee/overtime-requests";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return "redirect:/error";
        }
    }
}