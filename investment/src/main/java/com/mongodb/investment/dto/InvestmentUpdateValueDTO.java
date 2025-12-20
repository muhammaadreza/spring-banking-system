// InvestmentUpdateValueDTO.java
package com.mongodb.investment.dto;

public class InvestmentUpdateValueDTO {
    private double currentPricePerUnit;

    // Getter and Setter
    public double getCurrentPricePerUnit() {
        return currentPricePerUnit;
    }

    public void setCurrentPricePerUnit(double currentPricePerUnit) {
        this.currentPricePerUnit = currentPricePerUnit;
    }
}