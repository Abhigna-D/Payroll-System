package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.TaxDeclarationService;
import org.springframework.beans.factory.annotation.Autowired;
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
            // Changed from getEmployeeID() to getEmployeeID() and converted to Long
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
            @RequestParam(value = "pan", required = false) String pan,
            @RequestParam(value = "taxRegime", required = false) String taxRegime,
            @RequestParam(value = "epf", required = false) Integer epf,
            @RequestParam(value = "vpf", required = false) Integer vpf,
            @RequestParam(value = "lifeInsurance", required = false) Integer lifeInsurance,
            @RequestParam(value = "elss", required = false) Integer elss,
            @RequestParam(value = "ppf", required = false) Integer ppf,
            @RequestParam(value = "homeLoanPrincipal", required = false) Integer homeLoanPrincipal,
            @RequestParam(value = "sukanyaSamriddhi", required = false) Integer sukanyaSamriddhi,
            @RequestParam(value = "tuitionFees", required = false) Integer tuitionFees,
            @RequestParam(value = "nsc", required = false) Integer nsc,
            @RequestParam(value = "taxSavingFD", required = false) Integer taxSavingFD,
            @RequestParam(value = "nps", required = false) Integer nps,
            @RequestParam(value = "medicalInsurance", required = false) Integer medicalInsurance,
            @RequestParam(value = "educationLoan", required = false) Integer educationLoan,
            @RequestParam(value = "homeLoanInterest", required = false) Integer homeLoanInterest,
            @RequestParam(value = "donations", required = false) Integer donations,
            @RequestParam(value = "disabilityDeduction", required = false) Integer disabilityDeduction,
            @RequestParam(value = "isRenting", required = false) Boolean isRenting,
            @RequestParam(value = "monthlyRent", required = false) Integer monthlyRent,
            @RequestParam(value = "landlordPan", required = false) String landlordPan,
            @RequestParam(value = "rentalAddress", required = false) String rentalAddress,
            @RequestParam(value = "hasPreviousEmployment", required = false) Boolean hasPreviousEmployment,
            @RequestParam(value = "previousEmployerName", required = false) String previousEmployerName,
            @RequestParam(value = "previousTaxableIncome", required = false) Integer previousTaxableIncome,
            @RequestParam(value = "previousTaxDeducted", required = false) Integer previousTaxDeducted,
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
            // Changed from getId() to getEmployeeID() and converted to Long
            TaxDeclaration taxDeclaration = taxDeclarationService.findByEmployeeId(String.valueOf(employee.getEmployeeID()));
            if (taxDeclaration == null) {
                taxDeclaration = new TaxDeclaration();
                taxDeclaration.setEmployee(employee);
                taxDeclaration.setCreationDate(LocalDate.now());
            }
            
            // Update tax declaration fields
            taxDeclaration.setPan(pan);
            taxDeclaration.setTaxRegime(taxRegime);
            taxDeclaration.setEpf(epf != null ? epf : 0);
            taxDeclaration.setVpf(vpf != null ? vpf : 0);
            taxDeclaration.setLifeInsurance(lifeInsurance != null ? lifeInsurance : 0);
            taxDeclaration.setElss(elss != null ? elss : 0);
            taxDeclaration.setPpf(ppf != null ? ppf : 0);
            taxDeclaration.setHomeLoanPrincipal(homeLoanPrincipal != null ? homeLoanPrincipal : 0);
            taxDeclaration.setSukanyaSamriddhi(sukanyaSamriddhi != null ? sukanyaSamriddhi : 0);
            taxDeclaration.setTuitionFees(tuitionFees != null ? tuitionFees : 0);
            taxDeclaration.setNsc(nsc != null ? nsc : 0);
            taxDeclaration.setTaxSavingFD(taxSavingFD != null ? taxSavingFD : 0);
            taxDeclaration.setNps(nps != null ? nps : 0);
            taxDeclaration.setMedicalInsurance(medicalInsurance != null ? medicalInsurance : 0);
            taxDeclaration.setEducationLoan(educationLoan != null ? educationLoan : 0);
            taxDeclaration.setHomeLoanInterest(homeLoanInterest != null ? homeLoanInterest : 0);
            taxDeclaration.setDonations(donations != null ? donations : 0);
            taxDeclaration.setDisabilityDeduction(disabilityDeduction != null ? disabilityDeduction : 0);
            taxDeclaration.setIsRenting(isRenting != null ? isRenting : false);
            taxDeclaration.setMonthlyRent(monthlyRent != null ? monthlyRent : 0);
            taxDeclaration.setLandlordPan(landlordPan);
            taxDeclaration.setRentalAddress(rentalAddress);
            taxDeclaration.setHasPreviousEmployment(hasPreviousEmployment != null ? hasPreviousEmployment : false);
            taxDeclaration.setPreviousEmployerName(previousEmployerName);
            taxDeclaration.setPreviousTaxableIncome(previousTaxableIncome != null ? previousTaxableIncome : 0);
            taxDeclaration.setPreviousTaxDeducted(previousTaxDeducted != null ? previousTaxDeducted : 0);
            
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