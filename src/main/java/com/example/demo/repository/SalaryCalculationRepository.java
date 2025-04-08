package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Employee;
import com.example.demo.model.SalaryCalculation;

@Repository
public interface SalaryCalculationRepository extends JpaRepository<SalaryCalculation, Long> {
    
    /**
     * Find salary calculations by employee
     */
    List<SalaryCalculation> findByEmployee(Employee employee);
    
    /**
     * Find salary calculations by pay period month and year
     */
    List<SalaryCalculation> findByPayPeriodMonthAndPayPeriodYear(int month, int year);
    
    /**
     * Find salary calculations by employee and pay period
     */
    SalaryCalculation findByEmployeeAndPayPeriodMonthAndPayPeriodYear(
            Employee employee, int month, int year);
    
    /**
     * Find salary calculations by status
     */
    List<SalaryCalculation> findByStatus(String status);
    
    /**
     * Find salary calculations created on a specific date
     */
    List<SalaryCalculation> findByCreatedAt(LocalDate createdAt);
    
    /**
     * Find salary calculations by employee and date range
     */
    @Query("SELECT sc FROM SalaryCalculation sc WHERE sc.employee = :employee " +
           "AND sc.payPeriodYear * 100 + sc.payPeriodMonth BETWEEN :startYearMonth AND :endYearMonth " +
           "ORDER BY sc.payPeriodYear, sc.payPeriodMonth")
    List<SalaryCalculation> findByEmployeeAndPeriodRange(
            @Param("employee") Employee employee,
            @Param("startYearMonth") int startYearMonth, 
            @Param("endYearMonth") int endYearMonth);
    
    /**
     * Get the latest salary calculations for all employees for a specific period
     */
    @Query("SELECT sc FROM SalaryCalculation sc " +
           "WHERE sc.payPeriodMonth = :month AND sc.payPeriodYear = :year " +
           "AND sc.status = :status " +
           "ORDER BY sc.employee.FullName")
    List<SalaryCalculation> findLatestSalaryCalculations(
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") String status);
}