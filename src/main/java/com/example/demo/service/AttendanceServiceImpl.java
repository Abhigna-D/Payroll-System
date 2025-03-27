package com.example.demo.service;

import com.example.demo.model.Attendance;
import com.example.demo.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Override
    public List<Attendance> getEmployeeAttendance(String employeeID) {
        // Get all attendance records for the employee sorted by date in descending order
        return attendanceRepository.findByEmployeeId(
                employeeID, 
                Sort.by(Sort.Direction.DESC, "date"));
    }
    
    @Override
    public List<Attendance> getEmployeeAttendanceByMonth(String employeeID, LocalDate startDate, LocalDate endDate) {
        // Get all attendance records for the employee for a specific month sorted by date
        return attendanceRepository.findByEmployeeIdAndDateBetween(
                employeeID, 
                startDate, 
                endDate, 
                Sort.by(Sort.Direction.ASC, "date"));
    }
    
    @Override
    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }
}