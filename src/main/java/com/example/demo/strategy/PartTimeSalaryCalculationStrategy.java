package com.example.demo.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // Changed import for Sort
import org.springframework.stereotype.Component;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.PartTimeAttendance;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.BonusRecommendationRepository;
import com.example.demo.repository.PartTimeAttendanceRepository;

/**
 * Salary calculation strategy for part-time employees
 */
@Component
public class PartTimeSalaryCalculationStrategy implements SalaryCalculationStrategy {

    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    @Autowired
    private PartTimeAttendanceRepository partTimeAttendanceRepository;
    
    // Standard deduction amounts for different tax regimes (as per Budget 2025)
    private static final double OLD_REGIME_STANDARD_DEDUCTION = 50000.0;
    private static final double NEW_REGIME_STANDARD_DEDUCTION = 75000.0;
    
    // Tax rebate under section 87A (as per Budget 2025)
    private static final double MAX_REBATE_AMOUNT = 60000.0;
    private static final double REBATE_INCOME_LIMIT = 1200000.0;
    
    // Maximum allowed paid leaves per month
    private static final int MAX_PAID_LEAVES = 4;

    @Override
    public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
        // Create a new salary calculation object
        SalaryCalculation calculation = new SalaryCalculation(employee, month, year);
        
        // Get the hourly rate based on job title
        double hourlyRate = getHourlyRateByJobTitle(employee.getJobTitle());
        
        // Create date range for the month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Last day of month
        
        // Get all attendance records for the month - fixed the Sort import
        List<PartTimeAttendance> attendanceRecords = partTimeAttendanceRepository.findByEmployeeIdAndDateBetween(
                employee.getEmployeeID(), startDate, endDate, 
                Sort.by(Sort.Direction.ASC, "date"));
        
        // Calculate total hours worked
        double totalHoursWorked = 0.0;
        for (PartTimeAttendance attendance : attendanceRecords) {
            if (attendance.getTotalHours() != null && 
                (attendance.getStatus() == PartTimeAttendance.AttendanceStatus.PRESENT || 
                 attendance.getStatus() == PartTimeAttendance.AttendanceStatus.WORK_FROM_HOME ||
                 attendance.getStatus() == PartTimeAttendance.AttendanceStatus.HALF_DAY)) {
                totalHoursWorked += attendance.getTotalHours();
            }
        }
        
        // Calculate basic salary based on hours worked
        double basicSalary = totalHoursWorked * hourlyRate;
        calculation.setBasicSalary(basicSalary);
        
        // Set overtime pay to 0 since part-time employees are paid by the hour
        calculation.setOvertimePay(0.0);
        
        // Set allowances to 0 or a fixed amount - part-time employees typically don't get allowances
        // or they get reduced allowances
        calculation.setAllowances(0.0);
        
        // Calculate bonus
        double bonus = calculateBonus(employee, month, year);
        calculation.setBonus(bonus);
        
        // Calculate deductions
        
        // 1. Provident Fund (12% of basic salary)
        double pfPercentage = 0.12; 
        double providentFund = calculation.getBasicSalary() * pfPercentage;
        calculation.setProvidentFund(providentFund);
        
        // 2. Calculate income tax based on the selected tax regime
        // Get tax declaration to determine regime
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
        String taxRegime = (taxDeclaration != null && taxDeclaration.getTaxRegime() != null) ? 
                         taxDeclaration.getTaxRegime() : "NEW"; // Default to new regime
        
        // Calculate estimated annual income: hourlyRate × 7 × 22 × 12
        double estimatedAnnualIncome = hourlyRate * 7 * 22 * 12;
        
        // Calculate annual gross income (without including bonus as requested)
        double annualGrossIncome = estimatedAnnualIncome;
        double taxableIncome;
        
