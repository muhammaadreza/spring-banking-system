package com.mongodb.investment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mongodb.investment.model.ModelDeposito;
import com.mongodb.investment.model.ModelDeposito.DepositoStatus;

@Repository
public interface RepositoryDeposito extends MongoRepository<ModelDeposito, String> {
    
    // Find depositos by account ID
    List<ModelDeposito> findByAccount(int accountId);
    
    // Find depositos by multiple account IDs (for user portfolio)
    List<ModelDeposito> findByAccountIn(List<Integer> accountIds);
    
    // Find depositos by account IDs and specific status
    List<ModelDeposito> findByAccountInAndStatus(List<Integer> accountIds, DepositoStatus status);
    
    // Find depositos by account ID and status
    List<ModelDeposito> findByAccountAndStatus(int accountId, DepositoStatus status);
    
    // Find all active depositos for an account
    List<ModelDeposito> findByAccountAndStatusIn(int accountId, List<DepositoStatus> statuses);
    
    // Find depositos by status (for administrative purposes)
    List<ModelDeposito> findByStatus(DepositoStatus status);
    
    // Find depositos that need interest update (for batch processing)
    // This would typically be handled by a scheduled service
    List<ModelDeposito> findByStatusAndRemainingMonthsGreaterThan(DepositoStatus status, int months);
}