package com.example.demo.repository;

import com.example.demo.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    // Find pending complaints (SUBMITTED, UNDER_REVIEW, IN_PROGRESS)
    @Query("SELECT c FROM Complaint c WHERE c.status = 'SUBMITTED' OR c.status = 'UNDER_REVIEW' OR c.status = 'IN_PROGRESS' ORDER BY c.submissionDate DESC")
    List<Complaint> findPendingComplaints();
    
    // Find recent complaints (limited to a specific count)
    @Query(value = "SELECT * FROM complaints ORDER BY submission_date DESC LIMIT ?1", nativeQuery = true)
    List<Complaint> findRecentComplaints(int limit);
}