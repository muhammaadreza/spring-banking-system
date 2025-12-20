package com.postgresql.transaction.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.postgresql.transaction.dto.LoanCreateDTO;
import com.postgresql.transaction.dto.LoanUpdateDTO;
import com.postgresql.transaction.service.ServiceLoan;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/loans")
public class ControllerLoan {

    @Autowired
    private ServiceLoan serviceLoan;

    // Apply Loan
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLoan(@RequestBody LoanCreateDTO loanCreateDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.createLoan(loanCreateDTO, token);
        return ResponseEntity.status(response.containsKey("loan") ? 201 : 400).body(response);
    }

    // Get all User Loans
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLoanHistory(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.getLoanHistory(token);
        return ResponseEntity.ok(response);
    }

    // Get All Account Loans
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getLoansByAccount(@PathVariable int accountId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.getLoansByAccount(accountId, token);
        return ResponseEntity.ok(response);
    }

    // Get All Active Loans
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveLoans(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.getActiveLoans(token);
        return ResponseEntity.ok(response);
    }

    // Get Loan by id
    @GetMapping("/{loanId}")
    public ResponseEntity<Map<String, Object>> getLoanById(@PathVariable int loanId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.getLoanById(loanId, token);
        return ResponseEntity.ok(response);
    }

    // Update Loan
    @PutMapping("/{loanId}")
    public ResponseEntity<Map<String, Object>> updateLoan(@PathVariable int loanId,
            @RequestBody LoanUpdateDTO loanUpdateDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.updateLoan(loanId, loanUpdateDTO, token);
        return ResponseEntity.ok(response);
    }

    // Delete Loan
    @DeleteMapping("/{loanId}")
    public ResponseEntity<Map<String, Object>> deleteLoan(@PathVariable int loanId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceLoan.deleteLoan(loanId, token);
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