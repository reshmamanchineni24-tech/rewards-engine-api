
package com.example.rewards.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.rewards.entity.Transaction;

/** Runs the real derived queries against H2 using the production schema.sql. */
@DataJpaTest
class TransactionRepositoryTest {

    private static final long CUSTOMER = 7777L;
    private static final long OTHER_CUSTOMER = 7778L;

    @Autowired
    TransactionRepository repository;

    private void save(long customerId, String date) {
        Transaction transaction = new Transaction(
                null,
                customerId,
                new BigDecimal("100.00"),
                LocalDate.parse(date)
        );

        transaction.setCustomerName(
                customerId == CUSTOMER ? "Aarav Sharma" : "Priya Reddy"
        );

        repository.saveAndFlush(transaction);
    }

    @Test
    void dateWindowIncludesBothBoundsAndExcludesOutsideDates() {
        save(CUSTOMER, "2024-01-09");   // one day before
        save(CUSTOMER, "2024-01-10");   // lower bound
        save(CUSTOMER, "2024-02-10");   // inside
        save(CUSTOMER, "2024-03-10");   // upper bound
        save(CUSTOMER, "2024-03-11");   // one day after
        save(OTHER_CUSTOMER, "2024-02-01");

        List<Transaction> forCustomer = repository.findByCustomerIdAndTransactionDateBetween(
                CUSTOMER, LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 10));
        List<Transaction> forEveryone = repository.findByTransactionDateBetween(
                LocalDate.of(2024, 1, 10), LocalDate.of(2024, 3, 10));

        assertEquals(List.of(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 10)),
                forCustomer.stream().map(Transaction::getTransactionDate).sorted().toList());
        assertEquals(4, forEveryone.stream().filter(t -> t.getCustomerId() == CUSTOMER || t.getCustomerId() == OTHER_CUSTOMER).count());
    }

    @Test
    void singleDayWindowReturnsThatDay() {
        save(CUSTOMER, "2024-05-05");

        assertEquals(1, repository.findByCustomerIdAndTransactionDateBetween(
                CUSTOMER, LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 5)).size());
    }

    @Test
    void existsByCustomerIdDistinguishesKnownFromUnknown() {
        save(CUSTOMER, "2024-05-05");

        assertTrue(repository.existsByCustomerId(CUSTOMER));
        assertFalse(repository.existsByCustomerId(424242L));
    }

    @Test
    void transactionsAreReturnedNewestFirst() {
        save(CUSTOMER, "2024-01-01");
        save(CUSTOMER, "2024-03-01");
        save(CUSTOMER, "2024-02-01");

        assertEquals(List.of(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1)),
                repository.findByCustomerIdOrderByTransactionDateDesc(CUSTOMER).stream()
                        .map(Transaction::getTransactionDate).toList());
    }
}
