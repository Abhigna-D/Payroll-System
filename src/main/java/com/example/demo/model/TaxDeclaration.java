package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tax_declarations")
public class TaxDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "employee_id")
    private Employee employee;

    // Personal Information
    private String pan;
    
    // Bank Details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    
    // HRA & Rent Details
    private Boolean isRenting = false;
    private Integer monthlyRent = 0;
    private String landlordName;
    private String landlordPan;
    private String rentalAddress;
    private LocalDate rentFromDate;
    private LocalDate rentToDate;
    
    // Home Loan Details
    private Boolean hasHomeLoan = false;
    private String homeLoanAccountNumber;
    private String homeLoanBankName;
    private Integer homeLoanInterest = 0;
    
    // Previous Employment Details
    private Boolean hasPreviousEmployment = false;
    private String previousEmployerName;
    private Integer previousTaxableIncome = 0;
    private Integer previousTaxDeducted = 0;
    
    // Health Insurance
    private Integer medicalInsurance = 0;
    private String medicalInsurancePlan;
    
    // Professional Tax (Fixed - Monthly)
    private final Integer professionalTax = 2400;
       
    // Tax Regime Selection
    private String taxRegime = "NEW"; // Default to new regime: NEW or OLD
    
    // Status fields
    private String status = "DRAFT"; // DRAFT, PENDING, APPROVED, REJECTED
    private String rejectionReason;
    private LocalDate creationDate;
    private LocalDate lastUpdateDate;
    private LocalDate approvalDate;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public Boolean getIsRenting() {
        return isRenting;
    }

    public void setIsRenting(Boolean isRenting) {
        this.isRenting = isRenting;
    }

    public Integer getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(Integer monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public String getLandlordName() {
        return landlordName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public String getLandlordPan() {
        return landlordPan;
    }

    public void setLandlordPan(String landlordPan) {
        this.landlordPan = landlordPan;
    }

    public String getRentalAddress() {
        return rentalAddress;
    }

    public void setRentalAddress(String rentalAddress) {
        this.rentalAddress = rentalAddress;
    }

    public LocalDate getRentFromDate() {
        return rentFromDate;
    }

    public void setRentFromDate(LocalDate rentFromDate) {
        this.rentFromDate = rentFromDate;
    }

    public LocalDate getRentToDate() {
        return rentToDate;
    }

    public void setRentToDate(LocalDate rentToDate) {
        this.rentToDate = rentToDate;
    }

    public Boolean getHasHomeLoan() {
        return hasHomeLoan;
    }

    public void setHasHomeLoan(Boolean hasHomeLoan) {
        this.hasHomeLoan = hasHomeLoan;
    }

    public String getHomeLoanAccountNumber() {
        return homeLoanAccountNumber;
    }

    public void setHomeLoanAccountNumber(String homeLoanAccountNumber) {
        this.homeLoanAccountNumber = homeLoanAccountNumber;
    }

    public String getHomeLoanBankName() {
        return homeLoanBankName;
    }

    public void setHomeLoanBankName(String homeLoanBankName) {
        this.homeLoanBankName = homeLoanBankName;
    }

    public Integer getHomeLoanInterest() {
        return homeLoanInterest;
    }

    public void setHomeLoanInterest(Integer homeLoanInterest) {
        this.homeLoanInterest = homeLoanInterest;
    }

    public Boolean getHasPreviousEmployment() {
        return hasPreviousEmployment;
    }

    public void setHasPreviousEmployment(Boolean hasPreviousEmployment) {
        this.hasPreviousEmployment = hasPreviousEmployment;
    }

    public String getPreviousEmployerName() {
        return previousEmployerName;
    }

    public void setPreviousEmployerName(String previousEmployerName) {
        this.previousEmployerName = previousEmployerName;
    }

    public Integer getPreviousTaxableIncome() {
        return previousTaxableIncome;
    }

    public void setPreviousTaxableIncome(Integer previousTaxableIncome) {
        this.previousTaxableIncome = previousTaxableIncome;
    }

    public Integer getPreviousTaxDeducted() {
        return previousTaxDeducted;
    }

    public void setPreviousTaxDeducted(Integer previousTaxDeducted) {
        this.previousTaxDeducted = previousTaxDeducted;
    }

    

    public Integer getMedicalInsurance() {
        // If a plan is selected, return its value
        if (medicalInsurancePlan != null) {
            switch (medicalInsurancePlan) {
                case "CARE_SILVER":
                    return 5500;
                case "CARE_GOLD":
                    return 8500;
                case "CARE_PLATINUM":
                    return 12000;
                default:
                    // Return the stored value if the plan is unknown
                    return medicalInsurance;
            }
        }
        // Return the stored value if no plan is selected
        return medicalInsurance;
    }

    public void setMedicalInsurance(Integer medicalInsurance) {
        this.medicalInsurance = medicalInsurance;
    }

    public Integer getProfessionalTax() {
        return professionalTax;
    }

    public String getTaxRegime() {
        return taxRegime;
    }
    
    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getMedicalInsurancePlan() {
        return medicalInsurancePlan;
    }

    public void setMedicalInsurancePlan(String medicalInsurancePlan) {
        this.medicalInsurancePlan = medicalInsurancePlan;
    }
    
    // Calculate total deductions
    public Integer calculateTotalDeductions() {
        return professionalTax + 
               (medicalInsurance != null ? medicalInsurance : 0) + 
               (homeLoanInterest != null ? homeLoanInterest : 0);
    }
    
    // Get monthly professional tax (for payroll calculations)
    public double getMonthlyProfessionalTax() {
        return professionalTax / 12.0;
    }
}