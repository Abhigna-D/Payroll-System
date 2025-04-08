package com.example.demo.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Attendance;
import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.BonusRecommendationRepository;
import com.example.demo.repository.SalaryCalculationRepository;
import com.example.demo.repository.TaxDeclarationRepository;

@Service
public class SalaryCalculationService {

    @Autowired
    private SalaryCalculationRepository salaryCalculationRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private TaxDeclarationRepository taxDeclarationRepository;
    
    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    /**
     * Calculate salary for an employee for the specified month and year
     * 
     * @param employee The employee for whom to calculate salary
     * @param month The month (1-12)
     * @param year The year
     * @return The calculated salary details
     */
    public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
        // Create a new salary calculation object
        SalaryCalculation calculation = new SalaryCalculation(employee, month, year);
        
        // Get the employee's salary details
        SalarySlip salaryDetails = employee.getSalaryDetails();
        if (salaryDetails == null) {
            // If no salary details exist, create default values based on job title or other factors
            calculation.setBasicSalary(50000.0); // Default basic salary
            calculation.setAllowances(5000.0);   // Default allowances
        } else {
            // Use the employee's defined salary structure
            calculation.setBasicSalary(salaryDetails.getBasicSalary());
            calculation.setAllowances(salaryDetails.getHouseRentAllowance() + 
                                     salaryDetails.getConveyanceAllowance() + 
                                     salaryDetails.getMedicalAllowance() + 
                                     salaryDetails.getSpecialAllowance());
            
            // Alternatively, you can use the getTotalAllowances() method
            // calculation.setAllowances(salaryDetails.getTotalAllowances());
        }
        
        // Calculate overtime if any
        double overtimePay = calculateOvertimePay(employee, month, year);
        calculation.setOvertimePay(overtimePay);
        
        // Calculate bonus if applicable
        double bonus = calculateBonus(employee, month, year);
        calculation.setBonus(bonus);
        
        // Adjust salary for part-time employees
        if (employee.isPartTime()) {
            // Adjust basic salary and allowances for part-time employees
            // For example, multiply by 0.5 for half-time employees
            calculation.setBasicSalary(calculation.getBasicSalary() * 0.5);
            calculation.setAllowances(calculation.getAllowances() * 0.5);
        }
        
        // Calculate deductions
        
        // 1. Income Tax calculation
        double incomeTax = calculateIncomeTax(employee, calculation.getTotalEarnings(), month, year);
        calculation.setIncomeTax(incomeTax);
        
        // Calculate Provident Fund (typically a fixed percentage of basic salary)
        double pfPercentage = 0.12; // 12% is typical PF contribution
        double providentFund = calculation.getBasicSalary() * pfPercentage;
        
        // Alternatively, if SalarySlip has the calculation method, use it
        if (salaryDetails != null) {
            providentFund = salaryDetails.getProvidentFundContribution();
        }
        calculation.setProvidentFund(providentFund);
        
        // 3. Other deductions (loans, advances, etc.)
        double otherDeductions = calculateOtherDeductions(employee, month, year);
        calculation.setOtherDeductions(otherDeductions);
        
        // Calculate net salary
        calculation.calculateNetSalary();
        
        // Set audit fields
        calculation.setCreatedBy("System");
        calculation.setCreatedAt(LocalDate.now());
        
