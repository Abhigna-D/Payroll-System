package com.example.demo.service;

import com.example.demo.model.OvertimeRequest;
import com.example.demo.repository.OvertimeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
}