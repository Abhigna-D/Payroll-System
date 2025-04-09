package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.model.SalarySlip;
import com.example.demo.repository.SalarySlipRepository;

@Service
public class PayslipService {

    @Autowired
    private SalarySlipRepository salarySlipRepository;
    
    /**
     * Generate payslip data from a salary calculation
     * 
     * @param salaryCalculation The salary calculation to base the payslip on
     * @return A map containing the payslip data
     */
    public Map<String, Object> generatePayslipData(SalaryCalculation salaryCalculation) {
        Map<String, Object> payslipData = new HashMap<>();
        
        Employee employee = salaryCalculation.getEmployee();
        
        // Basic payslip information
        payslipData.put("payslipNumber", "PSL-" + employee.getEmployeeID() + "-" + 
                         salaryCalculation.getPayPeriodYear() + "-" + 
                         String.format("%02d", salaryCalculation.getPayPeriodMonth()));
        
        payslipData.put("payPeriod", salaryCalculation.getPayPeriodFormatted());
        payslipData.put("payDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        
        // Employee information
        Map<String, Object> employeeInfo = new HashMap<>();
        employeeInfo.put("id", employee.getEmployeeID());
        employeeInfo.put("name", employee.getFullName());
        employeeInfo.put("jobTitle", employee.getJobTitle());
        employeeInfo.put("department", employee.getDepartment() != null ? 
                          employee.getDepartment().getName() : "Not Assigned");
        employeeInfo.put("joiningDate", employee.getJoiningDate() != null ? 
                          employee.getJoiningDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "N/A");
        employeeInfo.put("paymentMode", "Bank Transfer");
        employeeInfo.put("bankAccount", maskBankAccount(employee.getBankAccount() != null ? 
        employee.getBankAccount().getAccountNumber() : null));

// OR - if you need to get account number from the tax declaration
employeeInfo.put("bankAccount", maskBankAccount(employee.getTaxDeclaration() != null ? 
        employee.getTaxDeclaration().getAccountNumber() : null));
        employeeInfo.put("panNumber", maskPanNumber(employee.getTaxDeclaration() != null ? 
                            employee.getTaxDeclaration().getPan() : null));
        employeeInfo.put("employeeType", employee.isPartTime() ? "Part-Time" : "Full-Time");
        
        payslipData.put("employee", employeeInfo);
        
        // Earnings
        Map<String, Object> earnings = new HashMap<>();
        earnings.put("basicSalary", formatCurrency(salaryCalculation.getBasicSalary()));
        earnings.put("houseRentAllowance", formatCurrency(getHouseRentAllowance(salaryCalculation)));
        earnings.put("conveyanceAllowance", formatCurrency(getConveyanceAllowance()));
        earnings.put("medicalAllowance", formatCurrency(getMedicalAllowance()));
        earnings.put("otherAllowances", formatCurrency(getOtherAllowances(salaryCalculation)));
        earnings.put("overtimePay", formatCurrency(salaryCalculation.getOvertimePay()));
        earnings.put("bonus", formatCurrency(salaryCalculation.getBonus()));
        earnings.put("totalEarnings", formatCurrency(salaryCalculation.getTotalEarnings()));
        
        payslipData.put("earnings", earnings);
        
        // Deductions
        Map<String, Object> deductions = new HashMap<>();
        deductions.put("providentFund", formatCurrency(salaryCalculation.getProvidentFund()));
        deductions.put("incomeTax", formatCurrency(salaryCalculation.getIncomeTax()));
        deductions.put("professionalTax", formatCurrency(getProfessionalTax()));
        deductions.put("otherDeductions", formatCurrency(getOtherDeductions(salaryCalculation)));
        deductions.put("totalDeductions", formatCurrency(salaryCalculation.getTotalDeductions()));
        
        payslipData.put("deductions", deductions);
        
        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("grossSalary", formatCurrency(salaryCalculation.getGrossSalary()));
        summary.put("totalDeductions", formatCurrency(salaryCalculation.getTotalDeductions()));
        summary.put("netSalary", formatCurrency(salaryCalculation.getNetSalary()));
        summary.put("netSalaryInWords", convertToWords(salaryCalculation.getNetSalary()));
        
        payslipData.put("summary", summary);
        
        return payslipData;
    }
    
    /**
     * Generate and save a payslip based on a salary calculation
     * 
     * @param salaryCalculation The salary calculation to base the payslip on
     * @return true if the payslip was generated successfully
     */
    public boolean generatePayslip(SalaryCalculation salaryCalculation) {
        try {
            // In a real implementation, this might involve:
            // 1. Creating a PDF of the payslip
            // 2. Saving it to a database or file system
            // 3. Sending an email notification to the employee
            
            // For this example, we'll just mark the calculation as having a payslip generated
            salaryCalculation.setStatus("PAYSLIP_GENERATED");
            salaryCalculation.setLastModifiedAt(LocalDate.now());
            salaryCalculation.setLastModifiedBy("Finance Officer");
            salaryCalculation.setRemarks("Payslip generated on " + LocalDate.now());
            
            // If there's a separate payslip entity, we would save that here too
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to format currency values
     */
    private String formatCurrency(double amount) {
        return String.format("₹%.2f", amount);
    }
    
    /**
     * Helper method to mask bank account numbers for privacy
     */
    private String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXXXXXXXXXX";
        }
        
        return "XXXXXXXX" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    /**
     * Helper method to mask PAN numbers for privacy
     */
    private String maskPanNumber(String panNumber) {
        if (panNumber == null || panNumber.length() < 10) {
            return "XXXXXXXXXX";
        }
        
        return panNumber.substring(0, 5) + "XXXXX";
    }
    
    /**
     * Calculate House Rent Allowance (typically 40-50% of basic salary)
     */
    private double getHouseRentAllowance(SalaryCalculation salaryCalculation) {
        // Check if we have HRA exemption information
        try {
            return salaryCalculation.getHraExemption();
        } catch (Exception e) {
            // If not available, calculate as 40% of basic salary
            return salaryCalculation.getBasicSalary() * 0.4;
        }
    }
    
    /**
     * Calculate Conveyance Allowance (fixed amount)
     */
    private double getConveyanceAllowance() {
        return 1600.0; // Standard amount
    }
    
    /**
     * Calculate Medical Allowance (fixed amount)
     */
    private double getMedicalAllowance() {
        return 1250.0; // Standard amount
    }
    
    /**
     * Calculate other allowances (remaining from total allowances)
     */
    private double getOtherAllowances(SalaryCalculation salaryCalculation) {
        double totalAllowances = salaryCalculation.getAllowances();
        double standardAllowances = getHouseRentAllowance(salaryCalculation) + 
                                   getConveyanceAllowance() + 
                                   getMedicalAllowance();
        
        return Math.max(0, totalAllowances - standardAllowances);
    }
    
    /**
     * Calculate Professional Tax (fixed amount based on salary slab)
     */
    private double getProfessionalTax() {
        return 200.0; // Standard amount
    }
    
    /**
     * Calculate other deductions (remaining from total deductions)
     */
    private double getOtherDeductions(SalaryCalculation salaryCalculation) {
        double totalDeductions = salaryCalculation.getTotalDeductions();
        double standardDeductions = salaryCalculation.getProvidentFund() + 
                                   salaryCalculation.getIncomeTax() + 
                                   getProfessionalTax();
        
        return Math.max(0, totalDeductions - standardDeductions);
    }
    
    /**
     * Convert a number to words (for displaying net salary in words)
     */
    private String convertToWords(double number) {
        // A simple implementation for converting numbers to words
        // In a real application, this would be more sophisticated
        
        if (number == 0) {
            return "Zero Rupees Only";
        }
        
        // Split the number into whole and decimal parts
        long wholePart = (long) number;
        long decimalPart = Math.round((number - wholePart) * 100);
        
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", 
                          "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", 
                          "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        // A very simplified implementation
        StringBuilder result = new StringBuilder();
        
        if (wholePart > 0) {
            if (wholePart < 20) {
                result.append(units[(int) wholePart]);
            } else if (wholePart < 100) {
                result.append(tens[(int) (wholePart / 10)]);
                if (wholePart % 10 > 0) {
                    result.append(" ").append(units[(int) (wholePart % 10)]);
                }
            } else {
                // For larger numbers, just show the numeric value
                result.append(wholePart);
            }
            
            result.append(" Rupees");
        }
        
        if (decimalPart > 0) {
            if (result.length() > 0) {
                result.append(" and ");
            }
            
            if (decimalPart < 20) {
                result.append(units[(int) decimalPart]);
            } else {
                result.append(tens[(int) (decimalPart / 10)]);
                if (decimalPart % 10 > 0) {
                    result.append(" ").append(units[(int) (decimalPart % 10)]);
                }
            }
            
            result.append(" Paise");
        }
        
        result.append(" Only");
        
        return result.toString();
    }
}