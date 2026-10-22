package com.example.rewards.service;

import com.example.rewards.dto.CustomerRewardsResponse;
import com.example.rewards.dto.CustomerRewardsResponse.MonthlyReward;
import com.example.rewards.entity.Transaction;
import com.example.rewards.exception.InvalidDateRangeException;
import com.example.rewards.exception.CustomerNotFoundException;
import com.example.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for window resolution, monthly grouping and error cases, using a fixed clock and a mocked repository. */
@ExtendWith(MockitoExtension.class)
class RewardsCalculationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 6, 30);
    private static final LocalDate DEFAULT_FROM = LocalDate.of(2025, 3, 30);

    @Mock
    TransactionRepository repository;

    private RewardsCalculationService service() {
        Clock clock = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
        return new RewardsCalculationService(repository, clock);
    }

    private static Transaction tx(long customerId, String amount, String date) {
        return new Transaction(null, customerId, new BigDecimal(amount), LocalDate.parse(date));
    }

    @Test
    void customerRewardsAreGroupedPerMonthAndSummed() {
        when(repository.existsByCustomerId(1001L)).thenReturn(true);
        when(repository.findByCustomerIdAndTransactionDateBetween(1001L, DEFAULT_FROM, TODAY)).thenReturn(List.of(
                tx(1001, "120.00", "2025-06-10"),   // 90
                tx(1001, "75.00", "2025-06-20"),    // 25
                tx(1001, "200.00", "2025-05-02"),   // 250
                tx(1001, "45.00", "2025-04-15")));  // 0

        CustomerRewardsResponse result = service().getRewardsByCustomerId(1001L, null, null);

        assertEquals(365, result.totalPoints());
        assertEquals(List.of(
                new MonthlyReward(YearMonth.of(2025, 4), 0),
                new MonthlyReward(YearMonth.of(2025, 5), 250),
                new MonthlyReward(YearMonth.of(2025, 6), 115)), result.monthlyRewards());
    }

    @Test
    void explicitBoundsOverrideTheDefaultWindow() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        when(repository.existsByCustomerId(1001L)).thenReturn(true);
        when(repository.findByCustomerIdAndTransactionDateBetween(1001L, from, to))
                .thenReturn(List.of(tx(1001, "155.00", "2025-01-10")));

        CustomerRewardsResponse result = service().getRewardsByCustomerId(1001L, from, to);

        assertEquals(160, result.totalPoints());
        verify(repository).findByCustomerIdAndTransactionDateBetween(1001L, from, to);
    }

    @Test
    void knownCustomerWithNoPurchasesInWindowGetsZeroPoints() {
        when(repository.existsByCustomerId(1001L)).thenReturn(true);
        when(repository.findByCustomerIdAndTransactionDateBetween(1001L, DEFAULT_FROM, TODAY)).thenReturn(List.of());

        CustomerRewardsResponse result = service().getRewardsByCustomerId(1001L, null, null);

        assertEquals(0, result.totalPoints());
        assertTrue(result.monthlyRewards().isEmpty());
    }

    @Test
    void unknownCustomerIsNotFound() {
        when(repository.existsByCustomerId(42L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> service().getRewardsByCustomerId(42L, null, null));
    }

    @Test
    void rewardsForAllCustomersAreOrderedByCustomerId() {
        when(repository.findByTransactionDateBetween(DEFAULT_FROM, TODAY)).thenReturn(List.of(
                tx(1002, "120.00", "2025-06-01"),
                tx(1001, "60.00", "2025-06-02"),
                tx(1002, "51.00", "2025-05-02")));

        List<CustomerRewardsResponse> result = service().getAllRewards(null, null);

        assertEquals(2, result.size());
        assertEquals(1001L, result.get(0).customerId());
        assertEquals(10, result.get(0).totalPoints());
        assertEquals(1002L, result.get(1).customerId());
        assertEquals(91, result.get(1).totalPoints());
    }

    @Test
    void noTransactionsInWindowYieldsEmptyList() {
        when(repository.findByTransactionDateBetween(DEFAULT_FROM, TODAY)).thenReturn(List.of());

        assertTrue(service().getAllRewards(null, null).isEmpty());
    }

    @Test
    void onlyToGiven_startIsThreeMonthsBeforeIt() {
        LocalDate to = LocalDate.of(2025, 5, 31);
        when(repository.findByTransactionDateBetween(LocalDate.of(2025, 2, 28), to)).thenReturn(List.of());

        service().getAllRewards(null, to);

        verify(repository).findByTransactionDateBetween(LocalDate.of(2025, 2, 28), to);
    }

    @Test
    void onlyFromGiven_endIsToday() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        when(repository.findByTransactionDateBetween(from, TODAY)).thenReturn(List.of());

        service().getAllRewards(from, null);

        verify(repository).findByTransactionDateBetween(from, TODAY);
    }

    @Test
    void sameMonthInDifferentYearsStaysSeparate() {
        when(repository.findByTransactionDateBetween(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31)))
                .thenReturn(List.of(tx(1001, "120.00", "2024-06-10"), tx(1001, "120.00", "2025-06-10")));

        CustomerRewardsResponse result = service()
                .getAllRewards(LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31)).get(0);

        assertEquals(List.of(new MonthlyReward(YearMonth.of(2024, 6), 90), new MonthlyReward(YearMonth.of(2025, 6), 90)),
                result.monthlyRewards());
        assertEquals(180, result.totalPoints());
    }

    @Test
    void totalsBeyondIntRangeDoNotOverflow() {
        when(repository.existsByCustomerId(1001L)).thenReturn(true);
        when(repository.findByCustomerIdAndTransactionDateBetween(1001L, DEFAULT_FROM, TODAY)).thenReturn(List.of(
                tx(1001, "9999999999.99", "2025-06-01"),
                tx(1001, "9999999999.99", "2025-06-02")));

        assertEquals(39_999_999_696L, service().getRewardsByCustomerId(1001L, null, null).totalPoints());
    }

    @Test
    void startAfterEndIsRejected() {
        LocalDate from = LocalDate.of(2025, 6, 1);
        LocalDate to = LocalDate.of(2025, 5, 1);

        assertThrows(InvalidDateRangeException.class, () -> service().getAllRewards(from, to));
        assertThrows(InvalidDateRangeException.class, () -> service().getRewardsByCustomerId(1001L, from, to));
    }
}
