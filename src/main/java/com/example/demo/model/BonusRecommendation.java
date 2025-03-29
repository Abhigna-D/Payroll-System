package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;  // Changed from javax.persistence

import com.example.demo.model.state.BonusRecommendationState;
import com.example.demo.model.state.PendingState;
import com.example.demo.model.state.UnderReviewState;
import com.example.demo.model.state.ApprovedState;
import com.example.demo.model.state.RejectedState;

@Entity
@Table(name = "bonus_recommendations")
public class BonusRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    private BigDecimal amount;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "recommended_date")
    private LocalDateTime recommendedDate;
    
    @ManyToOne
    @JoinColumn(name = "recommended_by")
    private User recommendedBy;
    
    @Enumerated(EnumType.STRING)
    private BonusRecommendationStatus status;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    @Transient
    private BonusRecommendationState state;
    
    // Default constructor for JPA
    public BonusRecommendation() {
        this.recommendedDate = LocalDateTime.now();
        this.status = BonusRecommendationStatus.PENDING;
        this.state = new PendingState();
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRecommendedDate() {
        return recommendedDate;
    }

    public void setRecommendedDate(LocalDateTime recommendedDate) {
        this.recommendedDate = recommendedDate;
    }

    public User getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(User recommendedBy) {
        this.recommendedBy = recommendedBy;
    }

    public BonusRecommendationStatus getStatus() {
        return status;
    }

    public void setStatus(BonusRecommendationStatus status) {
        this.status = status;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    // State pattern methods
    public void setState(BonusRecommendationState state) {
        this.state = state;
        this.status = state.getStatus();
    }
    
    public void submit() {
        state.submit(this);
    }
    
    public void review() {
        state.review(this);
    }
    
    public void approve() {
        state.approve(this);
    }
    
    public void reject(String reason) {
        state.reject(this, reason);
    }
    
    public boolean canEdit() {
        return state.canEdit();
    }
    
    // Method to correctly load state after fetching from database
    @PostLoad
    private void loadState() {
        switch (status) {
            case PENDING:
                this.state = new PendingState();
                break;
            case UNDER_REVIEW:
                this.state = new UnderReviewState();
                break;
            case APPROVED:
                this.state = new ApprovedState();
                break;
            case REJECTED:
                this.state = new RejectedState();
                break;
            default:
                this.state = new PendingState();
        }
    }
}