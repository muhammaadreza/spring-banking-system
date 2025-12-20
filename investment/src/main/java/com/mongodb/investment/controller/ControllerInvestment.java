// ControllerInvestment.java (Diperbarui untuk menggunakan DTO di Controller)
package com.mongodb.investment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mongodb.investment.dto.InvestmentCreateDTO;
import com.mongodb.investment.dto.InvestmentSellDTO;
import com.mongodb.investment.dto.InvestmentUpdateValueDTO;
import com.mongodb.investment.service.ServiceInvestment;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/investments")
public class ControllerInvestment {

    @Autowired
    private ServiceInvestment serviceInvestment;

    // Buy Investment
    @PostMapping("/buy")
    public ResponseEntity<Map<String, Object>> buyInvestment(@RequestBody InvestmentCreateDTO investmentCreateDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.buyInvestment(investmentCreateDTO, token);
        return ResponseEntity.status(response.containsKey("investment") ? 201 : 400).body(response);
    }

    // Sell Investment
    @PostMapping("/sell/{investmentId}")
    public ResponseEntity<Map<String, Object>> sellInvestment(@PathVariable String investmentId, @RequestBody InvestmentSellDTO investmentSellDTO, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.sellInvestment(investmentId, investmentSellDTO, token);
        return ResponseEntity.ok(response);
    }

    // Update Investment Value
    @PutMapping("/update-value/{investmentId}")
    public ResponseEntity<Map<String, Object>> updateInvestmentValue(@PathVariable String investmentId,
            @RequestBody InvestmentUpdateValueDTO investmentUpdateValueDTO,
            HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.updateInvestmentValue(investmentId, investmentUpdateValueDTO, token);
        return ResponseEntity.ok(response);
    }

    // Delete Investment
    @DeleteMapping("/{investmentId}")
    public ResponseEntity<Map<String, Object>> deleteInvestment(@PathVariable String investmentId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.deleteInvestment(investmentId, token);
        return ResponseEntity.ok(response);
    }

    // Get All User Investments
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInvestmentPortfolio(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.getInvestmentPortfolio(token);
        return ResponseEntity.ok(response);
    }

    // Get Investments by Account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<Map<String, Object>> getInvestmentsByAccount(@PathVariable int accountId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.getInvestmentsByAccount(accountId, token);
        return ResponseEntity.ok(response);
    }

    // Get Active Investments
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveInvestments(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.getActiveInvestments(token);
        return ResponseEntity.ok(response);
    }

    // Get Investment by ID
    @GetMapping("/{investmentId}")
    public ResponseEntity<Map<String, Object>> getInvestmentById(@PathVariable String investmentId, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = serviceInvestment.getInvestmentById(investmentId, token);
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