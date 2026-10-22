package com.example.rewards.dto;

import java.time.YearMonth;
import java.util.List;

/** API response with a customer total reward points and the monthly breakdown, ordered oldest month first. */
public record CustomerRewardsResponse(Long customerId, long totalPoints, List<MonthlyReward> monthlyRewards) {

    public record MonthlyReward(YearMonth month, long points) {
    }
}
