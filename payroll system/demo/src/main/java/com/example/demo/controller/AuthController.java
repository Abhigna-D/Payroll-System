package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/setup")
    @ResponseBody
    public String setupInitialUsers() {
        StringBuilder result = new StringBuilder("Setting up initial users...<br>");
        
        if (userService.existsByUsername("hradmin")) {
            return "Initial users already set up! You can log in with:<br>" +
                   "HR Manager: hradmin / password<br>" +
                   "Finance Officer: finance / password<br>" +
                   "Department Manager: manager / password<br>" +
                   "Employee: employee / password";
        }
        
        try {
            Role employeeRole = createRoleIfNotExists(ERole.ROLE_EMPLOYEE);
            Role hrRole = createRoleIfNotExists(ERole.ROLE_HR_MANAGER);
            Role financeRole = createRoleIfNotExists(ERole.ROLE_FINANCIAL_OFFICER);
            Role deptManagerRole = createRoleIfNotExists(ERole.ROLE_DEPARTMENT_MANAGER);
            
            createUser("hradmin", "hr@company.com", "password", hrRole, result);
            createUser("finance", "finance@company.com", "password", financeRole, result);
            createUser("manager", "manager@company.com", "password", deptManagerRole, result);
            createUser("employee", "employee@company.com", "password", employeeRole, result);
            
            return result.toString();
        } catch (Exception e) {
            return "Error setting up users: " + e.getMessage();
        }
    }

    private Role createRoleIfNotExists(ERole roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
    }

    private void createUser(String username, String email, String password, Role role, StringBuilder result) {
        if (userService.existsByUsername(username)) {
            result.append(username).append(" already exists! Skipping.<br>");
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(role));
    
        try {
            userService.saveUser(user);
            result.append("Created ").append(username).append(" / password<br>");
        } catch (Exception e) {
            result.append("Error creating ").append(username).append(": ").append(e.getMessage()).append("<br>");
        }
    }
    

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, 
                              @RequestParam("role") String roleType,
                              RedirectAttributes redirectAttributes) {
        
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username is already taken!");
            return "redirect:/register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email is already in use!");
            return "redirect:/register";
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        Set<Role> roles = new HashSet<>();
        ERole userRole;
        
        switch (roleType) {
            case "hr":
                userRole = ERole.ROLE_HR_MANAGER;
                break;
            case "finance":
                userRole = ERole.ROLE_FINANCIAL_OFFICER;
                break;
            case "manager":
                userRole = ERole.ROLE_DEPARTMENT_MANAGER;
                break;
            default:
                userRole = ERole.ROLE_EMPLOYEE;
        }
        
        Role role = roleRepository.findByName(userRole)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        roles.add(role);
        user.setRoles(roles);
        
        userService.saveUser(user);
        
        redirectAttributes.addFlashAttribute("success", "Registration successful! You can now log in.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_HR_MANAGER"))) {
                return "redirect:/hr/dashboard";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FINANCIAL_OFFICER"))) {
                return "redirect:/finance/dashboard";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_MANAGER"))) {
                return "redirect:/department/dashboard";
            } else {
                return "redirect:/employee/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
