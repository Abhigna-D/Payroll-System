package com.example.demo.service;

import com.example.demo.model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    
    /**
     * Get all attendance records for an employee
     * 
     * @param employeeID The employee ID
     * @return List of attendance records
     */
    List<Attendance> getEmployeeAttendance(String employeeID);
    
    /**
     * Get all attendance records for an employee for a specific month
     * 
     * @param employeeID The employee ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of attendance records
     */
    List<Attendance> getEmployeeAttendanceByMonth(String employeeID, LocalDate startDate, LocalDate endDate);
    
    /**
     * Save a new attendance record
     * 
     * @param attendance The attendance record to save
     * @return The saved attendance record
     */
    Attendance saveAttendance(Attendance attendance);
}