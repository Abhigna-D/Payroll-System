package com.example.demo.service;

import com.example.demo.model.OvertimeRequest;
import java.util.List;

public interface OvertimeRequestService {
    OvertimeRequest saveOvertimeRequest(OvertimeRequest overtimeRequest);
    List<OvertimeRequest> getEmployeeOvertimeRequests(String employeeId);
    List<OvertimeRequest> getPendingOvertimeRequests();
}