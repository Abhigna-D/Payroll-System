package com.example.demo.repository;

import com.example.demo.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
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
}