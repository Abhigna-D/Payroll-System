package com.example.demo.service;

import com.example.demo.model.Complaint;
import java.util.List;
import java.util.Optional;

public interface ComplaintService {
    
    

    // Submit a new complaint
    Complaint submitComplaint(Complaint complaint);
    
    // Get all complaints for an employee
    List<Complaint> getComplaintsByEmployeeId(String employeeId);
    
    // Get a specific complaint by ID
    Optional<Complaint> getComplaintById(Long id);
    
    // Update complaint status
    Complaint updateComplaintStatus(Long id, Complaint.ComplaintStatus status);
    
    // Resolve a complaint with resolution text
    Complaint resolveComplaint(Long id, String resolutionText);
    
    // Get all complaints with a specific status
    List<Complaint> findComplaintsByStatus(Complaint.ComplaintStatus status);
    
    // Get all complaints for an employee with a specific status
    List<Complaint> getComplaintsByEmployeeIdAndStatus(String employeeId, Complaint.ComplaintStatus status);
    List<Complaint> getAllComplaints();
    List<Complaint> getPendingComplaints();
     

}