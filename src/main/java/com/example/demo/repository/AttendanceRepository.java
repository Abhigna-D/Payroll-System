package com.example.demo.repository;

import com.example.demo.model.Attendance;
import com.example.demo.model.AttendanceStatus;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Find all attendance records for an employee by employee ID directly
    List<Attendance> findByEmployeeId(String employeeId, org.springframework.data.domain.Sort sort);
    
    // Find all attendance records for an employee between two dates
    List<Attendance> findByEmployeeIdAndDateBetween(
            String employeeId, 
            LocalDate startDate, 
            LocalDate endDate, 
            org.springframework.data.domain.Sort sort);
     /**
     * Find attendance records by employee ID and month
     */
    List<Attendance> findByEmployeeIdAndMonth(String employeeId, LocalDate month);
    
    /**
     * Count days with present status for an employee in a date range
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId " +
           "AND a.date BETWEEN :startDate AND :endDate " +
           "AND a.status = :status")
    Integer countDaysByStatusInDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") AttendanceStatus status);
    
    /**
     * Get attendance summary for an employee for a specific month
     */
    @Query("SELECT a FROM Attendance a WHERE a.employeeId = :employeeId AND a.month = :month")
    Attendance findAttendanceSummaryByEmployeeIdAndMonth(
            @Param("employeeId") String employeeId,
            @Param("month") LocalDate month);
    
    /**
     * Calculate overtime hours based on days present exceeding standard working days
     */
    @Query("SELECT a.daysPresent - :standardWorkingDays FROM Attendance a " +
           "WHERE a.employeeId = :employeeId AND a.month = :month " +
           "AND a.daysPresent > :standardWorkingDays")
    Integer calculateOvertimeDays(
            @Param("employeeId") String employeeId,
            @Param("month") LocalDate month,
            @Param("standardWorkingDays") Integer standardWorkingDays);
}