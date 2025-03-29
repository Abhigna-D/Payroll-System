package com.example.demo.repository;

import com.example.demo.model.DepartmentManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentManagerRepository extends JpaRepository<DepartmentManager, Long> {
    
    // Updated query to eagerly fetch the department with the manager
    @Query("SELECT dm FROM DepartmentManager dm JOIN FETCH dm.department JOIN User u ON dm.userId = u.id WHERE u.username = :username")
    DepartmentManager findByUsername(@Param("username") String username);
}