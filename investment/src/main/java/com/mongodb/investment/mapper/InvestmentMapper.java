package com.mongodb.investment.mapper;

import com.mongodb.investment.dto.InvestmentCreateDTO;
import com.mongodb.investment.dto.InvestmentResponseDTO;
import com.mongodb.investment.model.ModelInvestment;
import com.mongodb.investment.model.ModelInvestment.InvestmentType;

public class InvestmentMapper {

    public static ModelInvestment toModel(InvestmentCreateDTO dto) {
        ModelInvestment model = new ModelInvestment();
        model.setAccount(dto.getAccountId());
        model.setType(InvestmentType.valueOf(dto.getType().toLowerCase()));
        model.setProductName(dto.getProductName());
        model.setAmount(dto.getAmount());
        model.setPricePerUnit(dto.getPricePerUnit());
        model.setExpectedReturn(dto.getExpectedReturn());
        model.setDescription(dto.getDescription());
        return model;
    }

    public static InvestmentResponseDTO toDTO(ModelInvestment model) {
        InvestmentResponseDTO dto = new InvestmentResponseDTO();
        dto.setId(model.getId());
        dto.setAccount(model.getAccount());
        dto.setType(model.getType());
        dto.setProductName(model.getProductName());
        dto.setAmount(model.getAmount());
        dto.setCurrentValue(model.getCurrentValue());
        dto.setUnits(model.getUnits());
        dto.setPricePerUnit(model.getPricePerUnit());
        dto.setExpectedReturn(model.getExpectedReturn());
        dto.setStatus(model.getStatus());
        dto.setDescription(model.getDescription());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setLastUpdateAt(model.getLastUpdateAt());
        return dto;
    }
}