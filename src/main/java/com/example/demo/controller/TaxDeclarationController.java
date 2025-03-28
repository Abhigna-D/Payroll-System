package com.example.demo.controller;

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
}