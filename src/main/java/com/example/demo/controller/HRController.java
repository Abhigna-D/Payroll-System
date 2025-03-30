package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hr")
public class HRController {

    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Display list of all employees
     */
    @GetMapping("/employees")
    public String showEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String jobTitle,
            Model model) {
        
        // Get all employees (filtering would be implemented in a real app)
        List<Employee> employees = employeeService.getAllEmployees();
        
        // Add to model
        model.addAttribute("employees", employees);
        
        return "hr/employees";
    }

    /**
     * Display single employee details
     */
    @GetMapping("/employees/view/{id}")
    public String viewEmployee(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(id);
        
        if (employeeOpt.isPresent()) {
            model.addAttribute("employee", employeeOpt.get());
            return "hr/employee-view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + id);
            return "redirect:/hr/employees";
        }
    }

    /**
     * Show form to add new employee
     */
    @GetMapping("/employees/new")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "hr/employee-form";
    }

    /**
     * Handle add new employee form submission
     */
    @PostMapping("/employees/add")
    public String addEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            // Save the new employee
            Employee savedEmployee = employeeService.createEmployee(employee);
            redirectAttributes.addFlashAttribute("success", "Employee created successfully with ID: " + savedEmployee.getEmployeeID());
            return "redirect:/hr/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating employee: " + e.getMessage());
            return "redirect:/hr/employees/new";
        }
    }

    /**
     * Show form to edit existing employee
     */
    @GetMapping("/employees/edit/{id}")
    public String showEditEmployeeForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(id);
        
        if (employeeOpt.isPresent()) {
            model.addAttribute("employee", employeeOpt.get());
            return "hr/employee-form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + id);
            return "redirect:/hr/employees";
        }
    }

    /**
     * Handle edit employee form submission
     */
    @PostMapping("/employees/update")
    public String updateEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        try {
            // Check if employee exists
            Optional<Employee> existingEmp = employeeService.getEmployeeByEmployeeID(employee.getEmployeeID());
            
            if (existingEmp.isPresent()) {
                // Update the employee details
                Employee updatedEmployee = employeeService.updateEmployee(employee);
                redirectAttributes.addFlashAttribute("success", "Employee updated successfully: " + updatedEmployee.getFullName());
            } else {
                redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employee.getEmployeeID());
            }
            
            return "redirect:/hr/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating employee: " + e.getMessage());
            return "redirect:/hr/employees/edit/" + employee.getEmployeeID();
        }
    }

    /**
     * Handle employee deletion
     */
    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(id);
            
            if (employeeOpt.isPresent()) {
                // Delete the employee
                employeeService.deleteEmployee(id);
                redirectAttributes.addFlashAttribute("success", "Employee deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + id);
            }
            
            return "redirect:/hr/employees";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting employee: " + e.getMessage());
            return "redirect:/hr/employees";
        }
    }
}