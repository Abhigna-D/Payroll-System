package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "parttime_attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartTimeAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attributeid")
    private Long attributeid;

    @Column(name = "date")
    private LocalDate date;
    
    @Column(name = "login_time")
    private LocalTime loginTime;
    
    @Column(name = "logout_time")
    private LocalTime logoutTime;
    
    @Column(name = "total_hours")
    private Double totalHours;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AttendanceStatus status;
    
    @Column(name = "employee_id")
    private String employeeId;
    
    @Column(name = "month")
    private LocalDate month;
    
    @Column(name = "days_present")
    private Integer daysPresent;

    @Column(name = "days_absent")
    private Integer daysAbsent;

    @Column(name = "days_leave")
    private Integer daysLeave;
    
    // Method to calculate total hours worked
    @PrePersist
    @PreUpdate
    public void calculateTotalHours() {
        if (loginTime != null && logoutTime != null) {
            // Calculate hours between login and logout time
            double hours = (logoutTime.toSecondOfDay() - loginTime.toSecondOfDay()) / 3600.0;
            this.totalHours = Math.round(hours * 100.0) / 100.0; // Round to 2 decimal places
        }
    }
    
    // Enum for attendance status (reusing the same enum from Attendance class)
    public enum AttendanceStatus {
        ABSENT, HALF_DAY, PRESENT, SICK_LEAVE, WORK_FROM_HOME
    }
}