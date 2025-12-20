package com.mongodb.investment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongodb.investment.dto.DepositoCreateDTO;
import com.mongodb.investment.dto.DepositoWithdrawDTO;
import com.mongodb.investment.service.ServiceDeposito;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/depositos")
public class ControllerDeposito {

    @Autowired
    private ServiceDeposito serviceDeposito;

    // Create Deposito
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDeposito(@RequestBody DepositoCreateDTO depositoCreateDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.createDeposito(depositoCreateDTO, token);
        return ResponseEntity.status(response.containsKey("deposito") ? 201 : 400).body(response);
    }

    // Withdraw Deposito
    @PostMapping("/withdraw/{depositoId}")
    public ResponseEntity<Map<String, Object>> withdrawDeposito(@PathVariable String depositoId, @RequestBody DepositoWithdrawDTO depositoWithdrawDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.withdrawDeposito(depositoId, depositoWithdrawDTO, token);
        return ResponseEntity.ok(response);
    }

    // Update Deposito Interest
    @PutMapping("/update-interest/{depositoId}")
    public ResponseEntity<Map<String, Object>> updateDepositoInterest(@PathVariable String depositoId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.updateDepositoInterest(depositoId, token);
        return ResponseEntity.ok(response);
    }

    // Delete Deposito
    @DeleteMapping("/delete/{depositoId}")
    public ResponseEntity<Map<String, Object>> deleteDeposito(@PathVariable String depositoId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.deleteDeposito(depositoId, token);
        return ResponseEntity.ok(response);
    }

    // Get All User Depositos
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDepositoPortfolio(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.getDepositoPortfolio(token);
        return ResponseEntity.ok(response);
    }

    // Get Depositos by Account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getDepositosByAccount(@PathVariable int accountId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.getDepositosByAccount(accountId, token);
        return ResponseEntity.ok(response);
    }

    // Get Active Depositos
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveDepositos(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.getActiveDepositos(token);
        return ResponseEntity.ok(response);
    }

    // Get Matured Depositos
    @GetMapping("/matured")
    public ResponseEntity<Map<String, Object>> getMaturedDepositos(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.getMaturedDepositos(token);
        return ResponseEntity.ok(response);
    }

    // Get Deposito by ID
    @GetMapping("/{depositoId}")
    public ResponseEntity<Map<String, Object>> getDepositoById(@PathVariable String depositoId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceDeposito.getDepositoById(depositoId, token);
        return ResponseEntity.ok(response);
    }

    // Helper : Get Token from JWT
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}