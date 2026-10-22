
package com.example.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.rewards.entity.Transaction;

/** API representation of a single purchase transaction. */
public record TransactionResponse(
        Long id,
        Long customerId,
        String customerName,
        BigDecimal transactionAmount,
        LocalDate transactionDate
) {

    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCustomerId(),
                t.getCustomerName(),
                t.getTransactionAmount(),
                t.getTransactionDate()
        );
    }
}
