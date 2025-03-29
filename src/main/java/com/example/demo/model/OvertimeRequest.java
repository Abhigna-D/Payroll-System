package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "overtime_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;
    
    @Column(name = "employee_name", nullable = false)
    private String employeeName;
    
    @Column(name = "overtime_date", nullable = false)
    private LocalDate overtimeDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;
    
    @Column(name = "employment_type", nullable = false)
    private String employmentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OvertimeRequestStatus status;
    
    @Column(name = "manager_comments")
    private String managerComments;
    
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;
    
    @Column(name = "processed_date")
    private LocalDate processedDate;
    
    public enum OvertimeRequestStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // Pre-persist hook to set defaults before saving
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = OvertimeRequestStatus.PENDING;
        }
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }
    }
}