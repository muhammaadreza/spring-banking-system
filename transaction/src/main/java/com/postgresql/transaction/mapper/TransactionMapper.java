package com.postgresql.transaction.mapper;

import com.postgresql.transaction.dto.TransactionCreateDTO;
import com.postgresql.transaction.dto.TransactionResponseDTO;
import com.postgresql.transaction.dto.TransactionUpdateDTO;
import com.postgresql.transaction.model.ModelTransaction;

public class TransactionMapper {

    public static ModelTransaction toModel(TransactionCreateDTO dto) {
        ModelTransaction model = new ModelTransaction();
        model.setAccount(dto.getAccountId());
        model.setType(dto.getType());
        model.setAmount(dto.getAmount());
        model.setDescription(dto.getDescription());
        if (dto.getDestination() != null) {
            model.setDestination(dto.getDestination());
        }
        return model;
    }

    public static void updateModelFromDto(TransactionUpdateDTO dto, ModelTransaction model) {
        if (dto.getAmount() != null) {
            model.setAmount(dto.getAmount());
        }
        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }
    }

    public static TransactionResponseDTO toDTO(ModelTransaction model) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(model.getId());
        dto.setAccount(model.getAccount());
        dto.setType(model.getType());
        dto.setAmount(model.getAmount());
        dto.setDescription(model.getDescription());
        dto.setDestination(model.getDestination());
        dto.setTimestamp(model.getTimestamp());
        return dto;
    }
}