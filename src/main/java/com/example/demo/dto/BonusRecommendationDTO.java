package com.example.demo.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class BonusRecommendationDTO {
    @NotNull(message = "Employee ID is required")
    private String employeeId; // Changed from Long to String
    
    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Bonus must be at least ₹1,000")
    private BigDecimal amount;
    
    // Update getter and setter to use String
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}