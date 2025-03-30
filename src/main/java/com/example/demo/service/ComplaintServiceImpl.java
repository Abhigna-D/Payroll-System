package com.example.demo.service;

import com.example.demo.model.Complaint;
import com.example.demo.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Override
    @Transactional
    public Complaint submitComplaint(Complaint complaint) {
        // Set default values if not already set
        if (complaint.getSubmissionDate() == null) {
            complaint.setSubmissionDate(LocalDateTime.now());
        }
        
        if (complaint.getStatus() == null) {
            complaint.setStatus(Complaint.ComplaintStatus.SUBMITTED);
        }
        
        return complaintRepository.save(complaint);
    }

    @Override
    public List<Complaint> getComplaintsByEmployeeId(String employeeId) {
        return complaintRepository.findByEmployeeIdOrderBySubmissionDateDesc(employeeId);
    }

    @Override
    public Optional<Complaint> getComplaintById(Long id) {
        return complaintRepository.findById(id);
    }

    @Override
    @Transactional
    public Complaint updateComplaintStatus(Long id, Complaint.ComplaintStatus status) {
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isPresent()) {
            Complaint complaint = complaintOpt.get();
            complaint.setStatus(status);
            return complaintRepository.save(complaint);
        } else {
            throw new RuntimeException("Complaint not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public Complaint resolveComplaint(Long id, String resolutionText) {
        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isPresent()) {
            Complaint complaint = complaintOpt.get();
            complaint.setStatus(Complaint.ComplaintStatus.RESOLVED);
            complaint.setResolutionText(resolutionText);
            complaint.setResolutionDate(LocalDateTime.now());
            return complaintRepository.save(complaint);
        } else {
            throw new RuntimeException("Complaint not found with id: " + id);
        }
    }

    @Override
    public List<Complaint> findComplaintsByStatus(Complaint.ComplaintStatus status) {
        // Method renamed from getComplaintsByStatus to findComplaintsByStatus
        return complaintRepository.findByStatus(status);
    }

    @Override
    public List<Complaint> getComplaintsByEmployeeIdAndStatus(String employeeId, Complaint.ComplaintStatus status) {
        return complaintRepository.findByEmployeeIdAndStatus(employeeId, status);
    }
    @Override
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }
    @Override
    public List<Complaint> getPendingComplaints() {
        // Get all complaints that are not resolved or rejected
        List<Complaint.ComplaintStatus> nonPendingStatuses = Arrays.asList(
            Complaint.ComplaintStatus.RESOLVED, 
            Complaint.ComplaintStatus.REJECTED
        );
        
        return complaintRepository.findAll().stream()
            .filter(complaint -> !nonPendingStatuses.contains(complaint.getStatus()))
            .collect(Collectors.toList());
    }
    

    
}