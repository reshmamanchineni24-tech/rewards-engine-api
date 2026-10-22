package com.example.rewards.service;

import com.example.rewards.dto.TransactionResponse;
import com.example.rewards.entity.Transaction;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.repository.TransactionRepository;
import com.example.rewards.util.TransactionResponseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-only lookup of the transactions that belong to a customer. */
@Service
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public TransactionQueryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> getTransactionsByCustomerId(Long customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
        if (transactions.isEmpty()) {
            throw new CustomerNotFoundException("No transactions found for customer ID: " + customerId);
        }
        return TransactionResponseMapper.toDTOList(transactions);
    }
}
