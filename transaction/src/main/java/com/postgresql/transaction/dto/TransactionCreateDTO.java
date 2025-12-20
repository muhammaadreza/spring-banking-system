// TransactionCreateDTO.java
package com.postgresql.transaction.dto;

import com.postgresql.transaction.model.ModelTransaction.TransactionType;

public class TransactionCreateDTO {
    private int accountId;
    private TransactionType type;
    private double amount;
    private String description;
    private Integer destination;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDestination() {
        return destination;
    }

    public void setDestination(Integer destination) {
        this.destination = destination;
    }
}