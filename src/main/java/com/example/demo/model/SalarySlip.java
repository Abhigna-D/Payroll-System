package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "salary_slips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalarySlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "basic_salary")
    private Double basicSalary;

    @Column(name = "gross_salary")
    private Double grossSalary;

    @Column(name = "ctc")
    private Double ctc; // Cost to Company

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_current")
    private Boolean isCurrent = true;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // Helper methods to calculate different components
    
    /**
     * Calculate House Rent Allowance (typically 40-50% of basic salary)
     */
    public Double getHouseRentAllowance() {
        return basicSalary != null ? basicSalary * 0.4 : 0.0;
    }
    
    /**
     * Calculate Conveyance Allowance (fixed amount or percentage)
     */
    public Double getConveyanceAllowance() {
        return 1600.0; // Standard amount
    }
    
    /**
     * Calculate Medical Allowance (fixed amount or percentage)
     */
    public Double getMedicalAllowance() {
        return 1250.0; // Standard amount
    }
    
    /**
     * Calculate Special Allowance (remaining amount to match gross salary)
     */
    public Double getSpecialAllowance() {
        if (basicSalary == null || grossSalary == null) {
            return 0.0;
        }
        
        // Special allowance is the remaining amount after other components
        double totalOtherAllowances = getHouseRentAllowance() + getConveyanceAllowance() + getMedicalAllowance();
        double specialAllowance = grossSalary - (basicSalary + totalOtherAllowances);
        
        return Math.max(0.0, specialAllowance);
    }
    
    /**
     * Calculate total deductions (without tax - which is calculated separately)
     */
    public Double getStandardDeductions() {
        if (basicSalary == null) {
            return 0.0;
        }
        
        // Provident Fund (12% of basic)
        double pf = basicSalary * 0.12;
        
        // Professional Tax (fixed amount based on salary slab)
        double professionalTax = 200.0;
        
        return pf + professionalTax;
    }
    
    /**
     * Calculate Employee Provident Fund contribution (typically 12% of basic)
     */
    public Double getProvidentFundContribution() {
        return basicSalary != null ? basicSalary * 0.12 : 0.0;
    }
    
    /**
     * Calculate total allowances
     */
    public Double getTotalAllowances() {
        return getHouseRentAllowance() + getConveyanceAllowance() + getMedicalAllowance() + getSpecialAllowance();
    }
}