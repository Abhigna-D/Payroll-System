package com.example.demo.service;

import com.example.demo.model.PartTimeAttendance;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PartTimeAttendanceService {
    
    /**
     * Get all attendance records for a part-time employee
     * 
     * @param employeeID The employee ID
     * @return List of part-time attendance records
     */
    List<PartTimeAttendance> getEmployeeAttendance(String employeeID);
    
    /**
     * Get all attendance records for a part-time employee for a specific month
     * 
     * @param employeeID The employee ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of part-time attendance records
     */
    List<PartTimeAttendance> getEmployeeAttendanceByMonth(String employeeID, LocalDate startDate, LocalDate endDate);
    
    /**
     * Save a new part-time attendance record
     * 
     * @param attendance The part-time attendance record to save
     * @return The saved part-time attendance record
     */
    PartTimeAttendance saveAttendance(PartTimeAttendance attendance);
    
    /**
     * Record login time for a part-time employee
     * 
     * @param employeeID The employee ID
     * @param date The date
     * @param loginTime The login time
     * @return The updated part-time attendance record
     */
    PartTimeAttendance recordLoginTime(String employeeID, LocalDate date, LocalTime loginTime);
    
    /**
     * Record logout time for a part-time employee
     * 
     * @param employeeID The employee ID
     * @param date The date
     * @param logoutTime The logout time
     * @return The updated part-time attendance record
     */
    PartTimeAttendance recordLogoutTime(String employeeID, LocalDate date, LocalTime logoutTime);
}