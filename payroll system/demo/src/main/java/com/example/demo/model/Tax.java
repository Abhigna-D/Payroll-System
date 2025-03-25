package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "taxes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String taxId;
    private double taxableIncome;
    private double taxAmount;
    private String taxYear;
    
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}