package com.challenge.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private WalletType type;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Cached sum of this wallet's ledger entries, in minor units (cents for USD).
     * Only LedgerService should change this, in the same transaction as the entries.
     */
    @Column(name = "balance_minor", nullable = false)
    private long balanceMinor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    protected Wallet() {}

    public static Wallet forUser(Long userId, String currency) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.type = WalletType.USER;
        wallet.currency = currency;
        return wallet;
    }

    public boolean isUserWallet() {
        return type == WalletType.USER;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public WalletType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public long getBalanceMinor() {
        return balanceMinor;
    }

    public void setBalanceMinor(long balanceMinor) {
        this.balanceMinor = balanceMinor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
