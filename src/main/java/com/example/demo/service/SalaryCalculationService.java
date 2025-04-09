package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;
import com.example.demo.repository.SalaryCalculationRepository;
import com.example.demo.strategy.SalaryCalculationStrategyFactory;

@Service
public class SalaryCalculationService {

    @Autowired
    private SalaryCalculationRepository salaryCalculationRepository;
    
    @Autowired
    private SalaryCalculationStrategyFactory strategyFactory;
    
    /**
     * Calculate salary for an employee for the specified month and year using the strategy pattern
     * 
     * @param employee The employee for whom to calculate salary
     * @param month The month (1-12)
     * @param year The year
     * @return The calculated salary details
     */
    public SalaryCalculation calculateSalary(Employee employee, int month, int year) {
        // Get the appropriate strategy based on employee type
        return strategyFactory.getStrategy(employee).calculateSalary(employee, month, year);
    }
    
    /**
     * Save a calculated salary to the database
     * 
     * @param calculation The salary calculation to save
     * @return The saved salary calculation
     */
    public SalaryCalculation saveSalaryCalculation(SalaryCalculation calculation) {
        return salaryCalculationRepository.save(calculation);
    }
    
    /**
     * Find a salary calculation by ID
     * 
     * @param id The ID of the salary calculation
     * @return The salary calculation, if found
     */
    public Optional<SalaryCalculation> findById(Long id) {
        return salaryCalculationRepository.findById(id);
    }
    
    /**
     * Find salary calculations by employee
     * 
     * @param employee The employee
     * @return List of salary calculations for the employee
     */
    public List<SalaryCalculation> findByEmployee(Employee employee) {
        return salaryCalculationRepository.findByEmployee(employee);
    }
    
    /**
     * Find salary calculations by month and year
     * 
     * @param month The month
     * @param year The year
     * @return List of salary calculations for the month and year
     */
    public List<SalaryCalculation> findByMonthAndYear(int month, int year) {
        return salaryCalculationRepository.findByPayPeriodMonthAndPayPeriodYear(month, year);
    }
    
    /**
     * Find salary calculation for an employee for a specific month and year
     * 
     * @param employee The employee
     * @param month The month
     * @param year The year
     * @return The salary calculation, if found
     */
    public SalaryCalculation findByEmployeeAndMonthAndYear(Employee employee, int month, int year) {
        return salaryCalculationRepository.findByEmployeeAndPayPeriodMonthAndPayPeriodYear(employee, month, year);
    }
    
    /**
     * Find salary calculations by status
     * 
     * @param status The status
     * @return List of salary calculations with the specified status
     */
    public List<SalaryCalculation> findByStatus(String status) {
        return salaryCalculationRepository.findByStatus(status);
    }
    
    
    /**
 * Save a list of salary calculations (approve them)
 */
/**
 * Save a list of salary calculations (approve them)
 */
@Transactional
public boolean saveSalaryCalculations(List<Long> calculationIds) {
    try {
        // Log the received calculation IDs
        System.out.println("Saving salary calculations with IDs: " + calculationIds);
        
        if (calculationIds == null || calculationIds.isEmpty()) {
            System.out.println("No calculation IDs provided");
            return false;
        }
        
        // For each calculation ID, fetch the calculation and update its status
        for (Long id : calculationIds) {
            // Skip null IDs
            if (id == null) {
                System.out.println("Skipping null calculation ID");
                continue;
            }
            
            try {
                System.out.println("Processing calculation ID: " + id);
                Optional<SalaryCalculation> calcOpt = salaryCalculationRepository.findById(id);
                
                if (calcOpt.isPresent()) {
                    SalaryCalculation calc = calcOpt.get();
                    System.out.println("Found calculation: " + calc.getId() + " for employee " + 
                                     (calc.getEmployee() != null ? calc.getEmployee().getEmployeeID() : "unknown"));
                    
                    calc.setStatus("APPROVED");
                    calc.setLastModifiedAt(LocalDate.now());
                    calc.setLastModifiedBy("Finance Officer"); // In a real app, get from authenticated user
                    salaryCalculationRepository.save(calc);
                    System.out.println("Calculation saved successfully");
                } else {
                    System.out.println("Calculation not found with ID: " + id);
                }
            } catch (Exception ex) {
                // Log the exception but continue processing other IDs
                System.err.println("Error processing calculation ID " + id + ": " + ex.getMessage());
                ex.printStackTrace();
                // Don't rethrow - continue with other IDs
            }
        }
        return true;
    } catch (Exception e) {
        // Log the exception
        System.err.println("Error saving salary calculations: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
}