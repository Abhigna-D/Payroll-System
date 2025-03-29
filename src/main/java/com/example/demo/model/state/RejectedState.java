package com.example.demo.model.state;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;

public class RejectedState implements BonusRecommendationState {
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
        throw new IllegalStateException("Cannot approve a rejected recommendation");
    }
    
    @Override
    public void reject(BonusRecommendation recommendation, String reason) {
        // Already rejected, update reason if needed
        recommendation.setRejectionReason(reason);
    }
    
    @Override
    public boolean canEdit() {
        return false; // Cannot edit once rejected
    }
    
    @Override
    public BonusRecommendationStatus getStatus() {
        return BonusRecommendationStatus.REJECTED;
    }
}