package com.example.demo.controller;
import java.util.stream.Collectors;
import com.example.demo.model.Employee;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.TaxDeclarationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;


@Controller
public class TaxDeclarationController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private TaxDeclarationService taxDeclarationService;

    /**
     * Display tax declaration form page
     */
    @GetMapping("/employee/tax-declaration")
    public String viewTaxDeclaration(Model model) {
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
            
            // Get current tax declaration if exists
            TaxDeclaration taxDeclaration = taxDeclarationService.findByEmployeeId(String.valueOf(employee.getEmployeeID()));
            
            model.addAttribute("employee", employee);
            model.addAttribute("taxDeclaration", taxDeclaration);
            
            return "employee/tax-declaration";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    /**
     * Save or update tax declaration
     */
    @PostMapping("/employee/tax-declaration/save")
    public String saveTaxDeclaration(
            // Personal Information
            @RequestParam(value = "pan", required = false) String pan,
            
            // Bank Details
            @RequestParam(value = "bankName", required = false) String bankName,
            @RequestParam(value = "accountNumber", required = false) String accountNumber,
            @RequestParam(value = "ifscCode", required = false) String ifscCode,
            
            // HRA & Rent Details
            @RequestParam(value = "isRenting", required = false) Boolean isRenting,
            @RequestParam(value = "monthlyRent", required = false) Integer monthlyRent,
            @RequestParam(value = "landlordName", required = false) String landlordName,
            @RequestParam(value = "landlordPan", required = false) String landlordPan,
            @RequestParam(value = "rentalAddress", required = false) String rentalAddress,
            @RequestParam(value = "rentFromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentFromDate,
            @RequestParam(value = "rentToDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentToDate,
            
            // Home Loan Details
            @RequestParam(value = "hasHomeLoan", required = false) Boolean hasHomeLoan,
            @RequestParam(value = "homeLoanAccountNumber", required = false) String homeLoanAccountNumber,
            @RequestParam(value = "homeLoanBankName", required = false) String homeLoanBankName,
            @RequestParam(value = "homeLoanInterest", required = false) Integer homeLoanInterest,
            
            // Previous Employment Details
            @RequestParam(value = "hasPreviousEmployment", required = false) Boolean hasPreviousEmployment,
            @RequestParam(value = "previousEmployerName", required = false) String previousEmployerName,
            @RequestParam(value = "previousTaxableIncome", required = false) Integer previousTaxableIncome,
            @RequestParam(value = "previousTaxDeducted", required = false) Integer previousTaxDeducted,
            
            // Health Insurance
            @RequestParam(value = "medicalInsurance", required = false) Integer medicalInsurance,
            
            @RequestParam(value = "isDraft", required = false) Boolean isDraft,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get employee details
            Employee employee = employeeService.findByUsername(username);
            
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found");
                return "redirect:/employee/tax-declaration";
            }
            
            // Get current tax declaration or create new one
            TaxDeclaration taxDeclaration = taxDeclarationService.findByEmployeeId(String.valueOf(employee.getEmployeeID()));
            if (taxDeclaration == null) {
                taxDeclaration = new TaxDeclaration();
                taxDeclaration.setEmployee(employee);
                taxDeclaration.setCreationDate(LocalDate.now());
            }
            
            // Update personal information
            taxDeclaration.setPan(pan);
            
            // Update bank details
            taxDeclaration.setBankName(bankName);
            taxDeclaration.setAccountNumber(accountNumber);
            taxDeclaration.setIfscCode(ifscCode);
            
            // Update HRA & rent details
            taxDeclaration.setIsRenting(isRenting != null ? isRenting : false);
            taxDeclaration.setMonthlyRent(monthlyRent != null ? monthlyRent : 0);
            taxDeclaration.setLandlordName(landlordName);
            taxDeclaration.setLandlordPan(landlordPan);
            taxDeclaration.setRentalAddress(rentalAddress);
            taxDeclaration.setRentFromDate(rentFromDate);
            taxDeclaration.setRentToDate(rentToDate);
            
            // Update home loan details
            taxDeclaration.setHasHomeLoan(hasHomeLoan != null ? hasHomeLoan : false);
            taxDeclaration.setHomeLoanAccountNumber(homeLoanAccountNumber);
            taxDeclaration.setHomeLoanBankName(homeLoanBankName);
            taxDeclaration.setHomeLoanInterest(homeLoanInterest != null ? homeLoanInterest : 0);
            
            // Update previous employment details
            taxDeclaration.setHasPreviousEmployment(hasPreviousEmployment != null ? hasPreviousEmployment : false);
            taxDeclaration.setPreviousEmployerName(previousEmployerName);
            taxDeclaration.setPreviousTaxableIncome(previousTaxableIncome != null ? previousTaxableIncome : 0);
            taxDeclaration.setPreviousTaxDeducted(previousTaxDeducted != null ? previousTaxDeducted : 0);
            
            // Update health insurance
            taxDeclaration.setMedicalInsurance(medicalInsurance != null ? medicalInsurance : 0);
            
            // Set status based on draft flag
            if (isDraft != null && isDraft) {
                taxDeclaration.setStatus("DRAFT");
            } else {
                taxDeclaration.setStatus("PENDING");
            }
            
            taxDeclaration.setLastUpdateDate(LocalDate.now());
            
            // Save tax declaration
            taxDeclarationService.saveTaxDeclaration(taxDeclaration);
            
            if (isDraft != null && isDraft) {
                redirectAttributes.addFlashAttribute("success", "Tax declaration saved as draft!");
            } else {
                redirectAttributes.addFlashAttribute("success", "Tax declaration submitted successfully!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error saving tax declaration: " + e.getMessage());
        }
        
        return "redirect:/employee/tax-declaration";
    }
    
     // FINANCE OFFICER ENDPOINTS
    
    /**
     * List all tax declarations for finance officer
     */
    @GetMapping("/finance/tax-declarations")
    public String listTaxDeclarations(@RequestParam(value = "status", required = false) String status, Model model) {
        try {
            List<TaxDeclaration> declarations;
            
            // Filter declarations by status if provided
            if (status != null && !status.isEmpty()) {
                declarations = taxDeclarationService.findByStatus(status);
                model.addAttribute("activeStatus", status);
            } else {
                declarations = taxDeclarationService.findAll();
                model.addAttribute("activeStatus", "ALL");
            }
            
            // By default, also filter for pending declarations to display in the main table
            // If the status is already "PENDING", we'll use the same list
            if (!"PENDING".equals(status)) {
                List<TaxDeclaration> pendingDeclarations = declarations.stream()
                    .filter(d -> "PENDING".equals(d.getStatus()))
                    .collect(Collectors.toList());
                model.addAttribute("pendingDeclarations", pendingDeclarations);
            } else {
                model.addAttribute("pendingDeclarations", declarations);
            }
            
            model.addAttribute("declarations", declarations);
            return "finance/tax-declarations";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    
    /**
     * View details of a specific tax declaration
     */
    @GetMapping("/finance/tax-declarations/view/{id}")
    public String viewTaxDeclarationDetails(@PathVariable Long id, Model model) {
        try {
            TaxDeclaration declaration = taxDeclarationService.findById(id);
            
            if (declaration == null) {
                return "redirect:/finance/tax-declarations?error=Declaration+not+found";
            }
            
            model.addAttribute("declaration", declaration);
            return "finance/tax-declaration-view";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    
    /**
     * Approve a tax declaration
     */
    @PostMapping("/finance/tax-declarations/approve/{id}")
    public String approveTaxDeclaration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            TaxDeclaration declaration = taxDeclarationService.findById(id);
            
            if (declaration == null) {
                redirectAttributes.addFlashAttribute("error", "Tax declaration not found");
                return "redirect:/finance/tax-declarations";
            }
            
            declaration.setStatus("APPROVED");
            declaration.setApprovalDate(LocalDate.now());
            declaration.setLastUpdateDate(LocalDate.now());
            
            taxDeclarationService.saveTaxDeclaration(declaration);
            
            redirectAttributes.addFlashAttribute("success", "Tax declaration approved successfully");
            return "redirect:/finance/tax-declarations";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error approving tax declaration: " + e.getMessage());
            return "redirect:/finance/tax-declarations/view/" + id;
        }
    }
    
    /**
     * Reject a tax declaration
     */
    @PostMapping("/finance/tax-declarations/reject/{id}")
    public String rejectTaxDeclaration(
            @PathVariable Long id,
            @RequestParam("rejectionReason") String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            TaxDeclaration declaration = taxDeclarationService.findById(id);
            
            if (declaration == null) {
                redirectAttributes.addFlashAttribute("error", "Tax declaration not found");
                return "redirect:/finance/tax-declarations";
            }
            
            declaration.setStatus("REJECTED");
            declaration.setRejectionReason(rejectionReason);
            declaration.setLastUpdateDate(LocalDate.now());
            
            taxDeclarationService.saveTaxDeclaration(declaration);
            
            redirectAttributes.addFlashAttribute("success", "Tax declaration rejected successfully");
            return "redirect:/finance/tax-declarations";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error rejecting tax declaration: " + e.getMessage());
            return "redirect:/finance/tax-declarations/view/" + id;
        }
    }
    
    /**
     * Approve multiple tax declarations
     */
    @PostMapping("/finance/tax-declarations/approve-multiple")
    public String approveMultipleDeclarations(
            @RequestParam("ids") String ids,
            RedirectAttributes redirectAttributes) {
        try {
            String[] idArray = ids.split(",");
            int successCount = 0;
            
            for (String idStr : idArray) {
                try {
                    Long id = Long.parseLong(idStr);
                    TaxDeclaration declaration = taxDeclarationService.findById(id);
                    
                    if (declaration != null && "PENDING".equals(declaration.getStatus())) {
                        declaration.setStatus("APPROVED");
                        declaration.setApprovalDate(LocalDate.now());
                        declaration.setLastUpdateDate(LocalDate.now());
                        
                        taxDeclarationService.saveTaxDeclaration(declaration);
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                    continue;
                }
            }
            
            redirectAttributes.addFlashAttribute("success", successCount + " tax declaration(s) approved successfully");
            return "redirect:/finance/tax-declarations";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error approving tax declarations: " + e.getMessage());
            return "redirect:/finance/tax-declarations";
        }
    }

    /**
 * Update existing tax declaration
 */
@PostMapping("/employee/tax-declaration/update")
public String updateTaxDeclaration(
        // Personal Information
        @RequestParam(value = "id", required = true) Long declarationId,
        @RequestParam(value = "pan", required = false) String pan,
        
        // Bank Details
        @RequestParam(value = "bankName", required = false) String bankName,
        @RequestParam(value = "accountNumber", required = false) String accountNumber,
        @RequestParam(value = "ifscCode", required = false) String ifscCode,
        
        // HRA & Rent Details
        @RequestParam(value = "isRenting", required = false) Boolean isRenting,
        @RequestParam(value = "monthlyRent", required = false) Integer monthlyRent,
        @RequestParam(value = "landlordName", required = false) String landlordName,
        @RequestParam(value = "landlordPan", required = false) String landlordPan,
        @RequestParam(value = "rentalAddress", required = false) String rentalAddress,
        @RequestParam(value = "rentFromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentFromDate,
        @RequestParam(value = "rentToDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentToDate,
        
        // Home Loan Details
        @RequestParam(value = "hasHomeLoan", required = false) Boolean hasHomeLoan,
        @RequestParam(value = "homeLoanAccountNumber", required = false) String homeLoanAccountNumber,
        @RequestParam(value = "homeLoanBankName", required = false) String homeLoanBankName,
        @RequestParam(value = "homeLoanInterest", required = false) Integer homeLoanInterest,
        
        // Previous Employment Details
        @RequestParam(value = "hasPreviousEmployment", required = false) Boolean hasPreviousEmployment,
        @RequestParam(value = "previousEmployerName", required = false) String previousEmployerName,
        @RequestParam(value = "previousTaxableIncome", required = false) Integer previousTaxableIncome,
        @RequestParam(value = "previousTaxDeducted", required = false) Integer previousTaxDeducted,
        
        // Health Insurance
        @RequestParam(value = "medicalInsurance", required = false) Integer medicalInsurance,
        
        @RequestParam(value = "isDraft", required = false) Boolean isDraft,
        RedirectAttributes redirectAttributes) {
    
    try {
        // Get logged in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Get employee details
        Employee employee = employeeService.findByUsername(username);
        
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found");
            return "redirect:/employee/tax-declaration";
        }
        
        // Get the tax declaration being updated
        TaxDeclaration taxDeclaration = taxDeclarationService.findById(declarationId);
        
        // Verify the tax declaration belongs to this employee
        if (taxDeclaration == null || !taxDeclaration.getEmployee().getEmployeeID().equals(employee.getEmployeeID())) {
            redirectAttributes.addFlashAttribute("error", "Invalid tax declaration or access denied");
            return "redirect:/employee/tax-declaration";
        }
        
        // Only allow updating if status is DRAFT or REJECTED
        if (!taxDeclaration.getStatus().equals("DRAFT") && !taxDeclaration.getStatus().equals("REJECTED")) {
            redirectAttributes.addFlashAttribute("error", "Cannot update a declaration that is not in draft or rejected state");
            return "redirect:/employee/tax-declaration";
        }
        
        // Update personal information
        taxDeclaration.setPan(pan);
        
        // Update bank details
        taxDeclaration.setBankName(bankName);
        taxDeclaration.setAccountNumber(accountNumber);
        taxDeclaration.setIfscCode(ifscCode);
        
        // Update HRA & rent details
        taxDeclaration.setIsRenting(isRenting != null ? isRenting : false);
        taxDeclaration.setMonthlyRent(monthlyRent != null ? monthlyRent : 0);
        taxDeclaration.setLandlordName(landlordName);
        taxDeclaration.setLandlordPan(landlordPan);
        taxDeclaration.setRentalAddress(rentalAddress);
        taxDeclaration.setRentFromDate(rentFromDate);
        taxDeclaration.setRentToDate(rentToDate);
        
        // Update home loan details
        taxDeclaration.setHasHomeLoan(hasHomeLoan != null ? hasHomeLoan : false);
        taxDeclaration.setHomeLoanAccountNumber(homeLoanAccountNumber);
        taxDeclaration.setHomeLoanBankName(homeLoanBankName);
        taxDeclaration.setHomeLoanInterest(homeLoanInterest != null ? homeLoanInterest : 0);
        
        // Update previous employment details
        taxDeclaration.setHasPreviousEmployment(hasPreviousEmployment != null ? hasPreviousEmployment : false);
        taxDeclaration.setPreviousEmployerName(previousEmployerName);
        taxDeclaration.setPreviousTaxableIncome(previousTaxableIncome != null ? previousTaxableIncome : 0);
        taxDeclaration.setPreviousTaxDeducted(previousTaxDeducted != null ? previousTaxDeducted : 0);
        
        // Update health insurance
        taxDeclaration.setMedicalInsurance(medicalInsurance != null ? medicalInsurance : 0);
        
        // Set status based on draft flag
        if (isDraft != null && isDraft) {
            taxDeclaration.setStatus("DRAFT");
        } else {
            taxDeclaration.setStatus("PENDING");
            // Clear rejection reason if previously rejected
            if (taxDeclaration.getStatus().equals("REJECTED")) {
                taxDeclaration.setRejectionReason(null);
            }
        }
        
        taxDeclaration.setLastUpdateDate(LocalDate.now());
        
        // Save updated tax declaration
        taxDeclarationService.saveTaxDeclaration(taxDeclaration);
        
        if (isDraft != null && isDraft) {
            redirectAttributes.addFlashAttribute("success", "Tax declaration updated and saved as draft!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Tax declaration updated and submitted successfully!");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error updating tax declaration: " + e.getMessage());
    }
    
    return "redirect:/employee/tax-declaration";
}
}