        return calculation;
    }
    
    /**
     * Calculate overtime pay based on attendance records
     */
    private double calculateOvertimePay(Employee employee, int month, int year) {
        // In a real implementation, you would:
        // 1. Get attendance records for the employee for the given month
        // 2. Calculate overtime hours
        // 3. Apply overtime rate to calculate pay
        
        // Simplified implementation for demo:
        // Let's assume 10 hours of overtime at 1.5x hourly rate
        
        // Calculate hourly rate (assuming 22 working days, 8 hours per day)
        SalarySlip salaryDetails = employee.getSalaryDetails();
        double hourlyRate = 0;
        
        if (salaryDetails != null) {
            hourlyRate = salaryDetails.getBasicSalary() / (22 * 8);
        } else {
            hourlyRate = 300; // Default hourly rate
        }
        
        // Get the actual overtime hours from attendance records
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // In a real implementation, get actual overtime hours from attendance records
        // This is just a placeholder - you would replace with actual database query
        double overtimeHours = 10; // Default to 10 hours if no attendance records
        
        // Calculate overtime pay (1.5x hourly rate)
        return overtimeHours * hourlyRate * 1.5;
    }
    
    /**
     * Calculate bonus for the employee, including:
     * 1. Any fixed bonuses for specific months (e.g., festival or annual bonuses)
     * 2. Any approved bonus recommendations for the employee in the given month
     */
    private double calculateBonus(Employee employee, int month, int year) {
        try {
            double totalBonus = 0.0;
            
            // 1. Calculate fixed bonuses (e.g., annual bonus in December)
            if (month == 12) {
                SalarySlip salaryDetails = employee.getSalaryDetails();
                if (salaryDetails != null) {
                    totalBonus += salaryDetails.getBasicSalary() * 0.1; // 10% of basic salary as year-end bonus
                }
            }
            
            // 2. Add any approved bonus recommendations for this employee
            // Create date range for the current month
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Get all approved bonus recommendations for this employee
            List<BonusRecommendation> approvedBonuses = getApprovedBonusesForEmployee(employee, startOfMonth, endOfMonth);
            
            // Sum up all approved bonus amounts
            for (BonusRecommendation bonus : approvedBonuses) {
                totalBonus += bonus.getAmount().doubleValue();
            }
            
            return totalBonus;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            // Return 0 instead of failing the entire calculation
            return 0.0;
        }
    }
    /**
     * Helper method to get all approved bonuses for an employee within a date range
     */
    private List<BonusRecommendation> getApprovedBonusesForEmployee(Employee employee, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Use the repository method to query the database
            List<BonusRecommendation> bonuses = bonusRecommendationRepository.findByEmployeeAndStatusAndDateRange(
                employee, 
                BonusRecommendationStatus.APPROVED, 
                startDate, 
                endDate
            );
            
            System.out.println("Found " + bonuses.size() + " approved bonuses for employee " + employee.getEmployeeID());
            for (BonusRecommendation bonus : bonuses) {
                System.out.println("  Bonus: " + bonus.getId() + ", Amount: " + bonus.getAmount() + ", Date: " + bonus.getRecommendedDate());
            }
            
            return bonuses;
        } catch (Exception e) {
            System.err.println("Error retrieving bonuses for employee " + employee.getEmployeeID() + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculate income tax based on employee's tax declaration and earnings
     */
    private double calculateIncomeTax(Employee employee, double totalEarnings, int month, int year) {
        // Get the tax declaration from the employee
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
        
        // Default tax rate if no declaration exists
        double defaultTaxRate = 0.1; // 10%
        
        if (taxDeclaration == null) {
            // No tax declaration, use default rate
            return totalEarnings * defaultTaxRate;
        }
        
        // Annualize the monthly earnings to estimate annual income
        double annualIncome = totalEarnings * 12;
        
        // Add previous employment income if applicable
        if (taxDeclaration.getHasPreviousEmployment() != null && taxDeclaration.getHasPreviousEmployment()) {
            annualIncome += taxDeclaration.getPreviousTaxableIncome() != null ? 
                            taxDeclaration.getPreviousTaxableIncome() : 0;
        }
        
        // Calculate tax deductions
        double totalDeduction = 0;
        
        // 1. House Rent Allowance (HRA) exemption
        if (taxDeclaration.getIsRenting() != null && taxDeclaration.getIsRenting()) {
            // HRA component is typically 40% of basic salary for metro cities
            double hraComponent = totalEarnings * 0.4;
            
            // Exemption is minimum of:
            // a) Actual HRA received
            // b) Rent paid in excess of 10% of salary
            // c) 50% of salary (for metro cities)
            double rentPaid = taxDeclaration.getMonthlyRent() != null ? 
                              taxDeclaration.getMonthlyRent() * 12 : 0;
            double tenPercentSalary = annualIncome * 0.1;
            double rentExcess = Math.max(0, rentPaid - tenPercentSalary);
            double fiftyPercentSalary = annualIncome * 0.5;
            
            double hraExemption = Math.min(hraComponent, Math.min(rentExcess, fiftyPercentSalary));
            totalDeduction += hraExemption;
        }
        
        // 2. Home Loan Interest deduction (up to 2 lakhs for self-occupied property)
        if (taxDeclaration.getHasHomeLoan() != null && taxDeclaration.getHasHomeLoan()) {
            double homeLoanInterest = taxDeclaration.getHomeLoanInterest() != null ? 
                                     taxDeclaration.getHomeLoanInterest() : 0;
            // Cap at 2 lakhs (Rs. 200,000)
            totalDeduction += Math.min(homeLoanInterest, 200000);
        }
        
        // 3. Medical Insurance Premium (up to Rs. 25,000)
        double medicalInsurance = taxDeclaration.getMedicalInsurance() != null ? 
                                 taxDeclaration.getMedicalInsurance() : 0;
        totalDeduction += Math.min(medicalInsurance, 25000);
        
        // 4. Professional Tax (fixed monthly deduction)
        double professionalTax = taxDeclaration.getProfessionalTax() * 12;
        totalDeduction += professionalTax;
        
        // 5. Standard deduction for salaried employees (Rs. 50,000)
        totalDeduction += 50000;
        
        // Calculate taxable income after deductions
        double taxableIncome = Math.max(0, annualIncome - totalDeduction);
        
        // Apply progressive tax slabs (using 2023-24 tax slabs for example)
        double taxAmount = 0;
        
        // Option for Old Tax Regime (with deductions)
        if (taxableIncome <= 250000) {
            taxAmount = 0;
        } else if (taxableIncome <= 500000) {
            taxAmount = (taxableIncome - 250000) * 0.05;
        } else if (taxableIncome <= 750000) {
            taxAmount = 12500 + (taxableIncome - 500000) * 0.10;
        } else if (taxableIncome <= 1000000) {
            taxAmount = 37500 + (taxableIncome - 750000) * 0.15;
        } else if (taxableIncome <= 1250000) {
            taxAmount = 75000 + (taxableIncome - 1000000) * 0.20;
        } else if (taxableIncome <= 1500000) {
            taxAmount = 125000 + (taxableIncome - 1250000) * 0.25;
        } else {
            taxAmount = 187500 + (taxableIncome - 1500000) * 0.30;
        }
        
        // If the previous employer has already deducted some tax, subtract it
        if (taxDeclaration.getHasPreviousEmployment() != null && 
            taxDeclaration.getHasPreviousEmployment() && 
            taxDeclaration.getPreviousTaxDeducted() != null) {
            taxAmount -= taxDeclaration.getPreviousTaxDeducted();
        }
        
        // Calculate monthly tax amount (distribute annual tax over remaining months)
        int remainingMonths = 12 - (month - 1);
        double monthlyTax = Math.max(0, taxAmount / remainingMonths);
        
        return monthlyTax;
    }
    
    /**
     * Calculate other deductions such as loans, advances, etc.
     */
    private double calculateOtherDeductions(Employee employee, int month, int year) {
        // In a real implementation, get any loans or advances the employee has taken
        // and calculate the installment amount for this month
        
        // Simplified implementation for demo
        return 0; // No other deductions
    }
    
    /**
     * Save a list of salary calculations
     */
    @Transactional
    public boolean saveSalaryCalculations(List<Long> calculationIds) {
        try {
            // For each calculation ID, fetch the calculation and update its status
            for (Long id : calculationIds) {
                Optional<SalaryCalculation> calcOpt = salaryCalculationRepository.findById(id);
                if (calcOpt.isPresent()) {
                    SalaryCalculation calc = calcOpt.get();
                    calc.setStatus("APPROVED");
                    calc.setLastModifiedAt(LocalDate.now());
                    calc.setLastModifiedBy("Finance Officer"); // In a real app, get from authenticated user
                    salaryCalculationRepository.save(calc);
                }
            }
            return true;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return false;
        }
    }
}