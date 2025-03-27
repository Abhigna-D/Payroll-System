package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

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
                    String roleName = role.getName().name();
                    System.out.println("Adding role: " + roleName);
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    @Transactional
    public User saveUser(User user) {
        System.out.println("Saving user: " + user.getUsername());
        System.out.println("User password: " + user.getPassword());
        
        // Ensure the user has at least the employee role
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("WARNING: User has no roles assigned during save operation");
        }
        
        User savedUser = userRepository.save(user);
        userRepository.flush(); // Ensure data is written to the database immediately
        return savedUser;
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