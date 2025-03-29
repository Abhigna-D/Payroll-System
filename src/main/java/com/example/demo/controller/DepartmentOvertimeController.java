package com.example.demo.controller;

import com.example.demo.model.DepartmentManager;
import com.example.demo.model.Employee;
import com.example.demo.model.OvertimeRequest;
import com.example.demo.service.DepartmentManagerService;
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
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/department")
public class DepartmentOvertimeController {
    private static final Logger logger = Logger.getLogger(DepartmentOvertimeController.class.getName());

    @Autowired
    private OvertimeRequestService overtimeRequestService;
    
    @Autowired
    private DepartmentManagerService departmentManagerService;
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Display all overtime requests for the department
     */
    @GetMapping("/overtime")
    public String viewDepartmentOvertimeRequests(Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.info("Fetching overtime requests for manager: " + username);
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                logger.warning("Manager not found for username: " + username);
                redirectAttributes.addFlashAttribute("errorMessage", "Manager profile not found");
                return "redirect:/error";
            }
            
            // Check if department is loaded
            if (manager.getDepartment() == null) {
                logger.warning("Department not found for manager: " + username);
                redirectAttributes.addFlashAttribute("errorMessage", "Department not found for manager");
                return "redirect:/error";
            }
            
            logger.info("Found manager for department: " + manager.getDepartment().getName() + " with ID: " + manager.getDepartment().getId());
            
            // Get overtime requests for the department
            List<OvertimeRequest> overtimeRequests = overtimeRequestService.getDepartmentOvertimeRequests(manager.getDepartment().getId());
            logger.info("Found " + overtimeRequests.size() + " overtime requests for department ID: " + manager.getDepartment().getId());
            
            // Add to model
            model.addAttribute("deptManager", manager);
            model.addAttribute("overtimeRequests", overtimeRequests);
            
