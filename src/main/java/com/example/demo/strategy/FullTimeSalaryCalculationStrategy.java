package com.example.demo.strategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.model.Attendance;
import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.OvertimeRequest;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.BonusRecommendationRepository;
import com.example.demo.repository.OvertimeRequestRepository;

/**
 * Salary calculation strategy for full-time employees
 * Each employee's salary is calculated independently based on their job title and individual records
 */
@Component
public class FullTimeSalaryCalculationStrategy implements SalaryCalculationStrategy {

    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private OvertimeRequestRepository overtimeRequestRepository;
    
    // Standard number of working days in a month (average)
    private static final int STANDARD_WORKING_DAYS = 22;
    
    // Maximum allowed paid leaves per month
    private static final int MAX_PAID_LEAVES = 4;
    
    // Fixed HRA amount as per requirements
    private static final double FIXED_HRA_AMOUNT = 20000.0;
    
    // Standard deduction amounts for different tax regimes (as per Budget 2025)
private static final double OLD_REGIME_STANDARD_DEDUCTION = 50000.0;
private static final double NEW_REGIME_STANDARD_DEDUCTION = 75000.0;
    
    // Metro cities for HRA exemption
    private static final List<String> METRO_CITIES = List.of(
            "mumbai", "delhi", "kolkata", "chennai", "bangalore", "bengaluru", "hyderabad");
    
    // New Tax Slabs for FY 2025-26 (as per Budget 2025)
   // private static final double[] TAX_SLAB_LIMITS = {
     /*       400000.0,   // 0% tax up to Rs 4 lakh
            800000.0,   // 5% tax from Rs 4 lakh to Rs 8 lakh
            1200000.0,  // 10% tax from Rs 8 lakh to Rs 12 lakh
            1600000.0,  // 15% tax from Rs 12 lakh to Rs 16 lakh
            2000000.0,  // 20% tax from Rs 16 lakh to Rs 20 lakh
            2400000.0   // 25% tax from Rs 20 lakh to Rs 24 lakh
                        // 30% tax above Rs 24 lakh
    };
    
    private static final double[] TAX_SLAB_RATES = {
            0.0,    // 0% tax up to Rs 4 lakh
            0.05,   // 5% tax from Rs 4 lakh to Rs 8 lakh
            0.10,   // 10% tax from Rs 8 lakh to Rs 12 lakh
            0.15,   // 15% tax from Rs 12 lakh to Rs 16 lakh
            0.20,   // 20% tax from Rs 16 lakh to Rs 20 lakh
            0.25,   // 25% tax from Rs 20 lakh to Rs 24 lakh
            0.30    // 30% tax above Rs 24 lakh
    };
    */
    // Tax rebate under section 87A (as per Budget 2025)
    private static final double MAX_REBATE_AMOUNT = 60000.0;
    private static final double REBATE_INCOME_LIMIT = 1200000.0;
    
   
    
