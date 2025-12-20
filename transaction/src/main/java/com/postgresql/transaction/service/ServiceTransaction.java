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

import com.postgresql.transaction.dto.TransactionCreateDTO;
import com.postgresql.transaction.dto.TransactionResponseDTO;
import com.postgresql.transaction.dto.TransactionUpdateDTO;
import com.postgresql.transaction.mapper.TransactionMapper;
import com.postgresql.transaction.model.ModelLoan;
import com.postgresql.transaction.model.ModelTransaction;
import com.postgresql.transaction.model.ModelTransaction.TransactionType;
import com.postgresql.transaction.repository.RepositoryLoan;
import com.postgresql.transaction.repository.RepositoryTransaction;

@Service
public class ServiceTransaction {

    @Autowired
    private RepositoryTransaction repositoryTransaction;

    @Autowired
    private RepositoryLoan repositoryLoan;

    @Autowired
    private ServiceAccount serviceAccount;

    // Get All Account Mutations
    public Map<String, Object> getAccountMutations(int accountId, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        List<ModelTransaction> mutations = repositoryTransaction.findAllMutationsByAccount(accountId);
        List<TransactionResponseDTO> transactionDTOs = mutations.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
        response.put("success", true);
        response.put("data", transactionDTOs);
        response.put("count", transactionDTOs.size());
        return response;
    }

    // Get All User Mutations
    public Map<String, Object> getTransactionHistory(String token) {
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

        List<ModelTransaction> userTransactions = repositoryTransaction.findByAccountIn(userAccountIds);
        List<TransactionResponseDTO> transactionDTOs = userTransactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", transactionDTOs);
        response.put("count", transactionDTOs.size());
        response.put("userId", userId);
        response.put("accountIds", userAccountIds);
        return response;
    }

    // Deposit
    public Map<String, Object> deposit(TransactionCreateDTO transactionCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        int accountId = transactionCreateDTO.getAccountId();
        double amount = transactionCreateDTO.getAmount();
        String description = transactionCreateDTO.getDescription();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Amount must be greater than 0");
            return response;
        }

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

