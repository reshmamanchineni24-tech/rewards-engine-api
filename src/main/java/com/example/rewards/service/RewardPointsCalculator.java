package com.example.rewards.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 2 points per dollar over $100, plus 1 point per dollar between $50 and $100.
 * Points are earned per whole dollar, so the amount is truncated first
 * ($120.97 is treated as $120 = 90 points).
 */
public final class RewardPointsCalculator {

    private static final long UPPER_TIER = 100;
    private static final long LOWER_TIER = 50;

    private RewardPointsCalculator() {
    }

    public static long calculateRewardPoints(BigDecimal amount) {
        long dollars = amount.setScale(0, RoundingMode.DOWN).longValueExact();
        if (dollars > UPPER_TIER) {
            return 2 * (dollars - UPPER_TIER) + (UPPER_TIER - LOWER_TIER);
        }
        if (dollars > LOWER_TIER) {
            return dollars - LOWER_TIER;
        }
        return 0;
    }
}
