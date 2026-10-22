package com.example.rewards.controller;

import com.example.rewards.dto.TransactionResponse;
import com.example.rewards.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST endpoint that lists the raw purchase transactions recorded for a customer. */
@RestController
@Tag(name = "Transactions", description = "Recorded purchases")
public class CustomerTransactionsController {

    private final TransactionQueryService transactionQueryService;

    public CustomerTransactionsController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/transactions/{customerId}")
    @Operation(summary = "Transactions for one customer", description = "All recorded purchases, newest first.")
    @ApiResponse(responseCode = "200", description = "The customer transactions")
    @ApiResponse(responseCode = "400", description = "Invalid customer id")
    @ApiResponse(responseCode = "404", description = "The customer has no transactions")
    public List<TransactionResponse> getTransactionsByCustomerId(
            @Parameter(description = "Customer id", example = "1001")
            @PathVariable @Positive Long customerId) {
        return transactionQueryService.getTransactionsByCustomerId(customerId);
    }
}
