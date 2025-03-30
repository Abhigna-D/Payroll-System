package com.example.demo.controller;

import com.example.demo.model.Complaint;
import com.example.demo.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hr")
public class HRComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Display all complaints for HR manager
     */
    @GetMapping("/complaints")
    public String viewAllComplaints(Model model) {
        try {
            // Get all complaints
            List<Complaint> allComplaints = complaintService.getAllComplaints();
            
            // Get pending complaints (all except RESOLVED and REJECTED)
            List<Complaint> pendingComplaints = complaintService.getPendingComplaints();
            
            model.addAttribute("allComplaints", allComplaints);
            model.addAttribute("pendingComplaints", pendingComplaints);
            
            // Add complaint statuses for dropdown
            model.addAttribute("complaintStatuses", 
                Arrays.asList(Complaint.ComplaintStatus.values()));
            
            return "hr/complaints";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * View specific complaint details
     */
    @GetMapping("/complaints/{id}")
    public String viewComplaintDetails(@PathVariable("id") Long id, Model model) {
        try {
            // Get complaint details
            Optional<Complaint> complaintOpt = complaintService.getComplaintById(id);
            
            if (!complaintOpt.isPresent()) {
                return "redirect:/hr/complaints?error=Complaint not found";
            }
            
            Complaint complaint = complaintOpt.get();
            model.addAttribute("complaint", complaint);
            
            // Add complaint statuses for dropdown
            model.addAttribute("complaintStatuses", 
                Arrays.asList(Complaint.ComplaintStatus.values()));
            
            return "hr/complaint-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * Update complaint status
     */
    @PostMapping("/complaints/{id}/update-status")
    public String updateComplaintStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Complaint.ComplaintStatus status,
            RedirectAttributes redirectAttributes) {
        
        try {
            complaintService.updateComplaintStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Complaint status updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating complaint status: " + e.getMessage());
        }
        
        return "redirect:/hr/complaints/" + id;
    }

    /**
     * Resolve complaint with resolution text
     */
    @PostMapping("/complaints/{id}/resolve")
    public String resolveComplaint(
            @PathVariable("id") Long id,
            @RequestParam("resolutionText") String resolutionText,
            RedirectAttributes redirectAttributes) {
        
        try {
            complaintService.resolveComplaint(id, resolutionText);
            redirectAttributes.addFlashAttribute("success", "Complaint resolved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error resolving complaint: " + e.getMessage());
        }
        
        return "redirect:/hr/complaints";
    }
}