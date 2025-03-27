package com.example.demo.controller;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") User user, 
                                     @RequestParam("role") String roleStr,
                                     RedirectAttributes redirectAttributes) {
        // Check if the username or email already exists
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username is already taken!");
            return "redirect:/register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email is already in use!");
            return "redirect:/register";
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set user roles
        Set<Role> roles = new HashSet<>();
        
        // Map the role string to the appropriate ERole
        ERole eRole;
        switch (roleStr.toLowerCase()) {
            case "hr":
                eRole = ERole.ROLE_HR;
                break;
            case "department":
                eRole = ERole.ROLE_MANAGER;
                break;
            case "finance":
                eRole = ERole.ROLE_FINANCE;
                break;
            default:
                eRole = ERole.ROLE_EMPLOYEE;
        }
        
        Role userRole = roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        
        user.setRoles(roles);
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("success", "Registration successful! You can now login.");
        return "redirect:/login";
    }
}