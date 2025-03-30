package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.PartTimeAttendance;
import com.example.demo.model.OvertimeRequest;
import com.example.demo.service.PartTimeAttendanceService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.OvertimeRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PartTimeAttendanceController {

    @Autowired
    private PartTimeAttendanceService partTimeAttendanceService;

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private OvertimeRequestService overtimeRequestService;

    /**
     * Display part-time employee attendance page (view-only)
     */
    @GetMapping("/employee/parttime-attendance")
    public String viewPartTimeAttendance(
            @RequestParam(name = "month", required = false) String monthStr,
            Model model) {
        
        try {
            // Get logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get employee details
            Employee employee = employeeService.findByUsername(username);
            
            // If employee is null or not part-time, redirect to error page
            if (employee == null || !employee.isPartTime()) {
                return "redirect:/error";
            }
            
            // Set default month to current month if not specified
            YearMonth selectedMonth;
            if (monthStr == null || monthStr.isEmpty()) {
                selectedMonth = YearMonth.now();
            } else {
                // Parse the month string (format: yyyy-MM)
                selectedMonth = YearMonth.parse(monthStr);
            }
            
            // Get part-time attendance records for the employee for the selected month
            List<PartTimeAttendance> attendanceRecords = partTimeAttendanceService.getEmployeeAttendanceByMonth(
                    employee.getEmployeeID(), 
                    selectedMonth.atDay(1), 
                    selectedMonth.atEndOfMonth());
            
            // Calculate attendance statistics
            int daysPresent = 0;
            int daysAbsent = 0;
            int daysLeave = 0;
            int daysWorkFromHome = 0;
            int daysSickLeave = 0;
            int daysHalfDay = 0;
            double totalHoursWorked = 0;
            
            for (PartTimeAttendance attendance : attendanceRecords) {
                if (attendance.getStatus() != null) {
                    switch (attendance.getStatus()) {
                        case PRESENT:
                            daysPresent++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                        case ABSENT:
                            daysAbsent++;
                            break;
                        case SICK_LEAVE:
                            daysSickLeave++;
                            daysLeave++;
                            break;
                        case WORK_FROM_HOME:
                            daysWorkFromHome++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                        case HALF_DAY:
                            daysHalfDay++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                    }
                }
            }
            
            // Format selected month for display
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            String formattedMonth = selectedMonth.format(formatter);
            
            // Get recent overtime requests
            List<OvertimeRequest> recentOvertimeRequests = overtimeRequestService
                    .getEmployeeOvertimeRequests(employee.getEmployeeID());
            
            // Add to model
            model.addAttribute("employee", employee);
            model.addAttribute("attendanceRecords", attendanceRecords);
            model.addAttribute("selectedMonth", selectedMonth);
            model.addAttribute("formattedMonth", formattedMonth);
            model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
            model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
            model.addAttribute("currentMonth", YearMonth.now());
            model.addAttribute("recentOvertimeRequests", recentOvertimeRequests);
            
            // Add statistics
            model.addAttribute("daysPresent", daysPresent);
            model.addAttribute("daysAbsent", daysAbsent);
            model.addAttribute("daysLeave", daysLeave);
            model.addAttribute("daysWorkFromHome", daysWorkFromHome);
            model.addAttribute("daysSickLeave", daysSickLeave);
            model.addAttribute("daysHalfDay", daysHalfDay);
            model.addAttribute("totalHoursWorked", totalHoursWorked);
            
            return "employee/parttime-attendance";
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    
    /**
     * Admin endpoint to view part-time employee attendance
     */
    @GetMapping("/hr/parttime-attendance/{employeeId}")
    public String viewEmployeePartTimeAttendance(
            @PathVariable("employeeId") String employeeId,
            @RequestParam(name = "month", required = false) String monthStr,
            Model model) {
        
        try {
            // Get employee details
            Employee employee = employeeService.findByEmployeeId(employeeId);
            
            // If employee is null or not part-time, redirect to error page
            if (employee == null || !employee.isPartTime()) {
                return "redirect:/error";
            }
            
            // Set default month to current month if not specified
            YearMonth selectedMonth;
            if (monthStr == null || monthStr.isEmpty()) {
                selectedMonth = YearMonth.now();
            } else {
                // Parse the month string (format: yyyy-MM)
                selectedMonth = YearMonth.parse(monthStr);
            }
            
            // Get part-time attendance records for the employee for the selected month
            List<PartTimeAttendance> attendanceRecords = partTimeAttendanceService.getEmployeeAttendanceByMonth(
                    employee.getEmployeeID(), 
                    selectedMonth.atDay(1), 
                    selectedMonth.atEndOfMonth());
            
            // Calculate attendance statistics
            int daysPresent = 0;
            int daysAbsent = 0;
            int daysLeave = 0;
            int daysWorkFromHome = 0;
            int daysSickLeave = 0;
            int daysHalfDay = 0;
            double totalHoursWorked = 0;
            
            for (PartTimeAttendance attendance : attendanceRecords) {
                if (attendance.getStatus() != null) {
                    switch (attendance.getStatus()) {
                        case PRESENT:
                            daysPresent++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                        case ABSENT:
                            daysAbsent++;
                            break;
                        case SICK_LEAVE:
                            daysSickLeave++;
                            daysLeave++;
                            break;
                        case WORK_FROM_HOME:
                            daysWorkFromHome++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                        case HALF_DAY:
                            daysHalfDay++;
                            if (attendance.getTotalHours() != null) {
                                totalHoursWorked += attendance.getTotalHours();
                            }
                            break;
                    }
                }
            }
            
            // Format selected month for display
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            String formattedMonth = selectedMonth.format(formatter);
            
            // Get recent overtime requests
            List<OvertimeRequest> recentOvertimeRequests = overtimeRequestService
                    .getEmployeeOvertimeRequests(employee.getEmployeeID());
            
            // Add to model
            model.addAttribute("employee", employee);
            model.addAttribute("attendanceRecords", attendanceRecords);
            model.addAttribute("selectedMonth", selectedMonth);
            model.addAttribute("formattedMonth", formattedMonth);
            model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
            model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
            model.addAttribute("currentMonth", YearMonth.now());
            model.addAttribute("recentOvertimeRequests", recentOvertimeRequests);
            
            // Add statistics
            model.addAttribute("daysPresent", daysPresent);
            model.addAttribute("daysAbsent", daysAbsent);
            model.addAttribute("daysLeave", daysLeave);
            model.addAttribute("daysWorkFromHome", daysWorkFromHome);
            model.addAttribute("daysSickLeave", daysSickLeave);
            model.addAttribute("daysHalfDay", daysHalfDay);
            model.addAttribute("totalHoursWorked", totalHoursWorked);
            
            return "hr/parttime-attendance";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
}