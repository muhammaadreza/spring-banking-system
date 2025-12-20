package com.postgresql.transaction.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.postgresql.transaction.dto.LoanCreateDTO;
import com.postgresql.transaction.dto.LoanResponseDTO;
import com.postgresql.transaction.dto.LoanUpdateDTO;
import com.postgresql.transaction.mapper.LoanMapper;
import com.postgresql.transaction.model.ModelLoan;
import com.postgresql.transaction.model.ModelTransaction;
import com.postgresql.transaction.model.ModelTransaction.TransactionType;
import com.postgresql.transaction.repository.RepositoryLoan;
import com.postgresql.transaction.repository.RepositoryTransaction;

@Service
public class ServiceLoan {

    @Autowired
    private RepositoryLoan repositoryLoan;

    @Autowired
    private RepositoryTransaction repositoryTransaction;

    @Autowired
    private ServiceAccount serviceAccount;

    // Apply Loan
    public Map<String, Object> createLoan(LoanCreateDTO loanCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        int accountId = loanCreateDTO.getAccountId();
        double amount = loanCreateDTO.getAmount();
        float interestRate = loanCreateDTO.getInterestRate();
        int termMonths = loanCreateDTO.getTermMonths();
        String description = loanCreateDTO.getDescription();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Loan amount must be greater than 0");
            return response;
        }

        if (interestRate < 0) {
            response.put("success", false);
            response.put("message", "Interest rate cannot be negative");
            return response;
        }

        if (termMonths <= 0) {
            response.put("success", false);
            response.put("message", "Term months must be greater than 0");
            return response;
        }

        double monthlyInterestRate = interestRate / 100.0 / 12.0;
        double monthlyInstallment;

        if (monthlyInterestRate == 0) {
            monthlyInstallment = amount / termMonths;
        } else {
            double factor = Math.pow(1 + monthlyInterestRate, termMonths);
            monthlyInstallment = amount * (monthlyInterestRate * factor) / (factor - 1);
        }

        monthlyInstallment = Math.round(monthlyInstallment * 100.0) / 100.0;

