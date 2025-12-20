package com.postgresql.transaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.postgresql.transaction.model.ModelLoan;

@Repository
public interface RepositoryLoan extends JpaRepository<ModelLoan, Integer> {
    
    // Find loans by account ID
    List<ModelLoan> findByAccount(int accountId);
    
    // Find active loans (remaining months > 0) by account
    @Query("SELECT l FROM ModelLoan l WHERE l.account = :accountId AND l.remainingMonths > 0")
    List<ModelLoan> findActiveLoansByAccount(@Param("accountId") int accountId);
    
    // Find loans by multiple account IDs
    List<ModelLoan> findByAccountIn(List<Integer> accountIds);
    
    // Find active loans by multiple account IDs
    @Query("SELECT l FROM ModelLoan l WHERE l.account IN :accountIds AND l.remainingMonths > 0")
    List<ModelLoan> findActiveLoansByAccountIn(@Param("accountIds") List<Integer> accountIds);
}