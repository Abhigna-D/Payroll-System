package com.example.demo.service;

import com.example.demo.model.TaxDeclaration;
import java.util.List;

public interface TaxDeclarationService {
    
    /**
     * Find tax declaration by employee ID
     */
    TaxDeclaration findByEmployeeId(String employeeId);
    
    /**
     * Find tax declaration by ID
     */
    TaxDeclaration findById(Long id);
    
    /**
     * Save or update tax declaration
     */
    TaxDeclaration saveTaxDeclaration(TaxDeclaration taxDeclaration);
    
    /**
     * Delete tax declaration
     */
    void deleteTaxDeclaration(Long id);
    
    /**
     * Get all tax declarations (for admin)
     */
    List<TaxDeclaration> findAll();
    
    /**
     * Get all pending tax declarations (for admin)
     */
    List<TaxDeclaration> findAllByStatus(String status);
    
    /**
     * Approve tax declaration
     */
    TaxDeclaration approveTaxDeclaration(Long id);
    
    /**
     * Reject tax declaration
     */
    TaxDeclaration rejectTaxDeclaration(Long id, String reason);
    // TaxDeclarationService.java
List<TaxDeclaration> findByStatus(String status);

}