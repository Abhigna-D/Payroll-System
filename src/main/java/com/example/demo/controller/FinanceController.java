package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.model.SalarySlip;
import com.example.demo.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private EmployeeService employeeService;

    // Removed dashboard mapping to avoid conflict with DashboardController

    @GetMapping("/employees")
    public String getAllEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "finance/employee-details";
    }
    
    @GetMapping("/employees/view/{employeeID}")
    public String viewEmployeeDetails(@PathVariable String employeeID, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeID);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            model.addAttribute("employee", employee);
            return "finance/employee-view";
        } else {
            model.addAttribute("error", "Employee not found with ID: " + employeeID);
            return "redirect:/finance/employees";
        }
    }
    
    @GetMapping("/employees/salary/{employeeID}")
    public String viewEmployeeSalary(@PathVariable String employeeID, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeID);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            SalarySlip salaryDetails = employeeService.viewSalaryDetails(employeeID);
            
            model.addAttribute("employee", employee);
            model.addAttribute("salaryDetails", salaryDetails);
            return "finance/employee-salary";
        } else {
            model.addAttribute("error", "Employee not found with ID: " + employeeID);
            return "redirect:/finance/employees";
        }
    }
    
    @GetMapping("/employees/tax/{employeeID}")
    public String viewEmployeeTax(@PathVariable String employeeID, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeByEmployeeID(employeeID);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            model.addAttribute("employee", employee);
            model.addAttribute("taxDetails", employee.getTaxDeclaration());
            return "finance/employee-tax";
        } else {
            model.addAttribute("error", "Employee not found with ID: " + employeeID);
            return "redirect:/finance/employees";
        }
    }
}