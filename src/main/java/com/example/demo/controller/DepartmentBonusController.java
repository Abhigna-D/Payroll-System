package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.BonusRecommendationDTO;
import com.example.demo.model.BonusRecommendation;
import com.example.demo.service.BonusRecommendationService;
import com.example.demo.service.EmployeeService;

import jakarta.validation.Valid; // Changed from javax to jakarta
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/department/bonus-recommendations")
public class DepartmentBonusController {
    private final BonusRecommendationService bonusService;
    private final EmployeeService employeeService;
    
    public DepartmentBonusController(
            BonusRecommendationService bonusService,
            EmployeeService employeeService) {
        this.bonusService = bonusService;
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public String viewBonusRecommendations(Model model, Principal principal) {
        model.addAttribute("bonusRecommendations", 
                          bonusService.getRecommendationsByManager(principal.getName()));
        return "department/bonus-recommendations";
    }
    
    @GetMapping("/new")
    public String newBonusRecommendationForm(Model model, Principal principal) {
        model.addAttribute("bonusRecommendationDTO", new BonusRecommendationDTO());
        
        // Get the current user's department ID
        // Option 1: If you already have the department ID stored in a variable or can get it from a service
        int deptId = getDepartmentIdForUser(principal.getName());
        model.addAttribute("employees", employeeService.getEmployeesByDepartment(deptId));
        
        // Alternative Option: If you have the findByDepartmentId method available
        // Long departmentId = getDepartmentIdAsLong(principal.getName());
        // model.addAttribute("employees", employeeService.findByDepartmentId(departmentId));
        
        return "department/new-bonus-recommendation";
    }
    
    @PostMapping("/new")
    public String createBonusRecommendation(
            @Valid @ModelAttribute("bonusRecommendationDTO") BonusRecommendationDTO dto,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            // Get the current user's department ID again for the error case
            int deptId = getDepartmentIdForUser(principal.getName());
            model.addAttribute("employees", employeeService.getEmployeesByDepartment(deptId));
            return "department/new-bonus-recommendation";
        }
        
        bonusService.createRecommendation(dto, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Bonus recommendation submitted successfully!");
        return "redirect:/department/bonus-recommendations";
    }
    
    @GetMapping("/view/{id}")
    public String viewBonusRecommendation(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BonusRecommendation> recommendationOpt = bonusService.getRecommendation(id);
        
        if (recommendationOpt.isPresent()) {
            model.addAttribute("recommendation", recommendationOpt.get());
            return "department/view-bonus-recommendation";
        } else {
            redirectAttributes.addFlashAttribute("error", "Recommendation not found.");
            return "redirect:/department/bonus-recommendations";
        }
    }
    
    // Helper method to get department ID for a user
    private int getDepartmentIdForUser(String username) {
        // Implement logic to get the department ID for the given username
        // This might involve looking up the user in a repository or service
        // For example:
        // User user = userService.findByUsername(username);
        // return user.getDepartmentId();
        
        // Placeholder implementation - replace with actual logic
        return 1; // Default department ID
    }
}