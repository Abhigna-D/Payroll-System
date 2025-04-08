package com.example.demo.service;

import com.example.demo.model.Attendance;
import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.TaxDeclarationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    
    @Autowired
    private TaxDeclarationRepository taxDeclarationRepository;
    
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
        Employee employee = employeeRepository.findByEmployeeID(employeeID)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employeeRepository.delete(employee);
    }

    @Override
    public List<Employee> getEmployeesByDepartment(int deptId) {
        return employeeRepository.findByDepartmentId(deptId);
    }

    @Override
    public SalarySlip viewSalaryDetails(String employeeID) {
        return employeeRepository.findByEmployeeID(employeeID)
                .map(Employee::getSalaryDetails)
                .orElse(null);
    }

    @Override
    @Transactional
    public TaxDeclaration submitTaxDetails(String employeeID, TaxDeclaration taxDetails) {
        return employeeRepository.findByEmployeeID(employeeID).map(employee -> {
            taxDetails.setEmployee(employee);
            TaxDeclaration savedTax = taxDeclarationRepository.save(taxDetails);
            // Note: We need to update the Employee class to include a setTaxDeclaration method
            // employee.setTaxDeclaration(savedTax);
            employeeRepository.save(employee);
            return savedTax;
        }).orElse(null);
    }

    

    @Override
    @Transactional
    public boolean submitTimesheet(String employeeID, String details) {
        return employeeRepository.findByEmployeeID(employeeID).map(employee -> {
            // Create new attendance record
            Attendance attendance = new Attendance();
            
            // Set employee ID directly instead of using the setEmployee method
            attendance.setEmployeeId(employee.getEmployeeID());
            attendance.setDate(LocalDate.now());
            attendance.setMonth(LocalDate.now());

            // Set default values
            attendance.setDaysPresent(22);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            attendance.setStatus(Attendance.AttendanceStatus.PRESENT);

            // Save the attendance record
            Attendance savedAttendance = attendanceRepository.save(attendance);
            
            // If attendanceRecords list is null, initialize it
            if (employee.getAttendanceRecords() == null) {
                employee.setAttendanceRecords(new java.util.ArrayList<>());
            }
            
            // Add the new attendance record to the employee's list
            employee.getAttendanceRecords().add(savedAttendance);
            employeeRepository.save(employee);
            
            return true;
        }).orElse(false);
    }

    @Override
    public Employee findByUsername(String username) {
        // Changed to return Employee directly instead of Optional<Employee>
        // This avoids issues with the nested entity loading
        try {
            return employeeRepository.findByUser_Username(username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Implement the missing method findByEmployeeId
    @Override
    public Employee findByEmployeeId(String employeeId) {
        Optional<Employee> employee = employeeRepository.findByEmployeeID(employeeId);
        return employee.orElse(null);
    }
    
    // Implement the missing method findByDepartmentId
    @Override
    public List<Employee> findByDepartmentId(Long departmentId) {
        try {
            // Handle potential type mismatch - your existing method uses int, new one uses Long
            if (departmentId == null) {
                return List.of();
            }
            return employeeRepository.findByDepartmentId(departmentId.intValue());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}