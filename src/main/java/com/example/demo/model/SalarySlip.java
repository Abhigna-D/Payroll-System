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
    
    private double basicSalary;
    private double allowances;
    private double deductions;
    private double netSalary;
    private LocalDate paymentDate;
    
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}