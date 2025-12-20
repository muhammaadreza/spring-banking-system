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

import com.mongodb.investment.dto.DepositoCreateDTO;
import com.mongodb.investment.dto.DepositoResponseDTO;
import com.mongodb.investment.dto.DepositoWithdrawDTO;
import com.mongodb.investment.mapper.DepositoMapper;
import com.mongodb.investment.model.ModelDeposito;
import com.mongodb.investment.model.ModelDeposito.DepositoStatus;
import com.mongodb.investment.repository.RepositoryDeposito;

@Service
public class ServiceDeposito {

    @Autowired
    private RepositoryDeposito repositoryDeposito;

    @Autowired
    private ServiceAccount serviceAccount;

    @Autowired
    private ServiceTransaction serviceTransaction;

    // Create Deposito
    public Map<String, Object> createDeposito(DepositoCreateDTO depositoCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        int accountId = depositoCreateDTO.getAccountId();
        double principalAmount = depositoCreateDTO.getPrincipalAmount();
        float interestRate = depositoCreateDTO.getInterestRate();
        int termMonths = depositoCreateDTO.getTermMonths();
        String description = depositoCreateDTO.getDescription();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        if (principalAmount <= 0) {
            response.put("success", false);
            response.put("message", "Principal amount must be greater than 0");
            return response;
        }

        if (interestRate <= 0) {
            response.put("success", false);
            response.put("message", "Interest rate must be greater than 0");
            return response;
        }

        if (termMonths <= 0) {
            response.put("success", false);
            response.put("message", "Term months must be greater than 0");
            return response;
        }

        double currentBalance = serviceAccount.getAccountBalance(accountId, token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        if (currentBalance < principalAmount) {
            response.put("success", false);
            response.put("message", "Insufficient balance for deposito");
            return response;
        }

        double newBalance = currentBalance - principalAmount;
        if (!serviceAccount.updateAccountBalance(accountId, newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        // Calculate monthly interest and maturity date
        double monthlyInterest = (principalAmount * interestRate / 100) / 12;
        monthlyInterest = Math.round(monthlyInterest * 100.0) / 100.0;

        LocalDateTime maturityDate = LocalDateTime.now().plusMonths(termMonths);

        ModelDeposito deposito = DepositoMapper.toModel(depositoCreateDTO);
        deposito.setCurrentValue(principalAmount);
        deposito.setRemainingMonths(termMonths);
        deposito.setMonthlyInterest(monthlyInterest);
        deposito.setTotalInterest(0.0);
        deposito.setStatus(DepositoStatus.active);
        deposito.setCreatedAt(LocalDateTime.now());
        deposito.setMaturityDate(maturityDate);
        deposito.setLastInterestDate(LocalDateTime.now());
        ModelDeposito savedDeposito = repositoryDeposito.save(deposito);

        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("accountId", accountId);
        transactionData.put("type", "deposito");
        transactionData.put("amount", principalAmount);
        transactionData.put("description",
        description != null ? description : "Deposito creation - " + savedDeposito.getId());

        String destinationIdStr = savedDeposito.getId();
        try {
            transactionData.put("destination", Integer.parseInt(destinationIdStr));
        } catch (NumberFormatException e) {
            transactionData.put("destination", 0);
            transactionData.put("description",
                    transactionData.get("description") + " [Dep ID: " + destinationIdStr + "]");
        }

        Map<String, Object> transactionResult = serviceTransaction.createTransaction(transactionData, token);

        response.put("success", true);
        response.put("message", "Deposito created successfully");
        response.put("deposito", DepositoMapper.toDTO(savedDeposito));
        response.put("transaction", transactionResult.get("transaction"));
        response.put("newBalance", newBalance);
        response.put("monthlyInterest", monthlyInterest);
        response.put("maturityDate", maturityDate);
        return response;
    }

    // Withdraw Deposito
    public Map<String, Object> withdrawDeposito(String depositoId, DepositoWithdrawDTO depositoWithdrawDTO,
            String token) {
        Map<String, Object> response = new HashMap<>();

        boolean forceWithdraw = depositoWithdrawDTO.isForceWithdraw();
        String description = depositoWithdrawDTO.getDescription();

        Optional<ModelDeposito> depositoOpt = repositoryDeposito.findById(depositoId);
        if (!depositoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Deposito not found");
            return response;
        }

        ModelDeposito deposito = depositoOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(deposito.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Deposito not found or not owned by you");
            return response;
        }

        if (deposito.getStatus() != DepositoStatus.active && deposito.getStatus() != DepositoStatus.matured
                && deposito.getStatus() != DepositoStatus.extended) {
            response.put("success", false);
            response.put("message", "Deposito is not active or eligible for withdrawal");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isMatured = now.isAfter(deposito.getMaturityDate()) || now.isEqual(deposito.getMaturityDate());

        if (!isMatured && !forceWithdraw) {
            response.put("success", false);
            response.put("message",
                    "Deposito has not matured yet. Use forceWithdraw=true for early withdrawal with penalty");
            response.put("maturityDate", deposito.getMaturityDate());
            response.put("daysUntilMaturity",
                    java.time.temporal.ChronoUnit.DAYS.between(now, deposito.getMaturityDate()));
            return response;
        }

        double withdrawAmount = deposito.getCurrentValue();
        double penalty = 0.0;

        if (!isMatured && forceWithdraw) {
            penalty = deposito.getPrincipalAmount() * 0.01; // 1% penalty
            penalty = Math.round(penalty * 100.0) / 100.0;
            withdrawAmount = deposito.getCurrentValue() - penalty;
            if (withdrawAmount < deposito.getPrincipalAmount() * 0.8) {
                withdrawAmount = deposito.getPrincipalAmount() * 0.8; // Minimum 80% of principal
            }
        }

        double currentBalance = serviceAccount.getAccountBalance(deposito.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        double newBalance = currentBalance + withdrawAmount;
        if (!serviceAccount.updateAccountBalance(deposito.getAccount(), newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        deposito.setStatus(DepositoStatus.withdrawn);
        deposito.setCurrentValue(0);
        deposito.setRemainingMonths(0);

        ModelDeposito updatedDeposito = repositoryDeposito.save(deposito);

        // Create transaction record for withdrawal
        Map<String, Object> transactionData = new HashMap<>(); // GUNAKAN MAP SAJA
        transactionData.put("accountId", deposito.getAccount());
        transactionData.put("type", "deposito"); // String type
        transactionData.put("amount", withdrawAmount);
        transactionData.put("description", description != null ? description
                : "Deposito withdrawal - " + (isMatured ? "Matured" : "Early withdrawal"));

        String destinationIdStr = depositoId;
        try {
            transactionData.put("destination", Integer.parseInt(destinationIdStr)); // Try to parse as int
        } catch (NumberFormatException e) {
            transactionData.put("destination", 0); // Default to 0 if not numeric
            transactionData.put("description",
                    transactionData.get("description") + " [Dep ID: " + destinationIdStr + "]"); // Add ID to
                                                                                                 // description
        }

        Map<String, Object> transactionResult = serviceTransaction.createTransaction(transactionData, token); // Panggil
                                                                                                              // metode
                                                                                                              // umum
                                                                                                              // createTransaction

        response.put("success", true);
        response.put("message", isMatured ? "Deposito withdrawn successfully at maturity"
                : "Deposito withdrawn successfully with early withdrawal penalty");
        response.put("deposito", DepositoMapper.toDTO(updatedDeposito));
        response.put("transaction", transactionResult.get("transaction"));
        response.put("newBalance", newBalance);
        response.put("withdrawAmount", withdrawAmount);
        response.put("penalty", penalty);
        response.put("wasMatured", isMatured);
        response.put("totalInterestEarned", deposito.getTotalInterest());
        return response;
    }

    // ... (metode-metode lain tetap sama)
    public Map<String, Object> updateDepositoInterest(String depositoId, String token) {
        Map<String, Object> response = new HashMap<>();

        Optional<ModelDeposito> depositoOpt = repositoryDeposito.findById(depositoId);
        if (!depositoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Deposito not found");
            return response;
        }

        ModelDeposito deposito = depositoOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(deposito.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Deposito not found or not owned by you");
            return response;
        }

        if (deposito.getStatus() != DepositoStatus.active) {
            response.put("success", false);
            response.put("message", "Cannot update interest of inactive deposito");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastInterestDate = deposito.getLastInterestDate();

        if (lastInterestDate.plusMonths(1).isAfter(now)) {
            response.put("success", false);
            response.put("message", "Interest update not due yet");
            response.put("nextInterestDate", lastInterestDate.plusMonths(1));
            return response;
        }

        double oldCurrentValue = deposito.getCurrentValue();
        double monthlyInterest = deposito.getMonthlyInterest();
        double newCurrentValue = oldCurrentValue + monthlyInterest;
        double newTotalInterest = deposito.getTotalInterest() + monthlyInterest;
        int newRemainingMonths = deposito.getRemainingMonths() - 1;

        deposito.setCurrentValue(Math.round(newCurrentValue * 100.0) / 100.0);
        deposito.setTotalInterest(Math.round(newTotalInterest * 100.0) / 100.0);
        deposito.setRemainingMonths(newRemainingMonths);
        deposito.setLastInterestDate(now);

        if (newRemainingMonths <= 0 || now.isAfter(deposito.getMaturityDate())) {
            if (deposito.isAutoRenewal()) {
                deposito.setRemainingMonths(deposito.getTermMonths());
                deposito.setMaturityDate(now.plusMonths(deposito.getTermMonths()));
                deposito.setStatus(DepositoStatus.extended);
            } else {
                deposito.setStatus(DepositoStatus.matured);
            }
        }

        ModelDeposito updatedDeposito = repositoryDeposito.save(deposito);

        response.put("success", true);
        response.put("message", "Deposito interest updated successfully");
        response.put("deposito", DepositoMapper.toDTO(updatedDeposito));
        response.put("interestAdded", monthlyInterest);
        response.put("oldValue", oldCurrentValue);
        response.put("newValue", updatedDeposito.getCurrentValue());
        response.put("totalInterest", updatedDeposito.getTotalInterest());
        response.put("remainingMonths", updatedDeposito.getRemainingMonths());

        if (updatedDeposito.getStatus() == DepositoStatus.matured) {
            response.put("depositoStatus", "MATURED");
            response.put("message", "Deposito has matured and is ready for withdrawal");
        } else if (updatedDeposito.getStatus() == DepositoStatus.extended) {
            response.put("depositoStatus", "AUTO_RENEWED");
            response.put("message", "Deposito auto-renewed for another term");
        }
        return response;
    }

    // Delete Deposito
    public Map<String, Object> deleteDeposito(String depositoId, String token) {
        Map<String, Object> response = new HashMap<>();

        Optional<ModelDeposito> depositoOpt = repositoryDeposito.findById(depositoId);
        if (!depositoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Deposito not found");
            return response;
        }

        ModelDeposito deposito = depositoOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(deposito.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Deposito not found or not owned by you");
            return response;
        }

        if (deposito.getStatus() == DepositoStatus.active || deposito.getStatus() == DepositoStatus.extended) {
            response.put("success", false);
            response.put("message", "Cannot delete active deposito. Please withdraw first");
            response.put("currentStatus", deposito.getStatus());
            response.put("suggestion", "Use /withdraw endpoint to withdraw the deposito before deletion");
            return response;
        }

        String deletedDepositoId = deposito.getId();
        int accountId = deposito.getAccount();
        double principalAmount = deposito.getPrincipalAmount();
        double totalInterest = deposito.getTotalInterest();
        DepositoStatus status = deposito.getStatus();
        String productName = deposito.getDescription();

        repositoryDeposito.delete(deposito);

        response.put("success", true);
        response.put("message", "Deposito deleted successfully");
        response.put("deletedDepositoId", deletedDepositoId);
        response.put("accountId", accountId);
        response.put("principalAmount", principalAmount);
        response.put("totalInterestEarned", totalInterest);
        response.put("status", status);
        response.put("productName", productName);
        return response;
    }

    // Get All User Depositos
    public Map<String, Object> getDepositoPortfolio(String token) {
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

        List<ModelDeposito> userDepositos = repositoryDeposito.findByAccountIn(userAccountIds);
        List<DepositoResponseDTO> depositoDTOs = userDepositos.stream()
                .map(DepositoMapper::toDTO)
                .collect(Collectors.toList());

        double totalPrincipal = 0;
        double totalCurrentValue = 0;
        double totalInterestEarned = 0;
        int activeCount = 0;
        int maturedCount = 0;

        for (ModelDeposito deposito : userDepositos) {
            totalPrincipal += deposito.getPrincipalAmount();
            totalCurrentValue += deposito.getCurrentValue();
            totalInterestEarned += deposito.getTotalInterest();

            if (deposito.getStatus() == DepositoStatus.active || deposito.getStatus() == DepositoStatus.extended) {
                activeCount++;
            } else if (deposito.getStatus() == DepositoStatus.matured) {
                maturedCount++;
            }
        }

        double totalReturn = totalCurrentValue - totalPrincipal;
        double totalReturnPercentage = totalPrincipal > 0
                ? Math.round((totalReturn / totalPrincipal) * 10000.0) / 100.0
                : 0;

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("totalPrincipal", totalPrincipal);
        portfolio.put("totalCurrentValue", totalCurrentValue);
        portfolio.put("totalReturn", totalReturn);
        portfolio.put("totalReturnPercentage", totalReturnPercentage);
        portfolio.put("totalInterestEarned", totalInterestEarned);
        portfolio.put("activeDepositos", activeCount);
        portfolio.put("maturedDepositos", maturedCount);
        portfolio.put("totalDepositos", userDepositos.size());

        response.put("success", true);
        response.put("data", depositoDTOs);
        response.put("count", depositoDTOs.size());
        response.put("portfolio", portfolio);
        response.put("userId", userId);
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Depositos by Account
    public Map<String, Object> getDepositosByAccount(int accountId, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        List<ModelDeposito> accountDepositos = repositoryDeposito.findByAccount(accountId);
        List<DepositoResponseDTO> depositoDTOs = accountDepositos.stream()
                .map(DepositoMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", depositoDTOs);
        response.put("count", depositoDTOs.size());
        response.put("accountId", accountId);
        return response;
    }

    // Get Active Depositos
    public Map<String, Object> getActiveDepositos(String token) {
        Map<String, Object> response = new HashMap<>();

        List<Integer> userAccountIds = serviceAccount.getUserAccountId(token);

        if (userAccountIds.isEmpty()) {
            response.put("success", true);
            response.put("data", List.of());
            response.put("count", 0);
            response.put("message", "No accounts found");
            return response;
        }

        List<ModelDeposito> activeDepositos = repositoryDeposito.findByAccountInAndStatus(userAccountIds,
                DepositoStatus.active);
        List<DepositoResponseDTO> depositoDTOs = activeDepositos.stream()
                .map(DepositoMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", depositoDTOs);
        response.put("count", depositoDTOs.size());
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Matured Depositos
    public Map<String, Object> getMaturedDepositos(String token) {
        Map<String, Object> response = new HashMap<>();

        List<Integer> userAccountIds = serviceAccount.getUserAccountId(token);

        if (userAccountIds.isEmpty()) {
            response.put("success", true);
            response.put("data", List.of());
            response.put("count", 0);
            response.put("message", "No accounts found");
            return response;
        }

        List<ModelDeposito> maturedDepositos = repositoryDeposito.findByAccountInAndStatus(userAccountIds,
                DepositoStatus.matured);
        List<DepositoResponseDTO> depositoDTOs = maturedDepositos.stream()
                .map(DepositoMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", depositoDTOs);
        response.put("count", depositoDTOs.size());
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Deposito by ID
    public Map<String, Object> getDepositoById(String depositoId, String token) {
        Map<String, Object> response = new HashMap<>();

        Optional<ModelDeposito> depositoOpt = repositoryDeposito.findById(depositoId);

        if (!depositoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Deposito not found");
            return response;
        }

        ModelDeposito deposito = depositoOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(deposito.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Deposito not found or not owned by you");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysUntilMaturity = java.time.temporal.ChronoUnit.DAYS.between(now, deposito.getMaturityDate());
        double projectedValue = deposito.getPrincipalAmount()
                + (deposito.getMonthlyInterest() * deposito.getTermMonths());
        double currentReturn = deposito.getCurrentValue() - deposito.getPrincipalAmount();
        double currentReturnPercentage = deposito.getPrincipalAmount() > 0
                ? Math.round((currentReturn / deposito.getPrincipalAmount()) * 10000.0) / 100.0
                : 0;

        Map<String, Object> performance = new HashMap<>();
        performance.put("currentReturn", currentReturn);
        performance.put("currentReturnPercentage", currentReturnPercentage);
        performance.put("projectedFinalValue", Math.round(projectedValue * 100.0) / 100.0);
        performance.put("daysUntilMaturity", daysUntilMaturity);
        performance.put("isMatured", daysUntilMaturity <= 0);
        performance.put("monthsCompleted", deposito.getTermMonths() - deposito.getRemainingMonths());
        performance.put("progressPercentage",
                deposito.getTermMonths() > 0
                        ? Math.round(((double) (deposito.getTermMonths() - deposito.getRemainingMonths())
                                / deposito.getTermMonths()) * 10000.0) / 100.0
                        : 0);

        response.put("success", true);
        response.put("deposito", DepositoMapper.toDTO(deposito));
        response.put("performance", performance);
        return response;
    }
}