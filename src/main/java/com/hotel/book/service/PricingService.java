package com.hotel.book.service;

import org.springframework.stereotype.Service;

/**
 * Simple dynamic pricing service.
 * Higher occupancy -> higher price, lower occupancy -> discount.
 */
@Service
public class PricingService {

    /**
     * @param basePrice     base room price from DB
     * @param occupancyRate value between 0 and 1 (0% to 100%)
     * @return adjusted price
     */
    public double applyDynamicPricing(double basePrice, double occupancyRate) {
        if (occupancyRate >= 0.8) {
            // Very high demand: +50%
            return basePrice * 1.5;
        } else if (occupancyRate >= 0.5) {
            // Medium demand: +20%
            return basePrice * 1.2;
        } else if (occupancyRate <= 0.2) {
            // Very low demand: -20%
            return basePrice * 0.8;
        } else {
            // Normal demand: small discount
            return basePrice * 0.9;
        }
    }
}

