package com.example.rewards.controller;

import com.example.rewards.dto.TransactionResponse;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.service.TransactionQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web layer tests for the transactions endpoint, with the service mocked. */
@WebMvcTest(CustomerTransactionsController.class)
class CustomerTransactionsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TransactionQueryService transactionQueryService;

    @Test
    void returnsTransactionsForCustomer() throws Exception {
        when(transactionQueryService.getTransactionsByCustomerId(1001L)).thenReturn(List.of(
                new TransactionResponse(1L, 1001L, new BigDecimal("120.00"), LocalDate.of(2025, 5, 11))));

        mockMvc.perform(get("/transactions/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1001))
                .andExpect(jsonPath("$[0].transactionAmount").value(120.00))
                .andExpect(jsonPath("$[0].transactionDate").value("2025-05-11"));
    }

    @Test
    void unknownCustomerReturns404() throws Exception {
        when(transactionQueryService.getTransactionsByCustomerId(9L))
                .thenThrow(new CustomerNotFoundException("No transactions found for customer ID: 9"));

        mockMvc.perform(get("/transactions/9")).andExpect(status().isNotFound());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/transactions/abc")).andExpect(status().isBadRequest());
    }
}
