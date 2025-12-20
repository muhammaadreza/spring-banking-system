// AccountMapper.java
package com.mysql.user.mapper;

import com.mysql.user.dto.AccountCreateDTO;
import com.mysql.user.dto.AccountResponseDTO;
import com.mysql.user.dto.AccountUpdateDTO;
import com.mysql.user.model.ModelAccount;

public class AccountMapper {

    public static ModelAccount toEntity(AccountCreateDTO dto) {
        ModelAccount account = new ModelAccount();
        account.setAccountNumber(dto.getAccountNumber());
        account.setBalance(dto.getBalance());
        account.setAccountType(dto.getAccountType());
        return account;
    }

    public static void updateEntityFromDto(AccountUpdateDTO dto, ModelAccount entity) {
        if (dto.getAccountNumber() != null) {
            entity.setAccountNumber(dto.getAccountNumber());
        }
        if (dto.getAccountType() != null) {
            entity.setAccountType(dto.getAccountType());
        }
    }

    public static AccountResponseDTO toDTO(ModelAccount entity) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(entity.getId());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setBalance(entity.getBalance());
        dto.setAccountType(entity.getAccountType());
        dto.setUser(entity.getUser());
        return dto;
    }
}