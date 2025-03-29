package com.example.demo.service;

import com.example.demo.model.OvertimeRequest;
import com.example.demo.repository.OvertimeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OvertimeRequestServiceImpl implements OvertimeRequestService {

    @Autowired
    private OvertimeRequestRepository overtimeRequestRepository;
    
    @Override
    public OvertimeRequest saveOvertimeRequest(OvertimeRequest overtimeRequest) {
        return overtimeRequestRepository.save(overtimeRequest);
    }
    
    @Override
    public List<OvertimeRequest> getEmployeeOvertimeRequests(String employeeId) {
        return overtimeRequestRepository.findByEmployeeId(employeeId);
    }
    
    @Override
    public List<OvertimeRequest> getPendingOvertimeRequests() {
        return overtimeRequestRepository.findByStatus(OvertimeRequest.OvertimeRequestStatus.PENDING);
    }
    
    @Override
    public List<OvertimeRequest> getDepartmentOvertimeRequests(Long departmentId) {
        try {
            // Use the direct query method to get overtime requests for the department
            return overtimeRequestRepository.findByDepartmentId(departmentId);
        } catch (Exception e) {
            e.printStackTrace();
            // Return empty list in case of any exception
            return new ArrayList<>();
        }
    }
    
    @Override
    public OvertimeRequest getOvertimeRequestById(Long id) {
        Optional<OvertimeRequest> overtimeRequest = overtimeRequestRepository.findById(id);
        return overtimeRequest.orElse(null);
    }
    
    @Override
    public void updateOvertimeRequestStatus(Long id, OvertimeRequest.OvertimeRequestStatus status, String comments) {
        Optional<OvertimeRequest> optionalRequest = overtimeRequestRepository.findById(id);
        
        if (optionalRequest.isPresent()) {
            OvertimeRequest request = optionalRequest.get();
            request.setStatus(status);
            request.setManagerComments(comments);
            request.setProcessedDate(LocalDate.now());
            overtimeRequestRepository.save(request);
        }
    }
}