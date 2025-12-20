// DepositoWithdrawDTO.java
package com.mongodb.investment.dto;

public class DepositoWithdrawDTO {
    private boolean forceWithdraw;
    private String description;

    // Getters and Setters
    public boolean isForceWithdraw() {
        return forceWithdraw;
    }

    public void setForceWithdraw(boolean forceWithdraw) {
        this.forceWithdraw = forceWithdraw;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}