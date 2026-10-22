package com.example.rewards.controller;

import com.example.rewards.dto.CustomerRewardsResponse;
import com.example.rewards.service.RewardsCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** REST endpoints that expose reward points per customer, per month and in total, for an optional date window. */
@RestController
@Tag(name = "Rewards", description = "Reward points earned per customer, per month and in total")
public class CustomerRewardsController {

    private static final String START_DATE_DESCRIPTION =
            "First day of the window, inclusive (yyyy-MM-dd). Defaults to three months before endDate.";
    private static final String END_DATE_DESCRIPTION =
            "Last day of the window, inclusive (yyyy-MM-dd). Defaults to today.";

    private final RewardsCalculationService rewardsCalculationService;

    public CustomerRewardsController(RewardsCalculationService rewardsCalculationService) {
        this.rewardsCalculationService = rewardsCalculationService;
    }

    @GetMapping("/rewards")
    @Operation(summary = "Rewards for all customers",
            description = "Monthly and total points for every customer with at least one purchase in the window.")
    @ApiResponse(responseCode = "200", description = "Rewards, ordered by customer id (empty when nothing is in the window)")
    @ApiResponse(responseCode = "400", description = "Malformed date, or startDate after endDate")
    public List<CustomerRewardsResponse> getRewardsForAllCustomers(
            @Parameter(description = START_DATE_DESCRIPTION, example = "2026-07-06")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = END_DATE_DESCRIPTION, example = "2026-10-06")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return rewardsCalculationService.getAllRewards(startDate, endDate);
    }

    @GetMapping("/rewards/{customerId}")
    @Operation(summary = "Rewards for one customer",
            description = "Monthly and total points for a single customer. A known customer with no purchases "
                    + "in the window gets 0 points.")
    @ApiResponse(responseCode = "200", description = "Rewards for the customer")
    @ApiResponse(responseCode = "400", description = "Invalid customer id, malformed date, or startDate after endDate")
    @ApiResponse(responseCode = "404", description = "The customer has no transactions at all")
    public CustomerRewardsResponse getRewardsByCustomerId(
            @Parameter(description = "Customer id", example = "1001")
            @PathVariable @Positive Long customerId,
            @Parameter(description = START_DATE_DESCRIPTION, example = "2026-07-06")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = END_DATE_DESCRIPTION, example = "2026-10-06")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return rewardsCalculationService.getRewardsByCustomerId(customerId, startDate, endDate);
    }
}
