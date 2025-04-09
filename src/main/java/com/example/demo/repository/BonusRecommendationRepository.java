package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.model.BonusRecommendation;
import com.example.demo.model.BonusRecommendationStatus;
import com.example.demo.model.Employee;
import com.example.demo.model.User;

@Repository
public interface BonusRecommendationRepository extends JpaRepository<BonusRecommendation, Long> {
    List<BonusRecommendation> findByRecommendedBy(User user);
    List<BonusRecommendation> findByStatus(BonusRecommendationStatus status);
    List<BonusRecommendation> findByRecommendedByOrderByRecommendedDateDesc(User user);
    List<BonusRecommendation> findByStatusOrderByRecommendedDateDesc(BonusRecommendationStatus status);
    List<BonusRecommendation> findByStatusIn(List<BonusRecommendationStatus> statuses);
    /**
     * Find approved bonus recommendations for a specific employee within a date range
     * 
     * @param employee The employee for whom to find bonus recommendations
     * @param status The status of the bonus recommendations (typically APPROVED)
     * @param startDate The start date of the date range
     * @param endDate The end date of the date range
     * @return A list of bonus recommendations matching the criteria
     */
    @Query("SELECT b FROM BonusRecommendation b WHERE b.employee = :employee " +
           "AND b.status = :status " +
           "AND b.recommendedDate >= :startDate " +
           "AND b.recommendedDate <= :endDate " +
           "ORDER BY b.recommendedDate DESC")
           List<BonusRecommendation> findByEmployeeAndStatusAndDateRange(
            Employee employee,
            BonusRecommendationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
        );
        
    

}