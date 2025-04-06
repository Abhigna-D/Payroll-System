package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.PartTimeAttendance;
import com.example.demo.model.User;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.PartTimeAttendanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Attendance;

import com.example.demo.model.Department;
import com.example.demo.model.ERole;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private DepartmentService departmentService;

     // Inject PartTimeAttendanceService
     @Autowired
     private PartTimeAttendanceService partTimeAttendanceService;
    
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


// Then modify the addEmployee method to remove the username and designation references:

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
                User user = userOpt.get();
                employee.setUser(user);
                
                // Set email from user if not provided
                if (employee.getWorkEmail() == null || employee.getWorkEmail().isEmpty()) {
                    employee.setWorkEmail(user.getEmail());
                }
                
                // Remove the username setter - it doesn't exist in Employee class
                // employee.setUsername(user.getUsername());
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
            
            // Set department name if available
            Optional<Department> deptOpt = departmentService.getDepartmentById(departmentId);
            if (deptOpt.isPresent()) {
                employee.setDepartment(deptOpt.get());
            }
        }
        
        // Generate an employee ID if not provided
        if (employee.getEmployeeID() == null || employee.getEmployeeID().isEmpty()) {
            // Generate a unique employee ID (e.g., EMP001, EMP002, etc.)
            long count = employeeService.getAllEmployees().size() + 1;
            employee.setEmployeeID("EMP" + String.format("%03d", count));
        }
        
        // Set default values for empty fields
        setDefaultValuesIfEmpty(employee);
        
        // Save the employee
        Employee savedEmployee = employeeService.createEmployee(employee);
        
        redirectAttributes.addFlashAttribute("success", "Employee created successfully with ID: " + savedEmployee.getEmployeeID());
        return "redirect:/hr/employees";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error creating employee: " + e.getMessage());
        return "redirect:/hr/employees/new";
    }
}

// And update the setDefaultValuesIfEmpty method to remove references to designation:

