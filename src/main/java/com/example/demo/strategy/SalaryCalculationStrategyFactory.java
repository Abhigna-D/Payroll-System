package com.example.demo.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.model.Employee;

/**
 * Factory class for getting the appropriate salary calculation strategy
 */
@Component
public class SalaryCalculationStrategyFactory {
    
    @Autowired
    private FullTimeSalaryCalculationStrategy fullTimeStrategy;
    
    @Autowired
    private PartTimeSalaryCalculationStrategy partTimeStrategy;
    
    /**
     * Get the appropriate salary calculation strategy based on employee type
     * 
     * @param employee The employee for whom to get the strategy
     * @return The appropriate salary calculation strategy
     */
    public SalaryCalculationStrategy getStrategy(Employee employee) {
        if (employee.isPartTime()) {
            return partTimeStrategy;
        } else {
            return fullTimeStrategy;
        }
    }
}