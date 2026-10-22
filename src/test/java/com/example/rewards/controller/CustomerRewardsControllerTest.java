package com.example.rewards.controller;

import com.example.rewards.dto.CustomerRewardsResponse;
import com.example.rewards.dto.CustomerRewardsResponse.MonthlyReward;
import com.example.rewards.exception.InvalidDateRangeException;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.service.RewardsCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web layer tests for the rewards endpoints: status codes, JSON shape and input validation, with the service mocked. */
@WebMvcTest(CustomerRewardsController.class)
class CustomerRewardsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RewardsCalculationService rewardsCalculationService;

    @Test
    void returnsRewardsForCustomer() throws Exception {
        when(rewardsCalculationService.getRewardsByCustomerId(1001L, null, null)).thenReturn(
                new CustomerRewardsResponse(1001L, 90, List.of(new MonthlyReward(YearMonth.of(2025, 6), 90))));

        mockMvc.perform(get("/rewards/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1001))
                .andExpect(jsonPath("$.totalPoints").value(90))
                .andExpect(jsonPath("$.monthlyRewards[0].month").value("2025-06"))
                .andExpect(jsonPath("$.monthlyRewards[0].points").value(90));
    }

    @Test
    void passesDateBoundsToService() throws Exception {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);
        when(rewardsCalculationService.getAllRewards(from, to)).thenReturn(List.of());

        mockMvc.perform(get("/rewards").param("startDate", "2025-01-01").param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void unknownCustomerReturns404() throws Exception {
        when(rewardsCalculationService.getRewardsByCustomerId(9L, null, null))
                .thenThrow(new CustomerNotFoundException("No transactions found for customer ID: 9"));

        mockMvc.perform(get("/rewards/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No transactions found for customer ID: 9"));
    }

    @Test
    void invalidDateRangeReturns400() throws Exception {
        when(rewardsCalculationService.getAllRewards(any(), any())).thenThrow(new InvalidDateRangeException("bad range"));

        mockMvc.perform(get("/rewards").param("startDate", "2025-06-01").param("endDate", "2025-05-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedInputReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(get("/rewards/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/rewards/-5")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/rewards").param("startDate", "yesterday")).andExpect(status().isBadRequest());

        verifyNoInteractions(rewardsCalculationService);
    }

    @Test
    void writeMethodsAreNotAllowed() throws Exception {
        mockMvc.perform(post("/rewards")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/rewards/1001")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unexpectedFailureReturns500WithoutLeakingDetails() throws Exception {
        when(rewardsCalculationService.getAllRewards(any(), any())).thenThrow(new IllegalStateException("db password is hunter2"));

        mockMvc.perform(get("/rewards"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unexpected error"));
    }
}