private void setDefaultValuesIfEmpty(Employee employee) {
    // Set joining date if not provided
    if (employee.getJoiningDate() == null) {
        employee.setJoiningDate(LocalDate.now());
    }
    
    // Set employee type if not provided
    if (employee.getEmployeeType() == null || employee.getEmployeeType().isEmpty()) {
        employee.setEmployeeType("Full-time");
    }
    
    // Set job title placeholder if not provided
    if (employee.getJobTitle() == null || employee.getJobTitle().isEmpty()) {
        employee.setJobTitle("New Employee");
    }
    
    // Set office location if not provided
    if (employee.getOfficeLocation() == null || employee.getOfficeLocation().isEmpty()) {
        employee.setOfficeLocation("Main Office");
    }
    
    // Set work schedule if not provided
    if (employee.getWorkSchedule() == null || employee.getWorkSchedule().isEmpty()) {
        employee.setWorkSchedule("9am-5pm");
    }
    
    // Remove these lines as designation doesn't exist in Employee class
    // if (employee.getDesignation() == null || employee.getDesignation().isEmpty()) {
    //     employee.setDesignation(employee.getJobTitle());
    // }
    
    // If the full name is set but first/last name aren't, split the full name
    if (employee.getFullName() != null && !employee.getFullName().isEmpty()) {
        // If your Employee class has firstName/lastName fields, set them here
        // This depends on your Employee class implementation
    }
    
    // Initialize empty strings for text fields to avoid null values
    if (employee.getAddress() == null) employee.setAddress("");
    if (employee.getPersonalEmail() == null) employee.setPersonalEmail("");
    if (employee.getPersonalPhone() == null) employee.setPersonalPhone("");
    if (employee.getEmergencyContactName() == null) employee.setEmergencyContactName("");
    if (employee.getEmergencyContactPhone() == null) employee.setEmergencyContactPhone("");
    if (employee.getEmergencyContactRelationship() == null) employee.setEmergencyContactRelationship("");
    if (employee.getNationality() == null) employee.setNationality("");
    if (employee.getGender() == null) employee.setGender("");
    if (employee.getReportingManager() == null) employee.setReportingManager("");
    if (employee.getWorkPhone() == null) employee.setWorkPhone("");
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


/*// Update the URL mapping to match the standard pattern
@GetMapping("/attendance/employee/{employeeId}")
public String viewEmployeeAttendance(
        @PathVariable String employeeId,
        @RequestParam(name = "month", required = false) String monthStr,
        Model model,
        RedirectAttributes redirectAttributes) {
    
    try {
        // Get employee details
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeId);
        
        // If employee is not found, redirect to employees list
        if (employeeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employeeId);
            return "redirect:/hr/employees";
        }
        
        Employee employee = employeeOpt.get();
        
        // Set default month to current month if not specified
        YearMonth selectedMonth;
        if (monthStr == null || monthStr.isEmpty()) {
            selectedMonth = YearMonth.now();
        } else {
            // Parse the month string (format: yyyy-MM)
            selectedMonth = YearMonth.parse(monthStr);
        }
        
        // Get attendance records for the employee for the selected month
        List<Attendance> attendanceRecords = attendanceService.getEmployeeAttendanceByMonth(
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
        
        for (Attendance attendance : attendanceRecords) {
            if (attendance.getStatus() != null) {
                // Use the correct enum type from Attendance class
                switch (attendance.getStatus()) {
                    case PRESENT:
                        daysPresent++;
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
                        break;
                    case HALF_DAY:
                        daysHalfDay++;
                        break;
                }
            }
        }
        
        // Format selected month for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String formattedMonth = selectedMonth.format(formatter);
        
        // Add to model
        model.addAttribute("employee", employee);
        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("formattedMonth", formattedMonth);
        model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
        model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
        model.addAttribute("currentMonth", YearMonth.now());
        
        // Add statistics
        model.addAttribute("daysPresent", daysPresent);
        model.addAttribute("daysAbsent", daysAbsent);
        model.addAttribute("daysLeave", daysLeave);
        model.addAttribute("daysWorkFromHome", daysWorkFromHome);
        model.addAttribute("daysSickLeave", daysSickLeave);
        model.addAttribute("daysHalfDay", daysHalfDay);
        
        // Add all possible attendance statuses to model for dropdown
        // Use the enum from Attendance class
        model.addAttribute("attendanceStatuses", Attendance.AttendanceStatus.values());
        
        return "hr/employee-attendance";
    } catch (Exception e) {
        // Log the exception
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error viewing employee attendance: " + e.getMessage());
        return "redirect:/hr/employees";
    }
}
*/

@GetMapping("/attendance/employee/{employeeId}")
public String viewEmployeeAttendance(
        @PathVariable String employeeId,
        @RequestParam(name = "month", required = false) String monthStr,
        Model model,
        RedirectAttributes redirectAttributes) {

    try {
        // Get employee details
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeId);

        // If employee not found, redirect
        if (employeeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employeeId);
            return "redirect:/hr/employees";
        }

        Employee employee = employeeOpt.get();

        if (employee.isPartTime()) {
            return "redirect:/hr/attendance/parttime/" + employeeId + 
                  (monthStr != null ? "?month=" + monthStr : "");
        }


        // Set default month to current month if not specified
        YearMonth selectedMonth = (monthStr == null || monthStr.isEmpty()) 
                                  ? YearMonth.now() 
                                  : YearMonth.parse(monthStr);

        List<Attendance> attendanceRecords;
        int daysPresent = 0, daysAbsent = 0, daysLeave = 0;
        int daysWorkFromHome = 0, daysSickLeave = 0, daysHalfDay = 0;
        double totalHoursWorked = 0;

        // Check if the employee is part-time or full-time
        if (employee.isPartTime()) {
            // Fetch part-time attendance
            List<PartTimeAttendance> partTimeRecords = partTimeAttendanceService.getEmployeeAttendanceByMonth(
                    employee.getEmployeeID(),
                    selectedMonth.atDay(1),
                    selectedMonth.atEndOfMonth()
            );

            // Calculate statistics for part-time attendance
            for (PartTimeAttendance attendance : partTimeRecords) {
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

            model.addAttribute("attendanceRecords", partTimeRecords); // Add part-time records

        } else {
            // Fetch full-time attendance
            attendanceRecords = attendanceService.getEmployeeAttendanceByMonth(
                    employee.getEmployeeID(),
                    selectedMonth.atDay(1),
                    selectedMonth.atEndOfMonth()
            );

            // Calculate statistics for full-time attendance
            for (Attendance attendance : attendanceRecords) {
                if (attendance.getStatus() != null) {
                    switch (attendance.getStatus()) {
                        case PRESENT:
                            daysPresent++;
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
                            break;
                        case HALF_DAY:
                            daysHalfDay++;
                            break;
                    }
                }
            }

            model.addAttribute("attendanceRecords", attendanceRecords); // Add full-time records
        }

        // Format month for display
        String formattedMonth = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        // Add attributes to model
        model.addAttribute("employee", employee);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("formattedMonth", formattedMonth);
        model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
        model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
        model.addAttribute("currentMonth", YearMonth.now());

        // Add statistics
        model.addAttribute("daysPresent", daysPresent);
        model.addAttribute("daysAbsent", daysAbsent);
        model.addAttribute("daysLeave", daysLeave);
        model.addAttribute("daysWorkFromHome", daysWorkFromHome);
        model.addAttribute("daysSickLeave", daysSickLeave);
        model.addAttribute("daysHalfDay", daysHalfDay);
        model.addAttribute("totalHoursWorked", totalHoursWorked);

        // Enum for dropdown
        model.addAttribute("attendanceStatuses", Attendance.AttendanceStatus.values());

        return "hr/employee-attendance";

    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error viewing employee attendance: " + e.getMessage());
        return "redirect:/hr/employees";
    }
}

