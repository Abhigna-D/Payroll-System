package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    // Note: The primary key type is String since employeeID is the @Id field
    
    // Find employee by ID (returns Optional to match your existing implementation)
    Optional<Employee> findByEmployeeID(String employeeID);
    
    // Find employee by username through User relationship
    Employee findByUser_Username(String username);
    
    // Find employees by department id - using int to match your implementation
    List<Employee> findByDepartmentId(int deptId);
    
    // If needed, add the Long version for compatibility
    List<Employee> findByDepartmentId(Long deptId);
    
    // Add a method to find employees by a list of IDs for overtime request filtering
    List<Employee> findByEmployeeIDIn(List<String> employeeIds);
}