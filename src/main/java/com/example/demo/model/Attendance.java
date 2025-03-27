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
    @Column(name = "attributeid")
    private Long attributeid;

    @Column(name = "days_present")
    private Integer daysPresent;

    @Column(name = "days_absent")
    private Integer daysAbsent;

    @Column(name = "days_leave")
    private Integer daysLeave;

    @Column(name = "month")
    private LocalDate month;
    
    @Column(name = "date")
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatus status;
    
    // Store employee_id as a direct field instead of using a relationship
    // This avoids the "more than one row with the given identifier" error
    @Column(name = "employee_id")
    private String employeeId;
    
    // Enum for attendance status
    public enum AttendanceStatus {
        ABSENT, HALF_DAY, PRESENT, SICK_LEAVE, WORK_FROM_HOME
    }
}