/**
 * Handle add/update attendance for an employee
 */
@PostMapping("/attendance/update")
public String updateEmployeeAttendance(
        @RequestParam String employeeId,
        @RequestParam LocalDate date,
        @RequestParam Attendance.AttendanceStatus status,  // Use the correct enum type
        RedirectAttributes redirectAttributes) {
    
    try {
        // Get employee
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeId);
        
        if (employeeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Employee not found with ID: " + employeeId);
            return "redirect:/hr/employees";
        }
        
        // Check if an attendance record already exists for this date
        List<Attendance> existingRecords = attendanceService.getEmployeeAttendanceByMonth(
                employeeId, date, date);
        
        Attendance attendance;
        
        if (!existingRecords.isEmpty()) {
            // Update existing record
            attendance = existingRecords.get(0);
            attendance.setStatus(status);
            
            // Update days based on status
            updateAttendanceDays(attendance);
        } else {
            // Create new attendance record
            attendance = new Attendance();
            attendance.setEmployeeId(employeeId);
            attendance.setDate(date);
            attendance.setMonth(date.withDayOfMonth(1)); // First day of the month
            attendance.setStatus(status);
            
            // Set days based on status
            attendance.setDaysPresent(status == Attendance.AttendanceStatus.PRESENT || 
                                     status == Attendance.AttendanceStatus.WORK_FROM_HOME ? 1 : 0);
            attendance.setDaysAbsent(status == Attendance.AttendanceStatus.ABSENT ? 1 : 0);
            attendance.setDaysLeave(status == Attendance.AttendanceStatus.SICK_LEAVE || 
                                  status == Attendance.AttendanceStatus.HALF_DAY ? 1 : 0);
        }
        
        // Save attendance
        attendanceService.saveAttendance(attendance);
        
        // Add to employee's attendance records if not already included
        Employee employee = employeeOpt.get();
        if (employee.getAttendanceRecords() == null) {
            employee.setAttendanceRecords(new java.util.ArrayList<>());
        }
        
        boolean recordExists = employee.getAttendanceRecords().stream()
                .anyMatch(a -> a.getAttributeid() != null && 
                             a.getAttributeid().equals(attendance.getAttributeid()));
        
        if (!recordExists) {
            employee.getAttendanceRecords().add(attendance);
            employeeService.updateEmployee(employee);
        }
        
        redirectAttributes.addFlashAttribute("success", "Attendance updated successfully");
        
        // Include the month parameter to return to the same month view
        YearMonth month = YearMonth.from(date);
        return "redirect:/hr/attendance/employee/" + employeeId + "?month=" + month;
    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error updating attendance: " + e.getMessage());
        return "redirect:/hr/employees";
    }
}

/**
 * Helper method to update attendance days based on status
 */
private void updateAttendanceDays(Attendance attendance) {
    // Use the correct enum type
    switch (attendance.getStatus()) {
        case PRESENT:
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            break;
        case ABSENT:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(1);
            attendance.setDaysLeave(0);
            break;
        case SICK_LEAVE:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(1);
            break;
        case HALF_DAY:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(1);
            break;
        case WORK_FROM_HOME:
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            break;
    }
}
/**
 * View part-time employee attendance
 */
