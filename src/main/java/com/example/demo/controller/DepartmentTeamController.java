package com.example.demo.controller;

import com.example.demo.model.DepartmentManager;
import com.example.demo.model.Employee;
import com.example.demo.service.DepartmentManagerService;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class DepartmentTeamController {

    @Autowired
    private DepartmentManagerService departmentManagerService;
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * View all employees in the department (team)
     */
    @GetMapping("/department/team")
    public String viewDepartmentTeam(Model model) {
        try {
            // Get logged in manager
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            // Get department manager details
            DepartmentManager manager = departmentManagerService.findByUsername(username);
            
            // If manager is null, redirect to error page
            if (manager == null) {
                return "redirect:/error";
            }
            
            // Get all employees in the department
            List<Employee> departmentEmployees = employeeService.findByDepartmentId(manager.getDepartment().getId());
            
            // Add to model
            model.addAttribute("deptManager", manager);
            model.addAttribute("employees", departmentEmployees);
            model.addAttribute("department", manager.getDepartment());
            
            return "department/team";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }
    /**
 * Get employee details for modal
 */
@GetMapping("/department/employee/{id}")
@ResponseBody
public ResponseEntity<?> getEmployeeDetails(@PathVariable String id) {
    try {
        // Get logged in manager
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Get department manager details
        DepartmentManager manager = departmentManagerService.findByUsername(username);
        
        // If manager is null, return error
        if (manager == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Unauthorized access"));
        }
        
        // Get the employee
        Employee employee = employeeService.findByEmployeeId(id);
        
        // If employee not found or not in manager's department, return error
        if (employee == null || !employee.getDepartment().getId().equals(manager.getDepartment().getId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Employee not found in your department"));
        }
        
        // Create a map with employee details
        Map<String, Object> employeeDetails = new HashMap<>();
        employeeDetails.put("id", employee.getEmployeeID());
        employeeDetails.put("name", employee.getFullName());
        employeeDetails.put("jobTitle", employee.getJobTitle());
        employeeDetails.put("workEmail", employee.getWorkEmail());
        employeeDetails.put("personalEmail", employee.getPersonalEmail());
        employeeDetails.put("workPhone", employee.getWorkPhone());
        employeeDetails.put("personalPhone", employee.getPersonalPhone());
        employeeDetails.put("gender", employee.getGender());
        employeeDetails.put("nationality", employee.getNationality());
        employeeDetails.put("address", employee.getAddress());
        employeeDetails.put("type", employee.getEmployeeType());
        
        // Format dates
        if (employee.getDateOfBirth() != null) {
            employeeDetails.put("dob", employee.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }
        
        if (employee.getJoiningDate() != null) {
            employeeDetails.put("joiningDate", employee.getJoiningDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }
        
        // Emergency contact details
        employeeDetails.put("emergencyName", employee.getEmergencyContactName());
        employeeDetails.put("emergencyRelation", employee.getEmergencyContactRelationship());
        employeeDetails.put("emergencyPhone", employee.getEmergencyContactPhone());
        
        // Department
        employeeDetails.put("departmentName", employee.getDepartment().getName());
        
        return ResponseEntity.ok(employeeDetails);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
    }
}
}