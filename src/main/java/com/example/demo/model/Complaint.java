package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "complaint_text", columnDefinition = "TEXT", nullable = false)
    private String complaintText;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatus status;

    @Column(name = "resolution_text", columnDefinition = "TEXT")
    private String resolutionText;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    // Enumeration for complaint status
    public enum ComplaintStatus {
        SUBMITTED,
        UNDER_REVIEW,
        IN_PROGRESS,
        RESOLVED,
        REJECTED
    }
    
    // Pre-persist hook to set default values
    @PrePersist
    public void prePersist() {
        submissionDate = LocalDateTime.now();
        if (status == null) {
            status = ComplaintStatus.SUBMITTED;
        }
    }
}