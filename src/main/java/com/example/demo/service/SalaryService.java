package com.example.demo.service;

import com.example.demo.model.SalarySlip;
import java.util.List;

public interface SalaryService {
    
    /**
     * Get salary details for a specific employee
     * 
     * @param employeeID The employee ID
     * @return The salary details
     */
    SalarySlip getSalaryDetails(String employeeID);
    
    /**
     * Get historical salary records for an employee
     * 
     * @param employeeID The employee ID
     * @return List of historical salary records
     */
    List<SalarySlip> getSalaryHistory(String employeeID);
    
    /**
     * Get salary details for a specific month and year
     * 
     * @param employeeID The employee ID
     * @param month The month (1-12)
     * @param year The year
     * @return The salary details for that period
     */
    SalarySlip getSalaryForPeriod(String employeeID, int month, int year);
    
    /**
     * Generate a salary slip PDF
     * 
     * @param employeeID The employee ID
     * @param month The month (1-12)
     * @param year The year
     * @return Path to the generated PDF
     */
    String generateSalarySlipPDF(String employeeID, int month, int year);
}