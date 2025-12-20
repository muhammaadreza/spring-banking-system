package com.postgresql.transaction.mapper;

import com.postgresql.transaction.dto.LoanCreateDTO;
import com.postgresql.transaction.dto.LoanResponseDTO;
import com.postgresql.transaction.dto.LoanUpdateDTO;
import com.postgresql.transaction.model.ModelLoan;

import java.time.format.DateTimeFormatter;

public class LoanMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static ModelLoan toModel(LoanCreateDTO dto) {
        ModelLoan model = new ModelLoan();
        model.setAccount(dto.getAccountId());
        model.setAmount(dto.getAmount());
        model.setInterestRate(dto.getInterestRate());
        model.setTermMonths(dto.getTermMonths());
        model.setDescription(dto.getDescription());
        return model;
    }

    public static void updateModelFromDto(LoanUpdateDTO dto, ModelLoan model) {
        if (dto.getDescription() != null) {
            model.setDescription(dto.getDescription());
        }
        if (dto.getAmount() != null) {
            model.setAmount(dto.getAmount());
        }
        if (dto.getInterestRate() != null) {
            model.setInterestRate(dto.getInterestRate());
        }
        if (dto.getTermMonths() != null) {
            model.setTermMonths(dto.getTermMonths());
        }
    }

    public static LoanResponseDTO toDTO(ModelLoan model) {
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setId(model.getId());
        dto.setAccount(model.getAccount());
        dto.setAmount(model.getAmount());
        dto.setInterestRate(model.getInterestRate());
        dto.setTermMonths(model.getTermMonths());
        dto.setRemainingMonths(model.getRemainingMonths());
        dto.setMonthlyInstallment(model.getMonthlyInstallment());
        dto.setDescription(model.getDescription());
        if (model.getTimestamp() != null) {
            dto.setCreatedAt(model.getTimestamp().format(FORMATTER));
        }
        return dto;
    }
}