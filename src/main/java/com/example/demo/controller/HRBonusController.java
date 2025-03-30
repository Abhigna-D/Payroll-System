package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.BonusRecommendation;

import com.example.demo.service.BonusRecommendationService;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hr/bonus-recommendations")
public class HRBonusController {
    private final BonusRecommendationService bonusService;
    
    public HRBonusController(BonusRecommendationService bonusService) {
        this.bonusService = bonusService;
    }
    
    @GetMapping
    public String viewAllRecommendations(Model model) {
        // Get all types of recommendations
        List<BonusRecommendation> pendingRecs = bonusService.getPendingRecommendations();
        List<BonusRecommendation> underReviewRecs = bonusService.getUnderReviewRecommendations();
        List<BonusRecommendation> approvedRecs = bonusService.getApprovedRecommendations();
        List<BonusRecommendation> rejectedRecs = bonusService.getRejectedRecommendations();
        
        // Combine all recommendations into one list for the existing template
        List<BonusRecommendation> allRecommendations = new ArrayList<>();
        allRecommendations.addAll(pendingRecs);
        allRecommendations.addAll(underReviewRecs);
        allRecommendations.addAll(approvedRecs);
        allRecommendations.addAll(rejectedRecs);
        
        // Add the combined list to the model as "pendingRecommendations" to match the template
        model.addAttribute("pendingRecommendations", allRecommendations);
        
        return "hr/bonus-recommendations";
    }
    
    @GetMapping("/review/{id}")
    public String reviewRecommendation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bonusService.reviewRecommendation(id);
            redirectAttributes.addFlashAttribute("success", "Bonus recommendation has been moved to review status");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/hr/bonus-recommendations";
    }
    
    @GetMapping("/view/{id}")
    public String viewRecommendation(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<BonusRecommendation> recommendationOpt = bonusService.getRecommendation(id);
        
        if (recommendationOpt.isPresent()) {
            model.addAttribute("recommendation", recommendationOpt.get());
            return "hr/review-bonus-recommendation";
        } else {
            redirectAttributes.addFlashAttribute("error", "Recommendation not found.");
            return "redirect:/hr/bonus-recommendations";
        }
    }
    
    @PostMapping("/approve/{id}")
    public String approveRecommendation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bonusService.approveRecommendation(id);
            redirectAttributes.addFlashAttribute("success", "Bonus recommendation approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/hr/bonus-recommendations";
    }
    
    @PostMapping("/reject/{id}")
    public String rejectRecommendation(
            @PathVariable Long id,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {
        try {
            bonusService.rejectRecommendation(id, reason);
            redirectAttributes.addFlashAttribute("success", "Bonus recommendation rejected");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/hr/bonus-recommendations";
    }
}