        // For new regime, apply standard deduction and calculate
        if ("NEW".equals(taxRegime)) {
            // Apply standard deduction for new regime
            taxableIncome = annualGrossIncome - NEW_REGIME_STANDARD_DEDUCTION;
            taxableIncome = Math.max(0, taxableIncome);
            
            System.out.println("Part-time employee - Tax Regime: NEW");
            System.out.println("  Standard Deduction: ₹" + NEW_REGIME_STANDARD_DEDUCTION);
        } else {
            // For old regime, apply more deductions based on tax declaration
            double totalDeductions = OLD_REGIME_STANDARD_DEDUCTION; // Start with standard deduction
            
            // Add 80C deductions (if available in tax declaration)
            if (taxDeclaration != null) {
                // Home loan interest deduction under Section 24
                if (taxDeclaration.getHasHomeLoan() && taxDeclaration.getHomeLoanInterest() != null) {
                    // Maximum deduction for self-occupied property is 2 lakhs
                    double homeLoanDeduction = Math.min(taxDeclaration.getHomeLoanInterest(), 200000.0);
                    totalDeductions += homeLoanDeduction;
                    System.out.println("  Home Loan Interest Deduction: ₹" + homeLoanDeduction);
                }
                
                // Medical insurance premium under Section 80D
                if (taxDeclaration.getMedicalInsurance() != null) {
                    double medicalInsuranceDeduction = Math.min(taxDeclaration.getMedicalInsurance(), 25000.0);
                    totalDeductions += medicalInsuranceDeduction;
                    System.out.println("  Medical Insurance Deduction: ₹" + medicalInsuranceDeduction);
                }
            }
            
            // Print total deductions for old regime
            System.out.println("  Total Deductions (Old Regime): ₹" + totalDeductions);
            
            // Apply deductions to calculate taxable income
            taxableIncome = annualGrossIncome - totalDeductions;
            taxableIncome = Math.max(0, taxableIncome);
            
            System.out.println("Part-time employee - Tax Regime: OLD");
            System.out.println("  Total Deductions: ₹" + totalDeductions);
        }
        
        // Calculate annual income tax using the appropriate tax regime
        double annualIncomeTax = calculateIncomeTax(employee, taxableIncome, taxRegime);
        
        // Calculate monthly income tax (divide annual tax by 12)
        double monthlyIncomeTax = annualIncomeTax / 12;
        calculation.setIncomeTax(monthlyIncomeTax);
        
        // Debug log for tax calculation
        System.out.println("Income Tax Calculation for Part-time Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
        System.out.println("  Tax Regime: " + taxRegime);
        System.out.println("  Hourly Rate: ₹" + hourlyRate);
        System.out.println("  Estimated Annual Income: ₹" + estimatedAnnualIncome);
        System.out.println("  Annual Gross Income: ₹" + annualGrossIncome);
        System.out.println("  Taxable Income: ₹" + taxableIncome);
        System.out.println("  Annual Income Tax: ₹" + annualIncomeTax);
        System.out.println("  Monthly Income Tax: ₹" + monthlyIncomeTax);
        
        // 3. Other deductions (Professional Tax + Medical Insurance)
        double otherDeductions = 0;
        
        // a. Professional Tax (monthly) - may be reduced for part-time
        double professionalTaxMonthly = 200.0; // Keep the same regardless of part-time status
        otherDeductions += professionalTaxMonthly;
        
        // b. Medical Insurance Premium (monthly) - typically the same for part-time employees
        System.out.println("Employee ID: " + employee.getEmployeeID());
        System.out.println("Tax Declaration: " + taxDeclaration);
        if (taxDeclaration != null) {
            System.out.println("Medical Insurance: " + taxDeclaration.getMedicalInsurance());
        }

        if (taxDeclaration != null && taxDeclaration.getMedicalInsurance() != null) {
            double monthlyMedicalInsurance = taxDeclaration.getMedicalInsurance() / 12.0;
            otherDeductions += monthlyMedicalInsurance;
        }
        
        calculation.setOtherDeductions(otherDeductions);
        
        // Calculate net salary
        // Net Salary = Basic Salary + Bonus + Allowances + Overtime Pay - Total Deductions
        double totalDeductions = calculation.getProvidentFund() + calculation.getIncomeTax() + calculation.getOtherDeductions();
        double netSalary = calculation.getBasicSalary() + calculation.getBonus() + calculation.getAllowances() - totalDeductions;
        calculation.setNetSalary(netSalary);
        
        // Set audit fields
        calculation.setCreatedBy("System");
        calculation.setCreatedAt(LocalDate.now());
        
        return calculation;
    }
    
