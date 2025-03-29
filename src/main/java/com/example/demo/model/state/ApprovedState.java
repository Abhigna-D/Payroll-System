package com.example.demo.model.state;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;

public class ApprovedState implements BonusRecommendationState {
    @Override
    public void submit(BonusRecommendation recommendation) {
        throw new IllegalStateException("Recommendation already submitted");
    }
    
    @Override
    public void review(BonusRecommendation recommendation) {
        throw new IllegalStateException("Recommendation already reviewed");
    }
    
    @Override
    public void approve(BonusRecommendation recommendation) {
        // Already approved, do nothing
    }
    
    @Override
    public void reject(BonusRecommendation recommendation, String reason) {
        throw new IllegalStateException("Cannot reject an already approved recommendation");
    }
    
    @Override
    public boolean canEdit() {
        return false; // Cannot edit once approved
    }
    
    @Override
    public BonusRecommendationStatus getStatus() {
        return BonusRecommendationStatus.APPROVED;
    }
}