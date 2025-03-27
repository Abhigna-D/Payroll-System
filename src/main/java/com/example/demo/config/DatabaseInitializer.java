package com.example.demo.config;

import com.example.demo.model.ERole;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    @Transactional
    public void initialize() {
        // Initialize roles only if they don't exist
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles...");
            
            Role employeeRole = new Role(ERole.ROLE_EMPLOYEE);
            Role hrRole = new Role(ERole.ROLE_HR);
            Role managerRole = new Role(ERole.ROLE_MANAGER);
            Role financeRole = new Role(ERole.ROLE_FINANCE);
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            
            roleRepository.save(employeeRole);
            roleRepository.save(hrRole);
            roleRepository.save(managerRole);
            roleRepository.save(financeRole);
            roleRepository.save(adminRole);
            
            System.out.println("Roles initialized successfully!");
        }
    }
}