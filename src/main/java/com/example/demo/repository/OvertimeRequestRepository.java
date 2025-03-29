package com.example.demo.repository;

import com.example.demo.model.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    List<OvertimeRequest> findByEmployeeId(String employeeId);
    List<OvertimeRequest> findByStatus(OvertimeRequest.OvertimeRequestStatus status);
    List<OvertimeRequest> findByEmployeeIdIn(List<String> employeeIds);
    
    // This is the query that is generating the SQL you're seeing in logs
    @Query("SELECT o FROM OvertimeRequest o JOIN Employee e ON o.employeeId = e.employeeID WHERE e.department.id = :departmentId")
    List<OvertimeRequest> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    // Add a debugging query that just counts the results
    @Query("SELECT COUNT(o) FROM OvertimeRequest o JOIN Employee e ON o.employeeId = e.employeeID WHERE e.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Long departmentId);
}