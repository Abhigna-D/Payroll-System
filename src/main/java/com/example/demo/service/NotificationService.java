package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.model.BonusRecommendation;

@Service
public class NotificationService {
    
    // In a real application, this would use email, messaging, or in-app notifications
    
    public void notifyHR(BonusRecommendation recommendation) {
        // Logic to notify HR about new recommendation
        System.out.println("Notification to HR: New bonus recommendation submitted for " 
                + recommendation.getEmployee().getFullName());
    }
    
    public void notifyManager(BonusRecommendation recommendation, String message) {
        // Logic to notify the manager about recommendation status
        System.out.println("Notification to " + recommendation.getRecommendedBy().getUsername() 
                + ": " + message);
    }
}