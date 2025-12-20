// ControllerUser.java
package com.mysql.user.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.user.dto.UserUpdateDTO;
import com.mysql.user.security.JwtUtil;
import com.mysql.user.service.ServiceUser;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class ControllerUser {

    @Autowired
    private ServiceUser serviceUser;

    @Autowired
    private JwtUtil jwtUtil;

    // Get User Profile
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceUser.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    // Update User Profile
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateMyProfile(@RequestBody UserUpdateDTO updatedUserDTO, HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceUser.updateMyProfile(userId, updatedUserDTO);
        return ResponseEntity.ok(response);
    }

    // Delete User Account
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteMyAccount(HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceUser.deleteMyAccount(userId);
        return ResponseEntity.ok(response);
    }

    // Helper : Get userId from JWT
    private int getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}