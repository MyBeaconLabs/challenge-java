package com.challenge.service;

import com.challenge.entity.LedgerTransaction;
import com.challenge.entity.Wallet;
import com.challenge.exception.CurrencyMismatchException;
import com.challenge.exception.InsufficientFundsException;
import com.challenge.exception.NotFoundException;
import com.challenge.repository.LedgerTransactionRepository;
import com.challenge.repository.WalletRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The only component allowed to move money. Every posting is atomic, idempotent,
 * balanced (legs sum to zero), single-currency, and never overdraws a user wallet.
 */
@Service
public class LedgerService {

    private final LedgerTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final EntityManager entityManager;

    public LedgerService(LedgerTransactionRepository transactionRepository,
                         WalletRepository walletRepository,
                         EntityManager entityManager) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public LedgerTransaction post(Posting posting) {
        Optional<LedgerTransaction> existing =
            transactionRepository.findByTypeAndIdempotencyKey(posting.type(), posting.idempotencyKey());
        if (existing.isPresent()) {
            return existing.get();
        }

        validate(posting);

        List<Long> walletIds = posting.legs().stream().map(Posting.Leg::walletId).toList();
        Map<Long, Wallet> wallets = walletRepository.findAllByIdForUpdate(walletIds).stream()
            .collect(Collectors.toMap(Wallet::getId, Function.identity()));
        if (wallets.size() != walletIds.size()) {
            throw new NotFoundException("One or more wallets not found: " + walletIds);
        }
        // If a caller already loaded a wallet in this transaction, the lock query hands back
        // that (possibly stale) instance. Re-read the balance now that we hold the row lock.
        wallets.values().forEach(entityManager::refresh);
        if (wallets.values().stream().map(Wallet::getCurrency).distinct().count() > 1) {
            throw new CurrencyMismatchException("All legs of a posting must be in the same currency");
        }

        LedgerTransaction transaction =
            new LedgerTransaction(posting.type(), posting.idempotencyKey(), posting.description());
        for (Posting.Leg leg : posting.legs()) {
            Wallet wallet = wallets.get(leg.walletId());
            long newBalance = Math.addExact(wallet.getBalanceMinor(), leg.amountMinor());
            if (wallet.isUserWallet() && newBalance < 0) {
                throw new InsufficientFundsException(wallet.getId());
            }
            wallet.setBalanceMinor(newBalance);
            transaction.addEntry(wallet.getId(), leg.amountMinor());
        }
        return transactionRepository.save(transaction);
    }

    private static void validate(Posting posting) {
        if (posting.idempotencyKey() == null || posting.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
        List<Posting.Leg> legs = posting.legs();
        if (legs == null || legs.size() < 2) {
            throw new IllegalArgumentException("A posting needs at least two legs");
        }
        if (legs.stream().anyMatch(leg -> leg.amountMinor() == 0)) {
            throw new IllegalArgumentException("Legs must have a non-zero amount");
        }
        if (legs.stream().map(Posting.Leg::walletId).distinct().count() != legs.size()) {
            throw new IllegalArgumentException("Each wallet may appear only once in a posting");
        }
        long sum = legs.stream().mapToLong(Posting.Leg::amountMinor).reduce(0L, Math::addExact);
        if (sum != 0) {
            throw new IllegalArgumentException("Posting is unbalanced: legs sum to " + sum);
        }
    }
}
