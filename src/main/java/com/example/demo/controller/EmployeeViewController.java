package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmployeeViewController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * Display employee profile page
     */
    @GetMapping("/employee/profile")
    public String viewProfile(Model model) {
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
            
            model.addAttribute("employee", employee);
            
            return "employee/profile";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * Update employee profile - removed @ModelAttribute validation
     */
    @PostMapping("/employee/profile/update")
    public String updateProfile(@RequestParam("FullName") String fullName,
                              @RequestParam("dateOfBirth") String dateOfBirth,
                              @RequestParam("gender") String gender,
                              @RequestParam("nationality") String nationality,
                              @RequestParam("address") String address,
                              @RequestParam("personalPhone") String personalPhone,
                              @RequestParam("workPhone") String workPhone,
                              @RequestParam("personalEmail") String personalEmail,
                              @RequestParam("emergencyContactName") String emergencyContactName,
                              @RequestParam("emergencyContactRelationship") String emergencyContactRelationship,
                              @RequestParam("emergencyContactPhone") String emergencyContactPhone,
                              RedirectAttributes redirectAttributes) {
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get current employee details
            Employee currentEmployee = employeeService.findByUsername(username);
            
            if (currentEmployee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found");
                return "redirect:/employee/profile";
            }
            
            // Update only editable fields
            currentEmployee.setFullName(fullName);
            
            // Handle date of birth properly if it's not empty
            if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
                try {
                    currentEmployee.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    // Handle date parsing error
                    redirectAttributes.addFlashAttribute("error", "Invalid date format for Date of Birth");
                    return "redirect:/employee/profile";
                }
            }
            
            currentEmployee.setGender(gender);
            currentEmployee.setNationality(nationality);
            currentEmployee.setAddress(address);
            currentEmployee.setPersonalPhone(personalPhone);
            currentEmployee.setWorkPhone(workPhone);
            currentEmployee.setPersonalEmail(personalEmail);
            currentEmployee.setEmergencyContactName(emergencyContactName);
            currentEmployee.setEmergencyContactRelationship(emergencyContactRelationship);
            currentEmployee.setEmergencyContactPhone(emergencyContactPhone);
            
            // Save updates - don't touch the Department field
            employeeService.updateEmployee(currentEmployee);
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        
        return "redirect:/employee/profile";
    }
   
}