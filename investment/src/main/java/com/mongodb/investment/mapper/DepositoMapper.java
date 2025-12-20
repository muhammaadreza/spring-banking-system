package com.mongodb.investment.mapper;

import com.mongodb.investment.dto.DepositoCreateDTO;
import com.mongodb.investment.dto.DepositoResponseDTO;
import com.mongodb.investment.model.ModelDeposito;

public class DepositoMapper {

    public static ModelDeposito toModel(DepositoCreateDTO dto) {
        ModelDeposito model = new ModelDeposito();
        model.setAccount(dto.getAccountId());
        model.setPrincipalAmount(dto.getPrincipalAmount());
        model.setInterestRate(dto.getInterestRate());
        model.setTermMonths(dto.getTermMonths());
        model.setAutoRenewal(dto.isAutoRenewal());
        model.setDescription(dto.getDescription());
        return model;
    }

    public static DepositoResponseDTO toDTO(ModelDeposito model) {
        DepositoResponseDTO dto = new DepositoResponseDTO();
        dto.setId(model.getId());
        dto.setAccount(model.getAccount());
        dto.setPrincipalAmount(model.getPrincipalAmount());
        dto.setCurrentValue(model.getCurrentValue());
        dto.setInterestRate(model.getInterestRate());
        dto.setTermMonths(model.getTermMonths());
        dto.setRemainingMonths(model.getRemainingMonths());
        dto.setMonthlyInterest(model.getMonthlyInterest());
        dto.setTotalInterest(model.getTotalInterest());
        dto.setAutoRenewal(model.isAutoRenewal());
        dto.setStatus(model.getStatus());
        dto.setDescription(model.getDescription());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setMaturityDate(model.getMaturityDate());
        dto.setLastInterestDate(model.getLastInterestDate());
        return dto;
    }
}