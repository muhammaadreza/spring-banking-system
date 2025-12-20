package com.mongodb.investment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.investment.model.ModelInvestment;
import com.mongodb.investment.model.ModelInvestment.InvestmentStatus;
import com.mongodb.investment.model.ModelInvestment.InvestmentType;

@Repository
public interface RepositoryInvestment extends MongoRepository<ModelInvestment, String> {
    
    // Find investments by account ID
    List<ModelInvestment> findByAccount(int accountId);
    
    // Find investments by multiple account IDs
    List<ModelInvestment> findByAccountIn(List<Integer> accountIds);
    
    // Find investments by account and status
    List<ModelInvestment> findByAccountAndStatus(int accountId, InvestmentStatus status);
    
    // Find investments by multiple account IDs and status
    List<ModelInvestment> findByAccountInAndStatus(List<Integer> accountIds, InvestmentStatus status);
    
    // Find investments by account and type
    List<ModelInvestment> findByAccountAndType(int accountId, InvestmentType type);
    
    // Find investments by multiple account IDs and type
    List<ModelInvestment> findByAccountInAndType(List<Integer> accountIds, InvestmentType type);
    
    // Find active investments by account
    @Query("{'account': ?0, 'status': 'active'}")
    List<ModelInvestment> findActiveInvestmentsByAccount(int accountId);
    
    // Find active investments by multiple account IDs
    @Query("{'account': {'$in': ?0}, 'status': 'active'}")
    List<ModelInvestment> findActiveInvestmentsByAccountIn(List<Integer> accountIds);
    
    // Find investments by product name (case insensitive)
    @Query("{'productName': {'$regex': ?0, '$options': 'i'}}")
    List<ModelInvestment> findByProductNameContainingIgnoreCase(String productName);
    
    // Find investments by account and product name
    @Query("{'account': ?0, 'productName': {'$regex': ?1, '$options': 'i'}}")
    List<ModelInvestment> findByAccountAndProductNameContainingIgnoreCase(int accountId, String productName);
    
    // Get total investment amount by account
    @Query(value = "{'account': ?0}", fields = "{'amount': 1}")
    List<ModelInvestment> findInvestmentAmountsByAccount(int accountId);
    
    // Get total current value by account
    @Query(value = "{'account': ?0}", fields = "{'currentValue': 1}")
    List<ModelInvestment> findCurrentValuesByAccount(int accountId);
}