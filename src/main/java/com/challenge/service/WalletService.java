package com.challenge.service;

import com.challenge.entity.LedgerEntry;
import com.challenge.entity.Wallet;
import com.challenge.entity.WalletType;
import com.challenge.exception.ConflictException;
import com.challenge.exception.NotFoundException;
import com.challenge.repository.LedgerEntryRepository;
import com.challenge.repository.UserRepository;
import com.challenge.repository.WalletRepository;
import com.challenge.util.Money;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    static final String DEPOSIT = "DEPOSIT";

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerService ledgerService;

    public WalletService(WalletRepository walletRepository,
                         UserRepository userRepository,
                         LedgerEntryRepository ledgerEntryRepository,
                         LedgerService ledgerService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public Wallet createWallet(Long userId, String currency) {
        Money.requireSupported(currency);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        if (walletRepository.existsByUserIdAndCurrency(userId, currency)) {
            throw new ConflictException("User " + userId + " already has a " + currency + " wallet");
        }
        return walletRepository.save(Wallet.forUser(userId, currency));
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new NotFoundException("Wallet not found: " + walletId));
    }

    @Transactional(readOnly = true)
    public List<Wallet> getWalletsForUser(Long userId) {
        return walletRepository.findByUserIdOrderById(userId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> getEntries(Long walletId, int limit) {
        getWallet(walletId);
        return ledgerEntryRepository.findByWalletIdOrderByIdDesc(walletId, PageRequest.of(0, limit));
    }

    /**
     * Credits a user wallet with money that has arrived from outside the platform,
     * funded by the SYSTEM wallet for the same currency. Called by the funding pipeline;
     * retries with the same idempotency key are safe.
     */
    @Transactional
    public Wallet deposit(Long walletId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = getWallet(walletId);
        if (!wallet.isUserWallet()) {
            throw new IllegalArgumentException("Deposits can only be made into user wallets");
        }
        long amountMinor = Money.toMinorUnits(amount, wallet.getCurrency());
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Wallet funding = walletRepository.findByTypeAndCurrency(WalletType.SYSTEM, wallet.getCurrency())
            .orElseThrow(() -> new IllegalStateException("No system wallet for " + wallet.getCurrency()));

        ledgerService.post(new Posting(DEPOSIT, idempotencyKey, "Deposit", List.of(
            new Posting.Leg(funding.getId(), -amountMinor),
            new Posting.Leg(wallet.getId(), amountMinor))));

        return wallet;
    }
}
