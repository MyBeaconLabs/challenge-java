package com.challenge.service;

import com.challenge.IntegrationTest;
import com.challenge.TestData;
import com.challenge.entity.Wallet;
import com.challenge.exception.CurrencyMismatchException;
import com.challenge.exception.InsufficientFundsException;
import com.challenge.repository.LedgerTransactionRepository;
import com.challenge.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceTest extends IntegrationTest {

    @Autowired LedgerService ledgerService;
    @Autowired WalletService walletService;
    @Autowired UserService userService;
    @Autowired WalletRepository walletRepository;
    @Autowired LedgerTransactionRepository transactionRepository;

    @Test
    void postingMovesMoneyAndRecordsBalancedEntries() {
        Wallet a = funded("USD", "100.00");
        Wallet b = TestData.newUserWallet(userService, walletService, "USD");

        var tx = ledgerService.post(posting(a, b, 2_500));

        assertThat(balance(a)).isEqualTo(7_500);
        assertThat(balance(b)).isEqualTo(2_500);
        assertThat(tx.getEntries()).extracting("amountMinor").containsExactly(-2_500L, 2_500L);
    }

    @Test
    void samePostingTwiceIsAppliedOnce() {
        Wallet a = funded("USD", "100.00");
        Wallet b = TestData.newUserWallet(userService, walletService, "USD");
        Posting posting = posting(a, b, 1_000);

        var first = ledgerService.post(posting);
        var second = ledgerService.post(posting);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(balance(a)).isEqualTo(9_000);
        assertThat(balance(b)).isEqualTo(1_000);
    }

    @Test
    void rejectsOverdraftAndLeavesBalancesUntouched() {
        Wallet a = funded("USD", "10.00");
        Wallet b = TestData.newUserWallet(userService, walletService, "USD");

        assertThatThrownBy(() -> ledgerService.post(posting(a, b, 1_001)))
            .isInstanceOf(InsufficientFundsException.class);

        assertThat(balance(a)).isEqualTo(1_000);
        assertThat(balance(b)).isZero();
    }

    @Test
    void rejectsUnbalancedPosting() {
        Wallet a = funded("USD", "10.00");
        Wallet b = TestData.newUserWallet(userService, walletService, "USD");
        Posting unbalanced = new Posting("TEST", key(), "unbalanced", List.of(
            new Posting.Leg(a.getId(), -500), new Posting.Leg(b.getId(), 400)));

        assertThatThrownBy(() -> ledgerService.post(unbalanced)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCrossCurrencyPosting() {
        Wallet usd = funded("USD", "10.00");
        Wallet eur = TestData.newUserWallet(userService, walletService, "EUR");

        assertThatThrownBy(() -> ledgerService.post(posting(usd, eur, 100)))
            .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void concurrentPostingsInBothDirectionsKeepTotalsConsistent() throws Exception {
        Wallet a = funded("USD", "100.00");
        Wallet b = funded("USD", "100.00");

        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            boolean aToB = i % 2 == 0;
            futures.add(pool.submit(() -> ledgerService.post(aToB ? posting(a, b, 100) : posting(b, a, 100))));
        }
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        // 25 transfers each way: balances end where they started, nothing lost or created
        assertThat(balance(a)).isEqualTo(10_000);
        assertThat(balance(b)).isEqualTo(10_000);
    }

    private Wallet funded(String currency, String amount) {
        Wallet wallet = TestData.newUserWallet(userService, walletService, currency);
        walletService.deposit(wallet.getId(), new BigDecimal(amount), key());
        return wallet;
    }

    private Posting posting(Wallet from, Wallet to, long amountMinor) {
        return new Posting("TEST", key(), "test", List.of(
            new Posting.Leg(from.getId(), -amountMinor), new Posting.Leg(to.getId(), amountMinor)));
    }

    private long balance(Wallet wallet) {
        return walletRepository.findById(wallet.getId()).orElseThrow().getBalanceMinor();
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }
}
