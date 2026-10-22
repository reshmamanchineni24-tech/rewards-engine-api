package com.example.rewards.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.rewards.dto.CustomerRewardsResponse;
import com.example.rewards.dto.CustomerRewardsResponse.MonthlyReward;
import com.example.rewards.entity.Transaction;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.exception.InvalidDateRangeException;
import com.example.rewards.repository.TransactionRepository;

/** Builds reward summaries from transactions inside a date window (default: the three months ending today) and groups the points by month. The window uses an injected Clock so it can be tested deterministically. */
@Service
@Transactional(readOnly = true)
public class RewardsCalculationService {

    static final int DEFAULT_WINDOW_MONTHS = 3;

    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public RewardsCalculationService(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    /**
     * Rewards for every customer with at least one transaction in the window.
     * Null bounds default to the three months ending today.
     */
    public List<CustomerRewardsResponse> getAllRewards(LocalDate from, LocalDate to) {
        DateRange range = resolve(from, to);
        return transactionRepository.findByTransactionDateBetween(range.from(), range.to()).stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId, TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> summarize(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * Unknown customer gives 404. A known customer with no purchases in the window gets zero points.
     */
    public CustomerRewardsResponse getRewardsByCustomerId(Long customerId, LocalDate from, LocalDate to) {
        DateRange range = resolve(from, to);
        if (!transactionRepository.existsByCustomerId(customerId)) {
            throw new CustomerNotFoundException("No transactions found for customer ID: " + customerId);
        }
        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndTransactionDateBetween(customerId, range.from(), range.to());
        return summarize(customerId, transactions);
    }

    CustomerRewardsResponse summarize(Long customerId, List<Transaction> transactions) {
        Map<YearMonth, Long> pointsByMonth = new TreeMap<>();
        for (Transaction t : transactions) {
            pointsByMonth.merge(YearMonth.from(t.getTransactionDate()),
                    RewardPointsCalculator.calculateRewardPoints(t.getTransactionAmount()), Long::sum);
        }
        List<MonthlyReward> monthly = pointsByMonth.entrySet().stream()
                .map(e -> new MonthlyReward(e.getKey(), e.getValue()))
                .toList();
        long total = monthly.stream().mapToLong(MonthlyReward::points).sum();
        return new CustomerRewardsResponse(customerId, total, monthly);
    }

    private DateRange resolve(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.minusMonths(DEFAULT_WINDOW_MONTHS);
        if (start.isAfter(end)) {
            throw new InvalidDateRangeException("Start date " + start + " must not be after end date " + end);
        }
        return new DateRange(start, end);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
