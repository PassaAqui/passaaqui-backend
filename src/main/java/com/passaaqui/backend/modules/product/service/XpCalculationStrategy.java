package com.passaaqui.backend.modules.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class XpCalculationStrategy {

    @Value("${xp.take-rate}")
    private double takeRate;

    @Value("${xp.margin-factor}")
    private double marginFactor;

    @Value("${xp.absolute-ceiling}")
    private double absoluteCeiling;

    @Value("${xp.conversion-factor}")
    private double conversionFactor;

    public int calculate(Double price, Double categoryWeight) {
        if (price == null || price <= 0) return 0;
        if (categoryWeight == null || categoryWeight <= 0) categoryWeight = 1.0;

        double baseValue = price * takeRate * marginFactor * categoryWeight;
        double cappedValue = Math.min(baseValue, absoluteCeiling);
        return (int) Math.floor(cappedValue * conversionFactor);
    }
}
