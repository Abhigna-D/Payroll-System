package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import com.example.demo.dto.BonusRecommendationDTO;
import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.User;
import com.example.demo.repository.BonusRecommendationRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.UserRepository;

@Service
public class BonusRecommendationService {
    private final BonusRecommendationRepository bonusRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    // Constructor injection
    public BonusRecommendationService(
            BonusRecommendationRepository bonusRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.bonusRepository = bonusRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    @Transactional
    public BonusRecommendation createRecommendation(BonusRecommendationDTO dto, String username) {
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        // Convert the Long to String
        Employee employee = employeeRepository.findByEmployeeID(String.valueOf(dto.getEmployeeId()))
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    
        BonusRecommendation recommendation = new BonusRecommendation();
        recommendation.setEmployee(employee);
        recommendation.setAmount(dto.getAmount());
        recommendation.setReason(dto.getReason());
        recommendation.setRecommendedBy(currentUser);
        
        // The state is set by the constructor to PENDING
        BonusRecommendation saved = bonusRepository.save(recommendation);
        
        // Notify HR about new recommendation
        notificationService.notifyHR(saved);
        
        return saved;
    }
    
    @Transactional
    public void reviewRecommendation(Long id) {
        BonusRecommendation recommendation = bonusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
        recommendation.review();
        bonusRepository.save(recommendation);
        
        // Notify manager that HR is reviewing
        notificationService.notifyManager(recommendation, "Your bonus recommendation is being reviewed by HR.");
    }
    
    @Transactional
    public void approveRecommendation(Long id) {
        BonusRecommendation recommendation = bonusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
        recommendation.approve();
        bonusRepository.save(recommendation);
        
        // Notify manager about approval
        notificationService.notifyManager(recommendation, "Your bonus recommendation has been approved by HR.");
    }
    
    @Transactional
    public void rejectRecommendation(Long id, String reason) {
        BonusRecommendation recommendation = bonusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Recommendation not found"));
            
        recommendation.reject(reason);
        bonusRepository.save(recommendation);
        
        // Notify manager about rejection
        notificationService.notifyManager(recommendation, "Your bonus recommendation has been rejected by HR: " + reason);
    }
    
    public List<BonusRecommendation> getRecommendationsByManager(String username) {
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
        return bonusRepository.findByRecommendedByOrderByRecommendedDateDesc(currentUser);
    }
    
    public List<BonusRecommendation> getPendingRecommendations() {
        return bonusRepository.findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus.PENDING);
    }
    public List<BonusRecommendation> getApprovedRecommendations() {
        return bonusRepository.findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus.APPROVED);
    }
    public List<BonusRecommendation> getUnderReviewRecommendations() {
        return bonusRepository.findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus.UNDER_REVIEW);

    }
    public List<BonusRecommendation> getRejectedRecommendations() {
        return bonusRepository.findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus.REJECTED);
    }
    
    public Optional<BonusRecommendation> getRecommendation(Long id) {
        return bonusRepository.findById(id);
    }
    
}