        double currentBalance = serviceAccount.getAccountBalance(accountId, token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        double newBalance = currentBalance + amount;
        if (!serviceAccount.updateAccountBalance(accountId, newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        ModelLoan loan = LoanMapper.toModel(loanCreateDTO);
        loan.setRemainingMonths(termMonths);
        loan.setMonthlyInstallment(monthlyInstallment);

        ModelLoan savedLoan = repositoryLoan.save(loan);

        ModelTransaction transaction = new ModelTransaction();
        transaction.setAccount(accountId);
        transaction.setType(TransactionType.loan);
        transaction.setAmount(amount);
        transaction.setDescription("Loan disbursement - " + savedLoan.getId());
        transaction.setDestination(savedLoan.getId());

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Loan created successfully");
        response.put("loan", LoanMapper.toDTO(savedLoan));
        response.put("transaction", savedTransaction);
        response.put("newBalance", newBalance);
        response.put("totalInterest", (monthlyInstallment * termMonths) - amount);

        return response;
    }

    // Get all User Loans
    public Map<String, Object> getLoanHistory(String token) {
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

        List<ModelLoan> userLoans = repositoryLoan.findByAccountIn(userAccountIds);
        List<LoanResponseDTO> loanDTOs = userLoans.stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", loanDTOs);
        response.put("count", loanDTOs.size());
        response.put("userId", userId);
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get All Account Loans
    public Map<String, Object> getLoansByAccount(int accountId, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        List<ModelLoan> accountLoans = repositoryLoan.findByAccount(accountId);
        List<LoanResponseDTO> loanDTOs = accountLoans.stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", loanDTOs);
        response.put("count", loanDTOs.size());
        response.put("accountId", accountId);
        return response;
    }

    // Get All Active Loans
    public Map<String, Object> getActiveLoans(String token) {
        Map<String, Object> response = new HashMap<>();

        List<Integer> userAccountIds = serviceAccount.getUserAccountId(token);

        if (userAccountIds.isEmpty()) {
            response.put("success", true);
            response.put("data", List.of());
            response.put("count", 0);
            response.put("message", "No accounts found");
            return response;
        }

        List<ModelLoan> activeLoans = repositoryLoan.findActiveLoansByAccountIn(userAccountIds);
        List<LoanResponseDTO> loanDTOs = activeLoans.stream()
                .map(LoanMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", loanDTOs);
        response.put("count", loanDTOs.size());
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Get Loan by id
    public Map<String, Object> getLoanById(int loanId, String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelLoan> loanOpt = repositoryLoan.findById(loanId);

        if (!loanOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Loan not found");
            return response;
        }

        ModelLoan loan = loanOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(loan.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Loan not found or not owned by you");
            return response;
        }

        double totalPayable = loan.getMonthlyInstallment() * loan.getTermMonths();
        double totalInterest = totalPayable - loan.getAmount();
        double remainingPayable = loan.getMonthlyInstallment() * loan.getRemainingMonths();
        double paidAmount = totalPayable - remainingPayable;

        response.put("success", true);
        response.put("loan", LoanMapper.toDTO(loan));
        response.put("summary", Map.of(
                "totalPayable", totalPayable,
                "totalInterest", totalInterest,
                "remainingPayable", remainingPayable,
                "paidAmount", paidAmount,
                "status", loan.getRemainingMonths() > 0 ? "ACTIVE" : "PAID_OFF"));
        return response;
    }

    // Update Loan
    public Map<String, Object> updateLoan(int loanId, LoanUpdateDTO loanUpdateDTO, String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelLoan> loanOpt = repositoryLoan.findById(loanId);

        if (!loanOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Loan not found");
            return response;
        }

        ModelLoan loan = loanOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(loan.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Loan not found or not owned by you");
            return response;
        }

        if (loan.getRemainingMonths() < loan.getTermMonths()) {
            response.put("success", false);
            response.put("message", "Cannot update loan with existing payments");
            return response;
        }

        double oldAmount = loan.getAmount();
        double currentBalance = serviceAccount.getAccountBalance(loan.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        boolean recalculateInstallment = false;

        if (loanUpdateDTO.getAmount() != null && loanUpdateDTO.getAmount() != oldAmount) {
            double amountDifference = loanUpdateDTO.getAmount() - oldAmount;
            double newBalance = currentBalance + amountDifference;

            if (amountDifference < 0 && currentBalance < Math.abs(amountDifference)) {
                response.put("success", false);
                response.put("message", "Insufficient balance to reduce loan amount");
                return response;
            }

            if (!serviceAccount.updateAccountBalance(loan.getAccount(), newBalance, token)) {
                response.put("success", false);
                response.put("message", "Failed to update account balance");
                return response;
            }
            loan.setAmount(loanUpdateDTO.getAmount());
            recalculateInstallment = true;
        }

        if (loanUpdateDTO.getInterestRate() != null) {
            loan.setInterestRate(loanUpdateDTO.getInterestRate());
            recalculateInstallment = true;
        }

        if (loanUpdateDTO.getTermMonths() != null) {
            loan.setTermMonths(loanUpdateDTO.getTermMonths());
            loan.setRemainingMonths(loanUpdateDTO.getTermMonths());
            recalculateInstallment = true;
        }

        if (recalculateInstallment) {
            double monthlyInterestRate = loan.getInterestRate() / 100.0 / 12.0;
            double monthlyInstallment;
            if (monthlyInterestRate == 0) {
                monthlyInstallment = loan.getAmount() / loan.getTermMonths();
            } else {
                double factor = Math.pow(1 + monthlyInterestRate, loan.getTermMonths());
                monthlyInstallment = loan.getAmount() * (monthlyInterestRate * factor) / (factor - 1);
            }
            monthlyInstallment = Math.round(monthlyInstallment * 100.0) / 100.0;
            loan.setMonthlyInstallment(monthlyInstallment);
        }

        if (loanUpdateDTO.getDescription() != null && !loanUpdateDTO.getDescription().trim().isEmpty()) {
            loan.setDescription(loanUpdateDTO.getDescription().trim());
        }

        ModelLoan updatedLoan = repositoryLoan.save(loan);

        if (loanUpdateDTO.getAmount() != null && loanUpdateDTO.getAmount() != oldAmount) {
            List<ModelTransaction> loanTransactions = repositoryTransaction.findByDestination(loanId);
            for (ModelTransaction transaction : loanTransactions) {
                if (transaction.getType() == TransactionType.loan) {
                    transaction.setAmount(loanUpdateDTO.getAmount());
                    transaction.setDescription("Loan disbursement - " + loanId + " (Updated)");
                    repositoryTransaction.save(transaction);
                    break;
                }
            }
        }

        double newBalance = serviceAccount.getAccountBalance(loan.getAccount(), token);
        double totalInterest = (updatedLoan.getMonthlyInstallment() * updatedLoan.getTermMonths())
                - updatedLoan.getAmount();

        response.put("success", true);
        response.put("message", "Loan updated successfully");
        response.put("loan", LoanMapper.toDTO(updatedLoan));
        response.put("oldAmount", oldAmount);
        response.put("newBalance", newBalance);
        response.put("totalInterest", totalInterest);
        response.put("balanceChange", loanUpdateDTO.getAmount() != null ? loanUpdateDTO.getAmount() - oldAmount : 0);
        return response;
    }

    // Delete Loan
    public Map<String, Object> deleteLoan(int loanId, String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelLoan> loanOpt = repositoryLoan.findById(loanId);

        if (!loanOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Loan not found");
            return response;
        }

        ModelLoan loan = loanOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(loan.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Loan not found or not owned by you");
            return response;
        }

        if (loan.getRemainingMonths() < loan.getTermMonths()) {
            response.put("success", false);
            response.put("message", "Cannot delete loan with existing payments");
            return response;
        }

        double currentBalance = serviceAccount.getAccountBalance(loan.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        if (currentBalance < loan.getAmount()) {
            response.put("success", false);
            response.put("message", "Insufficient balance to reverse loan disbursement");
            return response;
        }

        double newBalance = currentBalance - loan.getAmount();
        if (!serviceAccount.updateAccountBalance(loan.getAccount(), newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        List<ModelTransaction> loanTransactions = repositoryTransaction.findByDestination(loanId);
        for (ModelTransaction transaction : loanTransactions) {
            if (transaction.getType() == TransactionType.loan) {
                repositoryTransaction.delete(transaction);
            }
        }

        repositoryLoan.delete(loan);

        response.put("success", true);
        response.put("message", "Loan deleted successfully");
        response.put("newBalance", newBalance);
        response.put("reversedAmount", loan.getAmount());
        return response;
    }
}