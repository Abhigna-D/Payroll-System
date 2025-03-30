package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "employee_id")
    private String employeeID;

    @Column(name = "FullName")
    private String FullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "address")
    private String address;

    @Column(name = "personal_phone")
    private String personalPhone;

    @Column(name = "work_phone")
    private String workPhone;

    @Column(name = "personal_email")
    private String personalEmail;

    @Column(name = "work_email")
    private String workEmail;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "reporting_manager")
    private String reportingManager;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "employee_type")
    private String employeeType;

    @Column(name = "office_location")
    private String officeLocation;

    @Column(name = "work_schedule")
    private String workSchedule;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private SalarySlip salaryDetails;

    // Changed from @OneToOne to @OneToMany since an employee can have multiple attendance records
    // Also removed the mappedBy attribute as we no longer have a direct relationship from Attendance to Employee
    @OneToMany
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private List<Attendance> attendanceRecords;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private TaxDeclaration taxDeclaration;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private BankAccount bankAccount;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Check if this employee is part-time
public boolean isPartTime() {
    return "PART_TIME".equalsIgnoreCase(this.employeeType) || 
           "Part-time".equalsIgnoreCase(this.employeeType);
}
}