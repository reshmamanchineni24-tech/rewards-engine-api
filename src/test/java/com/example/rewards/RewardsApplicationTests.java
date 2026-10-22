package com.example.rewards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End to end against the real schema and seed data (dated 2026-07-08 to 2026-10-03), asserting
 * hand-calculated points over the window that covers every row.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsApplicationTests {

    /** The seed data in data.sql is dated 2026-07-08 to 2026-10-03, which sits inside this window. */
    private static final String LAST_THREE_MONTHS = "?startDate=2026-07-06&endDate=2026-10-06";

    @Autowired
    MockMvc mockMvc;

    @Test
    void seededCustomerTotalsMatchHandCalculatedValues() throws Exception {
        mockMvc.perform(get("/rewards/1001" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(964));
        mockMvc.perform(get("/rewards/1002" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(657));
        mockMvc.perform(get("/rewards/1003" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(1221));
        mockMvc.perform(get("/rewards/1004" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(975));
        mockMvc.perform(get("/rewards/1005" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(1331));
        mockMvc.perform(get("/rewards/1006" + LAST_THREE_MONTHS)).andExpect(jsonPath("$.totalPoints").value(876));
    }

    @Test
    void monthlyBreakdownForCustomer1001() throws Exception {
        mockMvc.perform(get("/rewards/1001" + LAST_THREE_MONTHS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards.length()").value(4))
                .andExpect(jsonPath("$.monthlyRewards[0].month").value("2026-07"))
                .andExpect(jsonPath("$.monthlyRewards[0].points").value(370))
                .andExpect(jsonPath("$.monthlyRewards[1].month").value("2026-08"))
                .andExpect(jsonPath("$.monthlyRewards[1].points").value(210))
                .andExpect(jsonPath("$.monthlyRewards[2].month").value("2026-09"))
                .andExpect(jsonPath("$.monthlyRewards[2].points").value(224))
                .andExpect(jsonPath("$.monthlyRewards[3].month").value("2026-10"))
                .andExpect(jsonPath("$.monthlyRewards[3].points").value(160));
    }

    @Test
    void rewardsForEveryCustomerInTheWindow() throws Exception {
        mockMvc.perform(get("/rewards" + LAST_THREE_MONTHS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].customerId").value(1001))
                .andExpect(jsonPath("$[5].customerId").value(1006));
    }

    @Test
    void windowWithoutPurchasesGivesZeroPointsForKnownCustomer() throws Exception {
        mockMvc.perform(get("/rewards/1001?startDate=2020-01-01&endDate=2020-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(0))
                .andExpect(jsonPath("$.monthlyRewards.length()").value(0));
    }

    @Test
    void unknownCustomerIs404() throws Exception {
        mockMvc.perform(get("/rewards/9999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/transactions/9999")).andExpect(status().isNotFound());
    }

    @Test
    void listsAllTransactionsForCustomer() throws Exception {
        mockMvc.perform(get("/transactions/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));
    }
}
