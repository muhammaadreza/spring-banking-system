package com.mysql.user.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mysql.user.model.ModelUser;

@Repository
public interface RepositoryUser extends CrudRepository<ModelUser, Integer> {
    ModelUser findByUsername(String username);
}
