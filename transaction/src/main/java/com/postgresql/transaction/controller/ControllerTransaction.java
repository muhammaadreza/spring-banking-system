// ControllerTransaction.java
package com.postgresql.transaction.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.postgresql.transaction.dto.TransactionCreateDTO;
import com.postgresql.transaction.dto.TransactionUpdateDTO;
import com.postgresql.transaction.service.ServiceTransaction;
import com.postgresql.transaction.model.ModelTransaction.TransactionType;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/transactions")
public class ControllerTransaction {

    @Autowired
    private ServiceTransaction serviceTransaction;

    // Get All Account Mutations
    @GetMapping("/account/{accountId}/mutations")
    public ResponseEntity<Map<String, Object>> getAccountMutations(@PathVariable int accountId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.getAccountMutations(accountId, token);
        return ResponseEntity.ok(response);
    }

    // Get All User Mutations
    @GetMapping("/account/mutations")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.getTransactionHistory(token);
        return ResponseEntity.ok(response);
    }

    // Deposit
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@RequestBody TransactionCreateDTO transactionCreateDTO, HttpServletRequest request) {
        transactionCreateDTO.setType(TransactionType.deposit);
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.deposit(transactionCreateDTO, token);
        return ResponseEntity.status(response.containsKey("transaction") ? 201 : 400).body(response);
    }

    // Withdraw
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody TransactionCreateDTO transactionCreateDTO, HttpServletRequest request) {
        transactionCreateDTO.setType(TransactionType.withdraw);
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.withdraw(transactionCreateDTO, token);
        return ResponseEntity.status(response.containsKey("transaction") ? 201 : 400).body(response);
    }

    // Transfer
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        int sourceAccountId = (Integer) requestData.get("sourceAccountId");
        int destinationAccountId = (Integer) requestData.get("destinationAccountId");
        double amount = ((Number) requestData.get("amount")).doubleValue();
        String description = (String) requestData.get("description");

        Map<String, Object> response = serviceTransaction.transfer(sourceAccountId, destinationAccountId, amount, description, token);
        return ResponseEntity.status(response.containsKey("transaction") ? 201 : 400).body(response);
    }

    // Bill Payment
    @PostMapping("/bill")
    public ResponseEntity<Map<String, Object>> payBill(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        int accountId = (Integer) requestData.get("accountId");
        int loanId = (Integer) requestData.get("loanId");
        String description = (String) requestData.get("description");

        Map<String, Object> response = serviceTransaction.payBill(accountId, loanId, description, token);
        return ResponseEntity.status(response.containsKey("transaction") ? 200 : 400).body(response);
    }

    // Delete Transaction
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(@PathVariable int transactionId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.deleteTransaction(transactionId, token);
        return ResponseEntity.ok(response);
    }

    // Update Transaction
    @PutMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> updateTransaction(@PathVariable int transactionId,
            @RequestBody TransactionUpdateDTO transactionUpdateDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.updateTransaction(transactionId, transactionUpdateDTO, token);
        return ResponseEntity.ok(response);
    }

    // Investment Transaction (via REST API - internal call from Investment Service)
    @PostMapping("/investment")
    public ResponseEntity<Map<String, Object>> createInvestmentTransaction(@RequestBody TransactionCreateDTO transactionCreateDTO, HttpServletRequest request) {
        transactionCreateDTO.setType(TransactionType.investment);
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.createInvestmentTransaction(transactionCreateDTO, token);
        return ResponseEntity.ok(response);
    }

    // Deposito Transaction (via REST API - internal call from Investment Service)
    @PostMapping("/deposito")
    public ResponseEntity<Map<String, Object>> createDepositoTransaction(@RequestBody TransactionCreateDTO transactionCreateDTO, HttpServletRequest request) {
        transactionCreateDTO.setType(TransactionType.deposito);
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceTransaction.createDepositoTransaction(transactionCreateDTO, token);
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