package com.example.rewards.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Unit tests for the tiered point rules, including every tier boundary and very large amounts. */
class RewardPointsCalculatorTest {

    @ParameterizedTest(name = "{0} -> {1} points")
    @CsvSource({
            "0, 0",
            "0.99, 0",
            "49.99, 0",
            "50, 0",
            "50.99, 0",
            "51, 1",
            "75, 25",
            "99.99, 49",
            "100, 50",
            "100.99, 50",
            "101, 52",
            "120, 90",
            "120.97, 90",
            "140, 130",
            "200, 250",
            "-20, 0"
    })
    void appliesTieredRules(String amount, long expected) {
        assertEquals(expected, RewardPointsCalculator.calculateRewardPoints(new BigDecimal(amount)));
    }

    @Test
    void amountsBeyondIntRangeDoNotOverflow() {
        // largest value DECIMAL(12,2) can hold: 2 * (9_999_999_999 - 100) + 50 points
        assertEquals(19_999_999_848L, RewardPointsCalculator.calculateRewardPoints(new BigDecimal("9999999999.99")));
    }
}
