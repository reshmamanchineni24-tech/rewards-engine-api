
package com.example.rewards.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rewards.dto.TransactionResponse;
import com.example.rewards.entity.Transaction;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.repository.TransactionRepository;

/** Unit tests for the transaction lookup service. */
@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    TransactionRepository repository;

    @InjectMocks
    TransactionQueryService service;

    @Test
    void mapsEntitiesToDtos() {
        Transaction entity = new Transaction(
                7L,
                1001L,
                new BigDecimal("120.97"),
                LocalDate.of(2025, 2, 12)
        );

        entity.setCustomerName("Aarav Sharma");

        when(repository.findByCustomerIdOrderByTransactionDateDesc(1001L))
                .thenReturn(List.of(entity));

        List<TransactionResponse> result =
                service.getTransactionsByCustomerId(1001L);

        assertEquals(
                List.of(new TransactionResponse(
                        7L,
                        1001L,
                        "Aarav Sharma",
                        new BigDecimal("120.97"),
                        LocalDate.of(2025, 2, 12)
                )),
                result
        );
    }

    @Test
    void noTransactionsIsNotFound() {
        when(repository.findByCustomerIdOrderByTransactionDateDesc(1001L))
                .thenReturn(List.of());

        CustomerNotFoundException ex = assertThrows(
                CustomerNotFoundException.class,
                () -> service.getTransactionsByCustomerId(1001L)
        );

        assertEquals(
                "No transactions found for customer ID: 1001",
                ex.getMessage()
        );
    }
}
