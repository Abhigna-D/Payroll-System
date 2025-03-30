package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.User;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Department;
import com.example.demo.model.ERole;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hr")
public class HRController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
    // Create new employee instance
    Employee employee = new Employee();
    
    // If there's a user ID in flash attributes, retrieve that user
    if (model.containsAttribute("userId")) {
        Long userId = (Long) model.asMap().get("userId");
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                // Set the user in the employee object
                employee.setUser(userOpt.get());
                
                // Pre-fill some fields based on user data if appropriate
                // For example, you might set work email to the user's email
                employee.setWorkEmail(userOpt.get().getEmail());
            }
        } catch (Exception e) {
            // Log the error but continue
            System.err.println("Error retrieving user: " + e.getMessage());
        }
    }
    
    model.addAttribute("employee", employee);
    // Add any other attributes needed for the form (departments, etc.)
    
    return "hr/employee-form";
}
   /**
 * Handle add new employee form submission
 */
@PostMapping("/employees/add")
public String addEmployee(@ModelAttribute Employee employee, 
                         @RequestParam(required = false) Long userId,
                         @RequestParam(required = false) Long departmentId,
                         RedirectAttributes redirectAttributes) {
    try {
        // If user ID is provided but not set in employee
        if (userId != null && (employee.getUser() == null || employee.getUser().getId() == null)) {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                employee.setUser(userOpt.get());
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found with ID: " + userId);
                return "redirect:/hr/employees/new";
            }
        }
        
        // Handle department selection
        if (departmentId != null) {
            // Create a Department with just the ID set
            Department dept = new Department();
            dept.setId(departmentId);
            employee.setDepartment(dept);
        }
        
        // Generate an employee ID if not provided
        if (employee.getEmployeeID() == null || employee.getEmployeeID().isEmpty()) {
            // Generate a unique employee ID (e.g., EMP001, EMP002, etc.)
            long count = employeeService.getAllEmployees().size() + 1;
            employee.setEmployeeID("EMP" + String.format("%03d", count));
        }
        
        // Save the employee
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

    @PostMapping("/employees/update") 
public String updateEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
    try {
        System.out.println("Update request received for employee ID: " + employee.getEmployeeID());
        
        // Get the existing employee from the database
        Optional<Employee> existingEmpOpt = employeeService.getEmployeeByEmployeeID(employee.getEmployeeID());
        
        if (existingEmpOpt.isPresent()) {
            Employee existingEmp = existingEmpOpt.get();
            
            // Copy only the fields that should be updated
            existingEmp.setFullName(employee.getFullName());
            existingEmp.setJobTitle(employee.getJobTitle());
            existingEmp.setWorkEmail(employee.getWorkEmail());
            existingEmp.setWorkPhone(employee.getWorkPhone());
            
            // Now save the updated entity
            Employee updatedEmployee = employeeService.updateEmployee(existingEmp);
            
            System.out.println("Employee updated successfully: " + updatedEmployee.getEmployeeID());
            redirectAttributes.addFlashAttribute("success", "Employee updated successfully: " + updatedEmployee.getFullName());
        } else {
            System.out.println("Employee not found with ID: " + employee.getEmployeeID());
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employee.getEmployeeID());
        }
        
        return "redirect:/hr/employees";
    } catch (Exception e) {
        System.err.println("Error updating employee: " + e.getMessage());
        e.printStackTrace();
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
    
/**
 * Show list of available user accounts
 */
@GetMapping("/users")
public String showUsers(Model model) {
    List<User> users = userService.getAllUsers();
    
    // Add information about employee linkage to each user
    List<Employee> allEmployees = employeeService.getAllEmployees();
    
    for (User user : users) {
        boolean hasEmployee = false;
        String employeeId = null;
        
        // Find if this user has an associated employee
        for (Employee emp : allEmployees) {
            if (emp.getUser() != null && emp.getUser().getId() != null && 
                emp.getUser().getId().equals(user.getId())) {
                hasEmployee = true;
                employeeId = emp.getEmployeeID();
                break;
            }
        }
        
        // Add these attributes to the model for each user
        model.addAttribute("user_" + user.getId() + "_hasEmployee", hasEmployee);
        model.addAttribute("user_" + user.getId() + "_employeeId", employeeId);
    }
    
    model.addAttribute("users", users);
    return "hr/users";
}

/**
 * Show form to create a new user
 */
@GetMapping("/users/new")
public String showNewUserForm(Model model) {
    model.addAttribute("user", new User());
    return "hr/user-form";
}

/**
 * Process user creation and redirect to employee form
 */
@PostMapping("/users/add")
public String createUser(@ModelAttribute User user, 
                        @RequestParam(name = "roleType") String roleType,
                        RedirectAttributes redirectAttributes) {
    try {
        // Check if username or email already exists
        if (userService.existsByUsername(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists");
            return "redirect:/hr/users/new";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/hr/users/new";
        }
        
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Determine the role based on selection
        ERole selectedRole;
        switch (roleType) {
            case "EMPLOYEE":
                selectedRole = ERole.ROLE_EMPLOYEE;
                break;
            case "HR":
                selectedRole = ERole.ROLE_HR;
                break;
            case "MANAGER":
                selectedRole = ERole.ROLE_MANAGER;
                break;
            case "FINANCE":
                selectedRole = ERole.ROLE_FINANCE;
                break;
            default:
                selectedRole = ERole.ROLE_EMPLOYEE;
        }
        
        // Create the user with selected role
        User savedUser = userService.createUserWithRole(user, selectedRole);
        
        // Store user in flash attributes for the next request
        redirectAttributes.addFlashAttribute("success", "User account created successfully");
        redirectAttributes.addFlashAttribute("userId", savedUser.getId());
        
        // Redirect to the employee creation form
        return "redirect:/hr/employees/new";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        return "redirect:/hr/users/new";
    }
}
/**
 * Delete a user account
 */
@PostMapping("/users/delete/{id}")
public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        // First check if this user has an associated employee
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Find any associated employee
            List<Employee> employees = employeeService.getAllEmployees();
            for (Employee emp : employees) {
                if (emp.getUser() != null && emp.getUser().getId() != null && emp.getUser().getId().equals(id)) {
                    // Delete the employee first
                    employeeService.deleteEmployee(emp.getEmployeeID());
                    break;
                }
            }
            
            // Now delete the user
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User account deleted successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found with ID: " + id);
        }
        
        return "redirect:/hr/users";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        return "redirect:/hr/users";
    }
}
}