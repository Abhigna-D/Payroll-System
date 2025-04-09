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
    
    // Standard deduction for salaried individuals (as per Budget 2025)
    private static final double STANDARD_DEDUCTION = 75000.0;
    
    // Metro cities for HRA exemption
    private static final List<String> METRO_CITIES = List.of(
            "mumbai", "delhi", "kolkata", "chennai", "bangalore", "bengaluru", "hyderabad");
    
    // New Tax Slabs for FY 2025-26 (as per Budget 2025)
    private static final double[] TAX_SLAB_LIMITS = {
            400000.0,   // 0% tax up to Rs 4 lakh
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
    
    // Tax rebate under section 87A (as per Budget 2025)
    private static final double MAX_REBATE_AMOUNT = 60000.0;
    private static final double REBATE_INCOME_LIMIT = 1200000.0;
    
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
            
            // 2. Calculate income tax based on the new tax regime (Budget 2025)
            // Calculate annual gross salary - only annualize basic salary and allowances
            double annualBasicAndAllowances = (calculation.getBasicSalary() + calculation.getAllowances()) * 12;
            
            // Add current month's bonus and overtime - don't multiply these by 12
            double annualGross = annualBasicAndAllowances + calculation.getBonus() + calculation.getOvertimePay();
            
            // Calculate taxable income after standard deduction
            double taxableIncome = annualGross - STANDARD_DEDUCTION;
            taxableIncome = Math.max(0, taxableIncome);
            
            // Calculate annual income tax
            double annualIncomeTax = calculateIncomeTax(taxableIncome);
            
            // Calculate monthly income tax (divide annual tax by 12)
            double monthlyIncomeTax = annualIncomeTax / 12;
            calculation.setIncomeTax(monthlyIncomeTax);
            
            // Debug log for tax calculation
            System.out.println("Income Tax Calculation for " + employee.getFullName() + " (ID: " + employee.getEmployeeID() + ")");
            System.out.println("  Monthly Basic + Allowances: ₹" + (calculation.getBasicSalary() + calculation.getAllowances()));
            System.out.println("  Annual Basic + Allowances: ₹" + annualBasicAndAllowances);
            System.out.println("  Current Month Bonus: ₹" + calculation.getBonus());
            System.out.println("  Current Month Overtime: ₹" + calculation.getOvertimePay());
            System.out.println("  Annual Gross: ₹" + annualGross);
            System.out.println("  Standard Deduction: ₹" + STANDARD_DEDUCTION);
            System.out.println("  Taxable Income: ₹" + taxableIncome);
            System.out.println("  Annual Income Tax: ₹" + annualIncomeTax);
            System.out.println("  Monthly Income Tax: ₹" + monthlyIncomeTax);
            
            // 3. Other deductions (Professional Tax + Medical Insurance + Attendance Deduction) for THIS employee
            double otherDeductions = 0;
            
            // a. Professional Tax (monthly)
            double professionalTaxMonthly = 200.0; // ₹2400 per year / 12 months
            otherDeductions += professionalTaxMonthly;
            
            // b. Medical Insurance Premium (monthly) from THIS employee's tax declaration
            taxDeclaration = employee.getTaxDeclaration();
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
        }
    }
    
    /**
     * Calculate income tax based on the new tax regime (Budget 2025)
     * 
     * @param taxableIncome Annual taxable income after standard deduction
     * @return Annual income tax amount after rebate
     */
    private double calculateIncomeTax(double taxableIncome) {
        // Special case: No tax for income up to Rs 12,75,000 (Rs 12 lakhs + standard deduction)
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            return 0.0;
        }
        
        double tax = 0.0;
        double remainingIncome = taxableIncome;
        
        // Calculate tax slab by slab
        for (int i = 0; i < TAX_SLAB_LIMITS.length; i++) {
            double slabLimit = TAX_SLAB_LIMITS[i];
            double prevLimit = (i == 0) ? 0 : TAX_SLAB_LIMITS[i - 1];
            double slabWidth = slabLimit - prevLimit;
            
            if (remainingIncome <= 0) {
                break;
            }
            
            double taxableInThisSlab = Math.min(remainingIncome, slabWidth);
            tax += taxableInThisSlab * TAX_SLAB_RATES[i];
            remainingIncome -= taxableInThisSlab;
        }
        
        // If there's still remaining income, tax it at the highest rate
        if (remainingIncome > 0) {
            tax += remainingIncome * TAX_SLAB_RATES[TAX_SLAB_RATES.length - 1];
        }
        
        // Calculate rebate u/s 87A
        double rebate = 0.0;
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            rebate = Math.min(tax, MAX_REBATE_AMOUNT);
        }
        
        // Final tax after rebate
        tax -= rebate;
        
        // Add 4% health and education cess
        tax += tax * 0.04;
        
        return Math.round(tax);
    }
    
    /**
     * Calculate HRA exemption based on Income Tax rules
     * Returns an array where:
     * - index 0 = HRA exemption amount
     * - index 1 = Taxable HRA amount
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
                    return 200000.0;
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

}