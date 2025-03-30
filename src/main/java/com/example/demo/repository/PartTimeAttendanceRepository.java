package com.example.demo.repository;

import com.example.demo.model.PartTimeAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartTimeAttendanceRepository extends JpaRepository<PartTimeAttendance, Long> {
    // Find all attendance records for a part-time employee by employee ID
    List<PartTimeAttendance> findByEmployeeId(String employeeId, org.springframework.data.domain.Sort sort);
    
    // Find all attendance records for a part-time employee between two dates
    List<PartTimeAttendance> findByEmployeeIdAndDateBetween(
            String employeeId, 
            LocalDate startDate, 
            LocalDate endDate, 
            org.springframework.data.domain.Sort sort);
            
    // Find attendance record for a specific date and employee
    PartTimeAttendance findByEmployeeIdAndDate(String employeeId, LocalDate date);
    
}