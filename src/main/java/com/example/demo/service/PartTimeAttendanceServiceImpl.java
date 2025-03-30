package com.example.demo.service;

import com.example.demo.model.PartTimeAttendance;
import com.example.demo.repository.PartTimeAttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class PartTimeAttendanceServiceImpl implements PartTimeAttendanceService {

    @Autowired
    private PartTimeAttendanceRepository partTimeAttendanceRepository;
    
    @Override
    public List<PartTimeAttendance> getEmployeeAttendance(String employeeID) {
        // Get all attendance records for the part-time employee sorted by date in descending order
        return partTimeAttendanceRepository.findByEmployeeId(
                employeeID, 
                Sort.by(Sort.Direction.DESC, "date"));
    }
    
    @Override
    public List<PartTimeAttendance> getEmployeeAttendanceByMonth(String employeeID, LocalDate startDate, LocalDate endDate) {
        // Get all attendance records for the part-time employee for a specific month sorted by date
        return partTimeAttendanceRepository.findByEmployeeIdAndDateBetween(
                employeeID, 
                startDate, 
                endDate, 
                Sort.by(Sort.Direction.ASC, "date"));
    }
    
    @Override
    public PartTimeAttendance saveAttendance(PartTimeAttendance attendance) {
        return partTimeAttendanceRepository.save(attendance);
    }
    
    @Override
    public PartTimeAttendance recordLoginTime(String employeeID, LocalDate date, LocalTime loginTime) {
        // Check if an attendance record already exists for the employee on this date
        PartTimeAttendance attendance = partTimeAttendanceRepository.findByEmployeeIdAndDate(employeeID, date);
        
        if (attendance == null) {
            // Create a new attendance record if one doesn't exist
            attendance = new PartTimeAttendance();
            attendance.setEmployeeId(employeeID);
            attendance.setDate(date);
            attendance.setMonth(date.withDayOfMonth(1)); // Set to first day of month for grouping
            attendance.setStatus(PartTimeAttendance.AttendanceStatus.PRESENT);
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
        }
        
        // Set login time
        attendance.setLoginTime(loginTime);
        
        // Calculate total hours if logout time is already set
        if (attendance.getLogoutTime() != null) {
            attendance.calculateTotalHours();
        }
        
        // Save and return the attendance record
        return partTimeAttendanceRepository.save(attendance);
    }
    
    @Override
    public PartTimeAttendance recordLogoutTime(String employeeID, LocalDate date, LocalTime logoutTime) {
        // Check if an attendance record already exists for the employee on this date
        PartTimeAttendance attendance = partTimeAttendanceRepository.findByEmployeeIdAndDate(employeeID, date);
        
        if (attendance == null) {
            // Create a new attendance record if one doesn't exist
            attendance = new PartTimeAttendance();
            attendance.setEmployeeId(employeeID);
            attendance.setDate(date);
            attendance.setMonth(date.withDayOfMonth(1)); // Set to first day of month for grouping
            attendance.setStatus(PartTimeAttendance.AttendanceStatus.PRESENT);
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
        }
        
        // Set logout time
        attendance.setLogoutTime(logoutTime);
        
        // Calculate total hours if login time is already set
        if (attendance.getLoginTime() != null) {
            attendance.calculateTotalHours();
        }
        
        // Save and return the attendance record
        return partTimeAttendanceRepository.save(attendance);
    }
}