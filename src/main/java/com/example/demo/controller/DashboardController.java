package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check user's roles and redirect to the appropriate dashboard
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HR"))) {
            return "redirect:/hr/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return "redirect:/department/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_FINANCE"))) {
            return "redirect:/finance/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))) {
            return "redirect:/employee/dashboard";
        } else {
            // Default redirect if no specific role is found
            return "redirect:/";
        }
    }
    
    // HR Dashboard
    @GetMapping("/hr/dashboard")
    public String hrDashboard() {
        return "hr/dashboard"; // Using the dashboard.html template you provided
    }
    
    // Employee Dashboard
    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "employee/dashboard"; // You'll need to create this template
    }
    
    // Manager Dashboard
    @GetMapping("/department/dashboard")
    public String managerDashboard() {
        return "department/dashboard"; // You'll need to create this template
    }
    
    // Finance Dashboard
    @GetMapping("/finance/dashboard")
    public String financeDashboard() {
        return "finance/dashboard"; // You'll need to create this template
    }
    
    // Home page
    @GetMapping("/")
    public String home() {
        // If user is authenticated, redirect to dashboard
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return "redirect:/dashboard";
        }
        
        return "index"; // Your main index.html page
    }
}