package com.mysql.user.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysql.user.dto.AccountCreateDTO;
import com.mysql.user.dto.AccountResponseDTO;
import com.mysql.user.dto.AccountUpdateDTO;
import com.mysql.user.mapper.AccountMapper;
import com.mysql.user.model.ModelAccount;
import com.mysql.user.repository.RepositoryAccount;

@Service
public class ServiceAccount {

    @Autowired
    private RepositoryAccount repositoryAccount;

    // Get all user accounts
    public Map<String, Object> getMyAccounts(int userId) {
        Map<String, Object> response = new HashMap<>();
        List<ModelAccount> accounts = repositoryAccount.findByUser(userId);
        List<AccountResponseDTO> accountDTOs = accounts.stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("data", accountDTOs);
        response.put("count", accountDTOs.size());
        response.put("message", "Accounts retrieved successfully");
        return response;
    }

    // Create account
    public Map<String, Object> createAccount(int userId, AccountCreateDTO accountCreateDTO) {
        Map<String, Object> response = new HashMap<>();
        ModelAccount account = AccountMapper.toEntity(accountCreateDTO);
        account.setUser(userId);

        ModelAccount savedAccount = repositoryAccount.save(account);

        response.put("success", true);
        response.put("data", AccountMapper.toDTO(savedAccount));
        response.put("message", "Account created successfully");
        return response;
    }

    // Update account
    public Map<String, Object> updateAccount(int userId, int accountId, AccountUpdateDTO updatedAccountDTO) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelAccount> existingAccountOpt = repositoryAccount.findById(accountId);

        if (existingAccountOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account not found");
            return response;
        }

        ModelAccount existingAccount = existingAccountOpt.get();

        if (existingAccount.getUser() != userId) {
            response.put("success", false);
            response.put("message", "Account not owned by you");
            return response;
        }

        existingAccount.setAccountNumber(updatedAccountDTO.getAccountNumber());
        existingAccount.setAccountType(updatedAccountDTO.getAccountType());

        ModelAccount savedAccount = repositoryAccount.save(existingAccount);

        response.put("success", true);
        response.put("data", AccountMapper.toDTO(savedAccount));
        response.put("message", "Account updated successfully");
        return response;
    }

    // Delete account
    public Map<String, Object> deleteAccount(int userId, int accountId) {
        Map<String, Object> response = new HashMap<>();
        Optional<ModelAccount> existingAccountOpt = repositoryAccount.findById(accountId);

        if (existingAccountOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account not found");
            return response;
        }

        ModelAccount existingAccount = existingAccountOpt.get();

        if (existingAccount.getUser() != userId) {
            response.put("success", false);
            response.put("message", "Account not owned by you");
            return response;
        }

        repositoryAccount.delete(existingAccount);

        response.put("success", true);
        response.put("message", "Account deleted successfully");
        return response;
    }

    // Update Balance
    public boolean updateAccountBalance(int accountId, double newBalance, int currentUserId) {
        Optional<ModelAccount> existingAccountOpt = repositoryAccount.findById(accountId);

        if (existingAccountOpt.isEmpty()) {
            return false;
        }

        ModelAccount existingAccount = existingAccountOpt.get();

        existingAccount.setBalance(newBalance);
        repositoryAccount.save(existingAccount);
        return true;
    }

    // Internal helper for other services to get account balance
    public double getAccountBalanceInternal(int accountId, int currentUserId) {
        Optional<ModelAccount> existingAccountOpt = repositoryAccount.findById(accountId);
        if (existingAccountOpt.isEmpty() || existingAccountOpt.get().getUser() != currentUserId) {
            return -1.0;
        }
        return existingAccountOpt.get().getBalance();
    }

    // Internal helper to check account ownership for other services
    public boolean isAccountOwnedByUserInternal(int accountId, int currentUserId) {
        Optional<ModelAccount> existingAccountOpt = repositoryAccount.findById(accountId);
        return existingAccountOpt.isPresent() && existingAccountOpt.get().getUser() == currentUserId;
    }

    // Internal helper to get all account IDs for a user
    public List<Integer> getUserAccountIdsInternal(int userId) {
        List<ModelAccount> accounts = repositoryAccount.findByUser(userId);
        return accounts.stream()
                .map(ModelAccount::getId)
                .collect(Collectors.toList());
    }
}