package com.mysql.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mysql.user.model.ModelAccount;

@Repository
public interface RepositoryAccount extends CrudRepository<ModelAccount, Integer> {
    List<ModelAccount> findByUser(int user);
}
