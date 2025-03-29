package com.example.demo.repository;

import com.example.demo.model.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByEmployeeId(String employeeId);
    List<OvertimeRequest> findByStatus(OvertimeRequest.OvertimeRequestStatus status);
}