            return "department/overtime-requests";
        } catch (Exception e) {
            logger.severe("Error viewing department overtime requests: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading overtime requests: " + e.getMessage());
            return "redirect:/error";
        }
    }
    
    /**
     * View details of a specific overtime request
     */
    @GetMapping("/overtime/details/{id}")
    public String viewOvertimeRequestDetails(@PathVariable("id") Long requestId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Manager profile not found");
                return "redirect:/error";
            }
            
            // Get the overtime request
            OvertimeRequest overtimeRequest = overtimeRequestService.getOvertimeRequestById(requestId);
            
            // If overtime request is null or not from manager's department, redirect to error page
            if (overtimeRequest == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Overtime request not found");
                return "redirect:/error";
            }
            
            // Get employee details to verify department
            Employee employee = employeeService.findByEmployeeId(overtimeRequest.getEmployeeId());
            
            // Verify employee belongs to manager's department
            if (employee == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Employee not found");
                return "redirect:/error";
            }
            
            if (employee.getDepartment() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Employee department information missing");
                return "redirect:/error";
            }
            
            if (manager.getDepartment() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Manager department information missing");
                return "redirect:/error";
            }
            
            if (!employee.getDepartment().getId().equals(manager.getDepartment().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized: Employee not in your department");
                return "redirect:/department/overtime";
            }
            
            // Add to model
            model.addAttribute("deptManager", manager);
            model.addAttribute("overtimeRequest", overtimeRequest);
            model.addAttribute("employee", employee);
            
            return "department/overtime-details";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error viewing overtime details: " + e.getMessage());
            return "redirect:/error";
        }
    }
    
    /**
     * Approve overtime request
     */
    @GetMapping("/overtime/approve/{id}")
    public String approveOvertimeRequest(@PathVariable("id") Long requestId, @RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/department/dashboard";
            }
            
            // Get the overtime request
            OvertimeRequest overtimeRequest = overtimeRequestService.getOvertimeRequestById(requestId);
            
            // If overtime request is null, redirect with error
            if (overtimeRequest == null) {
                redirectAttributes.addFlashAttribute("error", "Overtime request not found");
                return "redirect:/department/overtime";
            }
            
            // Get employee details to verify department
            Employee employee = employeeService.findByEmployeeId(overtimeRequest.getEmployeeId());
            
            // Verify employee belongs to manager's department
            if (employee == null || employee.getDepartment() == null || 
                manager.getDepartment() == null || 
                !employee.getDepartment().getId().equals(manager.getDepartment().getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access: Employee not in your department");
                return "redirect:/department/overtime";
            }
            
            // Update the status
            overtimeRequest.setStatus(OvertimeRequest.OvertimeRequestStatus.APPROVED);
            overtimeRequest.setManagerComments(comments);
            overtimeRequest.setProcessedDate(LocalDate.now());
            
            // Save the updated request
            overtimeRequestService.saveOvertimeRequest(overtimeRequest);
            
            redirectAttributes.addFlashAttribute("success", "Overtime request successfully approved");
            return "redirect:/department/overtime";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error processing request: " + e.getMessage());
            return "redirect:/department/overtime";
        }
    }
    
    /**
     * Reject overtime request
     */
    @GetMapping("/overtime/reject/{id}")
    public String rejectOvertimeRequest(@PathVariable("id") Long requestId, @RequestParam(required = false) String comments, RedirectAttributes redirectAttributes) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access");
                return "redirect:/department/dashboard";
            }
            
            // Get the overtime request
            OvertimeRequest overtimeRequest = overtimeRequestService.getOvertimeRequestById(requestId);
            
            // If overtime request is null, redirect with error
            if (overtimeRequest == null) {
                redirectAttributes.addFlashAttribute("error", "Overtime request not found");
                return "redirect:/department/overtime";
            }
            
            // Get employee details to verify department
            Employee employee = employeeService.findByEmployeeId(overtimeRequest.getEmployeeId());
            
            // Verify employee belongs to manager's department
            if (employee == null || employee.getDepartment() == null || 
                manager.getDepartment() == null || 
                !employee.getDepartment().getId().equals(manager.getDepartment().getId())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access: Employee not in your department");
                return "redirect:/department/overtime";
            }
            
            // Update the status
            overtimeRequest.setStatus(OvertimeRequest.OvertimeRequestStatus.REJECTED);
            overtimeRequest.setManagerComments(comments);
            overtimeRequest.setProcessedDate(LocalDate.now());
            
            // Save the updated request
            overtimeRequestService.saveOvertimeRequest(overtimeRequest);
            
            redirectAttributes.addFlashAttribute("success", "Overtime request has been rejected");
            return "redirect:/department/overtime";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error processing request: " + e.getMessage());
            return "redirect:/department/overtime";
        }
    }
    
    /**
     * Show form to add comments when rejecting an overtime request
     */
    @GetMapping("/overtime/reject-form/{id}")
    public String showRejectForm(@PathVariable("id") Long requestId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Manager profile not found");
                return "redirect:/error";
            }
            
            // Get the overtime request
            OvertimeRequest overtimeRequest = overtimeRequestService.getOvertimeRequestById(requestId);
            
            // If overtime request is null, redirect to error page
            if (overtimeRequest == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Overtime request not found");
                return "redirect:/error";
            }
            
            // Add to model
            model.addAttribute("deptManager", manager);
            model.addAttribute("overtimeRequest", overtimeRequest);
            
            return "department/reject-overtime-form";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading reject form: " + e.getMessage());
            return "redirect:/error";
        }
    }
    
    /**
     * Process the rejection form submission
     */
    @PostMapping("/overtime/reject")
    public String processReject(@RequestParam("requestId") Long requestId, 
                                @RequestParam("comments") String comments,
                                RedirectAttributes redirectAttributes) {
        return rejectOvertimeRequest(requestId, comments, redirectAttributes);
    }
    
}