    /**
     * Get hourly rate based on job title as per specified rates
     */
    private double getHourlyRateByJobTitle(String jobTitle) {
        if (jobTitle == null) return 398.0; // Default to Software Developer rate
        
        switch (jobTitle.toLowerCase()) {
            case "software developer":
                return 300.0;
            case "software engineer":
                return 500.0;
            case "senior developer":
                return 850.0;
            case "tech lead":
                return 1130.0;
            default:
                return 398.0; // Default rate
        }
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
    
    /**
     * Calculate income tax based on the selected tax regime
     * 
     * @param employee The employee for whom to calculate taxes
     * @param taxableIncome Annual taxable income after applicable deductions
     * @param taxRegime The tax regime to use ("NEW" or "OLD")
     * @return Annual income tax amount
     */
    private double calculateIncomeTax(Employee employee, double taxableIncome, String taxRegime) {
        if ("NEW".equals(taxRegime)) {
            return calculateNewRegimeTax(taxableIncome);
        } else {
            return calculateOldRegimeTax(employee, taxableIncome);
        }
    }
    
    /**
     * Calculate tax based on the new tax regime (Budget 2025)
     * Updated to match the specified tax slabs for FY 2025-26
     */
    private double calculateNewRegimeTax(double taxableIncome) {
        // Special case: No tax for income up to Rs 12,00,000 due to rebate under Section 87A
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            System.out.println("  Zero tax due to rebate under Section 87A (income <= ₹12,00,000)");
            return 0.0;
        }
        
        double tax = 0.0;
        double remainingIncome = taxableIncome;
        
        // Tax slabs as per Budget 2025 for FY 2025-26
        // Up to Rs. 4,00,000 - NIL
        if (remainingIncome <= 400000.0) {
            return 0.0;
        }
        
        System.out.println("  New Regime Tax Calculation:");
        
        // First slab: Up to Rs. 4,00,000 - NIL
        System.out.println("  First ₹4,00,000: ₹0 (0%)");
        remainingIncome -= 400000.0;
        
        // Second slab: Rs. 4,00,001 to Rs. 8,00,000 - 5%
        if (remainingIncome > 0) {
            double taxableInSlab = Math.min(remainingIncome, 400000.0);
            double slabTax = taxableInSlab * 0.05;
            tax += slabTax;
            remainingIncome -= taxableInSlab;
            System.out.println("  ₹4,00,001 to ₹8,00,000: ₹" + slabTax + " (5% of ₹" + taxableInSlab + ")");
        }
        
        // Third slab: Rs. 8,00,001 to Rs. 12,00,000 - 10%
        if (remainingIncome > 0) {
            double taxableInSlab = Math.min(remainingIncome, 400000.0);
            double slabTax = taxableInSlab * 0.10;
            tax += slabTax;
            remainingIncome -= taxableInSlab;
            System.out.println("  ₹8,00,001 to ₹12,00,000: ₹" + slabTax + " (10% of ₹" + taxableInSlab + ")");
        }
        
        // Fourth slab: Rs. 12,00,001 to Rs. 16,00,000 - 15%
        if (remainingIncome > 0) {
            double taxableInSlab = Math.min(remainingIncome, 400000.0);
            double slabTax = taxableInSlab * 0.15;
            tax += slabTax;
            remainingIncome -= taxableInSlab;
            System.out.println("  ₹12,00,001 to ₹16,00,000: ₹" + slabTax + " (15% of ₹" + taxableInSlab + ")");
        }
        
        // Fifth slab: Rs. 16,00,001 to Rs. 20,00,000 - 20%
        if (remainingIncome > 0) {
            double taxableInSlab = Math.min(remainingIncome, 400000.0);
            double slabTax = taxableInSlab * 0.20;
            tax += slabTax;
            remainingIncome -= taxableInSlab;
            System.out.println("  ₹16,00,001 to ₹20,00,000: ₹" + slabTax + " (20% of ₹" + taxableInSlab + ")");
        }
        
        // Sixth slab: Rs. 20,00,001 to Rs. 24,00,000 - 25%
        if (remainingIncome > 0) {
            double taxableInSlab = Math.min(remainingIncome, 400000.0);
            double slabTax = taxableInSlab * 0.25;
            tax += slabTax;
            remainingIncome -= taxableInSlab;
            System.out.println("  ₹20,00,001 to ₹24,00,000: ₹" + slabTax + " (25% of ₹" + taxableInSlab + ")");
        }
        
        // Seventh slab: Above Rs. 24,00,000 - 30%
        if (remainingIncome > 0) {
            double slabTax = remainingIncome * 0.30;
            tax += slabTax;
            System.out.println("  Above ₹24,00,000: ₹" + slabTax + " (30% of ₹" + remainingIncome + ")");
        }
        
        // Calculate rebate u/s 87A (for taxable income above 12 lakhs, this won't apply)
        double rebate = 0.0;
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            rebate = Math.min(tax, MAX_REBATE_AMOUNT);
            System.out.println("  Rebate u/s 87A: -₹" + rebate);
        }
        
