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
        
        // 2. Set income tax to 0 (as per your example)
        calculation.setIncomeTax(0);
        
        // 3. Other deductions (Professional Tax + Medical Insurance)
        double otherDeductions = 0;
        
        // a. Professional Tax (monthly) - may be reduced for part-time
        double professionalTaxMonthly = 200.0; // Keep the same regardless of part-time status
        otherDeductions += professionalTaxMonthly;

        
        
        // b. Medical Insurance Premium (monthly) - typically the same for part-time employees
        TaxDeclaration taxDeclaration = employee.getTaxDeclaration();

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
        double netSalary = calculation.getBasicSalary() + calculation.getBonus() + calculation.getAllowances()  - totalDeductions;
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
}