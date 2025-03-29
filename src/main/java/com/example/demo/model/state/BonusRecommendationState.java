package com.example.demo.model.state;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;

public interface BonusRecommendationState {
    void submit(BonusRecommendation recommendation);
    void review(BonusRecommendation recommendation);
    void approve(BonusRecommendation recommendation);
    void reject(BonusRecommendation recommendation, String reason);
    boolean canEdit();
    BonusRecommendationStatus getStatus();
}