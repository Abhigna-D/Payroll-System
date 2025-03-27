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

    private String pan;
    private String taxRegime;
    
    // Section 80C investments
    private Integer epf = 0;
    private Integer vpf = 0;
    private Integer lifeInsurance = 0;
    private Integer elss = 0;
    private Integer ppf = 0;
    private Integer homeLoanPrincipal = 0;
    private Integer sukanyaSamriddhi = 0;
    private Integer tuitionFees = 0;
    private Integer nsc = 0;
    private Integer taxSavingFD = 0;
    
    // Other deductions
    private Integer nps = 0;
    private Integer medicalInsurance = 0;
    private Integer educationLoan = 0;
    private Integer homeLoanInterest = 0;
    private Integer donations = 0;
    private Integer disabilityDeduction = 0;
    
    // HRA details
    private Boolean isRenting = false;
    private Integer monthlyRent = 0;
    private String landlordPan;
    private String rentalAddress;
    
    // Previous employment details
    private Boolean hasPreviousEmployment = false;
    private String previousEmployerName;
    private Integer previousTaxableIncome = 0;
    private Integer previousTaxDeducted = 0;
    
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

    public String getTaxRegime() {
        return taxRegime;
    }

    public void setTaxRegime(String taxRegime) {
        this.taxRegime = taxRegime;
    }

    public Integer getEpf() {
        return epf;
    }

    public void setEpf(Integer epf) {
        this.epf = epf;
    }

    public Integer getVpf() {
        return vpf;
    }

    public void setVpf(Integer vpf) {
        this.vpf = vpf;
    }

    public Integer getLifeInsurance() {
        return lifeInsurance;
    }

    public void setLifeInsurance(Integer lifeInsurance) {
        this.lifeInsurance = lifeInsurance;
    }

    public Integer getElss() {
        return elss;
    }

    public void setElss(Integer elss) {
        this.elss = elss;
    }

    public Integer getPpf() {
        return ppf;
    }

    public void setPpf(Integer ppf) {
        this.ppf = ppf;
    }

    public Integer getHomeLoanPrincipal() {
        return homeLoanPrincipal;
    }

    public void setHomeLoanPrincipal(Integer homeLoanPrincipal) {
        this.homeLoanPrincipal = homeLoanPrincipal;
    }

    public Integer getSukanyaSamriddhi() {
        return sukanyaSamriddhi;
    }

    public void setSukanyaSamriddhi(Integer sukanyaSamriddhi) {
        this.sukanyaSamriddhi = sukanyaSamriddhi;
    }

    public Integer getTuitionFees() {
        return tuitionFees;
    }

    public void setTuitionFees(Integer tuitionFees) {
        this.tuitionFees = tuitionFees;
    }

    public Integer getNsc() {
        return nsc;
    }

    public void setNsc(Integer nsc) {
        this.nsc = nsc;
    }

    public Integer getTaxSavingFD() {
        return taxSavingFD;
    }

    public void setTaxSavingFD(Integer taxSavingFD) {
        this.taxSavingFD = taxSavingFD;
    }

    public Integer getNps() {
        return nps;
    }

    public void setNps(Integer nps) {
        this.nps = nps;
    }

    public Integer getMedicalInsurance() {
        return medicalInsurance;
    }

    public void setMedicalInsurance(Integer medicalInsurance) {
        this.medicalInsurance = medicalInsurance;
    }

    public Integer getEducationLoan() {
        return educationLoan;
    }

    public void setEducationLoan(Integer educationLoan) {
        this.educationLoan = educationLoan;
    }

    public Integer getHomeLoanInterest() {
        return homeLoanInterest;
    }

    public void setHomeLoanInterest(Integer homeLoanInterest) {
        this.homeLoanInterest = homeLoanInterest;
    }

    public Integer getDonations() {
        return donations;
    }

    public void setDonations(Integer donations) {
        this.donations = donations;
    }

    public Integer getDisabilityDeduction() {
        return disabilityDeduction;
    }

    public void setDisabilityDeduction(Integer disabilityDeduction) {
        this.disabilityDeduction = disabilityDeduction;
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
}