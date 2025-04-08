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
 * Salary calculation strategy for part-time employees
 */
@Component
public class PartTimeSalaryCalculationStrategy implements SalaryCalculationStrategy {

    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    @Override
    public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
        // Create a new salary calculation object
        SalaryCalculation calculation = new SalaryCalculation(employee, month, year);
        
        // Get the employee's salary details
        SalarySlip salaryDetails = employee.getSalaryDetails();
        
        // Set basic salary based on designation/job title - halved for part-time
        double basicSalary;
        if (salaryDetails != null && salaryDetails.getBasicSalary() != null) {
            basicSalary = salaryDetails.getBasicSalary() * 0.5; // 50% for part-time
        } else {
            // Assign basic pay based on job title if salary details are not available
            String designation = employee.getJobTitle();
            if (designation != null) {
                switch (designation.toLowerCase()) {
                    case "software developer":
                        basicSalary = 70000.0 * 0.5;
                        break;
                    case "software engineer":
                        basicSalary = 90000.0 * 0.5;
                        break;
                    case "senior developer":
                        basicSalary = 150000.0 * 0.5;
                        break;
                    case "tech lead":
                        basicSalary = 200000.0 * 0.5;
                        break;
                    default:
                        basicSalary = 50000.0 * 0.5; // Default value
                }
            } else {
                basicSalary = 50000.0 * 0.5; // Default value
            }
        }
        calculation.setBasicSalary(basicSalary);
        
        // Calculate allowances (also reduced for part-time)
        double totalAllowances;
        if (salaryDetails != null && salaryDetails.getTotalAllowances() != null) {
            totalAllowances = salaryDetails.getTotalAllowances() * 0.5; // 50% for part-time
        } else {
            // Default allowances (using ~54% of basic salary as a reference)
            totalAllowances = basicSalary * 0.54; // Already halved because basic salary is halved
        }
        calculation.setAllowances(totalAllowances);
        
        // Calculate overtime pay - part-time employees may have different overtime rates
        double overtimePay = calculateOvertimePay(employee, month, year);
        calculation.setOvertimePay(overtimePay);
        
        // Calculate bonus - part-time employees may have prorated bonuses
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
        
        // a. Professional Tax (monthly) - may be reduced for part-time
        double professionalTaxMonthly = 200.0; // Keep the same regardless of part-time status
        otherDeductions += professionalTaxMonthly;
        
        // b. Medical Insurance Premium (monthly) - typically the same for part-time employees
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
     * Calculate overtime pay based on attendance records - adjusted for part-time
     */
    private double calculateOvertimePay(Employee employee, int month, int year) {
        // For part-time employees, overtime might be calculated differently
        // They might not be eligible for overtime until they work more than their part-time hours
        
        // Calculate hourly rate (assuming 11 working days or 88 hours per month for part-time)
        SalarySlip salaryDetails = employee.getSalaryDetails();
        double hourlyRate = 0;
        
        if (salaryDetails != null) {
            // For part-time, the hourly rate might be the same as full-time
            // but calculated based on reduced working hours
            hourlyRate = (salaryDetails.getBasicSalary() * 0.5) / (11 * 8);
        } else {
            hourlyRate = 300; // Default hourly rate
        }
        
        // Get the actual overtime hours from attendance records
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // In a real implementation, get actual overtime hours from attendance records
        // Part-time employees might have fewer overtime hours
        double overtimeHours = 5; // Default to 5 hours for part-time (half of full-time)
        
        // Calculate overtime pay (1.5x hourly rate)
        return overtimeHours * hourlyRate * 1.5;
    }
    
    /**
     * Calculate bonus for the employee - adjusted for part-time
     */
    private double calculateBonus(Employee employee, int month, int year) {
        try {
            double totalBonus = 0.0;
            
            // 1. Calculate fixed bonuses (e.g., annual bonus in December) - prorated for part-time
            if (month == 12) {
                SalarySlip salaryDetails = employee.getSalaryDetails();
                if (salaryDetails != null) {
                    // Bonus is prorated to 50% for part-time employees
                    totalBonus += (salaryDetails.getBasicSalary() * 0.5) * 0.1;
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
            
            // Sum up all approved bonus amounts - these might already be prorated for part-time
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