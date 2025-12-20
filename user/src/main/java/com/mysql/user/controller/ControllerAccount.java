// ControllerAccount.java
package com.mysql.user.controller;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.user.dto.AccountCreateDTO;
import com.mysql.user.dto.AccountUpdateDTO;
import com.mysql.user.security.JwtUtil;
import com.mysql.user.service.ServiceAccount;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/accounts")
public class ControllerAccount {
    @Autowired
    private ServiceAccount serviceAccount;

    @Autowired
    private JwtUtil jwtUtil;

    // Get All User Accounts
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyAccounts(HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceAccount.getMyAccounts(userId);
        return ResponseEntity.ok(response);
    }

    // Add Account
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody AccountCreateDTO accountCreateDTO, HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceAccount.createAccount(userId, accountCreateDTO);
        return ResponseEntity.status(response.containsKey("data") ? 201 : 400).body(response);
    }

    // Update Account
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAccount(@PathVariable int id, @RequestBody AccountUpdateDTO updatedAccountDTO, HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceAccount.updateAccount(userId, id, updatedAccountDTO);
        return ResponseEntity.ok(response);
    }

    // Delete Account
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable int id, HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        Map<String, Object> response = serviceAccount.deleteAccount(userId, id);
        return ResponseEntity.ok(response);
    }

    // Update Balance (via REST API - internal call for other services)
    @PutMapping("/update/{id}")
    public boolean updateBalance(@PathVariable int id, @RequestBody Map<String, Double> requestBody, HttpServletRequest request) {
        int userId = getUserIdFromRequest(request);
        double newBalance = requestBody.get("balance");
        return serviceAccount.updateAccountBalance(id, newBalance, userId);
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