package com.example.rewards.dto;

import com.example.rewards.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

/** API representation of a single purchase transaction. */
public record TransactionResponse(Long id, Long customerId, BigDecimal transactionAmount, LocalDate transactionDate) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getCustomerId(), t.getTransactionAmount(), t.getTransactionDate());
    }
}
