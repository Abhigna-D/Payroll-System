package com.example.demo.repository;

import com.example.demo.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    // Find all complaints for a specific employee
    List<Complaint> findByEmployeeIdOrderBySubmissionDateDesc(String employeeId);
    
    // Find complaints by status
    List<Complaint> findByStatus(Complaint.ComplaintStatus status);
    
    // Find complaints by employee and status
    List<Complaint> findByEmployeeIdAndStatus(String employeeId, Complaint.ComplaintStatus status);
}