        // Final tax after rebate
        tax -= rebate;
        System.out.println("  Tax after rebate: ₹" + tax);
        
        // Add 4% health and education cess
        double cess = tax * 0.04;
        System.out.println("  Health & Education Cess (4%): ₹" + cess);
        tax += cess;
        
        System.out.println("  Final Tax: ₹" + Math.round(tax));
        
        return Math.round(tax);
    }
    
    /**
     * Calculate tax based on the old tax regime for FY 2025-26
     * Updated to match the specified tax slabs
     */
    private double calculateOldRegimeTax(Employee employee, double taxableIncome) {
        // Determine age category for old regime tax slabs
        int ageCategory = getAgeCategoryForTax(employee);
        String ageGroup = ageCategory == 0 ? "Under 60 years" : (ageCategory == 1 ? "60-80 years" : "Above 80 years");
        System.out.println("  Age Category: " + ageGroup);
        
        // Apply old regime rebate if income <= 5 lakhs (Section 87A)
        // For FY 2025-26, income up to ₹5,00,000 has zero tax due to rebate
        if (taxableIncome <= 500000.0) {
            System.out.println("  Zero tax due to rebate under Section 87A (income <= ₹5,00,000)");
            return 0.0;
        }
        
        double tax = 0.0;
        double remainingIncome = taxableIncome;
        
        System.out.println("  Old Regime Tax Calculation:");
        
        // Apply tax based on age category
        if (ageCategory == 0) { // Age < 60 years
            // Up to Rs. 2,50,000 - NIL
            System.out.println("  First ₹2,50,000: ₹0 (0%)");
            
            if (remainingIncome <= 250000.0) {
                return 0.0;
            }
            remainingIncome -= 250000.0;
            
            // Rs. 2,50,001 to Rs. 5,00,000 - 5%
            double taxableInSlab1 = Math.min(remainingIncome, 250000.0);
            double slab1Tax = taxableInSlab1 * 0.05;
            tax += slab1Tax;
            System.out.println("  ₹2,50,001 to ₹5,00,000: ₹" + slab1Tax + " (5% of ₹" + taxableInSlab1 + ")");
            remainingIncome -= taxableInSlab1;
            
            // Rs. 5,00,001 to Rs. 10,00,000 - 20%
            if (remainingIncome > 0) {
                double taxableInSlab2 = Math.min(remainingIncome, 500000.0);
                double slab2Tax = taxableInSlab2 * 0.20;
                tax += slab2Tax;
                System.out.println("  ₹5,00,001 to ₹10,00,000: ₹" + slab2Tax + " (20% of ₹" + taxableInSlab2 + ")");
                remainingIncome -= taxableInSlab2;
                
                // Above Rs. 10,00,000 - 30%
                if (remainingIncome > 0) {
                    double slab3Tax = remainingIncome * 0.30;
                    tax += slab3Tax;
                    System.out.println("  Above ₹10,00,000: ₹" + slab3Tax + " (30% of ₹" + remainingIncome + ")");
                }
            }
        } else if (ageCategory == 1) { // Age 60-80 years
            // Up to Rs. 3,00,000 - NIL
            System.out.println("  First ₹3,00,000: ₹0 (0%)");
            
            if (remainingIncome <= 300000.0) {
                return 0.0;
            }
            remainingIncome -= 300000.0;
            
            // Rs. 3,00,001 to Rs. 5,00,000 - 5%
            double taxableInSlab1 = Math.min(remainingIncome, 200000.0);
            double slab1Tax = taxableInSlab1 * 0.05;
            tax += slab1Tax;
            System.out.println("  ₹3,00,001 to ₹5,00,000: ₹" + slab1Tax + " (5% of ₹" + taxableInSlab1 + ")");
            remainingIncome -= taxableInSlab1;
            
            // Rs. 5,00,001 to Rs. 10,00,000 - 20%
            if (remainingIncome > 0) {
                double taxableInSlab2 = Math.min(remainingIncome, 500000.0);
                double slab2Tax = taxableInSlab2 * 0.20;
                tax += slab2Tax;
                System.out.println("  ₹5,00,001 to ₹10,00,000: ₹" + slab2Tax + " (20% of ₹" + taxableInSlab2 + ")");
                remainingIncome -= taxableInSlab2;
                
                // Above Rs. 10,00,000 - 30%
                if (remainingIncome > 0) {
                    double slab3Tax = remainingIncome * 0.30;
                    tax += slab3Tax;
                    System.out.println("  Above ₹10,00,000: ₹" + slab3Tax + " (30% of ₹" + remainingIncome + ")");
                }
            }
        } else { // Age > 80 years (Super Senior Citizen)
            // Up to Rs. 5,00,000 - NIL
            System.out.println("  First ₹5,00,000: ₹0 (0%)");
            
            if (remainingIncome <= 500000.0) {
                return 0.0;
            }
            remainingIncome -= 500000.0;
            
            // Rs. 5,00,001 to Rs. 10,00,000 - 20%
            double taxableInSlab1 = Math.min(remainingIncome, 500000.0);
            double slab1Tax = taxableInSlab1 * 0.20;
            tax += slab1Tax;
            System.out.println("  ₹5,00,001 to ₹10,00,000: ₹" + slab1Tax + " (20% of ₹" + taxableInSlab1 + ")");
            remainingIncome -= taxableInSlab1;
            
            // Above Rs. 10,00,000 - 30%
            if (remainingIncome > 0) {
                double slab2Tax = remainingIncome * 0.30;
                tax += slab2Tax;
                System.out.println("  Above ₹10,00,000: ₹" + slab2Tax + " (30% of ₹" + remainingIncome + ")");
            }
        }
        
        System.out.println("  Tax before cess: ₹" + tax);
        
        // Add 4% health and education cess
        double cess = tax * 0.04;
        System.out.println("  Health & Education Cess (4%): ₹" + cess);
        tax += cess;
        
        System.out.println("  Final Tax: ₹" + Math.round(tax));
        
        return Math.round(tax);
    }
    
    /**
     * Determine employee's age category for old tax regime
     * 0 = Under 60 years
     * 1 = 60-80 years
     * 2 = Above 80 years
     */
    private int getAgeCategoryForTax(Employee employee) {
        int age = 0;
        
        if (employee.getDateOfBirth() != null) {
            // Calculate age as of end of financial year (March 31)
            LocalDate currentDate = LocalDate.now();
            int currentYear = currentDate.getYear();
            int ageAsOfEndOfFY;
            
            // Check if we're before or after March 31
            if (currentDate.getMonthValue() <= 3) {
                // Before April - use previous year's March 31
                ageAsOfEndOfFY = currentYear - 1 - employee.getDateOfBirth().getYear();
            } else {
                // April onwards - use current year's March 31
                ageAsOfEndOfFY = currentYear - employee.getDateOfBirth().getYear();
            }
            
            age = ageAsOfEndOfFY;
        }
        
        // Determine age category
        if (age >= 80) {
            return 2; // Super Senior Citizen
        } else if (age >= 60) {
            return 1; // Senior Citizen
        } else {
            return 0; // Normal
        }
    }
}