package com.example.demo.service;

import com.example.demo.model.DepartmentManager;

public interface DepartmentManagerService {
    DepartmentManager findByUsername(String username);
}