        ModelTransaction transaction = TransactionMapper.toModel(transactionCreateDTO);
        transaction.setType(TransactionType.deposit);
        transaction.setDestination(0);

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Deposit successful");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        response.put("newBalance", newBalance);
        return response;
    }

    // Withdraw
    public Map<String, Object> withdraw(TransactionCreateDTO transactionCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        int accountId = transactionCreateDTO.getAccountId();
        double amount = transactionCreateDTO.getAmount();
        String description = transactionCreateDTO.getDescription();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Amount must be greater than 0");
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
            response.put("message", "Insufficient balance");
            return response;
        }

        double newBalance = currentBalance - amount;
        if (!serviceAccount.updateAccountBalance(accountId, newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        ModelTransaction transaction = TransactionMapper.toModel(transactionCreateDTO);
        transaction.setType(TransactionType.withdraw); // Ensure type is correct
        transaction.setDestination(0); // Withdraw has no destination account

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Withdraw successful");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        response.put("newBalance", newBalance);
        return response;
    }

    // Transfer
    public Map<String, Object> transfer(int sourceAccountId, int destinationAccountId, double amount,
            String description, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(sourceAccountId, token)) {
            response.put("success", false);
            response.put("message", "Source account not found or not owned by you");
            return response;
        }

        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Amount must be greater than 0");
            return response;
        }

        if (sourceAccountId == destinationAccountId) {
            response.put("success", false);
            response.put("message", "Cannot transfer to the same account");
            return response;
        }

        double sourceBalance = serviceAccount.getAccountBalance(sourceAccountId, token);
        if (sourceBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get source account balance");
            return response;
        }

        if (sourceBalance < amount) {
            response.put("success", false);
            response.put("message", "Insufficient balance in source account");
            return response;
        }

        double destinationBalance = serviceAccount.getAccountBalance(destinationAccountId, token);
        if (destinationBalance < 0) {
            response.put("success", false);
            response.put("message", "Destination account not found");
            return response;
        }

        double newSourceBalance = sourceBalance - amount;
        if (!serviceAccount.updateAccountBalance(sourceAccountId, newSourceBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update source account balance");
            return response;
        }

        double newDestinationBalance = destinationBalance + amount;
        if (!serviceAccount.updateAccountBalance(destinationAccountId, newDestinationBalance, token)) {
            serviceAccount.updateAccountBalance(sourceAccountId, sourceBalance, token); // Rollback
            response.put("success", false);
            response.put("message", "Failed to update destination account balance");
            return response;
        }

        ModelTransaction transaction = new ModelTransaction();
        transaction.setAccount(sourceAccountId);
        transaction.setType(TransactionType.transfer);
        transaction.setAmount(amount);
        transaction.setDescription(description != null ? description : "Transfer");
        transaction.setDestination(destinationAccountId);

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Transfer successful");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        response.put("sourceNewBalance", newSourceBalance);
        response.put("destinationNewBalance", newDestinationBalance);
        return response;
    }

    // Bill Payment (Loan Installment)
    public Map<String, Object> payBill(int accountId, int loanId, String description, String token) {
        Map<String, Object> response = new HashMap<>();

        if (!serviceAccount.isAccountOwnedByUser(accountId, token)) {
            response.put("success", false);
            response.put("message", "Account not found or not owned by you");
            return response;
        }

        Optional<ModelLoan> loanOpt = repositoryLoan.findById(loanId);
        if (!loanOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Loan not found");
            return response;
        }

        ModelLoan loan = loanOpt.get();

        if (loan.getAccount() != accountId) {
            response.put("success", false);
            response.put("message", "Loan does not belong to this account");
            return response;
        }

        if (loan.getRemainingMonths() <= 0) {
            response.put("success", false);
            response.put("message", "Loan is already paid off");
            return response;
        }

        double monthlyInstallment = loan.getMonthlyInstallment();

        double currentBalance = serviceAccount.getAccountBalance(accountId, token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        if (currentBalance < monthlyInstallment) {
            response.put("success", false);
            response.put("message", "Insufficient balance for loan payment");
            return response;
        }

        double newBalance = currentBalance - monthlyInstallment;
        if (!serviceAccount.updateAccountBalance(accountId, newBalance, token)) {
            response.put("success", false);
            response.put("message", "Failed to update account balance");
            return response;
        }

        loan.setRemainingMonths(loan.getRemainingMonths() - 1);
        ModelLoan updatedLoan = repositoryLoan.save(loan);

        ModelTransaction transaction = new ModelTransaction();
        transaction.setAccount(accountId);
        transaction.setType(TransactionType.bill);
        transaction.setAmount(monthlyInstallment);
        transaction.setDescription(description != null ? description : "Loan payment - " + loanId);
        transaction.setDestination(loanId); // destination is loan ID

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Bill payment successful");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        response.put("newBalance", newBalance);
        response.put("remainingMonths", updatedLoan.getRemainingMonths());
        response.put("paidAmount", monthlyInstallment);

        if (updatedLoan.getRemainingMonths() == 0) {
            response.put("loanStatus", "FULLY_PAID");
            response.put("message", "Bill payment successful - Loan fully paid off!");
        } else {
            response.put("loanStatus", "ACTIVE");
        }
        return response;
    }

    // Delete Transaction
    public Map<String, Object> deleteTransaction(int transactionId, String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelTransaction> transactionOpt = repositoryTransaction.findById(transactionId);

        if (!transactionOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Transaction not found");
            return response;
        }

        ModelTransaction transaction = transactionOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(transaction.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Transaction not found or not owned by you");
            return response;
        }

        if (transaction.getType() == TransactionType.loan) {
            response.put("success", false);
            response.put("message", "Cannot delete loan disbursement transaction");
            return response;
        }

        if (transaction.getType() == TransactionType.bill) {
            response.put("success", false);
            response.put("message", "Cannot delete bill payment transaction");
            return response;
        }

        double currentBalance = serviceAccount.getAccountBalance(transaction.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        double newBalance = currentBalance;
        String balanceAction = "";

        switch (transaction.getType()) {
            case deposit:
                if (currentBalance < transaction.getAmount()) {
                    response.put("success", false);
                    response.put("message", "Insufficient balance to reverse deposit");
                    return response;
                }
                newBalance = currentBalance - transaction.getAmount();
                balanceAction = "reversed deposit";
                break;

            case withdraw:
                newBalance = currentBalance + transaction.getAmount();
                balanceAction = "reversed withdrawal";
                break;

            case transfer:
                newBalance = currentBalance + transaction.getAmount();
                balanceAction = "reversed transfer (source side)";
                break;

            case investment:
            case deposito:
                balanceAction = "removed transaction record";
                newBalance = currentBalance;
                break;

            default:
                response.put("success", false);
                response.put("message", "Cannot delete this type of transaction");
                return response;
        }

        if (newBalance != currentBalance) {
            if (!serviceAccount.updateAccountBalance(transaction.getAccount(), newBalance, token)) {
                response.put("success", false);
                response.put("message", "Failed to update account balance");
                return response;
            }
        }

        repositoryTransaction.delete(transaction);

        response.put("success", true);
        response.put("message", "Transaction deleted successfully");
        response.put("action", balanceAction);
        response.put("oldBalance", currentBalance);
        response.put("newBalance", newBalance);
        response.put("deletedTransaction", TransactionMapper.toDTO(transaction));
        return response;
    }

    // Update Transaction
    public Map<String, Object> updateTransaction(int transactionId, TransactionUpdateDTO transactionUpdateDTO,
            String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelTransaction> transactionOpt = repositoryTransaction.findById(transactionId);

        if (!transactionOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "Transaction not found");
            return response;
        }

        ModelTransaction transaction = transactionOpt.get();

        if (!serviceAccount.isAccountOwnedByUser(transaction.getAccount(), token)) {
            response.put("success", false);
            response.put("message", "Transaction not found or not owned by you");
            return response;
        }

        if (transaction.getType() == TransactionType.loan) {
            response.put("success", false);
            response.put("message", "Cannot update loan disbursement transaction");
            return response;
        }

        if (transaction.getType() == TransactionType.bill) {
            response.put("success", false);
            response.put("message", "Cannot update bill payment transaction");
            return response;
        }

        Double newAmount = transactionUpdateDTO.getAmount();
        String newDescription = transactionUpdateDTO.getDescription();

        if (newAmount != null && newAmount <= 0) {
            response.put("success", false);
            response.put("message", "Amount must be greater than 0");
            return response;
        }

        double oldAmount = transaction.getAmount();
        double currentBalance = serviceAccount.getAccountBalance(transaction.getAccount(), token);
        if (currentBalance < 0) {
            response.put("success", false);
            response.put("message", "Failed to get account balance");
            return response;
        }

        double newBalance = currentBalance;
        if (newAmount != null && newAmount != oldAmount) {
            double amountDifference = newAmount - oldAmount;

            switch (transaction.getType()) {
                case deposit:
                    newBalance = currentBalance + amountDifference;
                    break;
                case withdraw:
                    if (currentBalance < amountDifference && amountDifference > 0) {
                        response.put("success", false);
                        response.put("message", "Insufficient balance for updated withdrawal amount");
                        return response;
                    }
                    newBalance = currentBalance - amountDifference;
                    break;
                case transfer:
                    if (currentBalance < amountDifference && amountDifference > 0) {
                        response.put("success", false);
                        response.put("message", "Insufficient balance for updated transfer amount");
                        return response;
                    }
                    newBalance = currentBalance - amountDifference;
                    break;
                case investment:
                case deposito:
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "Cannot update this type of transaction");
                    return response;
            }

            if (newBalance != currentBalance) {
                if (!serviceAccount.updateAccountBalance(transaction.getAccount(), newBalance, token)) {
                    response.put("success", false);
                    response.put("message", "Failed to update account balance");
                    return response;
                }
            }
            transaction.setAmount(newAmount);
        }

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            transaction.setDescription(newDescription.trim());
        }

        ModelTransaction updatedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Transaction updated successfully");
        response.put("transaction", TransactionMapper.toDTO(updatedTransaction));
        response.put("oldAmount", oldAmount);
        response.put("newBalance", newBalance);
        response.put("balanceChange", newBalance - currentBalance);
        return response;
    }

    // Create Investment Transaction (internal call for Project 3)
    public Map<String, Object> createInvestmentTransaction(TransactionCreateDTO transactionCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        ModelTransaction transaction = TransactionMapper.toModel(transactionCreateDTO);
        transaction.setType(TransactionType.investment);

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Investment transaction recorded successfully");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        return response;
    }

    // Create Deposito Transaction (internal call for Project 3)
    public Map<String, Object> createDepositoTransaction(TransactionCreateDTO transactionCreateDTO, String token) {
        Map<String, Object> response = new HashMap<>();

        ModelTransaction transaction = TransactionMapper.toModel(transactionCreateDTO);
        transaction.setType(TransactionType.deposito);

        ModelTransaction savedTransaction = repositoryTransaction.save(transaction);

        response.put("success", true);
        response.put("message", "Deposito transaction recorded successfully");
        response.put("transaction", TransactionMapper.toDTO(savedTransaction));
        return response;
    }
}