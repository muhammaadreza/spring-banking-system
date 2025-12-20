package com.mongodb.investment.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mongodb.investment.dto.InvestmentCreateDTO;
import com.mongodb.investment.dto.InvestmentResponseDTO;
import com.mongodb.investment.dto.InvestmentSellDTO;
import com.mongodb.investment.dto.InvestmentUpdateValueDTO;
import com.mongodb.investment.mapper.InvestmentMapper;
import com.mongodb.investment.model.ModelInvestment;
import com.mongodb.investment.model.ModelInvestment.InvestmentStatus;
import com.mongodb.investment.model.ModelInvestment.InvestmentType;
import com.mongodb.investment.repository.RepositoryInvestment;

@Service
public class ServiceInvestment {

    @Autowired
    private RepositoryInvestment repositoryInvestment;

    @Autowired
    private ServiceAccount serviceAccount;

    @Autowired
    private ServiceTransaction serviceTransaction;

    // Buy Investment
    public Map<String, Object> buyInvestment(InvestmentCreateDTO investmentCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        int accountId = investmentCreateDTO.getAccountId();
        String typeStr = investmentCreateDTO.getType();
        double amount = investmentCreateDTO.getAmount();
        double pricePerUnit = investmentCreateDTO.getPricePerUnit();
        String description = investmentCreateDTO.getDescription();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Investment amount must be greater than 0");
            return response;
        }

        if (pricePerUnit <= 0) {
            response.put("success", false);
            response.put("message", "Price per unit must be greater than 0");
            return response;
        }

