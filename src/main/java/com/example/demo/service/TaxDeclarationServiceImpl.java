package com.example.demo.service;

import com.example.demo.model.TaxDeclaration;
import com.example.demo.repository.TaxDeclarationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaxDeclarationServiceImpl implements TaxDeclarationService {

    @Autowired
    private TaxDeclarationRepository taxDeclarationRepository;
    
    @Override
    public TaxDeclaration findByEmployeeId(String employeeId) {
        return taxDeclarationRepository.findByEmployee_EmployeeID(employeeId);
    }
    
    @Override
    public TaxDeclaration findById(Long id) {
        return taxDeclarationRepository.findById(id).orElse(null);
    }
    
    @Override
    public TaxDeclaration saveTaxDeclaration(TaxDeclaration taxDeclaration) {
        try {
            return taxDeclarationRepository.save(taxDeclaration);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save tax declaration: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteTaxDeclaration(Long id) {
        taxDeclarationRepository.deleteById(id);
    }
    
    @Override
    public List<TaxDeclaration> findAll() {
        return taxDeclarationRepository.findAll();
    }
    
    @Override
    public List<TaxDeclaration> findAllByStatus(String status) {
        return taxDeclarationRepository.findByStatus(status);
    }
    
    @Override
    public TaxDeclaration approveTaxDeclaration(Long id) {
        TaxDeclaration declaration = findById(id);
        if (declaration == null) {
            throw new RuntimeException("Tax declaration not found");
        }
        
        declaration.setStatus("APPROVED");
        declaration.setApprovalDate(LocalDate.now());
        return saveTaxDeclaration(declaration);
    }
    
    @Override
    public TaxDeclaration rejectTaxDeclaration(Long id, String reason) {
        TaxDeclaration declaration = findById(id);
        if (declaration == null) {
            throw new RuntimeException("Tax declaration not found");
        }
        
        declaration.setStatus("REJECTED");
        declaration.setRejectionReason(reason);
        return saveTaxDeclaration(declaration);

    }
    // TaxDeclarationServiceImpl.java
@Override
public List<TaxDeclaration> findByStatus(String status) {
    return taxDeclarationRepository.findByStatus(status);
}

}