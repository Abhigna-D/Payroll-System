package com.example.demo.repository;

import com.example.demo.model.SalarySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
    Optional<SalarySlip> findByEmployeeEmployeeID(String employeeID);
}