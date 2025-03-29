package com.example.demo.model.state;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;

public class UnderReviewState implements BonusRecommendationState {
    @Override
    public void submit(BonusRecommendation recommendation) {
        throw new IllegalStateException("Recommendation already submitted");
    }
    
    @Override
    public void review(BonusRecommendation recommendation) {
        // Already under review, do nothing
    }
    
    @Override
    public void approve(BonusRecommendation recommendation) {
        recommendation.setState(new ApprovedState());
        // Additional logic like notifications
    }
    
    @Override
    public void reject(BonusRecommendation recommendation, String rejectReason) {
        recommendation.setRejectionReason(rejectReason);
        recommendation.setState(new RejectedState());
        // Store rejection reason and notify
    }
    
    @Override
    public boolean canEdit() {
        return false; // Cannot edit while under review
    }
    
    @Override
    public BonusRecommendationStatus getStatus() {
        return BonusRecommendationStatus.UNDER_REVIEW;
    }
}