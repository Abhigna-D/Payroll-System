package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "salary_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    @Column(name = "pay_period_month", nullable = false)
    private int payPeriodMonth;

    @Column(name = "pay_period_year", nullable = false)
    private int payPeriodYear;

    @Column(name = "basic_salary", nullable = false)
    private double basicSalary;

    @Column(name = "allowances")
    private double allowances;

    @Column(name = "overtime_pay")
    private double overtimePay;

    @Column(name = "bonus")
    private double bonus;

    @Column(name = "income_tax")
    private double incomeTax;

    @Column(name = "provident_fund")
    private double providentFund;

    @Column(name = "other_deductions")
    private double otherDeductions;

    @Column(name = "net_salary", nullable = false)
    private double netSalary;

    @Column(name = "status")
    private String status;  // DRAFT, APPROVED, PAID, etc.

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @Column(name = "last_modified_at")
    private LocalDate lastModifiedAt;

    @Column(name = "gross_salary")
private double grossSalary;

@Column(name = "hra_exemption")
private double hraExemption;

@Column(name = "taxable_hra")
private double taxableHra;

    // Constructor with essential fields
    public SalaryCalculation(Employee employee, int month, int year) {
        this.employee = employee;
        this.payPeriodMonth = month;
        this.payPeriodYear = year;
        this.calculationDate = LocalDate.now();
        this.status = "DRAFT";
        this.createdAt = LocalDate.now();
    }

    // Helper methods
    public String getPayPeriodFormatted() {
        YearMonth yearMonth = YearMonth.of(payPeriodYear, payPeriodMonth);
        return yearMonth.getMonth() + " " + yearMonth.getYear();
    }

    public double getTotalEarnings() {
        return basicSalary + allowances + overtimePay + bonus;
    }

    public double getTotalDeductions() {
        return incomeTax + providentFund + otherDeductions;
    }

    // Calculate net salary
    public void calculateNetSalary() {
        this.netSalary = getTotalEarnings() - getTotalDeductions();
    }
}