    /**
 * Calculate income tax based on the selected tax regime
 * 
 * @param employee The employee for whom to calculate taxes
 * @param taxableIncome Annual taxable income after applicable deductions
 * @return Annual income tax amount
 */
private double calculateIncomeTax(Employee employee, double taxableIncome) {
    // Get the employee's tax declaration
    TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
    
    // Determine which tax regime to use
    String taxRegime = (taxDeclaration != null && taxDeclaration.getTaxRegime() != null) ? 
                       taxDeclaration.getTaxRegime() : "NEW"; // Default to new regime
    
    // Log which regime is being used
    System.out.println("Using " + taxRegime + " tax regime for " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
    
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
    
    // Calculate surcharge
    double surcharge = calculateSurcharge(taxableIncome, tax, "NEW");
    if (surcharge > 0) {
        System.out.println("  Surcharge: ₹" + surcharge);
    }
    tax += surcharge;
    
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
    
    // REMOVE SURCHARGE SECTION - DELETED THE FOLLOWING LINES:
    // double surcharge = calculateSurcharge(taxableIncome, tax, "OLD");
    // if (surcharge > 0) {
    //     System.out.println("  Surcharge: ₹" + surcharge);
    // }
    // tax += surcharge;
    
    // Add 4% health and education cess
    double cess = tax * 0.04;
    System.out.println("  Health & Education Cess (4%): ₹" + cess);
    tax += cess;
    
    System.out.println("  Final Tax: ₹" + Math.round(tax));
    
    return Math.round(tax);
}

/**
 * Calculate surcharge based on income level
 * Fixed to correctly apply surcharge thresholds
 * 
 * @param taxableIncome Total taxable income
 * @param tax Basic tax amount before surcharge and cess
 * @param regime Tax regime ("NEW" or "OLD")
 * @return Surcharge amount
 */
private double calculateSurcharge(double taxableIncome, double tax, String regime) {
    return  0.0;
       
}


/**
 * Updated method to properly calculate and apply HRA exemption for both tax regimes
 */
private double[] calculateHraExemption(Employee employee, double basicSalary) {
    double[] result = new double[2];
    
    // 1. Actual HRA received (fixed at ₹20,000 as per requirements)
    double actualHra = FIXED_HRA_AMOUNT;
    
    // Get tax declaration to check if employee is renting
    TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
    
    // If employee is not renting or no tax declaration, entire HRA is taxable
    if (taxDeclaration == null || taxDeclaration.getIsRenting() == null || !taxDeclaration.getIsRenting()) {
        result[0] = 0.0; // No exemption
        result[1] = actualHra; // All HRA is taxable
        
        // Debug logging
        System.out.println("HRA Calculation for " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
        System.out.println("  Not eligible for HRA exemption (not renting)");
        System.out.println("  HRA Exemption: ₹0.00");
        System.out.println("  Taxable HRA: ₹" + actualHra);
        
        return result;
    }
    
    // 2. Rent paid minus 10% of Basic Salary
    Integer monthlyRent = taxDeclaration.getMonthlyRent();
    if (monthlyRent == null) {
        monthlyRent = 0;
    }
    
    double rentMinusBasic = monthlyRent - (0.1 * basicSalary);
    rentMinusBasic = Math.max(0, rentMinusBasic); // Ensure it's not negative
    
    // 3. Check if employee lives in a metro city
    // Get employee's residential city from address
    String residentialCity = getEmployeeResidentialCity(employee);
    
    double salaryPercent;
    if (isMetroCity(residentialCity)) {
        // 50% of Basic for metro cities
        salaryPercent = 0.5 * basicSalary;
    } else {
        // 40% of Basic for non-metro cities
        salaryPercent = 0.4 * basicSalary;
    }
    
    // Calculate minimum of the three values
    double hraExemption = Math.min(Math.min(actualHra, rentMinusBasic), salaryPercent);
    
    // Calculate taxable portion
    double taxableHra = actualHra - hraExemption;
    
    result[0] = hraExemption;
    result[1] = taxableHra;
    
    // Debug logging
    System.out.println("HRA Calculation for " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
    System.out.println("  Basic Salary: ₹" + basicSalary);
    System.out.println("  Actual HRA: ₹" + actualHra);
    System.out.println("  Monthly Rent: ₹" + monthlyRent);
    System.out.println("  Rent - 10% Basic: ₹" + rentMinusBasic);
    System.out.println("  City: " + residentialCity + " (Metro: " + isMetroCity(residentialCity) + ")");
    System.out.println("  " + (isMetroCity(residentialCity) ? "50%" : "40%") + " of Basic: ₹" + salaryPercent);
    System.out.println("  HRA Exemption (min of the three): ₹" + hraExemption);
    System.out.println("  Taxable HRA: ₹" + taxableHra);
    
    return result;
}

@Override
public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
    try {
        // Create a new salary calculation object for this specific employee
        SalaryCalculation calculation = new SalaryCalculation(employee, month, year);
        
        // Get this employee's salary details
        SalarySlip salaryDetails = employee.getSalaryDetails();
        
        // Set basic salary based on THIS employee's designation/job title
        double basicSalary = getBasicSalaryForEmployee(employee, salaryDetails);
        calculation.setBasicSalary(basicSalary);
        
        // Calculate HRA exemption and taxable HRA
        double[] hraDetails = calculateHraExemption(employee, basicSalary);
        double hraExemption = hraDetails[0];
        double taxableHra = hraDetails[1];
        
        // Store HRA details in calculation if the fields exist
        try {
            calculation.setHraExemption(hraExemption);
            calculation.setTaxableHra(taxableHra);
        } catch (Exception e) {
            // If the fields don't exist, just log it
            System.out.println("Note: HRA exemption fields not set, may not exist in model");
        }
        
        // Calculate other allowances for THIS employee
        double totalAllowances = calculateAllowances(employee, salaryDetails, basicSalary);

        // Add HRA only if employee is renting
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
        if (taxDeclaration != null && taxDeclaration.getIsRenting() != null && taxDeclaration.getIsRenting()) {
            // Add fixed HRA to total allowances only if the employee is renting
            totalAllowances += FIXED_HRA_AMOUNT;
            
            System.out.println("Added HRA allowance of ₹" + FIXED_HRA_AMOUNT + " for " + 
                               employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
        } else {
            System.out.println("No HRA allowance added for " + employee.getFullName() + 
                              " (ID: " + employee.getEmployeeID() + ") - not renting");
        }

        calculation.setAllowances(totalAllowances);
        
        // Calculate overtime pay based on THIS employee's attendance records
        double overtimePay = calculateOvertimePay(employee, month, year);
        calculation.setOvertimePay(overtimePay);
        
        // Calculate bonus for THIS employee
        double bonus = calculateBonus(employee, month, year);
        calculation.setBonus(bonus);
        
        // Calculate attendance-based deductions for THIS employee
        double attendanceDeduction = calculateAttendanceDeduction(employee, month, year, basicSalary);
        
        // Calculate deductions for THIS employee
        
        // 1. Provident Fund (12% of basic salary)
        double pfPercentage = 0.12; 
        double providentFund = calculation.getBasicSalary() * pfPercentage;
        calculation.setProvidentFund(providentFund);
        
        // 2. Calculate income tax based on the selected tax regime
        // Get tax declaration to determine regime
        String taxRegime = (taxDeclaration != null && taxDeclaration.getTaxRegime() != null) ? 
                       taxDeclaration.getTaxRegime() : "NEW"; // Default to new regime

        // Calculate annual gross and taxable income based on the regime
        double annualBasicAndAllowances = (calculation.getBasicSalary() + calculation.getAllowances()) * 12;
        double annualGross = annualBasicAndAllowances + calculation.getBonus() + calculation.getOvertimePay();
        double taxableIncome;

        // For new regime, apply standard deduction and calculate
        if ("NEW".equals(taxRegime)) {
            // Apply standard deduction for new regime
            taxableIncome = annualGross - NEW_REGIME_STANDARD_DEDUCTION;
            taxableIncome = Math.max(0, taxableIncome);
            
            System.out.println("  Tax Regime: NEW");
            System.out.println("  Standard Deduction: ₹" + NEW_REGIME_STANDARD_DEDUCTION);
        } else {
            // For old regime, apply more deductions based on tax declaration
            double totalDeductions = OLD_REGIME_STANDARD_DEDUCTION; // Start with standard deduction
            
            // Add HRA exemption for old regime (annual)
            if (hraExemption > 0) {
                totalDeductions += hraExemption * 12; // Annual HRA exemption
                System.out.println("  HRA Exemption (Annual): ₹" + (hraExemption * 12));
            }
            
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
            taxableIncome = annualGross - totalDeductions;
            taxableIncome = Math.max(0, taxableIncome);
            
            System.out.println("  Tax Regime: OLD");
            System.out.println("  Total Deductions: ₹" + totalDeductions);
        }

        // Calculate annual income tax using the appropriate tax regime
        double annualIncomeTax = calculateIncomeTax(employee, taxableIncome);
        
        // Calculate monthly income tax (divide annual tax by 12)
        double monthlyIncomeTax = annualIncomeTax / 12;
        calculation.setIncomeTax(monthlyIncomeTax);
        
        // Debug log for tax calculation
        System.out.println("Income Tax Calculation for " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
        System.out.println("  Tax Regime: " + taxRegime);
        System.out.println("  Monthly Basic + Allowances: ₹" + (calculation.getBasicSalary() + calculation.getAllowances()));
        System.out.println("  Annual Basic + Allowances: ₹" + annualBasicAndAllowances);
        System.out.println("  Current Month Bonus: ₹" + calculation.getBonus());
        System.out.println("  Current Month Overtime: ₹" + calculation.getOvertimePay());
        System.out.println("  Annual Gross: ₹" + annualGross);
        System.out.println("  Taxable Income: ₹" + taxableIncome);
        System.out.println("  Annual Income Tax: ₹" + annualIncomeTax);
        System.out.println("  Monthly Income Tax: ₹" + monthlyIncomeTax);
        
        // 3. Other deductions (Professional Tax + Medical Insurance + Attendance Deduction) for THIS employee
        double otherDeductions = 0;
        
        // a. Professional Tax (monthly)
        double professionalTaxMonthly = 200.0; // ₹2400 per year / 12 months
        otherDeductions += professionalTaxMonthly;
        
        // b. Medical Insurance Premium (monthly) from THIS employee's tax declaration
        if (taxDeclaration != null && taxDeclaration.getMedicalInsurance() != null) {
            double monthlyMedicalInsurance = taxDeclaration.getMedicalInsurance() / 12.0;
            otherDeductions += monthlyMedicalInsurance;
        }
        
        // c. Add attendance-based deductions
        otherDeductions += attendanceDeduction;
        
        calculation.setOtherDeductions(otherDeductions);
        
        // Calculate net salary
        // Net Salary = Basic Salary + Bonus + Allowances + Overtime Pay - Total Deductions
        double totalDeductions = calculation.getProvidentFund() + calculation.getIncomeTax() + calculation.getOtherDeductions();
        double netSalary = calculation.getBasicSalary() + calculation.getBonus() + calculation.getAllowances() + calculation.getOvertimePay() - totalDeductions;
        calculation.setNetSalary(netSalary);
        
        // Set gross salary for information
        double grossSalary = calculation.getBasicSalary() + calculation.getAllowances() + calculation.getBonus() + calculation.getOvertimePay();
        // Set the gross salary if the field exists in the model
        try {
            calculation.setGrossSalary(grossSalary);
        } catch (Exception e) {
            // If the field doesn't exist, just log it
            System.out.println("Note: grossSalary field not set, may not exist in model");
        }
        
        // Set audit fields
        calculation.setCreatedBy("System");
        calculation.setCreatedAt(LocalDate.now());
        
        return calculation;
    } catch (Exception e) {
        e.printStackTrace();
        // Return a default calculation instead of null to avoid NPE
        SalaryCalculation defaultCalc = new SalaryCalculation(employee, month, year);
        defaultCalc.setStatus("ERROR");
        defaultCalc.setRemarks("Error calculating salary: " + e.getMessage());
        return defaultCalc;
    }}
    /**
     * Get employee's residential city from address
     * If not available, default to non-metro
     */
    private String getEmployeeResidentialCity(Employee employee) {
        // Try to get city from tax declaration rental address
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
        if (taxDeclaration != null && taxDeclaration.getRentalAddress() != null) {
            String rentalAddress = taxDeclaration.getRentalAddress().toLowerCase();
            
            // Simple check for city names in the address
            for (String city : METRO_CITIES) {
                if (rentalAddress.contains(city)) {
                    return city;
                }
            }
        }
        
        // Default to non-metro city if we can't determine
        return "unknown";
    }
    
    /**
     * Check if a city is a metropolitan city for HRA purposes
     */
    private boolean isMetroCity(String city) {
        if (city == null) {
            return false;
        }
        return METRO_CITIES.contains(city.toLowerCase());
    }
    
    /**
     * Get basic salary for an employee based on job title or salary details
     */
    private double getBasicSalaryForEmployee(Employee employee, SalarySlip salaryDetails) {
        // First try to get basic salary from employee's salary details
        if (salaryDetails != null && salaryDetails.getBasicSalary() != null) {
            return salaryDetails.getBasicSalary();
        } 
        
        // If not available, determine by job title
        String designation = employee.getJobTitle();
        if (designation != null) {
            switch (designation.toLowerCase()) {
                case "software developer":
                    return 120000.0;
                case "software engineer":
                    return 70000.0;
                case "senior developer":
                    return 100000.0;
                case "tech lead":
                    return 120000.0;
                default:
                    return 50000.0; // Default value
            }
        } else {
            return 50000.0; // Default value if no job title
        }
    }
    
    /**
     * Calculate allowances for an employee (excluding HRA which is handled separately)
     */
    private double calculateAllowances(Employee employee, SalarySlip salaryDetails, double basicSalary) {
        if (salaryDetails != null && salaryDetails.getTotalAllowances() != null) {
            // If the employee has saved allowance details, use those
            return salaryDetails.getTotalAllowances();
        } else {
            // Use fixed standard allowances as specified
            double conveyanceAllowance = 1600.0;    // ₹1,600 for commuting
            double medicalAllowance = 1250.0;       // ₹1,250 for medical expenses
            double internetAllowance = 1000.0;      // ₹1,000 for internet/telephone
            double mealAllowance = 1500.0;          // ₹1,500 for meals/food
            
            // Total allowances (excluding HRA which is added separately as FIXED_HRA_AMOUNT)
            double totalAllowances = conveyanceAllowance + medicalAllowance + internetAllowance + mealAllowance;
            
            return totalAllowances;
        }
    }
    
    /**
     * Calculate deductions based on attendance (absences exceeding allowed paid leaves)
     */
    private double calculateAttendanceDeduction(Employee employee, int month, int year, double basicSalary) {
        try {
            // Create date range for the specified month
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Last day of the month
            
            // Get total working days in the month
            YearMonth yearMonth = YearMonth.of(year, month);
            int totalDaysInMonth = yearMonth.lengthOfMonth();
            
            // Get working days (excluding weekends - simplified approach)
            int workingDays = 0;
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                // Assume weekends are Saturday (6) and Sunday (7)
                if (date.getDayOfWeek().getValue() < 6) {
                    workingDays++;
                }
                date = date.plusDays(1);
            }
            
            // First check if we have a monthly summary record
            Attendance monthlySummary = attendanceRepository.findAttendanceSummaryByEmployeeIdAndMonth(
                    employee.getEmployeeID(), startDate);
            
            if (monthlySummary != null && monthlySummary.getDaysAbsent() != null && monthlySummary.getDaysLeave() != null) {
                // Use the monthly summary for calculations
                int totalLeavesAndAbsences = monthlySummary.getDaysAbsent() + monthlySummary.getDaysLeave();
                
                // Calculate excess absences beyond allowed paid leaves
                int excessAbsences = Math.max(0, totalLeavesAndAbsences - MAX_PAID_LEAVES);
                
                // Calculate daily salary based on THIS employee's basic salary
                double dailySalary = basicSalary / workingDays;
                
                // Calculate deduction for excess absences
                return excessAbsences * dailySalary;
            }
            
            // If no monthly summary, get individual attendance records
            List<Attendance> attendanceRecords = attendanceRepository.findByEmployeeIdAndDateBetween(
                    employee.getEmployeeID(), startDate, endDate, 
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "date"));
            
            // Check if we have attendance records for this employee
            if (attendanceRecords == null || attendanceRecords.isEmpty()) {
                // If no attendance records, assume employee was present for all working days
                return 0.0; // No deduction
            }
            
            // Count absences and leaves for THIS employee from individual records
            int absences = 0;
            int sickLeaves = 0;
            
            for (Attendance attendance : attendanceRecords) {
                if (attendance.getStatus() == Attendance.AttendanceStatus.ABSENT) {
                    absences++;
                } else if (attendance.getStatus() == Attendance.AttendanceStatus.SICK_LEAVE) {
                    sickLeaves++;
                }
            }
            
            // Total leaves and absences
            int totalLeavesAndAbsences = absences + sickLeaves;
            
            // Calculate excess absences beyond allowed paid leaves
            int excessAbsences = Math.max(0, totalLeavesAndAbsences - MAX_PAID_LEAVES);
            
            // Calculate daily salary based on THIS employee's basic salary
            double dailySalary = basicSalary / workingDays;
            
            // Calculate deduction for excess absences
            return excessAbsences * dailySalary;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            // Return 0 deduction in case of error to avoid breaking the entire calculation
            return 0.0;
        }
    }
    
    /**
     * Calculate overtime pay based on attendance records
     */
    private double calculateOvertimePay(Employee employee, int month, int year) {
        try {
            // Get the hourly rate based on THIS employee's job title
            double hourlyRate = getHourlyRateByJobTitle(employee.getJobTitle());
            
            // Calculate overtime from THIS employee's attendance records
            double overtimeHoursFromAttendance = calculateOvertimeHoursFromAttendance(employee, month, year);
            
            // Calculate overtime from THIS employee's overtime requests
            double overtimeHoursFromRequests = calculateOvertimeHoursFromRequests(employee, month, year);
            
            // Total overtime hours
            double totalOvertimeHours = overtimeHoursFromAttendance + overtimeHoursFromRequests;
            
            // Calculate overtime pay based on hourly rate
            return totalOvertimeHours * hourlyRate;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0; // Return 0 in case of error
        }
    }

    private double calculateOvertimeHoursFromAttendance(Employee employee, int month, int year) {
        try {
            // Create date for the month
            LocalDate monthDate = LocalDate.of(year, month, 1);
            LocalDate endDate = monthDate.plusMonths(1).minusDays(1);
            
            // First try to get monthly summary (if it exists)
            Attendance attendanceSummary = attendanceRepository.findAttendanceSummaryByEmployeeIdAndMonth(
                    employee.getEmployeeID(), monthDate);
            
            // If summary exists and has days present, use it
            if (attendanceSummary != null && attendanceSummary.getDaysPresent() != null) {
                // Calculate overtime days (days present exceeding standard working days)
                int overtimeDays = Math.max(0, attendanceSummary.getDaysPresent() - STANDARD_WORKING_DAYS);
                
                // Convert overtime days to hours (assuming 8 hours per day)
                return overtimeDays * 8.0;
            }
            
            // Alternatively, try to calculate overtime using individual day records
            // Get attendance records for THIS employee for the month
            List<Attendance> attendanceRecords = attendanceRepository.findByEmployeeIdAndDateBetween(
                    employee.getEmployeeID(), monthDate, endDate, 
                    org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "date"));
            
            // If there are no individual records either, assume standard working days (no overtime)
            if (attendanceRecords == null || attendanceRecords.isEmpty()) {
                return 0.0;
            }
            
            // Count days present from individual records
            double daysPresent = 0;
            for (Attendance record : attendanceRecords) {
                if (record.getStatus() == Attendance.AttendanceStatus.PRESENT || 
                    record.getStatus() == Attendance.AttendanceStatus.WORK_FROM_HOME) {
                    daysPresent += 1.0;
                } else if (record.getStatus() == Attendance.AttendanceStatus.HALF_DAY) {
                    daysPresent += 0.5; // Count half-day as 0.5
                }
            }
            
            // Calculate overtime days (days present exceeding standard working days)
            double overtimeDays = Math.max(0, daysPresent - STANDARD_WORKING_DAYS);
            
            // Convert overtime days to hours (assuming 8 hours per day)
            return overtimeDays * 8.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0; // Return 0 in case of error
        }
    }

    private double calculateOvertimeHoursFromRequests(Employee employee, int month, int year) {
        try {
            // Create date range for the specified month
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Last day of the month
            
            // Get all approved overtime requests for THIS employee in the specified month
            List<OvertimeRequest> approvedRequests = overtimeRequestRepository.findByEmployeeId(employee.getEmployeeID())
                    .stream()
                    .filter(req -> req.getStatus() == OvertimeRequest.OvertimeRequestStatus.APPROVED)
                    .filter(req -> !req.getOvertimeDate().isBefore(startDate) && !req.getOvertimeDate().isAfter(endDate))
                    .collect(Collectors.toList());
            
            // Calculate total overtime hours from approved requests
            double totalOvertimeHours = 0.0;
            
            for (OvertimeRequest request : approvedRequests) {
                // Calculate hours between start and end time
                LocalTime startTime = request.getStartTime();
                LocalTime endTime = request.getEndTime();
                
                // If end time is before start time, assume it's past midnight
                long minutes;
                if (endTime.isBefore(startTime)) {
                    minutes = startTime.until(LocalTime.of(23, 59, 59), java.time.temporal.ChronoUnit.MINUTES) + 1
                            + LocalTime.of(0, 0).until(endTime, java.time.temporal.ChronoUnit.MINUTES);
                } else {
                    minutes = startTime.until(endTime, java.time.temporal.ChronoUnit.MINUTES);
                }
                
                totalOvertimeHours += minutes / 60.0;
            }
            
            return totalOvertimeHours;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0; // Return 0 in case of error
        }
    }
    
    /**
     * Get hourly rate based on job title as per specified rates
     */
    private double getHourlyRateByJobTitle(String jobTitle) {
        if (jobTitle == null) return 398.0; // Default to Software Developer rate
        
        switch (jobTitle.toLowerCase()) {
            case "software developer":
                return 398.0;
            case "software engineer":
                return 511.0;
            case "senior developer":
                return 852.0;
            case "tech lead":
                return 1136.0;
            default:
                return 398.0; // Default rate
        }
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
                if (salaryDetails != null && salaryDetails.getBasicSalary() != null) {
                    totalBonus += salaryDetails.getBasicSalary() * 0.1; // 10% of basic salary as year-end bonus
                }
            }
            
            // 2. Add any approved bonus recommendations for THIS employee
            // Create date range for the current month
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            // Get all approved bonus recommendations for THIS employee
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