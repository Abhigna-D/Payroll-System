package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @Column(name = "employee_id")
    private String employeeID;
    
    private String name;
    private String position;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private SalarySlip salaryDetails;
    
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Attendance attendance;
    
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private Tax taxDetails;
    
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private BankAccount bankAccount;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}