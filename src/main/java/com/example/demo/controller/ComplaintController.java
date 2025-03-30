package com.example.demo.controller;

import com.example.demo.model.Complaint;
import com.example.demo.model.Employee;
import com.example.demo.service.ComplaintService;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Display complaint form page
     */
    @GetMapping("/employee/complaint")
    public String showComplaintForm(Model model) {
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
            
            // Get existing complaints for this employee
            List<Complaint> complaints = complaintService.getComplaintsByEmployeeId(employee.getEmployeeID());
            model.addAttribute("complaints", complaints);
            
            return "employee/complaint";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * Handle complaint submission
     */
    @PostMapping("/employee/complaint/submit")
    public String submitComplaint(
            @RequestParam("employeeId") String employeeId,
            @RequestParam("fullName") String fullName,
            @RequestParam("contactNumber") String contactNumber,
            @RequestParam("email") String email,
            @RequestParam("complaintText") String complaintText,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Create new complaint object
            Complaint complaint = new Complaint();
            complaint.setEmployeeId(employeeId);
            complaint.setFullName(fullName);
            complaint.setContactNumber(contactNumber);
            complaint.setEmail(email);
            complaint.setComplaintText(complaintText);
            
            // Submit complaint
            complaintService.submitComplaint(complaint);
            
            redirectAttributes.addFlashAttribute("success", "Complaint submitted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error submitting complaint: " + e.getMessage());
        }
        
        return "redirect:/employee/complaint";
    }

    /**
     * View specific complaint details
     */
    @GetMapping("/employee/complaint/{id}")
    public String viewComplaintDetails(@PathVariable("id") Long id, Model model) {
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
            
            // Get complaint details
            Complaint complaint = complaintService.getComplaintById(id)
                    .orElseThrow(() -> new RuntimeException("Complaint not found"));
            
            // Security check - make sure this complaint belongs to the logged-in employee
            if (!complaint.getEmployeeId().equals(employee.getEmployeeID())) {
                return "redirect:/access-denied";
            }
            
            model.addAttribute("employee", employee);
            model.addAttribute("complaint", complaint);
            
            return "employee/complaint-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
}