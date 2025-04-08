package com.example.demo.strategy;

import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;

/**
 * Strategy interface for different salary calculation algorithms
 */
public interface SalaryCalculationStrategy {
    
    /**
     * Calculate salary for an employee for the specified month and year
     * 
     * @param employee The employee for whom to calculate salary
     * @param month The month (1-12)
     * @param year The year
     * @return The calculated salary details
     */
    SalaryCalculation calculateSalary(Employee employee, int month, int year);
}