// InvestmentSellDTO.java
package com.mongodb.investment.dto;

public class InvestmentSellDTO {
    private double currentPricePerUnit;
    private Double unitsToSell; // Use Double for nullable
    private String description;

    // Getters and Setters
    public double getCurrentPricePerUnit() {
        return currentPricePerUnit;
    }

    public void setCurrentPricePerUnit(double currentPricePerUnit) {
        this.currentPricePerUnit = currentPricePerUnit;
    }

    public Double getUnitsToSell() {
        return unitsToSell;
    }

    public void setUnitsToSell(Double unitsToSell) {
        this.unitsToSell = unitsToSell;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}