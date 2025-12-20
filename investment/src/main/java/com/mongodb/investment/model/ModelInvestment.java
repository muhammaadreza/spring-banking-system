package com.mongodb.investment.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "investments")
public class ModelInvestment {
    public enum InvestmentType { stocks, bonds, mutual_funds, gold, crypto }
    public enum InvestmentStatus { active, sold, suspended }

    @Id
    private String id;
    private int account;
    private InvestmentType type;
    private String productName;
    private double amount;
    private double currentValue;
    private double units;
    private double pricePerUnit;
    private double expectedReturn;
    private InvestmentStatus status;
    private String description;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdateAt;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAccount() {
        return this.account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public InvestmentType getType() {
        return this.type;
    }

    public void setType(InvestmentType type) {
        this.type = type;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getUnits() {
        return this.units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public double getPricePerUnit() {
        return this.pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getExpectedReturn() {
        return this.expectedReturn;
    }

    public void setExpectedReturn(double expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public InvestmentStatus getStatus() {
        return this.status;
    }

    public void setStatus(InvestmentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdateAt() {
        return this.lastUpdateAt;
    }

    public void setLastUpdateAt(LocalDateTime lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

}
