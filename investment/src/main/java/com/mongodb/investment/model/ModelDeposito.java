package com.mongodb.investment.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "depositos")
public class ModelDeposito {
    public enum DepositoStatus { active, matured, withdrawn, extended }

    @Id
    private String id;
    private int account;
    private double principalAmount;
    private double currentValue;
    private float interestRate;
    private int termMonths;
    private int remainingMonths;
    private double monthlyInterest;
    private double totalInterest;
    private boolean autoRenewal;
    private DepositoStatus status;
    private String description;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime maturityDate;
    private LocalDateTime lastInterestDate;

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

    public double getPrincipalAmount() {
        return this.principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public float getInterestRate() {
        return this.interestRate;
    }

    public void setInterestRate(float interestRate) {
        this.interestRate = interestRate;
    }

    public int getTermMonths() {
        return this.termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public int getRemainingMonths() {
        return this.remainingMonths;
    }

    public void setRemainingMonths(int remainingMonths) {
        this.remainingMonths = remainingMonths;
    }

    public double getMonthlyInterest() {
        return this.monthlyInterest;
    }

    public void setMonthlyInterest(double monthlyInterest) {
        this.monthlyInterest = monthlyInterest;
    }

    public double getTotalInterest() {
        return this.totalInterest;
    }

    public void setTotalInterest(double totalInterest) {
        this.totalInterest = totalInterest;
    }

    public boolean isAutoRenewal() {
        return this.autoRenewal;
    }

    public void setAutoRenewal(boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }

    public DepositoStatus getStatus() {
        return this.status;
    }

    public void setStatus(DepositoStatus status) {
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

    public LocalDateTime getMaturityDate() {
        return this.maturityDate;
    }

    public void setMaturityDate(LocalDateTime maturityDate) {
        this.maturityDate = maturityDate;
    }

    public LocalDateTime getLastInterestDate() {
        return this.lastInterestDate;
    }

    public void setLastInterestDate(LocalDateTime lastInterestDate) {
        this.lastInterestDate = lastInterestDate;
    }
    
}
