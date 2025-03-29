package com.example.demo.model.state;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;

public class PendingState implements BonusRecommendationState {
    @Override
    public void submit(BonusRecommendation recommendation) {
        // Already submitted, do nothing
    }
    
    @Override
    public void review(BonusRecommendation recommendation) {
        recommendation.setState(new UnderReviewState());
        // Additional logic like notifications could go here
    }
    
    @Override
    public void approve(BonusRecommendation recommendation) {
        throw new IllegalStateException("Cannot approve a recommendation that hasn't been reviewed");
    }
    
    @Override
    public void reject(BonusRecommendation recommendation, String reason) {
        throw new IllegalStateException("Cannot reject a recommendation that hasn't been reviewed");
    }
    
    @Override
    public boolean canEdit() {
        return true; // Pending recommendations can be edited
    }
    
    @Override
    public BonusRecommendationStatus getStatus() {
        return BonusRecommendationStatus.PENDING;
    }
}