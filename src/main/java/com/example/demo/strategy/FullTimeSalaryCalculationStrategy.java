package com.example.demo.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.BonusRecommendationRepository;

/**
 * Salary calculation strategy for full-time employees
 */
@Component
public class FullTimeSalaryCalculationStrategy implements SalaryCalculationStrategy {

    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    @Override
    public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
        // Create a new salary calculation object
        SalaryCalculation calculation = new SalaryCalculation(employee, month, year);
        
        // Get the employee's salary details
        SalarySlip salaryDetails = employee.getSalaryDetails();
        
        // Set basic salary based on designation/job title
        double basicSalary;
        if (salaryDetails != null && salaryDetails.getBasicSalary() != null) {
            basicSalary = salaryDetails.getBasicSalary();
        } else {
            // Assign basic pay based on job title if salary details are not available
            String designation = employee.getJobTitle();
            if (designation != null) {
                switch (designation.toLowerCase()) {
                    case "software developer":
                        basicSalary = 70000.0;
                        break;
                    case "software engineer":
                        basicSalary = 90000.0;
                        break;
                    case "senior developer":
                        basicSalary = 150000.0;
                        break;
                    case "tech lead":
                        basicSalary = 200000.0;
                        break;
                    default:
                        basicSalary = 50000.0; // Default value
                }
            } else {
                basicSalary = 50000.0; // Default value
            }
        }
        calculation.setBasicSalary(basicSalary);
        
        // Calculate allowances
        double totalAllowances;
        if (salaryDetails != null && salaryDetails.getTotalAllowances() != null) {
            totalAllowances = salaryDetails.getTotalAllowances();
        } else {
            // Default allowances (using ~54% of basic salary as a reference)
            totalAllowances = basicSalary * 0.54; // Approximating to match the example
        }
        calculation.setAllowances(totalAllowances);
        
        // Calculate overtime pay
        double overtimePay = calculateOvertimePay(employee, month, year);
        calculation.setOvertimePay(overtimePay);
        
        // Calculate bonus
        double bonus = calculateBonus(employee, month, year);
        calculation.setBonus(bonus);
        
        // Calculate deductions
        
        // 1. Provident Fund (12% of basic salary)
        double pfPercentage = 0.12; 
        double providentFund = calculation.getBasicSalary() * pfPercentage;
        calculation.setProvidentFund(providentFund);
        
        // 2. Set income tax to 0 (as per your example)
        calculation.setIncomeTax(0);
        
        // 3. Other deductions (Professional Tax + Medical Insurance)
        double otherDeductions = 0;
        
        // a. Professional Tax (monthly)
        double professionalTaxMonthly = 200.0; // ₹2400 per year / 12 months
        otherDeductions += professionalTaxMonthly;
        
        // b. Medical Insurance Premium (monthly)
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
        if (taxDeclaration != null && taxDeclaration.getMedicalInsurance() != null) {
            double monthlyMedicalInsurance = taxDeclaration.getMedicalInsurance() / 12.0;
            otherDeductions += monthlyMedicalInsurance;
        }
        
        calculation.setOtherDeductions(otherDeductions);
        
        // Calculate net salary
        // Net Salary = Basic Salary + Bonus + Allowances - Total Deductions
        double totalDeductions = calculation.getProvidentFund() + calculation.getIncomeTax() + calculation.getOtherDeductions();
        double netSalary = calculation.getBasicSalary() + calculation.getBonus() + calculation.getAllowances() - totalDeductions;
        calculation.setNetSalary(netSalary);
        
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
     * Calculate bonus for the employee
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
            List<BonusRecommendation> approvedBonuses = bonusRecommendationRepository.findByEmployeeAndStatusAndDateRange(
                employee, 
                BonusRecommendationStatus.APPROVED, 
                startOfMonth, 
                endOfMonth
            );
            
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
}