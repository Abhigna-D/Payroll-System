package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.ERole;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user: " + username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found with username: " + username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("User has no assigned roles: " + username);
            throw new UsernameNotFoundException("User has no assigned roles.");
        }

        // Debug - print user details
        System.out.println("Found user: " + user.getUsername());
        System.out.println("User password: " + user.getPassword());
        System.out.println("User roles: " + user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", ")));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName().name(); // Ensure "ROLE_" prefix
                    System.out.println("Adding role: " + roleName);
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList());
            
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }

    @Transactional
    public User saveUser(User user) {
        System.out.println("Saving user: " + user.getUsername());
        System.out.println("User password: " + user.getPassword());
        
        // Ensure the user has at least the employee role if no roles are specified
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("User has no roles - assigning default EMPLOYEE role");
            Role employeeRole = roleRepository.findByName(ERole.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Default role EMPLOYEE not found"));
            
            // Initialize roles set if null
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            
            user.getRoles().add(employeeRole);
        }
        
        // Debug roles before saving
        if (user.getRoles() != null) {
            System.out.println("Saving user with roles: " + 
                user.getRoles().stream()
                    .map(r -> r.getName().toString())
                    .collect(Collectors.joining(", ")));
        }
        
        // Save the user with its roles
        User savedUser = userRepository.save(user);
        userRepository.flush(); // Ensure data is written to the database immediately
        
        // Verify the user was saved with roles
        User verifiedUser = userRepository.findById(savedUser.getId()).orElse(null);
        if (verifiedUser != null && verifiedUser.getRoles() != null) {
            System.out.println("Verified saved user roles: " + 
                verifiedUser.getRoles().stream()
                    .map(r -> r.getName().toString())
                    .collect(Collectors.joining(", ")));
        } else {
            System.out.println("WARNING: Could not verify user roles after save!");
        }
        
        return savedUser;
    }

    @Transactional
    public User createUserWithRole(User user, ERole roleName) {
        // Find the specified role
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
        
        // Set the role for the user
        user.setRoles(Collections.singleton(role));
        
        // Save the user with the role
        return saveUser(user);
    }

    @Transactional
    public User addRoleToUser(Long userId, ERole roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
        
        // Initialize roles set if null
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteUser(Long id) {
        System.out.println("Deleting user with ID: " + id);
        userRepository.deleteById(id);
    }
}