package com.example.demo.service;

import com.example.demo.model.OvertimeRequest;
import java.util.List;

public interface OvertimeRequestService {
    OvertimeRequest saveOvertimeRequest(OvertimeRequest overtimeRequest);
    List<OvertimeRequest> getEmployeeOvertimeRequests(String employeeId);
    List<OvertimeRequest> getPendingOvertimeRequests();
    
    // New methods for department manager functionality
    List<OvertimeRequest> getDepartmentOvertimeRequests(Long departmentId);
    OvertimeRequest getOvertimeRequestById(Long id);
    void updateOvertimeRequestStatus(Long id, OvertimeRequest.OvertimeRequestStatus status, String comments);
}