package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/debug/class-info")
    public Map<String, Object> getClassInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get information about the ComplaintServiceImpl class
            Class<?> implClass = Class.forName("com.example.demo.service.ComplaintServiceImpl");
            
            // Get all methods
            Method[] methods = implClass.getDeclaredMethods();
            
            // Create a list of method signatures
            String[] methodSignatures = Arrays.stream(methods)
                .map(m -> m.getName() + ": " + Arrays.toString(m.getParameterTypes()) + " -> " + m.getReturnType())
                .toArray(String[]::new);
            
            // Count method names
            Map<String, Integer> methodCounts = new HashMap<>();
            for (Method m : methods) {
                String name = m.getName();
                methodCounts.put(name, methodCounts.getOrDefault(name, 0) + 1);
            }
            
            // Add information to result
            result.put("className", implClass.getName());
            result.put("methods", methodSignatures);
            result.put("methodCounts", methodCounts);
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("stackTrace", Arrays.toString(e.getStackTrace()));
        }
        
        return result;
    }
}