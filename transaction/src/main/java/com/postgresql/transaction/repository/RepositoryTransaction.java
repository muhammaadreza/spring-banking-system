package com.postgresql.transaction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.postgresql.transaction.model.ModelTransaction;

@Repository
public interface RepositoryTransaction extends JpaRepository<ModelTransaction, Integer> {
    
    // ✅ Mencari transaksi berdasarkan account (keluar dari account)
    List<ModelTransaction> findByAccount(int accountId);
    List<ModelTransaction> findByAccountIn(List<Integer> accountIds);
    
    // ✅ Mencari transaksi berdasarkan destination (masuk ke account)
    List<ModelTransaction> findByDestination(int accountId);
    
    // ✅ Mencari semua mutasi (keluar dan masuk) dari suatu account
    @Query("SELECT t FROM ModelTransaction t WHERE t.account = :accountId OR t.destination = :accountId ORDER BY t.timestamp DESC")
    List<ModelTransaction> findAllMutationsByAccount(@Param("accountId") int accountId);
}