        InvestmentType investmentType;
        try {
            investmentType = InvestmentType.valueOf(typeStr.toLowerCase());
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message",
                    "Invalid investment type. Valid types: stocks, bonds, mutual_funds, gold, crypto");
            return response;
        }

        double currentBalance = serviceAccount.getAccountBalance(accountId, token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        if (currentBalance < amount) {
            response.put("success", false);
            response.put("message", "Insufficient balance for investment");
            return response;
        }

        double units = amount / pricePerUnit;
        units = Math.round(units * 10000.0) / 10000.0;

        double newBalance = currentBalance - amount;
        if (!serviceAccount.updateAccountBalance(accountId, newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        ModelInvestment investment = InvestmentMapper.toModel(investmentCreateDTO);
        investment.setCurrentValue(amount);
        investment.setUnits(units);
        investment.setStatus(InvestmentStatus.active);
        investment.setCreatedAt(LocalDateTime.now());
        investment.setLastUpdateAt(LocalDateTime.now());

        ModelInvestment savedInvestment = repositoryInvestment.save(investment);

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("accountId", accountId);
        transactionData.put("type", "investment");
        transactionData.put("amount", amount);
        transactionData.put("description",
                description != null ? description : "Investment purchase - " + savedInvestment.getId());

        String destinationIdStr = savedInvestment.getId();
        try {
            transactionData.put("destination", Integer.parseInt(destinationIdStr));
        } catch (NumberFormatException e) {
            transactionData.put("destination", 0);
            transactionData.put("description",
                    transactionData.get("description") + " [Inv ID: " + destinationIdStr + "]");
        }

        Map<String, Object> transactionResult = serviceTransaction.createTransaction(transactionData, token);

        response.put("success", true);
        response.put("message", "Investment purchased successfully");
        response.put("investment", InvestmentMapper.toDTO(savedInvestment));
        response.put("transaction", transactionResult.get("transaction"));
        response.put("newBalance", newBalance);
        response.put("units", units);
        return response;
    }

    // Sell Investment
    public Map<String, Object> sellInvestment(String investmentId, InvestmentSellDTO investmentSellDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        double currentPricePerUnit = investmentSellDTO.getCurrentPricePerUnit();
        Double unitsToSell = investmentSellDTO.getUnitsToSell();
        String description = investmentSellDTO.getDescription();

        Optional<ModelInvestment> investmentOpt = repositoryInvestment.findById(investmentId);
        if (!investmentOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Investment not found");
            return response;
        }

        ModelInvestment investment = investmentOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(investment.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Investment not found or not owned by you");
            return response;
        }

        if (investment.getStatus() != InvestmentStatus.active) {
            response.put("success", false);
            response.put("message", "Investment is not active");
            return response;
        }

        if (currentPricePerUnit <= 0) {
            response.put("success", false);
            response.put("message", "Current price per unit must be greater than 0");
            return response;
        }

        if (unitsToSell == null || unitsToSell <= 0) {
            unitsToSell = investment.getUnits();
        }

        if (unitsToSell > investment.getUnits()) {
            response.put("success", false);
            response.put("message", "Cannot sell more units than owned");
            return response;
        }

        double saleAmount = unitsToSell * currentPricePerUnit;
        saleAmount = Math.round(saleAmount * 100.0) / 100.0;

        double currentBalance = serviceAccount.getAccountBalance(investment.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        double newBalance = currentBalance + saleAmount;
        if (!serviceAccount.updateAccountBalance(investment.getAccount(), newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        double originalValue = unitsToSell * investment.getPricePerUnit();
        double profitLoss = saleAmount - originalValue;

        if (unitsToSell == investment.getUnits()) {
            investment.setStatus(InvestmentStatus.sold);
            investment.setUnits(0);
            investment.setCurrentValue(0);
        } else {
            double remainingUnits = investment.getUnits() - unitsToSell;
            investment.setUnits(remainingUnits);
            investment.setCurrentValue(remainingUnits * currentPricePerUnit);
        }

        investment.setLastUpdateAt(LocalDateTime.now());
        ModelInvestment updatedInvestment = repositoryInvestment.save(investment);

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("accountId", investment.getAccount());
        transactionData.put("type", "investment");
        transactionData.put("amount", saleAmount);
        transactionData.put("description", description != null ? description
                : "Investment sale - " + investment.getProductName() + " (" + unitsToSell + " units)");

        String destinationIdStr = investmentId;
        try {
            transactionData.put("destination", Integer.parseInt(destinationIdStr));
        } catch (NumberFormatException e) {
            transactionData.put("destination", 0);
            transactionData.put("description",
                    transactionData.get("description") + " [Inv ID: " + destinationIdStr + "]");
        }

        Map<String, Object> transactionResult = serviceTransaction.createTransaction(transactionData, token);

        response.put("success", true);
        response.put("message", "Investment sold successfully");
        response.put("investment", InvestmentMapper.toDTO(updatedInvestment));
        response.put("transaction", transactionResult.get("transaction"));
        response.put("newBalance", newBalance);
        response.put("saleAmount", saleAmount);
        response.put("unitsSold", unitsToSell);
        response.put("profitLoss", profitLoss);
        response.put("profitLossPercentage", Math.round((profitLoss / originalValue) * 10000.0) / 100.0);

        if (unitsToSell == investment.getUnits()) {
            response.put("investmentStatus", "FULLY_SOLD");
        } else {
            response.put("investmentStatus", "PARTIALLY_SOLD");
            response.put("remainingUnits", updatedInvestment.getUnits());
        }
        return response;
    }

    // ... (metode-metode lain tetap sama)
    public Map<String, Object> updateInvestmentValue(String investmentId,
            InvestmentUpdateValueDTO investmentUpdateValueDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        double currentPricePerUnit = investmentUpdateValueDTO.getCurrentPricePerUnit();

        Optional<ModelInvestment> investmentOpt = repositoryInvestment.findById(investmentId);
        if (!investmentOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Investment not found");
            return response;
        }

        ModelInvestment investment = investmentOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(investment.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Investment not found or not owned by you");
            return response;
        }

        if (investment.getStatus() != InvestmentStatus.active) {
            response.put("success", false);
            response.put("message", "Cannot update value of inactive investment");
            return response;
        }

        if (currentPricePerUnit <= 0) {
            response.put("success", false);
            response.put("message", "Current price per unit must be greater than 0");
            return response;
        }

        double oldValue = investment.getCurrentValue();
        double newValue = investment.getUnits() * currentPricePerUnit;
        newValue = Math.round(newValue * 100.0) / 100.0;

        investment.setCurrentValue(newValue);
        investment.setLastUpdateAt(LocalDateTime.now());

        ModelInvestment updatedInvestment = repositoryInvestment.save(investment);

        double valueChange = newValue - oldValue;
        double valueChangePercentage = oldValue > 0 ? Math.round((valueChange / oldValue) * 10000.0) / 100.0 : 0;

        response.put("success", true);
        response.put("message", "Investment value updated successfully");
        response.put("investment", InvestmentMapper.toDTO(updatedInvestment));
        response.put("oldValue", oldValue);
        response.put("newValue", newValue);
        response.put("valueChange", valueChange);
        response.put("valueChangePercentage", valueChangePercentage);
        return response;
    }

    // Delete Investment
    public Map<String, Object> deleteInvestment(String investmentId, String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelInvestment> investmentOpt = repositoryInvestment.findById(investmentId);
        if (!investmentOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Investment not found");
            return response;
        }

        ModelInvestment investment = investmentOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(investment.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Investment not found or not owned by you");
            return response;
        }

        if (investment.getStatus() == InvestmentStatus.active) {
            response.put("success", false);
            response.put("message", "Cannot delete active investment. Please sell it first");
            response.put("investmentStatus", investment.getStatus().toString());
            response.put("currentValue", investment.getCurrentValue());
            response.put("units", investment.getUnits());
            return response;
        }

        if (investment.getStatus() != InvestmentStatus.sold) {
            response.put("success", false);
            response.put("message", "Can only delete sold investments");
            response.put("investmentStatus", investment.getStatus().toString());
            return response;
        }

        Map<String, Object> deletedInvestmentInfo = new HashMap<>();
        deletedInvestmentInfo.put("id", investment.getId());
        deletedInvestmentInfo.put("productName", investment.getProductName());
        deletedInvestmentInfo.put("type", investment.getType().toString());
        deletedInvestmentInfo.put("originalAmount", investment.getAmount());
        deletedInvestmentInfo.put("originalUnits", investment.getUnits());
        deletedInvestmentInfo.put("pricePerUnit", investment.getPricePerUnit());
        deletedInvestmentInfo.put("status", investment.getStatus().toString());
        deletedInvestmentInfo.put("createdAt", investment.getCreatedAt());
        deletedInvestmentInfo.put("accountId", investment.getAccount());

        repositoryInvestment.delete(investment);

        response.put("success", true);
        response.put("message", "Investment deleted successfully");
        response.put("deletedInvestment", deletedInvestmentInfo);
        return response;
    }

    // Get All User Investments
    public Map<String, Object> getInvestmentPortfolio(String token) {
        Map<String, Object> response = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        List<Integer> userAccountIds = serviceAccount.getUserAccountId(token);

        if (userAccountIds.isEmpty()) {
            response.put("success", true);
            response.put("data", List.of());
            response.put("count", 0);
            response.put("message", "No accounts found");
            return response;
        }

        List<ModelInvestment> userInvestments = repositoryInvestment.findByAccountIn(userAccountIds);
        List<InvestmentResponseDTO> investmentDTOs = userInvestments.stream()
                .map(InvestmentMapper::toDTO)
                .collect(Collectors.toList());

        double totalInvestment = 0;
        double totalCurrentValue = 0;
        int activeCount = 0;

        for (ModelInvestment investment : userInvestments) {
            totalInvestment += investment.getAmount();
            totalCurrentValue += investment.getCurrentValue();
            if (investment.getStatus() == InvestmentStatus.active) {
                activeCount++;
            }
        }

        double totalProfitLoss = totalCurrentValue - totalInvestment;
        double totalProfitLossPercentage = totalInvestment > 0
                ? Math.round((totalProfitLoss / totalInvestment) * 10000.0) / 100.0
                : 0;

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("totalInvestment", totalInvestment);
        portfolio.put("totalCurrentValue", totalCurrentValue);
        portfolio.put("totalProfitLoss", totalProfitLoss);
        portfolio.put("totalProfitLossPercentage", totalProfitLossPercentage);
        portfolio.put("activeInvestments", activeCount);
        portfolio.put("totalInvestments", userInvestments.size());

        response.put("success", true);
        response.put("data", investmentDTOs);
        response.put("count", investmentDTOs.size());
        response.put("portfolio", portfolio);
        response.put("userId", userId);
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Investments by Account
    public Map<String, Object> getInvestmentsByAccount(int accountId, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        List<ModelInvestment> accountInvestments = repositoryInvestment.findByAccount(accountId);
        List<InvestmentResponseDTO> investmentDTOs = accountInvestments.stream()
                .map(InvestmentMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", investmentDTOs);
        response.put("count", investmentDTOs.size());
        response.put("accountId", accountId);
        return response;
    }

    // Get Active Investments
    public Map<String, Object> getActiveInvestments(String token) {
        Map<String, Object> response = new HashMap<>();

        List<Integer> userAccountIds = serviceAccount.getUserAccountId(token);

        if (userAccountIds.isEmpty()) {
            response.put("success", true);
            response.put("data", List.of());
            response.put("count", 0);
            response.put("message", "No accounts found");
            return response;
        }

        List<ModelInvestment> activeInvestments = repositoryInvestment.findByAccountInAndStatus(userAccountIds,
                InvestmentStatus.active);
        List<InvestmentResponseDTO> investmentDTOs = activeInvestments.stream()
                .map(InvestmentMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", investmentDTOs);
        response.put("count", investmentDTOs.size());
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Investment by ID
    public Map<String, Object> getInvestmentById(String investmentId, String token) {
        Map<String, Object> response = new HashMap<>();

        Optional<ModelInvestment> investmentOpt = repositoryInvestment.findById(investmentId);

        if (!investmentOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Investment not found");
            return response;
        }

        ModelInvestment investment = investmentOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(investment.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Investment not found or not owned by you");
            return response;
        }

        double profitLoss = investment.getCurrentValue() - investment.getAmount();
        double profitLossPercentage = investment.getAmount() > 0
                ? Math.round((profitLoss / investment.getAmount()) * 10000.0) / 100.0
                : 0;

        Map<String, Object> performance = new HashMap<>();
        performance.put("profitLoss", profitLoss);
        performance.put("profitLossPercentage", profitLossPercentage);
        performance.put("currentPricePerUnit",
                investment.getUnits() > 0
                        ? Math.round((investment.getCurrentValue() / investment.getUnits()) * 10000.0) / 10000.0
                        : 0);
        performance.put("originalPricePerUnit", investment.getPricePerUnit());
        performance.put("priceChange",
                investment.getUnits() > 0
                        ? (investment.getCurrentValue() / investment.getUnits()) - investment.getPricePerUnit()
                        : 0);

        response.put("success", true);
        response.put("investment", InvestmentMapper.toDTO(investment));
        response.put("performance", performance);
        return response;
    }
}