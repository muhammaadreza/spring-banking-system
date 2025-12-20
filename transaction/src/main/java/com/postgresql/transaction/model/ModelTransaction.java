package com.postgresql.transaction.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ModelTransaction {
    public enum TransactionType { deposit, withdraw, transfer, loan, bill, investment, deposito }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int account;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private double amount;
    private String description;
    private int destination;
    @CreationTimestamp
    private LocalDateTime timestamp;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccount() {
        return this.account;
    }

    public void setAccount(int account) {
        this.account = account;
    }

    public TransactionType getType() {
        return this.type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDestination() {
        return this.destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }
}
