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
            
            // Calculate allowances for THIS employee
            double totalAllowances = calculateAllowances(employee, salaryDetails, basicSalary);
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
            
            // 2. Set income tax to 0 (as per your example)
            calculation.setIncomeTax(0);
            
            // 3. Other deductions (Professional Tax + Medical Insurance + Attendance Deduction) for THIS employee
            double otherDeductions = 0;
            
            // a. Professional Tax (monthly)
            double professionalTaxMonthly = 200.0; // ₹2400 per year / 12 months
            otherDeductions += professionalTaxMonthly;
            
            // b. Medical Insurance Premium (monthly) from THIS employee's tax declaration
            TaxDeclaration taxDeclaration = employee.getTaxDeclaration();
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
        }
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
                    return 70000.0;
                case "software engineer":
                    return 90000.0;
                case "senior developer":
                    return 150000.0;
                case "tech lead":
                    return 200000.0;
                default:
                    return 50000.0; // Default value
            }
        } else {
            return 50000.0; // Default value if no job title
        }
    }
    
    /**
     * Calculate allowances for an employee
     */
    private double calculateAllowances(Employee employee, SalarySlip salaryDetails, double basicSalary) {
        if (salaryDetails != null && salaryDetails.getTotalAllowances() != null) {
            return salaryDetails.getTotalAllowances();
        } else {
            // Default allowances (using ~54% of basic salary as a reference)
            return basicSalary * 0.54;
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
}