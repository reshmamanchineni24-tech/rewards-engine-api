package com.example.rewards.repository;

import com.example.rewards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** Data access for purchases. All date bounds are inclusive, so filtering happens in the database rather than in memory. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByCustomerIdOrderByTransactionDateDesc(Long customerId);

    /** Both bounds are inclusive. */
    List<Transaction> findByTransactionDateBetween(LocalDate from, LocalDate to);

    /** Both bounds are inclusive. */
    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, LocalDate from, LocalDate to);

    boolean existsByCustomerId(Long customerId);
}
