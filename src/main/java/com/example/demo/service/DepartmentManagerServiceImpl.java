package com.example.demo.service;

import com.example.demo.model.DepartmentManager;
import com.example.demo.repository.DepartmentManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartmentManagerServiceImpl implements DepartmentManagerService {

    @Autowired
    private DepartmentManagerRepository departmentManagerRepository;
    
    @Override
    public DepartmentManager findByUsername(String username) {
        return departmentManagerRepository.findByUsername(username);
    }
}