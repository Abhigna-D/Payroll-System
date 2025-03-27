package com.example.demo.repository;

import com.example.demo.model.TaxDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxDeclarationRepository extends JpaRepository<TaxDeclaration, Long> {
    
    /**
     * Find tax declaration by employee ID
     * Using the correct property name from Employee class
     */
    TaxDeclaration findByEmployee_EmployeeID(String employeeId);
    
    /**
     * Find all tax declarations by status
     */
    List<TaxDeclaration> findByStatus(String status);
}