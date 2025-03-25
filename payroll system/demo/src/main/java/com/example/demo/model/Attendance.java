package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int daysPresent;
    private int daysAbsent;
    private int daysLeave;
    private LocalDate month;
    
    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}