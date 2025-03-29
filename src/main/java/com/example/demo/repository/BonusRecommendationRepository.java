package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.User;

@Repository
public interface BonusRecommendationRepository extends JpaRepository<BonusRecommendation, Long> {
    List<BonusRecommendation> findByRecommendedBy(User user);
    List<BonusRecommendation> findByStatus(BonusRecommendationStatus status);
    List<BonusRecommendation> findByRecommendedByOrderByRecommendedDateDesc(User user);
    List<BonusRecommendation> findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus status);
    List<BonusRecommendation> findByStatusIn(List<BonusRecommendationStatus> statuses);
    
}