@GetMapping("/attendance/parttime/{employeeId}")
public String viewPartTimeEmployeeAttendance(
        @PathVariable String employeeId,
        @RequestParam(name = "month", required = false) String monthStr,
        Model model,
        RedirectAttributes redirectAttributes) {
    
    try {
        // Get employee details
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeId);
        
        // If employee is not found or not part-time, redirect to employees list
        if (employeeOpt.isEmpty() || !employeeOpt.get().isPartTime()) {
            redirectAttributes.addFlashAttribute("error", "Part-time employee not found with ID: " + employeeId);
            return "redirect:/hr/employees";
        }
        
        Employee employee = employeeOpt.get();
        
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
        
        // Add to model
        model.addAttribute("employee", employee);
        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("formattedMonth", formattedMonth);
        model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
        model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
        model.addAttribute("currentMonth", YearMonth.now());
        
        // Add statistics
        model.addAttribute("daysPresent", daysPresent);
        model.addAttribute("daysAbsent", daysAbsent);
        model.addAttribute("daysLeave", daysLeave);
        model.addAttribute("daysWorkFromHome", daysWorkFromHome);
        model.addAttribute("daysSickLeave", daysSickLeave);
        model.addAttribute("daysHalfDay", daysHalfDay);
        model.addAttribute("totalHoursWorked", totalHoursWorked);
        
        // Add all possible attendance statuses to model for dropdown
        model.addAttribute("attendanceStatuses", PartTimeAttendance.AttendanceStatus.values());
        
        return "hr/parttime-attendance";
    } catch (Exception e) {
        // Log the exception
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error viewing part-time employee attendance: " + e.getMessage());
        return "redirect:/hr/employees";
    }
}

/**
 * Handle add/update attendance for a part-time employee
 */
@PostMapping("/parttime-attendance/update")
public String updatePartTimeEmployeeAttendance(
        @RequestParam String employeeId,
        @RequestParam LocalDate date,
        @RequestParam PartTimeAttendance.AttendanceStatus status,
        @RequestParam(required = false) String loginTime,
        @RequestParam(required = false) String logoutTime,
        RedirectAttributes redirectAttributes) {
    
    try {
        // Get employee
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeId);
        
        if (employeeOpt.isEmpty() || !employeeOpt.get().isPartTime()) {
            redirectAttributes.addFlashAttribute("error", "Part-time employee not found with ID: " + employeeId);
            return "redirect:/hr/employees";
        }
        
        
        
        // Check if an attendance record already exists for this date
        PartTimeAttendance attendance = partTimeAttendanceService.findByEmployeeIdAndDate(employeeId, date);
        
        if (attendance == null) {
            // Create new attendance record
            attendance = new PartTimeAttendance();
            attendance.setEmployeeId(employeeId);
            attendance.setDate(date);
            attendance.setMonth(date.withDayOfMonth(1)); // First day of the month
            attendance.setStatus(status);
            
            // Set default values
            attendance.setDaysPresent(status == PartTimeAttendance.AttendanceStatus.PRESENT || 
                                     status == PartTimeAttendance.AttendanceStatus.WORK_FROM_HOME ? 1 : 0);
            attendance.setDaysAbsent(status == PartTimeAttendance.AttendanceStatus.ABSENT ? 1 : 0);
            attendance.setDaysLeave(status == PartTimeAttendance.AttendanceStatus.SICK_LEAVE ? 1 : 0);
        } else {
            // Update existing record
            attendance.setStatus(status);
            
            // Update days based on status
            updatePartTimeAttendanceDays(attendance);
        }
        
        // Set time if provided
        if (loginTime != null && !loginTime.isEmpty()) {
            attendance.setLoginTime(LocalTime.parse(loginTime));
        }
        
        if (logoutTime != null && !logoutTime.isEmpty()) {
            attendance.setLogoutTime(LocalTime.parse(logoutTime));
        }
        
        // Calculate total hours if both login and logout times are set
        if (attendance.getLoginTime() != null && attendance.getLogoutTime() != null) {
            attendance.calculateTotalHours();
        }
        
        // Save attendance record
        partTimeAttendanceService.saveAttendance(attendance);
        
        redirectAttributes.addFlashAttribute("success", "Part-time attendance updated successfully");
        
        // Include the month parameter to return to the same month view
        YearMonth month = YearMonth.from(date);
        return "redirect:/hr/attendance/parttime/" + employeeId + "?month=" + month;
    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Error updating part-time attendance: " + e.getMessage());
        return "redirect:/hr/employees";
    }
}

/**
 * Helper method to update part-time attendance days based on status
 */
private void updatePartTimeAttendanceDays(PartTimeAttendance attendance) {
    switch (attendance.getStatus()) {
        case PRESENT:
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            break;
        case ABSENT:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(1);
            attendance.setDaysLeave(0);
            break;
        case SICK_LEAVE:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(1);
            break;
        case HALF_DAY:
            attendance.setDaysPresent(0);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(1);
            break;
        case WORK_FROM_HOME:
            attendance.setDaysPresent(1);
            attendance.setDaysAbsent(0);
            attendance.setDaysLeave(0);
            break;
    }
}
}