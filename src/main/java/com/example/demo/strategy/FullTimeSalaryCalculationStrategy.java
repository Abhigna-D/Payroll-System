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
 */
@Component
public class FullTimeSalaryCalculationStrategy implements SalaryCalculationStrategy {

    @Autowired
    private BonusRecommendationRepository bonusRecommendationRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    // Standard number of working days in a month (average)
    private static final int STANDARD_WORKING_DAYS = 22;
    
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
        
        // Calculate overtime pay based on your attendance model
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
        // Net Salary = Basic Salary + Bonus + Allowances + Overtime Pay - Total Deductions
        double totalDeductions = calculation.getProvidentFund() + calculation.getIncomeTax() + calculation.getOtherDeductions();
        double netSalary = calculation.getBasicSalary() + calculation.getBonus() + calculation.getAllowances() + calculation.getOvertimePay() - totalDeductions;
        calculation.setNetSalary(netSalary);
        
        // Set audit fields
        calculation.setCreatedBy("System");
        calculation.setCreatedAt(LocalDate.now());
        
        return calculation;
    }
    
    /**
     * Calculate overtime pay based on attendance records
     * This method adapts to your specific Attendance model structure
     */
    @Autowired
private OvertimeRequestRepository overtimeRequestRepository;

private double calculateOvertimePay(Employee employee, int month, int year) {
    // Get the hourly rate based on job title
    double hourlyRate = getHourlyRateByJobTitle(employee.getJobTitle());
    
    // Calculate overtime from attendance records (existing code)
    double overtimeHoursFromAttendance = calculateOvertimeHoursFromAttendance(employee, month, year);
    
    // Calculate overtime from overtime requests
    double overtimeHoursFromRequests = calculateOvertimeHoursFromRequests(employee, month, year);
    
    // Total overtime hours
    double totalOvertimeHours = overtimeHoursFromAttendance + overtimeHoursFromRequests;
    
    // Calculate overtime pay based on hourly rate
    return totalOvertimeHours * hourlyRate;
}

private double calculateOvertimeHoursFromAttendance(Employee employee, int month, int year) {
    // Existing attendance-based calculation logic
    LocalDate monthDate = LocalDate.of(year, month, 1);
    
    // Get attendance summary for the month
    Attendance attendanceSummary = attendanceRepository.findAttendanceSummaryByEmployeeIdAndMonth(
            employee.getEmployeeID(), monthDate);
    
    double overtimeHours = 0.0;
    
    if (attendanceSummary != null && attendanceSummary.getDaysPresent() != null) {
        // Calculate overtime days (days present exceeding standard working days)
        int overtimeDays = Math.max(0, attendanceSummary.getDaysPresent() - STANDARD_WORKING_DAYS);
        
        // Convert overtime days to hours (assuming 8 hours per day)
        overtimeHours = overtimeDays * 8.0;
    } else {
        // Alternatively, we can calculate overtime days directly
        Integer overtimeDays = attendanceRepository.calculateOvertimeDays(
                employee.getEmployeeID(), monthDate, STANDARD_WORKING_DAYS);
        
        if (overtimeDays != null && overtimeDays > 0) {
            overtimeHours = overtimeDays * 8.0;
        }
    }
    
    return overtimeHours;
}

private double calculateOvertimeHoursFromRequests(Employee employee, int month, int year) {
    // Create date range for the specified month
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1).minusDays(1); // Last day of the month
    
    // Get all approved overtime requests for this employee in the specified month
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