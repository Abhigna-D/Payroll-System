package com.example.demo.service;

import com.example.demo.model.Attendance;
import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.Tax;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.SalarySlipRepository;
import com.example.demo.repository.TaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private SalarySlipRepository salarySlipRepository;
    
    @Autowired
    private TaxRepository taxRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> getEmployeeByEmployeeID(String employeeID) {
        return employeeRepository.findByEmployeeID(employeeID);
    }

    @Override
    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteEmployee(String employeeID) {
        employeeRepository.findByEmployeeID(employeeID).ifPresent(employee -> 
            employeeRepository.delete(employee));
    }

    @Override
    public List<Employee> getEmployeesByDepartment(int deptId) {
        // You might need to modify this method if your Department entity has a different ID type
        try {
            // Try to use the existing method if available
            return employeeRepository.findByDepartmentId(deptId);
        } catch (Exception e) {
            // Fallback method (customize based on your actual model)
            return Collections.emptyList();
        }
    }

    @Override
    public SalarySlip viewSalaryDetails(String employeeID) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeID(employeeID);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            return employee.getSalaryDetails();
        }
        return null;
    }

    @Override
    @Transactional
    public Tax submitTaxDetails(String employeeID, Tax taxDetails) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeID(employeeID);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            taxDetails.setEmployee(employee);
            Tax savedTax = taxRepository.save(taxDetails);
            employee.setTaxDetails(savedTax);  // Updated to use setter instead of method
            employeeRepository.save(employee);
            return savedTax;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean requestSalaryCorrection(String employeeID, SalarySlip correctedSalary) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeID(employeeID);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            
            // Here you would implement the business logic for salary correction requests
            // For this example, we'll just update the salary details
            SalarySlip currentSalary = employee.getSalaryDetails();
            if (currentSalary != null) {
                currentSalary.setBasicSalary(correctedSalary.getBasicSalary());
                currentSalary.setAllowances(correctedSalary.getAllowances());
                currentSalary.setDeductions(correctedSalary.getDeductions());
                currentSalary.setNetSalary(correctedSalary.getNetSalary());
                salarySlipRepository.save(currentSalary);
            } else {
                correctedSalary.setEmployee(employee);
                salarySlipRepository.save(correctedSalary);
                employee.setSalaryDetails(correctedSalary);
                employeeRepository.save(employee);
            }
            
            // In a real application, you might create a separate entity to track correction requests
            // For now, we'll just return true to indicate success
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean submitTimesheet(String employeeID, String details) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeID(employeeID);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            
            // Parse details and create attendance record
            // In a real system, you would parse the details string more thoroughly
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setMonth(LocalDate.now());
            
            // Just an example - in a real system you'd parse these from the details
            attendance.setDaysPresent(22);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            
            Attendance savedAttendance = attendanceRepository.save(attendance);
            employee.setAttendance(savedAttendance);  // Updated to use setter instead of method
            employeeRepository.save(employee);
            
            return true;
        }
        return false;
    }
}